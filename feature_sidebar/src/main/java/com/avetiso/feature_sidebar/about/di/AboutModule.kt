package com.avetiso.feature_sidebar.about.di

import com.avetiso.feature_sidebar.about.data.AboutRepositoryImpl
import com.avetiso.feature_sidebar.about.domain.repository.AboutRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface AboutModule {

    @Binds
    @Singleton
    fun bindAboutRepository(
        impl: AboutRepositoryImpl
    ): AboutRepository
}