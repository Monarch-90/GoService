package com.avetiso.feature_statistics.di

import com.avetiso.feature_statistics.repository.StatisticsRepositoryImpl
import com.avetiso.feature_statistics.repository.StatisticsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * DI-модуль для фичи Статистики.
 * Изолирован от Core-модуля. Указывает Hilt, какие конкретные реализации
 * использовать для контрактов (интерфейсов) внутри этого модуля.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class StatisticsModule {

    @Binds
    @Singleton
    abstract fun bindStatisticsRepository(
        impl: StatisticsRepositoryImpl
    ): StatisticsRepository

}