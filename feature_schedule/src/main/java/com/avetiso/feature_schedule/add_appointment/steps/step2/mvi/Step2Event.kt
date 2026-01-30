package com.avetiso.feature_schedule.add_appointment.steps.step2.mvi

import com.avetiso.core.model.UiText

sealed interface Step2Event {
    data class ShowToast(val message: UiText) : Step2Event
}