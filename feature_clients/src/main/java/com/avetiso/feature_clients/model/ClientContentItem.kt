package com.avetiso.feature_clients.model

import com.avetiso.core.entity.ClientEntity

data class ClientContentItem(
    val client: ClientEntity
) : ClientsListItem
