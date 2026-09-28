package com.avetiso.feature_clients.mvi

import com.avetiso.feature_clients.model.ClientsListItem

data class ClientsState(
    val items: List<ClientsListItem> = emptyList(),
    val searchQuery: String = ""
)
