package com.avetiso.feature_clients.add_edit.mvi

sealed interface AddEditClientEvent {
    data class ShowToast(val message: String) : AddEditClientEvent
    data object NavigateBackWithResult : AddEditClientEvent
    data object InitialDataSet : AddEditClientEvent
}