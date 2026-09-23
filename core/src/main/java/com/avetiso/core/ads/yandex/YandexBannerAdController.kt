package com.avetiso.core.ads.yandex

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnLayout
import com.avetiso.core.AppConstants
import com.avetiso.core.ads.AdsConfiguration
import com.avetiso.core.ads.AdsSdkReadiness
import com.avetiso.core.ads.BannerAdController
import com.yandex.mobile.ads.banner.BannerAdEventListener
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import java.util.WeakHashMap
import javax.inject.Inject
import kotlin.math.roundToInt

class YandexBannerAdController @Inject constructor(
    private val adsConfiguration: AdsConfiguration,
    private val adsSdkReadiness: AdsSdkReadiness
) : BannerAdController {

    private val pendingAttachRequests = WeakHashMap<ViewGroup, Any>()
    private val activeBanners = WeakHashMap<ViewGroup, BannerAdView>()
    private val attachStateListeners =
        WeakHashMap<ViewGroup, View.OnAttachStateChangeListener>()

    override suspend fun showBanner(container: ViewGroup) {
        if (!adsSdkReadiness.awaitReady()) {
            return
        }

        destroyBanner(container)

        val requestToken = Any()
        pendingAttachRequests[container] = requestToken

        container.doOnLayout {
            if (pendingAttachRequests[container] !== requestToken) {
                return@doOnLayout
            }

            pendingAttachRequests.remove(container)
            attachBanner(container)
        }
    }

    private fun attachBanner(container: ViewGroup) {
        val availableWidthPixels =
            container.width - container.paddingLeft - container.paddingRight

        if (availableWidthPixels <= 0) {
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

                override fun onAdFailedToLoad(
                    error: AdRequestError
                ) {
                    destroyBannerIfCurrent(
                        container = container,
                        banner = banner
                    )
                }

                override fun onAdClicked() = Unit

                override fun onImpression(
                    impressionData: ImpressionData?
                ) = Unit
            }
        )

        val attachStateListener = object : View.OnAttachStateChangeListener {

            override fun onViewAttachedToWindow(view: View) = Unit

            override fun onViewDetachedFromWindow(view: View) {
                destroyBannerIfCurrent(
                    container = container,
                    banner = banner
                )
            }
        }

        activeBanners[container] = banner
        attachStateListeners[container] = attachStateListener

        container.addOnAttachStateChangeListener(attachStateListener)
        container.addView(banner)

        banner.loadAd(
            AdRequest.Builder(
                adsConfiguration.bannerAdUnitId
            ).build()
        )
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

    private fun destroyBanner(container: ViewGroup) {
        pendingAttachRequests.remove(container)

        attachStateListeners.remove(container)?.let { listener ->
            container.removeOnAttachStateChangeListener(listener)
        }

        activeBanners.remove(container)?.let { banner ->
            banner.setBannerAdEventListener(null)

            if (banner.parent === container) {
                container.removeView(banner)
            }

            banner.destroy()
        }
    }
}
