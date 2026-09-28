package com.avetiso.feature_clients.mvi

data class NavigateToAddEditClientEvent(
    val clientId: Long?
) : ClientsEvent
