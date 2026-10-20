package com.avetiso.core.ads.yandex

import com.avetiso.core.AppConstants
import org.junit.Assert.assertEquals
import org.junit.Test

class YandexAdsConfigurationFactoryTest {

    private val factory = YandexAdsConfigurationFactory()

    @Test
    fun debugBuildUsesDemoAdUnitIds() {
        val configuration = factory.create(isDebuggable = true)

        assertEquals(
            AppConstants.AdConstants.Yandex.APP_OPEN_DEMO_ID,
            configuration.appOpenAdUnitId
        )
        assertEquals(
            AppConstants.AdConstants.Yandex.BANNER_DEMO_ID,
            configuration.bannerAdUnitId
        )
    }

    @Test
    fun releaseBuildUsesProductionAdUnitIds() {
        val configuration = factory.create(isDebuggable = false)

        assertEquals(
            AppConstants.AdConstants.Yandex.APP_OPEN_PRODUCTION_ID,
            configuration.appOpenAdUnitId
        )
        assertEquals(
            AppConstants.AdConstants.Yandex.BANNER_PRODUCTION_ID,
            configuration.bannerAdUnitId
        )
    }
}
