package com.avetiso.feature_sidebar.mvi

import com.avetiso.core.model.AppCurrency

sealed interface SettingsEvent {
    data class AskConfirmation(val currency: AppCurrency) : SettingsEvent
    data class RestoreSelection(val currencyCode: String?) : SettingsEvent // Чтобы вернуть спиннер назад при отказе
}