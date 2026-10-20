package com.avetiso.core.ads.yandex

import com.avetiso.core.AppConstants
import com.avetiso.core.ads.AdsConfiguration
import javax.inject.Inject

class YandexAdsConfigurationFactory @Inject constructor() {

    fun create(isDebuggable: Boolean): AdsConfiguration {
        return AdsConfiguration(
            appOpenAdUnitId = if (isDebuggable) {
                AppConstants.AdConstants.Yandex.APP_OPEN_DEMO_ID
            } else {
                AppConstants.AdConstants.Yandex.APP_OPEN_PRODUCTION_ID
            },
            bannerAdUnitId = if (isDebuggable) {
                AppConstants.AdConstants.Yandex.BANNER_DEMO_ID
            } else {
                AppConstants.AdConstants.Yandex.BANNER_PRODUCTION_ID
            }
        )
    }
}
