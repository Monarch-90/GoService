package com.avetiso.feature_schedule.add_appointment.steps.step1.add_service.mvi

data class AddServiceState(
    val selectedHour: Int = 0,
    val selectedMinute: Int = 0,
    val isPriceFrom: Boolean = false,
    val selectedCurrency: String = "BYN"
)