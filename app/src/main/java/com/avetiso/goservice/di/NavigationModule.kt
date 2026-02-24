package com.avetiso.goservice.di

import com.avetiso.feature_clients.selector.ClientSelectorProviderImpl
import com.avetiso.goservice.navigation.ClientsNavigatorImpl
import com.avetiso.goservice.navigation.ScheduleNavigatorImpl
import com.avetiso.goservice.navigation.SidebarNavigatorImpl
import com.avetiso.navigation.providers.ClientSelectorProvider
import com.avetiso.navigation.routers.ClientsNavigator
import com.avetiso.navigation.routers.ScheduleNavigator
import com.avetiso.navigation.routers.SidebarNavigator
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

    @Binds
    @Singleton
    abstract fun bindSidebarNavigator(impl: SidebarNavigatorImpl): SidebarNavigator

    @Binds
    @Singleton
    abstract fun bindScheduleNavigator(impl: ScheduleNavigatorImpl): ScheduleNavigator
}