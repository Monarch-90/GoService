package com.avetiso.feature_clients.mvi

import com.avetiso.core.entity.ClientEntity

data class ClientsState(
    val clients: List<ClientEntity> = emptyList(),
    val searchQuery: String = ""
)