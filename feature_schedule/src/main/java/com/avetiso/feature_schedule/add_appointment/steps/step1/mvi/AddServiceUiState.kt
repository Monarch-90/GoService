package com.avetiso.feature_schedule.add_appointment.steps.step1.mvi

data class AddServiceUiState(
    val selectedHour: Int = 0,
    val selectedMinute: Int = 0,
    val isPriceFrom: Boolean = false,
    val selectedCurrency: String = "BYN"
)