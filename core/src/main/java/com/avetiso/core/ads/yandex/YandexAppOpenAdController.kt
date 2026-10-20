package com.avetiso.core.ads.yandex

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import com.avetiso.core.ads.AdsConfiguration
import com.avetiso.core.ads.AdsConsentRepository
import com.avetiso.core.ads.AdsDataConsent
import kotlinx.coroutines.flow.first
import com.avetiso.core.ads.AdsSdkReadiness
import com.avetiso.core.ads.AdsProvider
import com.avetiso.core.ads.AppOpenAdProvider
import com.avetiso.core.ads.AppOpenAdShowResult
import com.avetiso.core.ads.qualifier.YandexAds
import com.yandex.mobile.ads.appopenad.AppOpenAd
import com.yandex.mobile.ads.appopenad.AppOpenAdEventListener
import com.yandex.mobile.ads.appopenad.AppOpenAdLoadListener
import com.yandex.mobile.ads.appopenad.AppOpenAdLoader
import com.yandex.mobile.ads.common.AdError
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YandexAppOpenAdController @Inject constructor(
    @ApplicationContext context: Context,
    @YandexAds private val adsConfiguration: AdsConfiguration,
    @YandexAds private val adsSdkReadiness: AdsSdkReadiness,
    private val consentRepository: AdsConsentRepository
) : AppOpenAdProvider {

    override val provider = AdsProvider.YANDEX

    private val stateLock = Any()
    private val applicationContext = context.applicationContext
    private val application = applicationContext as Application
    private val appOpenAdLoader by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        AppOpenAdLoader(applicationContext)
    }

    private var appOpenAd: AppOpenAd? = null
    private var loadedForConsent: AdsDataConsent? = null
    private var appOpenAdLoadCompletion: CompletableDeferred<Boolean>? = null
    private var isAppOpenAdShowing = false
    private var activeShowAd: AppOpenAd? = null
    private var activeShowLifecycleCallbacks: Application.ActivityLifecycleCallbacks? = null

    override suspend fun loadAppOpenAd() {
        if (!adsSdkReadiness.awaitReady()) {
            return
        }

        val currentConsent = consentRepository.consent.first { it != AdsDataConsent.UNDECIDED }
        val loadCompletion = synchronized(stateLock) {
            if (loadedForConsent != currentConsent) {
                appOpenAd = null
                loadedForConsent = null
            }
            if (appOpenAd != null || appOpenAdLoadCompletion?.isActive == true) {
                return
            }

            CompletableDeferred<Boolean>().also { completion ->
                appOpenAdLoadCompletion = completion
            }
        }

        val request = AdRequest.Builder(
            adsConfiguration.appOpenAdUnitId
        ).build()

        try {
            appOpenAdLoader.loadAd(
                request,
                object : AppOpenAdLoadListener {

                    override fun onAdLoaded(ad: AppOpenAd) {
                        synchronized(stateLock) {
                            appOpenAd = ad
                            loadedForConsent = currentConsent

                            if (appOpenAdLoadCompletion === loadCompletion) {
                                appOpenAdLoadCompletion = null
                            }
                        }

                        loadCompletion.complete(true)
                    }

                    override fun onAdFailedToLoad(error: AdRequestError) {
                        clearLoadCompletion(loadCompletion)
                        loadCompletion.complete(false)
                    }
                }
            )
        } catch (exception: Exception) {
            clearLoadCompletion(loadCompletion)
            loadCompletion.complete(false)
        }
    }

    override suspend fun showAppOpenAd(
        activity: Activity
    ): AppOpenAdShowResult {
        if (!reserveShowRequest()) {
            return AppOpenAdShowResult.ALREADY_SHOWING
        }

        val ad = try {
            awaitAvailableAd()
        } catch (exception: CancellationException) {
            releaseShowRequest()
            throw exception
        } catch (exception: Exception) {
            releaseShowRequest()
            return AppOpenAdShowResult.FAILED_TO_SHOW
        }

        if (ad == null) {
            releaseShowRequest()
            return AppOpenAdShowResult.NOT_AVAILABLE
        }

        try {
            currentCoroutineContext().ensureActive()
        } catch (exception: CancellationException) {
            releaseShowRequest()
            throw exception
        }

        if (activity.isFinishing || activity.isDestroyed) {
            releaseShowRequest()
            return AppOpenAdShowResult.FAILED_TO_SHOW
        }

        val lifecycleCallbacks = createActivityLifecycleCallbacks(
            hostActivity = activity,
            ad = ad
        )

        return try {
            activateShow(
                ad = ad,
                lifecycleCallbacks = lifecycleCallbacks
            )

            ad.setAdEventListener(
                object : AppOpenAdEventListener {

                    override fun onAdShown() = Unit

                    override fun onAdFailedToShow(error: AdError) {
                        finishShow(
                            ad = ad,
                            lifecycleCallbacks = lifecycleCallbacks
                        )
                    }

                    override fun onAdDismissed() {
                        finishShow(
                            ad = ad,
                            lifecycleCallbacks = lifecycleCallbacks
                        )
                    }

                    override fun onAdClicked() = Unit

                    override fun onAdImpression(
                        impressionData: ImpressionData?
                    ) = Unit
                }
            )

            ad.show(activity)
            AppOpenAdShowResult.SHOW_REQUESTED
        } catch (exception: CancellationException) {
            finishShow(
                ad = ad,
                lifecycleCallbacks = lifecycleCallbacks
            )
            throw exception
        } catch (exception: Exception) {
            finishShow(
                ad = ad,
                lifecycleCallbacks = lifecycleCallbacks
            )
            AppOpenAdShowResult.FAILED_TO_SHOW
        }
    }

    private fun clearLoadCompletion(
        loadCompletion: CompletableDeferred<Boolean>
    ) {
        synchronized(stateLock) {
            if (appOpenAdLoadCompletion === loadCompletion) {
                appOpenAdLoadCompletion = null
            }
        }
    }

    private suspend fun awaitAvailableAd(): AppOpenAd? {
        val currentConsent = consentRepository.consent.first { it != AdsDataConsent.UNDECIDED }
        synchronized(stateLock) {
            if (loadedForConsent != currentConsent) {
                appOpenAd = null
                loadedForConsent = null
            }
        }
        synchronized(stateLock) {
            appOpenAd
        }?.let { ad ->
            return ad
        }

        loadAppOpenAd()

        synchronized(stateLock) {
            if (loadedForConsent == currentConsent) appOpenAd else null
        }?.let { ad ->
            return ad
        }

        val loadCompletion = synchronized(stateLock) {
            appOpenAdLoadCompletion
        } ?: return synchronized(stateLock) {
            if (loadedForConsent == currentConsent) appOpenAd else null
        }

        if (!loadCompletion.await()) {
            return null
        }

        return synchronized(stateLock) {
            if (loadedForConsent == currentConsent) {
                appOpenAd
            } else {
                appOpenAd = null
                loadedForConsent = null
                null
            }
        }
    }

    private fun reserveShowRequest(): Boolean {
        return synchronized(stateLock) {
            if (isAppOpenAdShowing) {
                false
            } else {
                isAppOpenAdShowing = true
                true
            }
        }
    }

    private fun releaseShowRequest() {
        synchronized(stateLock) {
            if (activeShowAd == null) {
                isAppOpenAdShowing = false
            }
        }
    }

    private fun activateShow(
        ad: AppOpenAd,
        lifecycleCallbacks: Application.ActivityLifecycleCallbacks
    ) {
        synchronized(stateLock) {
            activeShowAd = ad
            activeShowLifecycleCallbacks = lifecycleCallbacks
        }

        application.registerActivityLifecycleCallbacks(lifecycleCallbacks)
    }

    private fun finishShow(
        ad: AppOpenAd,
        lifecycleCallbacks: Application.ActivityLifecycleCallbacks
    ) {
        val shouldFinish = synchronized(stateLock) {
            if (
                activeShowAd !== ad ||
                activeShowLifecycleCallbacks !== lifecycleCallbacks
            ) {
                false
            } else {
                if (appOpenAd === ad) {
                    appOpenAd = null
                    loadedForConsent = null
                }

                activeShowAd = null
                activeShowLifecycleCallbacks = null
                isAppOpenAdShowing = false
                true
            }
        }

        if (!shouldFinish) {
            return
        }

        ad.setAdEventListener(null)
        application.unregisterActivityLifecycleCallbacks(lifecycleCallbacks)
    }

    private fun createActivityLifecycleCallbacks(
        hostActivity: Activity,
        ad: AppOpenAd
    ): Application.ActivityLifecycleCallbacks {
        return object : Application.ActivityLifecycleCallbacks {

            override fun onActivityCreated(
                activity: Activity,
                savedInstanceState: Bundle?
            ) = Unit

            override fun onActivityStarted(activity: Activity) = Unit

            override fun onActivityResumed(activity: Activity) = Unit

            override fun onActivityPaused(activity: Activity) = Unit

            override fun onActivityStopped(activity: Activity) = Unit

            override fun onActivitySaveInstanceState(
                activity: Activity,
                outState: Bundle
            ) = Unit

            override fun onActivityDestroyed(activity: Activity) {
                if (activity === hostActivity) {
                    finishShow(
                        ad = ad,
                        lifecycleCallbacks = this
                    )
                }
            }
        }
    }
}
