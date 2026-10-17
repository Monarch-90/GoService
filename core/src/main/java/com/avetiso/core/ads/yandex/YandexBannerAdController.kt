package com.avetiso.core.ads.yandex

import android.app.Activity
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnLayout
import com.avetiso.core.AppConstants
import com.avetiso.core.ads.AdsConfiguration
import com.avetiso.core.ads.AdsSdkReadiness
import com.avetiso.core.ads.AdsProvider
import com.avetiso.core.ads.BannerAdProvider
import com.avetiso.core.ads.qualifier.YandexAds
import com.yandex.mobile.ads.banner.BannerAdEventListener
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import kotlinx.coroutines.CancellationException
import java.util.WeakHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@Singleton
class YandexBannerAdController @Inject constructor(
    @YandexAds private val adsConfiguration: AdsConfiguration,
    @YandexAds private val adsSdkReadiness: AdsSdkReadiness
) : BannerAdProvider {

    override val provider = AdsProvider.YANDEX

    private val pendingAttachRequests = WeakHashMap<ViewGroup, Any>()
    private val activeBanners = WeakHashMap<ViewGroup, BannerAdView>()

    override suspend fun showBanner(container: ViewGroup) {
        requireMainThread()
        container.visibility = View.VISIBLE

        if (
            activeBanners.containsKey(container) ||
            pendingAttachRequests.containsKey(container)
        ) {
            return
        }

        val requestToken = Any()
        pendingAttachRequests[container] = requestToken

        val isReady = try {
            adsSdkReadiness.awaitReady()
        } catch (exception: CancellationException) {
            removePendingRequest(container, requestToken)
            throw exception
        }

        if (!isReady) {
            removePendingRequest(container, requestToken)
            container.visibility = View.GONE
            return
        }

        if (pendingAttachRequests[container] !== requestToken) {
            return
        }

        container.doOnLayout {
            if (pendingAttachRequests[container] !== requestToken) {
                return@doOnLayout
            }

            pendingAttachRequests.remove(container)
            attachBanner(container)
        }
    }

    override fun destroyBanner(container: ViewGroup) {
        requireMainThread()

        pendingAttachRequests.remove(container)

        activeBanners.remove(container)?.let { banner ->
            banner.setBannerAdEventListener(null)

            if (banner.parent === container) {
                container.removeView(banner)
            }

            banner.destroy()
        }
    }

    private fun attachBanner(container: ViewGroup) {
        val availableWidthPixels =
            container.width - container.paddingLeft - container.paddingRight

        if (availableWidthPixels <= 0) {
            container.visibility = View.GONE
            return
        }

        val density = container.resources.displayMetrics.density
        val availableWidthDp =
            (availableWidthPixels / density).roundToInt()

        val banner = BannerAdView(container.context)

        banner.setAdSize(
            BannerAdSize.inline(
                container.context,
                availableWidthDp,
                AppConstants.AdConstants.BANNER_HEIGHT_DP
            )
        )

        banner.setBannerAdEventListener(
            object : BannerAdEventListener {

                override fun onAdLoaded() {
                    val activity = container.context as? Activity

                    if (activity?.isDestroyed == true || activity?.isFinishing == true) {
                        destroyBannerIfCurrent(
                            container = container,
                            banner = banner
                        )
                    }
                }

                override fun onAdFailedToLoad(error: AdRequestError) {
                    hideFailedBannerIfCurrent(
                        container = container,
                        banner = banner
                    )
                }

                override fun onAdClicked() = Unit

                override fun onImpression(impressionData: ImpressionData?) = Unit
            }
        )

        activeBanners[container] = banner
        container.addView(banner)

        banner.loadAd(
            AdRequest.Builder(
                adsConfiguration.bannerAdUnitId
            ).build()
        )
    }

    private fun hideFailedBannerIfCurrent(
        container: ViewGroup,
        banner: BannerAdView
    ) {
        if (activeBanners[container] === banner) {
            destroyBanner(container)
            container.visibility = View.GONE
        } else {
            banner.setBannerAdEventListener(null)
            banner.destroy()
        }
    }

    private fun destroyBannerIfCurrent(
        container: ViewGroup,
        banner: BannerAdView
    ) {
        if (activeBanners[container] === banner) {
            destroyBanner(container)
        } else {
            banner.setBannerAdEventListener(null)
            banner.destroy()
        }
    }

    private fun removePendingRequest(
        container: ViewGroup,
        requestToken: Any
    ) {
        if (pendingAttachRequests[container] === requestToken) {
            pendingAttachRequests.remove(container)
        }
    }

    private fun requireMainThread() {
        check(Looper.getMainLooper().isCurrentThread) {
            "Yandex Mobile Ads SDK must be accessed from the main thread"
        }
    }
}
