package com.avetiso.feature_statistics.mvi

import com.avetiso.feature_statistics.models.TimePeriod

sealed interface StatisticsIntent {
    data class ChangePeriod(val period: TimePeriod) : StatisticsIntent
    object OpenCustomDateSelector : StatisticsIntent

    object OnFinancialCardClicked : StatisticsIntent

    object OnInventorySeeAllClicked : StatisticsIntent
    object OnInventoryCopyToClipboardClicked : StatisticsIntent

    object OnClientsLoadClicked : StatisticsIntent

    object OnGenerateFreeSlotsClicked : StatisticsIntent
    object OnSharePriceClicked : StatisticsIntent
}