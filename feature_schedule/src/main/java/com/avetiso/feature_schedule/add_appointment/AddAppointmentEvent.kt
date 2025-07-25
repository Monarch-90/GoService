package com.avetiso.feature_schedule.add_appointment

sealed interface AddAppointmentEvent {
    data object NextButtonClicked : AddAppointmentEvent
    data class StepDataChanged(val isStepComplete: Boolean) : AddAppointmentEvent
}