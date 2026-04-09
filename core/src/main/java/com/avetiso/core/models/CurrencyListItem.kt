package com.avetiso.core.models

sealed interface CurrencyListItem {
    data class ActionAdd(val label: String) : CurrencyListItem
    data class ActionDelete(val label: String) : CurrencyListItem
    data class Currency(val code: String) : CurrencyListItem
}