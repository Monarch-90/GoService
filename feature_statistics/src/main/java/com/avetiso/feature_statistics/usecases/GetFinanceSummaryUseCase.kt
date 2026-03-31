package com.avetiso.feature_statistics.usecases

import com.avetiso.feature_statistics.StatisticsConstants
import com.avetiso.feature_statistics.models.FinanceSummary
import com.avetiso.feature_statistics.models.TimePeriod
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
    private val repository: StatisticsRepository
) {
    suspend fun execute(
        period: TimePeriod,
        currencyCode: String, // НОВОЕ: Передаем валюту для фильтрации
        customStart: Long?,
        customEnd: Long?
    ): FinanceSummary = withContext(Dispatchers.IO) {

        // 1. Получаем точные timestamp'ы для запроса в БД для выбранного периода
        val (currentStart, currentEnd) = calculatePeriodTimestamps(period, customStart, customEnd)

        // 2. Запрашиваем агрегированные данные из репозитория ТОЛЬКО для нужной валюты
        val currentRevenue = repository.getRevenueBetween(currentStart, currentEnd, currencyCode)
        val servicesCount = repository.getCompletedServicesCountBetween(currentStart, currentEnd, currencyCode)

        // 3. Вычисляем средний чек (защита от деления на ноль)
        val averageCheck = if (servicesCount > StatisticsConstants.Math.COUNT) {
            currentRevenue.divide(BigDecimal(servicesCount), 2, RoundingMode.HALF_UP)
        } else {
            BigDecimal.ZERO
        }

        // 4. Рассчитываем тренд (сравнение с предыдущим аналогичным периодом В ТОЙ ЖЕ ВАЛЮТЕ)
        val (prevStart, prevEnd) = calculatePreviousPeriodTimestamps(period, currentStart, currentEnd)
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

    /**
     * Конвертирует TimePeriod в конкретные Unix-timestamp'ы начала и конца.
     */
    private fun calculatePeriodTimestamps(period: TimePeriod, customStart: Long?, customEnd: Long?): Pair<Long, Long> {
        val now = System.currentTimeMillis()
        // В будущем вынесем это в отдельный DateTimeProvider, пока оставляем заглушки для контракта
        return when (period) {
            TimePeriod.TODAY -> Pair(now - StatisticsConstants.Time.MILLIS_IN_DAY, now)
            TimePeriod.WEEK -> Pair(now - StatisticsConstants.Time.MILLIS_IN_WEEK, now)
            TimePeriod.MONTH -> Pair(now - StatisticsConstants.Time.MILLIS_IN_MONTH, now)
            TimePeriod.CUSTOM -> Pair(customStart ?: StatisticsConstants.Time.CUSTOM_START, customEnd ?: now)
        }
    }

    /**
     * Вычисляет временные рамки для предыдущего периода (чтобы сравнить месяц к месяцу, неделю к неделе).
     */
    private fun calculatePreviousPeriodTimestamps(period: TimePeriod, currentStart: Long, currentEnd: Long): Pair<Long, Long> {
        val duration = currentEnd - currentStart
        return Pair(currentStart - duration, currentEnd - duration)
    }
}