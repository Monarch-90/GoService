package com.avetiso.feature_sidebar.contact_developers.data.repository

import com.avetiso.feature_sidebar.SidebarConstants
import com.avetiso.feature_sidebar.contact_developers.data.api.FeedbackApi
import com.avetiso.feature_sidebar.contact_developers.domain.ContactDevelopersRepository
import dagger.Lazy
import javax.inject.Inject

/**
 * Реализация репозитория.
 * Использует dagger.Lazy для отложенной инициализации тяжелых сетевых библиотек (Retrofit/OkHttp).
 */
class ContactDevelopersRepositoryImpl @Inject constructor(
    private val apiLazy: Lazy<FeedbackApi>
) : ContactDevelopersRepository {

    override suspend fun sendFeedback(email: String, message: String): Result<Unit> {
        return try {
            // ИСПОЛЬЗУЕМ ТВОЮ ГОТОВУЮ КОНСТАНТУ ИЗ БЛОКА Api
            val response = apiLazy.get().sendFeedback(
                endpointUrl = SidebarConstants.Api.FEEDBACK_ENDPOINT,
                email = email,
                message = message
            )

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                // Если сервер нас отшил (например, email не подтвержден)
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("AppTrace", "Formspree API Error: code ${response.code()}, body: $errorBody")
                Result.failure(Exception("API Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            // Если Retrofit вообще не смог собраться или упал (например, из-за BaseURL)
            android.util.Log.e("AppTrace", "Retrofit Crash during sendFeedback", e)
            Result.failure(e)
        }
    }
}