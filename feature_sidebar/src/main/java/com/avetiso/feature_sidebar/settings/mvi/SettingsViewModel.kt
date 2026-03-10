package com.avetiso.feature_sidebar.settings.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avetiso.core.AppConstants
import com.avetiso.core.data.repository.SettingsRepository
import com.avetiso.core.model.AppCurrency
import com.avetiso.core.model.CurrencyListItem
import com.avetiso.core.usecase.GetCurrencyListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    getCurrencyListUseCase: GetCurrencyListUseCase,
) : ViewModel() {

    // Подписываемся на реальные данные из БД
    val currentDefaultCurrency = settingsRepository.defaultCurrency
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(AppConstants.Ui.SNACKBAR_LONG_DURATION),
            null
        )

    // Реактивный список для адаптера (кнопки + валюты)
    val currencyList = getCurrencyListUseCase()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(AppConstants.Ui.SNACKBAR_LONG_DURATION),
            emptyList()
        )

    private val _events = Channel<SettingsEvent>()
    val events = _events.receiveAsFlow()

    private var pendingCurrency: String? = null

    // Пользователь нажал на спиннер
    fun onItemSelected(item: CurrencyListItem) {
        when (item) {
            is CurrencyListItem.ActionAdd -> {
                viewModelScope.launch {
                    // Возвращаем визуальный выбор назад, чтобы "+ Добавить" не висело в поле
                    _events.send(SettingsEvent.RestoreSelection(currentDefaultCurrency.value))
                    _events.send(SettingsEvent.ShowAddCurrencyDialog)
                }
            }

            is CurrencyListItem.ActionDelete -> {
                viewModelScope.launch {
                    _events.send(SettingsEvent.RestoreSelection(currentDefaultCurrency.value))
                    // Берем список текущих кастомных валют для диалога удаления
                    val customCurrencies = settingsRepository.customCurrencies.first().toTypedArray()
                    _events.send(SettingsEvent.ShowDeleteCurrencyDialog(customCurrencies))
                }
            }

            is CurrencyListItem.Currency -> {
                if (item.code != currentDefaultCurrency.value) {
                    pendingCurrency = item.code
                    viewModelScope.launch {
                        _events.send(SettingsEvent.AskConfirmation(item.code))
                    }
                }
            }
        }
    }

    fun onConfirmationSuccess() {
        val currencyToSave = pendingCurrency ?: return
        viewModelScope.launch {
            settingsRepository.setDefaultCurrency(currencyToSave)
            pendingCurrency = null
        }
    }

    // Пользователь нажал "Нет"
    fun onDeclineCurrencyChange() {
        pendingCurrency = null
        viewModelScope.launch {
            _events.send(SettingsEvent.RestoreSelection(currentDefaultCurrency.value))
        }
    }

    fun addCustomCurrency(currency: String) {
        viewModelScope.launch {
            settingsRepository.addCustomCurrency(currency)
        }
    }

    fun deleteCustomCurrency(currency: String) {
        viewModelScope.launch {
            settingsRepository.removeCustomCurrency(currency)
            // Если удалили ту валюту, которая сейчас стоит по умолчанию, безопасно сбрасываем на GEL
            if (currentDefaultCurrency.value == currency) {
                settingsRepository.setDefaultCurrency(AppCurrency.GEL.name)
            }
        }
    }
}