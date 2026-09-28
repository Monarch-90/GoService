package com.avetiso.feature_clients.mvi

data class OpenClientIntent(
    val clientId: Long
) : ClientsIntent
