package com.avetiso.goservice.mvi

sealed interface MainSideEffect {
    data object CloseSidebar : MainSideEffect
    data class Navigate(val destinationId: Int) : MainSideEffect
}