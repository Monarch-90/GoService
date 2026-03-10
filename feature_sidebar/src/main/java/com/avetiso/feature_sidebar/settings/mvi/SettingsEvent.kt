package com.avetiso.feature_sidebar.settings.mvi

sealed interface SettingsEvent {
    data class AskConfirmation(val currencyCode: String) : SettingsEvent
    data class RestoreSelection(val currencyCode: String?) : SettingsEvent // Чтобы вернуть спиннер назад при отказе
    data object ShowAddCurrencyDialog : SettingsEvent
    data class ShowDeleteCurrencyDialog(val currencies: Array<String>) : SettingsEvent
}