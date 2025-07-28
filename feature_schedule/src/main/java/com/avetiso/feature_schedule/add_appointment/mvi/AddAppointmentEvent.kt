package com.avetiso.feature_schedule.add_appointment.mvi

sealed interface AddAppointmentEvent {
    data object NextButtonClicked : AddAppointmentEvent
    data class StepDataChanged(val isStepComplete: Boolean) : AddAppointmentEvent
    data object NavigateToAddService : AddAppointmentEvent
}