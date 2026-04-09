package com.avetiso.feature_schedule.add_appointment.steps.step1.add_service.mvi

import com.avetiso.core.models.UiText

sealed interface AddServiceEvent {
    data class ShowToast(val message: UiText) : AddServiceEvent
    data object NavigateBackWithResult : AddServiceEvent
    data class AskToSetDefaultCurrency(val currencyCode: String) : AddServiceEvent
    data object ShowAddCurrencyDialog : AddServiceEvent
    data class RestoreCurrencySelection(val previousCurrencyCode: String?) : AddServiceEvent
    data class ShowDeleteCurrencyDialog(val currencies: Array<String>) : AddServiceEvent
}