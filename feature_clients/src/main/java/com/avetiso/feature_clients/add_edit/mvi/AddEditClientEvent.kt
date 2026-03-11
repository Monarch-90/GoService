package com.avetiso.feature_clients.add_edit.mvi

import com.avetiso.core.models.UiText

sealed interface AddEditClientEvent {
    data class ShowToast(val message: UiText) : AddEditClientEvent
    data object NavigateBackWithResult : AddEditClientEvent
}