package com.avetiso.feature_statistics.models

/**
 * Базовый контракт для всех элементов главного списка экрана статистики.
 * Строго плоская структура, никаких вложенных (nested) классов.
 */
sealed interface StatisticsListItem

// --- UI-Модели (мапятся из Domain-моделей во Fragment/ViewModel) ---

/**
 * UI-модель: Шапка с выбором периода.
 */
data class PeriodFilterItem(
    val selectedPeriod: TimePeriod,
    val customDateRange: String? = null
) : StatisticsListItem

/**
 * UI-модель: Главная финансовая карточка.
 * Деньги и чеки уже конвертированы в отформатированные строки для UI.
 */
data class FinanceCardItem(
    val totalRevenue: String,
    val averageCheck: String,
    val servicesCount: Int,
    val trendPercent: Int,
    val isTrendPositive: Boolean,

    // --- НОВОЕ: Мультивалютность ---
    val selectedCurrency: String, // Текущая выбранная валюта (например, "GEL")
    val availableCurrencies: List<String> // Список валют для спиннера (например, ["BTC", "GEL", "USD"])
) : StatisticsListItem

/**
 * UI-модель: Конкретный дефицитный материал.
 */
data class InventoryShortUIItem(
    val id: Long,
    val name: String,
    val leftCount: Int,
    val measureUnit: String
)

/**
 * UI-модель: Карточка "Складской радар".
 */
data class InventoryWarningItem(
    val items: List<InventoryShortUIItem>
) : StatisticsListItem

/**
 * UI-модель: Карточка "Клиенты и Загруженность".
 */
data class WorkloadItem(
    val newClientsCount: Int,
    val cancellationsCount: Int,
    val totalWorkHours: Int
) : StatisticsListItem

/**
 * UI-модель: Блок быстрых действий (статичный, поэтому object).
 */
object QuickActionsItem : StatisticsListItem