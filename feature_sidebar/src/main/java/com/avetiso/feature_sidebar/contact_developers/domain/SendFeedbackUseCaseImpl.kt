package com.avetiso.feature_sidebar.contact_developers.domain

import com.avetiso.core.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Реализация бизнес-логики отправки обратной связи.
 * Делегирует реальную работу с сетью слою данных (Repository).
 * Запуск происходит на изолированном IO-диспетчере, внедряемом через Hilt.
 */
class SendFeedbackUseCaseImpl @Inject constructor(
    private val repository: ContactDevelopersRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : SendFeedbackUseCase {

    override suspend fun invoke(email: String, message: String): Result<Unit> {
        return withContext(ioDispatcher) {
            repository.sendFeedback(email = email, message = message)
        }
    }
}