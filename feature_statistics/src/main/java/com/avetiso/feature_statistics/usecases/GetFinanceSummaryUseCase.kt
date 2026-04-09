package com.avetiso.feature_statistics.usecases

import com.avetiso.feature_statistics.StatisticsConstants
import com.avetiso.feature_statistics.models.FinanceSummary
import com.avetiso.feature_statistics.models.TimePeriod
import com.avetiso.feature_statistics.providers.StatisticsDateProvider
import com.avetiso.feature_statistics.repository.StatisticsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

/**
 * UseCase для формирования финансовой сводки.
 * Отвечает за бизнес-логику расчета доходов, среднего чека и финансовых трендов.
 * Выполняется на IO-диспетчере, так как работает с данными из БД.
 */
class GetFinanceSummaryUseCase @Inject constructor(
    private val repository: StatisticsRepository,
    private val dateProvider: StatisticsDateProvider // Внедряем провайдер дат
) {
    suspend fun execute(
        period: TimePeriod,
        currencyCode: String,
        customStart: Long?,
        customEnd: Long?
    ): FinanceSummary = withContext(Dispatchers.IO) {

        // 1. Получаем точные timestamp'ы через единый DateProvider
        val (currentStart, currentEnd) = dateProvider.getPeriodTimestamps(period, customStart, customEnd)

        // 2. Запрашиваем агрегированные данные из репозитория ТОЛЬКО для нужной валюты
        val currentRevenue = repository.getRevenueBetween(currentStart, currentEnd, currencyCode)
        val servicesCount = repository.getCompletedServicesCountBetween(currentStart, currentEnd, currencyCode)

        // 3. Вычисляем средний чек (защита от деления на ноль)
        val averageCheck = if (servicesCount > StatisticsConstants.Math.COUNT) {
            currentRevenue.divide(BigDecimal(servicesCount), StatisticsConstants.Math.RATIO, RoundingMode.HALF_UP)
        } else {
            BigDecimal.ZERO
        }

        // 4. Рассчитываем тренд (сравнение с предыдущим аналогичным периодом В ТОЙ ЖЕ ВАЛЮТЕ)
        val (prevStart, prevEnd) = dateProvider.getPreviousPeriodTimestamps(period, currentStart, currentEnd)
        val previousRevenue = repository.getRevenueBetween(prevStart, prevEnd, currencyCode)

        val trendPercentage = calculateTrend(currentRevenue, previousRevenue)

        return@withContext FinanceSummary(
            totalRevenue = currentRevenue,
            revenueTrendPercentage = trendPercentage,
            averageCheck = averageCheck,
            servicesRenderedCount = servicesCount
        )
    }

    /**
     * Рассчитывает процентную разницу между текущим и прошлым доходом.
     */
    private fun calculateTrend(current: BigDecimal, previous: BigDecimal): Int {
        if (previous.compareTo(BigDecimal.ZERO) == StatisticsConstants.Math.COUNT) {
            return if (current > BigDecimal.ZERO) StatisticsConstants.Math.PERCENTAGE_MULTIPLIER.toInt() else StatisticsConstants.Math.COUNT
        }

        val difference = current.subtract(previous)
        val ratio = difference.divide(previous, StatisticsConstants.Math.RATIO, RoundingMode.HALF_UP)
        return ratio.multiply(BigDecimal.valueOf(StatisticsConstants.Math.PERCENTAGE_MULTIPLIER)).toInt()
    }
}