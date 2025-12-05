package com.avetiso.feature_clients.details.mvi

import com.avetiso.core.entity.ClientEntity

data class ClientDetailsState(
    val client: ClientEntity? = null,
    val isLoading: Boolean = true,
)