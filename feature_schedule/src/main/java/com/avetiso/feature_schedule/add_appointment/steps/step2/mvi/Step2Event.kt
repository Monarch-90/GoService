package com.avetiso.feature_schedule.add_appointment.steps.step2.mvi

sealed interface Step2Event {
    data class ShowToast(val message: String) : Step2Event
}