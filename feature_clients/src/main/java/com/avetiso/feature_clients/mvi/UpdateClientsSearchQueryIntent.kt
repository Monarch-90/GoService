package com.avetiso.feature_clients.mvi

data class UpdateClientsSearchQueryIntent(
    val query: String
) : ClientsIntent
