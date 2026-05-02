package com.avetiso.feature_sidebar.contact_developers.mvi

import androidx.annotation.StringRes

sealed interface ContactDevelopersEvent
data class ShowToast(@StringRes val messageResId: Int) : ContactDevelopersEvent
object CloseScreen : ContactDevelopersEvent