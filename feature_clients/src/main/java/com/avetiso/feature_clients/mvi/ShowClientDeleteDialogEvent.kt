package com.avetiso.feature_clients.mvi

data class ShowClientDeleteDialogEvent(
    val clientName: String
) : ClientsEvent
