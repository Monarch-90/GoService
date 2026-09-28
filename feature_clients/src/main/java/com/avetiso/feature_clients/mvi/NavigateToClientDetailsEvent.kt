package com.avetiso.feature_clients.mvi

data class NavigateToClientDetailsEvent(
    val clientId: Long
) : ClientsEvent
