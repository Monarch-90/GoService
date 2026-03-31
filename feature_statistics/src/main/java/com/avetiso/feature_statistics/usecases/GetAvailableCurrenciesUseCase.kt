package com.avetiso.feature_statistics.usecases

import com.avetiso.feature_statistics.StatisticsConstants
import com.avetiso.feature_statistics.models.TimePeriod
import com.avetiso.feature_statistics.repository.StatisticsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * UseCase для получения списка доступных валют за выбранный период.
 * Сканирует исполненные записи и возвращает уникальный список валют,
 * чтобы UI мог правильно сформировать спиннер.
 */
class GetAvailableCurrenciesUseCase @Inject constructor(
    private val repository: StatisticsRepository
) {
    suspend fun execute(
        period: TimePeriod,
        customStart: Long?,
        customEnd: Long?
    ): List<String> = withContext(Dispatchers.IO) {

        // 1. Вычисляем точные временные рамки (Unix timestamp) для SQL-запроса
        val (startTimestamp, endTimestamp) = calculatePeriodTimestamps(period, customStart, customEnd)

        // 2. Получаем уникальный набор (Set) валют из репозитория
        val currenciesSet = repository.getAvailableCurrenciesBetween(startTimestamp, endTimestamp)

        // 3. Конвертируем в список и сортируем по алфавиту для предсказуемого отображения в UI
        return@withContext currenciesSet.toList().sorted()
    }

    /**
     * Конвертирует TimePeriod в конкретные Unix-timestamp'ы.
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