package com.avetiso.goservice.di

import android.content.Context
import android.content.pm.ApplicationInfo
import com.avetiso.core.AppConstants
import com.avetiso.core.ads.AdsConfiguration
import com.avetiso.core.ads.yandex.YandexAdsConfigurationFactory
import com.avetiso.core.ads.qualifier.AdMobAds
import com.avetiso.core.ads.qualifier.YandexAds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AdsConfigurationModule {

    @Provides
    @Singleton
    @YandexAds
    fun provideYandexAdsConfiguration(
        @ApplicationContext context: Context,
        factory: YandexAdsConfigurationFactory
    ): AdsConfiguration {
        val isDebuggable =
            (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

        return factory.create(isDebuggable)
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
