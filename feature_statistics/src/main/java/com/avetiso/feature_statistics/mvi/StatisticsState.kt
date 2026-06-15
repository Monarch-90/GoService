package com.avetiso.feature_statistics.mvi

import com.avetiso.feature_statistics.domain.models.FinanceSummary
import com.avetiso.feature_statistics.domain.models.InventoryShortageItem
import com.avetiso.feature_statistics.domain.models.WorkloadSummary
import com.avetiso.feature_statistics.domain.models.TimePeriod

/**
 * Состояние экрана статистики (Дашборд).
 * Никаких вложенных sealed классов. Плоская, легко масштабируемая структура.
 * Все изменения периода мгновенно отражаются здесь, а UI просто отрисовывает этот стейт.
 */
data class StatisticsState(
    val isLoading: Boolean = true,

    // Шапка: Выбор периода
    val selectedPeriod: TimePeriod = TimePeriod.TODAY,
    val customDateStart: Long? = null, // Unix timestamp для точной выборки из БД
    val customDateEnd: Long? = null,

    // --- НОВОЕ: Мультивалютность ---
    val selectedCurrency: String? = null, // Текущая выбранная валюта для расчетов (по умолчанию будет из настроек)
    val availableCurrencies: List<String> = emptyList(), // Список всех валют, по которым были доходы в этом периоде (для спиннера)

    // Виджет 1: Финансы
    val financeSummary: FinanceSummary? = null,

    // Виджет 2: Расходники (Складской радар)
    val inventoryShortages: List<InventoryShortageItem> = emptyList(),

    // Виджет 3: Клиенты и загруженность
    val workloadSummary: WorkloadSummary? = null
)