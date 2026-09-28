package com.avetiso.feature_clients.mvi

data class EditClientIntent(
    val clientId: Long
) : ClientsIntent
