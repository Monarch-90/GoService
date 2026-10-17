
package com.avetiso.goservice.di

import com.avetiso.core.AppConstants
import com.avetiso.core.ads.AdsConfiguration
import com.avetiso.core.ads.qualifier.AdMobAds
import com.avetiso.core.ads.qualifier.YandexAds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AdsConfigurationModule {

    @Provides
    @Singleton
    @YandexAds
    fun provideYandexAdsConfiguration(): AdsConfiguration {
        return AdsConfiguration(
            appOpenAdUnitId = AppConstants.AdConstants.Yandex.APP_OPEN_DEMO_ID,
            bannerAdUnitId = AppConstants.AdConstants.Yandex.BANNER_DEMO_ID
        )
    }

    @Provides
    @Singleton
    @AdMobAds
    fun provideAdMobAdsConfiguration(): AdsConfiguration {
        return AdsConfiguration(
            appOpenAdUnitId = AppConstants.AdConstants.AdMob.APP_OPEN_DEMO_ID,
            bannerAdUnitId = AppConstants.AdConstants.AdMob.BANNER_DEMO_ID
        )
    }
}
