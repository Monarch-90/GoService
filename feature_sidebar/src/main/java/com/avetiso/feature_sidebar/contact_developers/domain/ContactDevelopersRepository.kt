package com.avetiso.feature_sidebar.contact_developers.domain

/**
 * Контракт репозитория для функции связи с разработчиками.
 * Изолирует доменный слой от деталей реализации сетевых запросов.
 */
interface ContactDevelopersRepository {

    /**
     * Отправляет сообщение обратной связи.
     *
     * @param email Email пользователя для ответа.
     * @param message Текст сообщения.
     * @return Result<Unit> — успех или объект исключения в случае сбоя.
     */
    suspend fun sendFeedback(email: String, message: String): Result<Unit>
}