package com.avetiso.feature_schedule.add_appointment.steps.step1.add_service.mvi

import com.avetiso.core.model.AppCurrency

sealed interface AddServiceEvent {
    data class ShowToast(val message: String) : AddServiceEvent
    data object NavigateBackWithResult : AddServiceEvent
    data class AskToSetDefaultCurrency(val currency: AppCurrency) : AddServiceEvent
}