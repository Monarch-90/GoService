package com.avetiso.core.ads

import android.app.Activity
import android.content.Context
import android.util.DisplayMetrics
import android.util.Log
import android.view.ViewGroup
import com.avetiso.core.AppConstants
import com.avetiso.core.AppConstants.AdConstants
// Правильные импорты для версии 8.0.0+
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@Singleton
class YandexAdManager @Inject constructor(
    @ApplicationContext private val context: Context
) : AdManager {

    private var appOpenAd: com.yandex.mobile.ads.appopenad.AppOpenAd? = null
    private var isAppOpenAdLoading = false

    // Переменные для отложенного показа (если реклама еще качается)
    private var pendingActivity: Activity? = null
    private var pendingOnAdClosed: (() -> Unit)? = null

    override fun loadAppOpenAd() {
        if (isAppOpenAdLoading || appOpenAd != null) return

        isAppOpenAdLoading = true
        val appOpenAdLoader = com.yandex.mobile.ads.appopenad.AppOpenAdLoader(context)
        val adRequest = com.yandex.mobile.ads.common.AdRequest.Builder(com.avetiso.core.AppConstants.AdConstants.YANDEX_APP_OPEN_DEMO_ID).build()

        val listener = object : com.yandex.mobile.ads.appopenad.AppOpenAdLoadListener {
            override fun onAdLoaded(ad: com.yandex.mobile.ads.appopenad.AppOpenAd) {
                appOpenAd = ad
                isAppOpenAdLoading = false

                // Состояние гонки: если экран уже запросил показ, показываем немедленно
                val activity = pendingActivity
                val closeCallback = pendingOnAdClosed
                if (activity != null) {
                    showAppOpenAd(activity, closeCallback ?: {})
                    pendingActivity = null
                    pendingOnAdClosed = null
                }
            }

            override fun onAdFailedToLoad(error: com.yandex.mobile.ads.common.AdRequestError) {
                isAppOpenAdLoading = false
                pendingOnAdClosed?.invoke()
                pendingActivity = null
                pendingOnAdClosed = null
            }
        }
        appOpenAdLoader.loadAd(adRequest, listener)
    }

    override fun showAppOpenAd(activity: Activity, onAdClosed: () -> Unit) {
        val ad = appOpenAd
        if (ad != null) {
            ad.setAdEventListener(object : com.yandex.mobile.ads.appopenad.AppOpenAdEventListener {
                override fun onAdShown() {}
                override fun onAdFailedToShow(error: com.yandex.mobile.ads.common.AdError) {
                    appOpenAd = null
                    onAdClosed()
                }

                override fun onAdDismissed() {
                    appOpenAd = null
                    onAdClosed()
                }

                override fun onAdClicked() {}
                override fun onAdImpression(impressionData: com.yandex.mobile.ads.common.ImpressionData?) {}
            })
            ad.show(activity)
        } else if (isAppOpenAdLoading) {
            // Реклама еще качается. Запоминаем команду на показ.
            pendingActivity = activity
            pendingOnAdClosed = onAdClosed
        } else {
            onAdClosed()
        }
    }

    override fun showBanner(container: ViewGroup) {
        // 1. Создаем саму View баннера
        val banner = BannerAdView(container.context)

        // 2. Вычисляем ширину экрана
        val displayMetrics: DisplayMetrics = container.context.resources.displayMetrics
        val screenWidthDp = (displayMetrics.widthPixels / displayMetrics.density).roundToInt()

        // 3. Устанавливаем размер (в SDK 8+ метод называется sticky, а не stickySize)
        val maxHeightDp = 50
        banner.setAdSize(BannerAdSize.inline(container.context, screenWidthDp, maxHeightDp))

        // 4. В SDK 8+ ID рекламного блока передается прямо в Builder,
        // метод banner.setAdUnitId() был удален разработчиками Яндекса.
        val adRequest = AdRequest.Builder(AdConstants.YANDEX_BANNER_DEMO_ID).build()

        // 5. Загружаем рекламу с нашим запросом
        banner.loadAd(adRequest)

        // 6. Добавляем баннер на экран
        container.removeAllViews()
        container.addView(banner)
    }

    override fun loadRewardedAd() {
        // Заглушка на будущее
    }

    override fun showRewardedAd(activity: Activity, onRewardEarned: () -> Unit, onAdClosed: () -> Unit) {
        // Заглушка на будущее
        onAdClosed()
    }
}