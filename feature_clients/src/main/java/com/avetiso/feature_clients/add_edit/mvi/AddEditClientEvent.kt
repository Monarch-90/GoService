package com.avetiso.feature_clients.add_edit.mvi

import com.avetiso.core.model.UiText

sealed interface AddEditClientEvent {
    data class ShowToast(val message: UiText) : AddEditClientEvent
    data object NavigateBackWithResult : AddEditClientEvent
}