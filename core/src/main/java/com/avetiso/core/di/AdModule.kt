package com.avetiso.core.di

import com.avetiso.core.ads.AdsSdkInitializer
import com.avetiso.core.ads.AdsSdkReadiness
import com.avetiso.core.ads.AppLaunchCounterRepository
import com.avetiso.core.ads.AppOpenAdController
import com.avetiso.core.ads.BannerAdController
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
        impl: YandexAppOpenAdController
    ): AppOpenAdController

    @Binds
    fun bindBannerAdController(
        impl: YandexBannerAdController
    ): BannerAdController

    @Binds
    fun bindAppLaunchCounterRepository(
        impl: DataStoreAppLaunchCounterRepository
    ): AppLaunchCounterRepository

    @Binds
    fun bindAdsSdkReadiness(
        impl: YandexAdsSdkInitializer
    ): AdsSdkReadiness

    @Binds
    @IntoSet
    fun bindAdsSdkInitializer(
        impl: YandexAdsSdkInitializer
    ): AdsSdkInitializer
}
