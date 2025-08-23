package com.avetiso.feature_clients.add_edit.mvi

import com.avetiso.core.entity.ClientEntity

data class AddEditClientState(
    val client: ClientEntity? = null,
    val isEditing: Boolean = false,
    val navigateBack: Boolean = false,
)