package com.avetiso.core.models

data class CurrencyTotal(
    val currency: String,
    val amount: Double,
    val isPriceFrom: Boolean
)