package com.avetiso.feature_settings.mvi

sealed interface SettingsEvent {
    data class AskConfirmation(val currency: String) : SettingsEvent
    data class RestoreSelection(val currency: String?) : SettingsEvent // Чтобы вернуть спиннер назад при отказе
}