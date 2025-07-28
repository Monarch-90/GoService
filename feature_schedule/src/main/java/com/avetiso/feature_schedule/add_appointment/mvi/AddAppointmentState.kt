package com.avetiso.feature_schedule.add_appointment.mvi

data class AddAppointmentState(
    val currentStep: Int = 0, // Шаги от 0 до 2
    val isNextButtonEnabled: Boolean = false
)