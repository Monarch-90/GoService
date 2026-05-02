package com.avetiso.feature_sidebar.contact_developers.domain

/**
 * Контракт для отправки обратной связи разработчикам.
 * Изолирует ViewModel от деталей реализации работы с сетью.
 */
interface SendFeedbackUseCase {

    /**
     * @param email Email пользователя для обратной связи.
     * @param message Текст сообщения.
     * @return Result<Unit>, где успех означает доставку, а ошибка (Throwable) содержит причину сбоя.
     */
    suspend operator fun invoke(email: String, message: String): Result<Unit>
}