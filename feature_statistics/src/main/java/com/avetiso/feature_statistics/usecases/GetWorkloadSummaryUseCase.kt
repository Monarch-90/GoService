package com.avetiso.feature_statistics.usecases

import com.avetiso.feature_statistics.StatisticsConstants
import com.avetiso.feature_statistics.models.TimePeriod
import com.avetiso.feature_statistics.models.WorkloadSummary
import com.avetiso.feature_statistics.repository.StatisticsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * UseCase для формирования сводки по загруженности мастера (Виджет 3).
 * Отвечает за запрос количества новых клиентов, отмен и общего рабочего времени.
 * Выполняется строго на IO-диспетчере для безопасности потоков.
 */
class GetWorkloadSummaryUseCase @Inject constructor(
    private val repository: StatisticsRepository
) {
    suspend fun execute(
        period: TimePeriod,
        customStart: Long?,
        customEnd: Long?
    ): WorkloadSummary = withContext(Dispatchers.IO) {

        // 1. Вычисляем точные временные рамки (Unix timestamp) для SQL-запроса
        val (startTimestamp, endTimestamp) = calculatePeriodTimestamps(period, customStart, customEnd)

        // 2. Запрашиваем агрегированные данные из репозитория
        // Репозиторий сам решит, как делать JOIN'ы таблиц клиентов и записей.
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

    /**
     * Конвертирует TimePeriod в конкретные Unix-timestamp'ы.
     * Дублирование этой логики из финансов временно допустимо, пока мы не вынесем
     * работу с датами в отдельный DateProvider (Core-модуль), как это принято в Enterprise.
     */
    private fun calculatePeriodTimestamps(period: TimePeriod, customStart: Long?, customEnd: Long?): Pair<Long, Long> {
        val now = System.currentTimeMillis()
        return when (period) {
            TimePeriod.TODAY -> Pair(now - StatisticsConstants.Time.MILLIS_IN_DAY, now)
            TimePeriod.WEEK -> Pair(now - StatisticsConstants.Time.MILLIS_IN_WEEK, now)
            TimePeriod.MONTH -> Pair(now - StatisticsConstants.Time.MILLIS_IN_MONTH, now)
            TimePeriod.CUSTOM -> Pair(customStart ?: StatisticsConstants.Time.CUSTOM_START, customEnd ?: now)
        }
    }
}