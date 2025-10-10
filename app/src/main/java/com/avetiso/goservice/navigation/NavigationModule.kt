package com.avetiso.goservice.navigation

import com.avetiso.feature_clients.selector.ClientSelectorProviderImpl
import com.avetiso.navigation.ClientSelectorProvider
import com.avetiso.navigation.ClientsNavigator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NavigationModule {

    @Binds
    @Singleton
    abstract fun bindClientsNavigator(impl: ClientsNavigatorImpl): ClientsNavigator

    // Этот метод связывает абстрактный запрос (интерфейс)
    // с конкретным исполнителем (реализацией)
    @Binds
    @Singleton
    abstract fun bindClientSelector(impl: ClientSelectorProviderImpl): ClientSelectorProvider
}