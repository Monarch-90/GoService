package com.avetiso.feature_clients.mvi

data class DeleteClientClickedIntent(
    val clientId: Long,
    val clientName: String
) : ClientsIntent
