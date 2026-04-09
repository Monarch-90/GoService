package com.avetiso.feature_statistics.usecases

import com.avetiso.feature_statistics.models.TimePeriod
import com.avetiso.feature_statistics.providers.StatisticsDateProvider
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
    private val repository: StatisticsRepository,
    private val dateProvider: StatisticsDateProvider // Внедряем провайдер дат
) {
    suspend fun execute(
        period: TimePeriod,
        customStart: Long?,
        customEnd: Long?
    ): List<String> = withContext(Dispatchers.IO) {

        // 1. Вычисляем точные временные рамки через единый DateProvider
        val (startTimestamp, endTimestamp) = dateProvider.getPeriodTimestamps(period, customStart, customEnd)

        // 2. Получаем уникальный набор (Set) валют из репозитория
        val currenciesSet = repository.getAvailableCurrenciesBetween(startTimestamp, endTimestamp)

        // 3. Конвертируем в список и сортируем по алфавиту для предсказуемого отображения в UI
        return@withContext currenciesSet.toList().sorted()
    }
}