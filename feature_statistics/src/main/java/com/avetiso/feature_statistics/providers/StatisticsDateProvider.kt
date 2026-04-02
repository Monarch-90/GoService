package com.avetiso.feature_statistics.providers

import com.avetiso.feature_statistics.StatisticsConstants
import com.avetiso.feature_statistics.models.TimePeriod
import java.util.Calendar
import javax.inject.Inject

/**
 * Провайдер для централизованной работы с датами и периодами в модуле статистики.
 * Избавляет UseCase-ы от дублирования логики и хардкода.
 */
class StatisticsDateProvider @Inject constructor() {

    fun getPeriodTimestamps(period: TimePeriod, customStart: Long?, customEnd: Long?): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis

        return when (period) {
            TimePeriod.TODAY -> {
                calendar.set(Calendar.HOUR_OF_DAY, StatisticsConstants.Time.HOUR_START)
                calendar.set(Calendar.MINUTE, StatisticsConstants.Time.MINUTE_START)
                calendar.set(Calendar.SECOND, StatisticsConstants.Time.SECOND_START)
                calendar.set(Calendar.MILLISECOND, StatisticsConstants.Time.MILLISECOND_START)
                val startOfDay = calendar.timeInMillis

                calendar.set(Calendar.HOUR_OF_DAY, StatisticsConstants.Time.HOUR_END)
                calendar.set(Calendar.MINUTE, StatisticsConstants.Time.MINUTE_END)
                calendar.set(Calendar.SECOND, StatisticsConstants.Time.SECOND_END)
                calendar.set(Calendar.MILLISECOND, StatisticsConstants.Time.MILLISECOND_END)
                val endOfDay = calendar.timeInMillis

                Pair(startOfDay, endOfDay)
            }
            TimePeriod.WEEK -> {
                // Конец периода — строго конец сегодняшних суток (23:59:59.999)
                calendar.set(Calendar.HOUR_OF_DAY, StatisticsConstants.Time.HOUR_END)
                calendar.set(Calendar.MINUTE, StatisticsConstants.Time.MINUTE_END)
                calendar.set(Calendar.SECOND, StatisticsConstants.Time.SECOND_END)
                calendar.set(Calendar.MILLISECOND, StatisticsConstants.Time.MILLISECOND_END)
                val endOfWeek = calendar.timeInMillis

                // Начало периода — отступаем на 6 дней назад и берем начало тех суток (00:00:00.000)
                calendar.add(Calendar.DAY_OF_YEAR, StatisticsConstants.Time.WEEK_OFFSET_DAYS)
                calendar.set(Calendar.HOUR_OF_DAY, StatisticsConstants.Time.HOUR_START)
                calendar.set(Calendar.MINUTE, StatisticsConstants.Time.MINUTE_START)
                calendar.set(Calendar.SECOND, StatisticsConstants.Time.SECOND_START)
                calendar.set(Calendar.MILLISECOND, StatisticsConstants.Time.MILLISECOND_START)
                val startOfWeek = calendar.timeInMillis

                Pair(startOfWeek, endOfWeek)
            }
            TimePeriod.MONTH -> Pair(now - StatisticsConstants.Time.MILLIS_IN_MONTH, now)
            TimePeriod.CUSTOM -> Pair(customStart ?: StatisticsConstants.Time.CUSTOM_START, customEnd ?: now)
        }
    }

    fun getPreviousPeriodTimestamps(period: TimePeriod, currentStart: Long, currentEnd: Long): Pair<Long, Long> {
        val duration = currentEnd - currentStart
        return Pair(currentStart - duration, currentEnd - duration)
    }
}