package com.avetiso.feature_settings.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avetiso.core.AppConstants
import com.avetiso.core.data.repository.SettingsRepository
import com.avetiso.core.model.AppCurrency
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
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(AppConstants.Ui.SNACKBAR_LONG_DURATION),
            null
        )

    private val _events = Channel<SettingsEvent>()
    val events = _events.receiveAsFlow()


    private var pendingCurrency: AppCurrency? = null

    // Пользователь нажал на спиннер
    fun onCurrencySelected(newCurrency: AppCurrency) {
        // Важно: проверяем, отличается ли выбор от того, что уже сохранено
        if (newCurrency.name != currentDefaultCurrency.value) {

            // Запоминаем выбор во ViewModel
            pendingCurrency = newCurrency

            viewModelScope.launch {
                _events.send(SettingsEvent.AskConfirmation(newCurrency))
            }
        }
    }

    fun onConfirmationSuccess() {
        val currencyToSave = pendingCurrency ?: return // Если пусто - выходим (защита)

        viewModelScope.launch {
            settingsRepository.setDefaultCurrency(currencyToSave.name)
            // Очищаем временное хранилище после сохранения
            pendingCurrency = null
        }
    }

    // Пользователь нажал "Нет"
    fun onDeclineCurrencyChange() {
        // Очищаем ожидание
        pendingCurrency = null

        viewModelScope.launch {
            // Возвращаем спиннер визуально на старое значение (которое сейчас в БД)
            _events.send(SettingsEvent.RestoreSelection(currentDefaultCurrency.value))
        }
    }
}