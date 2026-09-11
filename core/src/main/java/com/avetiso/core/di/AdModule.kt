package com.avetiso.core.di

import com.avetiso.core.ads.AdManager
import com.avetiso.core.ads.YandexAdManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface AdModule {

    @Binds
    fun bindAdManager(impl: YandexAdManager): AdManager
}