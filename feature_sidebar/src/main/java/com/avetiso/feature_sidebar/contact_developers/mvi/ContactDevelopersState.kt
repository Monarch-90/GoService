package com.avetiso.feature_sidebar.contact_developers.mvi

data class ContactDevelopersState(
    val isLoading: Boolean = false,
    val emailErrorRes: Int? = null,
    val messageErrorRes: Int? = null
)