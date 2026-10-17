
package com.avetiso.core.ads.admob

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.SystemClock
import com.avetiso.core.AppConstants
import com.avetiso.core.ads.AdsConfiguration
import com.avetiso.core.ads.AdsProvider
import com.avetiso.core.ads.AdsSdkReadiness
import com.avetiso.core.ads.AppOpenAdProvider
import com.avetiso.core.ads.AppOpenAdShowResult
import com.avetiso.core.ads.qualifier.AdMobAds
import com.avetiso.core.di.MainDispatcher
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdMobAppOpenAdController @Inject constructor(
    @ApplicationContext context: Context,
    @AdMobAds private val adsConfiguration: AdsConfiguration,
    @AdMobAds private val adsSdkReadiness: AdsSdkReadiness,
    @MainDispatcher private val mainDispatcher: CoroutineDispatcher
) : AppOpenAdProvider {

    override val provider = AdsProvider.ADMOB

    private val stateLock = Any()
    private val applicationContext = context.applicationContext
    private val application = applicationContext as Application

    private var appOpenAd: AppOpenAd? = null
    private var loadedAtElapsedRealtime = 0L
    private var appOpenAdLoadCompletion: CompletableDeferred<Boolean>? = null
    private var isAppOpenAdShowing = false
    private var activeShowAd: AppOpenAd? = null
    private var activeShowLifecycleCallbacks: Application.ActivityLifecycleCallbacks? = null

    override suspend fun loadAppOpenAd() = withContext(mainDispatcher) {
        if (!adsSdkReadiness.awaitReady()) {
            return@withContext
        }

        val loadCompletion = synchronized(stateLock) {
            if (
                availableAdLocked() != null ||
                appOpenAdLoadCompletion?.isActive == true
            ) {
                return@withContext
            }

            CompletableDeferred<Boolean>().also { completion ->
                appOpenAdLoadCompletion = completion
            }
        }

        try {
            AppOpenAd.load(
                applicationContext,
                adsConfiguration.appOpenAdUnitId,
                AdRequest.Builder().build(),
                object : AppOpenAdLoadCallback() {

                    override fun onAdLoaded(ad: AppOpenAd) {
                        synchronized(stateLock) {
                            appOpenAd = ad
                            loadedAtElapsedRealtime = SystemClock.elapsedRealtime()

                            if (appOpenAdLoadCompletion === loadCompletion) {
                                appOpenAdLoadCompletion = null
                            }
                        }

                        loadCompletion.complete(true)
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
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
    ): AppOpenAdShowResult = withContext(mainDispatcher) {
        if (!reserveShowRequest()) {
            return@withContext AppOpenAdShowResult.ALREADY_SHOWING
        }

        val ad = try {
            awaitAvailableAd()
        } catch (exception: CancellationException) {
            releaseShowRequest()
            throw exception
        } catch (exception: Exception) {
            releaseShowRequest()
            return@withContext AppOpenAdShowResult.FAILED_TO_SHOW
        }

        if (ad == null) {
            releaseShowRequest()
            return@withContext AppOpenAdShowResult.NOT_AVAILABLE
        }

        try {
            currentCoroutineContext().ensureActive()
        } catch (exception: CancellationException) {
            releaseShowRequest()
            throw exception
        }

        if (activity.isFinishing || activity.isDestroyed) {
            releaseShowRequest()
            return@withContext AppOpenAdShowResult.FAILED_TO_SHOW
        }

        val lifecycleCallbacks = createActivityLifecycleCallbacks(
            hostActivity = activity,
            ad = ad
        )

        return@withContext try {
            activateShow(
                ad = ad,
                lifecycleCallbacks = lifecycleCallbacks
            )

            ad.fullScreenContentCallback = object : FullScreenContentCallback() {

                override fun onAdShowedFullScreenContent() = Unit

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    finishShow(
                        ad = ad,
                        lifecycleCallbacks = lifecycleCallbacks
                    )
                }

                override fun onAdDismissedFullScreenContent() {
                    finishShow(
                        ad = ad,
                        lifecycleCallbacks = lifecycleCallbacks
                    )
                }

                override fun onAdClicked() = Unit

                override fun onAdImpression() = Unit
            }

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
        synchronized(stateLock) {
            availableAdLocked()
        }?.let { ad ->
            return ad
        }

        loadAppOpenAd()

        synchronized(stateLock) {
            availableAdLocked()
        }?.let { ad ->
            return ad
        }

        val loadCompletion = synchronized(stateLock) {
            appOpenAdLoadCompletion
        } ?: return synchronized(stateLock) {
            availableAdLocked()
        }

        if (!loadCompletion.await()) {
            return null
        }

        return synchronized(stateLock) {
            availableAdLocked()
        }
    }

    private fun availableAdLocked(): AppOpenAd? {
        val ad = appOpenAd ?: return null
        val adAge = SystemClock.elapsedRealtime() - loadedAtElapsedRealtime

        if (adAge < AppConstants.AdConstants.AdMob.APP_OPEN_EXPIRATION_MILLIS) {
            return ad
        }

        appOpenAd = null
        loadedAtElapsedRealtime = 0L
        return null
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
                    loadedAtElapsedRealtime = 0L
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

        ad.fullScreenContentCallback = null
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
