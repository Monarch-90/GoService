package com.avetiso.feature_statistics.mvi

import com.avetiso.feature_statistics.models.StatisticsListItem
import com.avetiso.feature_statistics.models.TimePeriod

data class StatisticsState(
    val isLoading: Boolean,
    val selectedPeriod: TimePeriod,
    // Экран будет строиться динамически на основе списка UI-моделей.
    // Это исключает жесткую привязку виджетов к XML и позволяет легко расширять дашборд.
    val dashboardItems: List<StatisticsListItem>,
    val error: Throwable?
) {
    companion object {
        fun initial() = StatisticsState(
            isLoading = true,
            selectedPeriod = TimePeriod.TODAY,
            dashboardItems = emptyList(),
            error = null
        )
    }
}