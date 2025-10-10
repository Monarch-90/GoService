package com.avetiso.feature_clients.selector.mvi

import com.avetiso.core.entity.ClientEntity

data class ClientSelectorState(
    val searchQuery: String = "",
    val selectedClient: ClientEntity? = null, // Состояние для выбранного клиента
)