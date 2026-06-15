package com.avetiso.feature_sidebar.about.mvi

sealed interface AboutEvent {
    data class ShowToast(val message: String) : AboutEvent
    object NavigateBack : AboutEvent
    data class OpenWebPage(val url: String) : AboutEvent
}