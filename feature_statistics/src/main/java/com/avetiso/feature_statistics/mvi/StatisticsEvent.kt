package com.avetiso.feature_statistics.mvi

sealed interface StatisticsEvent {
    data class ShowToast(val messageResId: Int) : StatisticsEvent
    data class CopyToClipboard(val text: String) : StatisticsEvent

    // Навигация выносится в события, чтобы Fragment дергал Router/Navigator
    object NavigateToFinancialDetails : StatisticsEvent
    object NavigateToInventory : StatisticsEvent
    object NavigateToClientsStats : StatisticsEvent
    object NavigateToCustomDateRangePicker : StatisticsEvent
}