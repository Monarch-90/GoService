package com.avetiso.feature_settings.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avetiso.core.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    // Подписываемся на реальные данные из БД
    val currentDefaultCurrency = settingsRepository.defaultCurrency
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _events = Channel<SettingsEvent>()
    val events = _events.receiveAsFlow()

    // Пользователь нажал на спиннер
    fun onCurrencySelected(newCurrency: String) {
        // Важно: проверяем, отличается ли выбор от того, что уже сохранено
        if (newCurrency != currentDefaultCurrency.value) {
            viewModelScope.launch {
                _events.send(SettingsEvent.AskConfirmation(newCurrency))
            }
        }
    }

    // Пользователь нажал "Да"
    fun confirmCurrencyChange(currency: String) {
        viewModelScope.launch {
            settingsRepository.setDefaultCurrency(currency)
        }
    }

    // Пользователь нажал "Нет"
    fun onDeclineCurrencyChange() {
        viewModelScope.launch {
            // Возвращаем спиннер на старое значение (которое сейчас в БД)
            _events.send(SettingsEvent.RestoreSelection(currentDefaultCurrency.value))
        }
    }
}