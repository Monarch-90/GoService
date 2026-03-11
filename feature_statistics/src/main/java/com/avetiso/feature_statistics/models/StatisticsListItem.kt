package com.avetiso.feature_statistics.models

sealed interface StatisticsListItem {

    data class PeriodFilter(
        val selectedPeriod: TimePeriod,
        val customDateRange: String? = null // Для отображения "12-18 марта", если выбран CUSTOM
    ) : StatisticsListItem

    data class FinanceCard(
        val totalRevenue: String,    // Уже отформатированная строка с валютой
        val averageCheck: String,    // Форматированная строка
        val servicesCount: Int,
        val trendPercent: Int,       // Например, 15
        val isTrendPositive: Boolean // Влияет на цвет стрелочки (зеленая вверх/красная вниз)
    ) : StatisticsListItem

    data class InventoryWarning(
        val items: List<InventoryShortItem>
    ) : StatisticsListItem {
        // Локальная модель только для этого виджета, чтобы не тащить целую Entity из БД
        data class InventoryShortItem(
            val id: Long,
            val name: String,
            val leftCount: Int,
            val measureUnit: String // "шт.", "уп."
        )
    }

    data class Workload(
        val newClientsCount: Int,
        val cancellationsCount: Int,
        val totalWorkHours: Int
    ) : StatisticsListItem

    // Поскольку кнопки быстрых действий статичны и не зависят от данных из БД,
    // достаточно передать data object. Клики будут обрабатываться через делегат адаптера.
    data object QuickActions : StatisticsListItem
}