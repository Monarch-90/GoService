package com.avetiso.feature_sidebar.contact_developers.di

import com.avetiso.feature_sidebar.SidebarConstants
import com.avetiso.feature_sidebar.contact_developers.data.api.FeedbackApi
import com.avetiso.feature_sidebar.contact_developers.data.repository.ContactDevelopersRepositoryImpl
import com.avetiso.feature_sidebar.contact_developers.domain.ContactDevelopersRepository
import com.avetiso.feature_sidebar.contact_developers.domain.SendFeedbackUseCase
import com.avetiso.feature_sidebar.contact_developers.domain.SendFeedbackUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ContactDevelopersModule {

    @Binds
    @Singleton
    abstract fun bindContactDevelopersRepository(
        impl: ContactDevelopersRepositoryImpl
    ): ContactDevelopersRepository

    @Binds
    @Singleton
    abstract fun bindSendFeedbackUseCase(
        impl: SendFeedbackUseCaseImpl
    ): SendFeedbackUseCase

    companion object {
        @Provides
        @Singleton
        fun provideFeedbackApi(retrofitBuilder: Retrofit.Builder): FeedbackApi {
            return retrofitBuilder
                // Базовый URL должен заканчиваться на слэш (например, "https://formspree.io/")
                .baseUrl(SidebarConstants.Api.FORMSPREE_BASE_URL)
                .build()
                .create(FeedbackApi::class.java)
        }
    }
}