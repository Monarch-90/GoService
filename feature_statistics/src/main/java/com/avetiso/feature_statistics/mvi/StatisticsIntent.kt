package com.avetiso.feature_statistics.mvi

import com.avetiso.feature_statistics.models.TimePeriod

/**
 * Интенты (действия пользователя и жизненного цикла) для экрана статистики.
 * Написано строго без вложенных классов (nested/inner classes).
 */
sealed interface StatisticsIntent

// --- Жизненный цикл экрана ---
object OnScreenResumed : StatisticsIntent // НОВОЕ: Интент для тихого обновления данных при возврате на вкладку

// --- Шапка экрана: Фильтры ---
data class SelectPeriod(val period: TimePeriod) : StatisticsIntent
data class SelectCustomPeriod(val startDateTimestamp: Long, val endDateTimestamp: Long) : StatisticsIntent

// --- Виджет 1: Финансы ---
object OnFinanceCardClicked : StatisticsIntent
data class SelectCurrency(val currencyCode: String) : StatisticsIntent

// --- Виджет 2: Расходники (Складской радар) ---
object OnInventorySeeAllClicked : StatisticsIntent
object OnInventoryCopyToClipboardClicked : StatisticsIntent

// --- Виджет 3: Клиенты и Загруженность ---
object OnWorkloadCardClicked : StatisticsIntent

// --- Виджет 4: Быстрые действия ---
object OnGenerateFreeWindowsClicked : StatisticsIntent
object OnSharePriceListClicked : StatisticsIntent