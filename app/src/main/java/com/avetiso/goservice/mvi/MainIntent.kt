package com.avetiso.goservice.mvi

sealed interface MainIntent {
    data class OnSidebarItemClicked(val itemId: Int) : MainIntent
    data object OnDrawerClosed : MainIntent
}