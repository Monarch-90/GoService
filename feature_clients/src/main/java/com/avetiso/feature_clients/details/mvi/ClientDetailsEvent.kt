package com.avetiso.feature_clients.details.mvi

sealed interface ClientDetailsEvent {
    data object EditClient : ClientDetailsEvent
    data object NavigateBack : ClientDetailsEvent
    data object DeleteClient : ClientDetailsEvent
}