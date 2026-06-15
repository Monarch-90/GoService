package com.avetiso.feature_sidebar.about.mvi

sealed interface AboutState {
    object Idle : AboutState
    object Loading : AboutState
    data class Success(val text: String) : AboutState
    data class Error(val message: String) : AboutState
}