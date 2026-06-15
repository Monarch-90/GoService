package com.avetiso.feature_statistics.domain.usecases

import com.avetiso.feature_statistics.domain.models.TimePeriod
import com.avetiso.feature_statistics.domain.models.WorkloadSummary
import com.avetiso.feature_statistics.domain.provider.StatisticsDateProvider
import com.avetiso.feature_statistics.domain.repository.StatisticsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * UseCase для формирования сводки по загруженности мастера (Виджет 3).
 * Отвечает за запрос количества новых клиентов, отмен и общего рабочего времени.
 * Выполняется строго на IO-диспетчере для безопасности потоков.
 */
class GetWorkloadSummaryUseCase @Inject constructor(
    private val repository: StatisticsRepository,
    private val dateProvider: StatisticsDateProvider // Внедряем провайдер дат
) {
    suspend fun execute(
        period: TimePeriod,
        customStart: Long?,
        customEnd: Long?
    ): WorkloadSummary = withContext(Dispatchers.IO) {

        // 1. Вычисляем точные временные рамки через единый DateProvider
        val (startTimestamp, endTimestamp) = dateProvider.getPeriodTimestamps(period, customStart, customEnd)

        // 2. Запрашиваем агрегированные данные из репозитория
        val newClients = repository.getNewClientsCountBetween(startTimestamp, endTimestamp)
        val cancellations = repository.getCancellationsCountBetween(startTimestamp, endTimestamp)
        val totalMinutes = repository.getTotalWorkMinutesBetween(startTimestamp, endTimestamp)

        // 3. Возвращаем чистую модель данных для UI
        return@withContext WorkloadSummary(
            newClientsCount = newClients,
            cancellationsCount = cancellations,
            totalWorkMinutes = totalMinutes
        )
    }
}