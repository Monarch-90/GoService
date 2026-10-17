package com.avetiso.core.di

import com.avetiso.core.ads.AdsNetworkSdkInitializer
import com.avetiso.core.ads.AdsProviderPolicy
import com.avetiso.core.ads.AdsRegionResolver
import com.avetiso.core.ads.AdsSdkInitializer
import com.avetiso.core.ads.AdsSdkReadiness
import com.avetiso.core.ads.AppLaunchCounterRepository
import com.avetiso.core.ads.AppOpenAdController
import com.avetiso.core.ads.AppOpenAdProvider
import com.avetiso.core.ads.BannerAdController
import com.avetiso.core.ads.BannerAdProvider
import com.avetiso.core.ads.DefaultAdsProviderPolicy
import com.avetiso.core.ads.DeviceAdsRegionResolver
import com.avetiso.core.ads.RoutingAdsSdkInitializer
import com.avetiso.core.ads.RoutingAppOpenAdController
import com.avetiso.core.ads.RoutingBannerAdController
import com.avetiso.core.ads.admob.AdMobAdsSdkInitializer
import com.avetiso.core.ads.admob.AdMobAppOpenAdController
import com.avetiso.core.ads.admob.AdMobBannerAdController
import com.avetiso.core.ads.qualifier.AdMobAds
import com.avetiso.core.ads.qualifier.YandexAds
import com.avetiso.core.ads.yandex.YandexAdsSdkInitializer
import com.avetiso.core.ads.yandex.YandexAppOpenAdController
import com.avetiso.core.ads.yandex.YandexBannerAdController
import com.avetiso.core.data.repository.DataStoreAppLaunchCounterRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
interface AdModule {

    @Binds
    fun bindAppOpenAdController(
        impl: RoutingAppOpenAdController
    ): AppOpenAdController

    @Binds
    fun bindBannerAdController(
        impl: RoutingBannerAdController
    ): BannerAdController

    @Binds
    fun bindAdsProviderPolicy(
        impl: DefaultAdsProviderPolicy
    ): AdsProviderPolicy

    @Binds
    fun bindAdsRegionResolver(
        impl: DeviceAdsRegionResolver
    ): AdsRegionResolver

    @Binds
    fun bindAppLaunchCounterRepository(
        impl: DataStoreAppLaunchCounterRepository
    ): AppLaunchCounterRepository

    @Binds
    @IntoSet
    fun bindSelectedAdsSdkInitializer(
        impl: RoutingAdsSdkInitializer
    ): AdsSdkInitializer

    @Binds
    @IntoSet
    fun bindYandexNetworkSdkInitializer(
        impl: YandexAdsSdkInitializer
    ): AdsNetworkSdkInitializer

    @Binds
    @IntoSet
    fun bindAdMobNetworkSdkInitializer(
        impl: AdMobAdsSdkInitializer
    ): AdsNetworkSdkInitializer

    @Binds
    @YandexAds
    fun bindYandexAdsSdkReadiness(
        impl: YandexAdsSdkInitializer
    ): AdsSdkReadiness

    @Binds
    @AdMobAds
    fun bindAdMobAdsSdkReadiness(
        impl: AdMobAdsSdkInitializer
    ): AdsSdkReadiness

    @Binds
    @IntoSet
    fun bindYandexAppOpenAdProvider(
        impl: YandexAppOpenAdController
    ): AppOpenAdProvider

    @Binds
    @IntoSet
    fun bindAdMobAppOpenAdProvider(
        impl: AdMobAppOpenAdController
    ): AppOpenAdProvider

    @Binds
    @IntoSet
    fun bindYandexBannerAdProvider(
        impl: YandexBannerAdController
    ): BannerAdProvider

    @Binds
    @IntoSet
    fun bindAdMobBannerAdProvider(
        impl: AdMobBannerAdController
    ): BannerAdProvider
}
