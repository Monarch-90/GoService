package com.avetiso.feature_clients.selector.mvi

import com.avetiso.core.entity.ClientEntity
import com.avetiso.feature_clients.model.ClientsListItem

data class ClientSelectorState(
    val searchQuery: String = "",
    val selectedClient: ClientEntity? = null,
    val items: List<ClientsListItem> = emptyList()
)
