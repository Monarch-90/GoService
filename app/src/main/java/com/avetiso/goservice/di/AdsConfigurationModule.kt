package com.avetiso.goservice.di

import com.avetiso.core.AppConstants
import com.avetiso.core.ads.AdsConfiguration
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
    fun provideAdsConfiguration(): AdsConfiguration {
        return AdsConfiguration(
            appOpenAdUnitId = AppConstants.AdConstants.YANDEX_APP_OPEN_DEMO_ID,
            bannerAdUnitId = AppConstants.AdConstants.YANDEX_BANNER_DEMO_ID
        )
    }
}
