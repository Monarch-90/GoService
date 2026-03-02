package com.avetiso.core.model

data class CurrencyTotal(
    val currency: String,
    val amount: Double,
    val isPriceFrom: Boolean
)