package com.avetiso.feature_schedule.add_appointment.steps.step1.add_service.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avetiso.core.AppConstants
import com.avetiso.core.data.dao.ServiceDao
import com.avetiso.core.data.repository.SettingsRepository
import com.avetiso.core.entity.ServiceEntity
import com.avetiso.core.models.CurrencyListItem
import com.avetiso.core.models.UiText
import com.avetiso.core.usecase.GetCurrencyListUseCase
import com.avetiso.feature_schedule.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddServiceViewModel @Inject constructor(
    private val serviceDao: ServiceDao,
    private val settingsRepository: SettingsRepository,
    getCurrencyListUseCase: GetCurrencyListUseCase,
) : ViewModel() {

    // Приватный MutableStateFlow для хранения и изменения состояния
    private val _uiState = MutableStateFlow(AddServiceState())

    // Публичный StateFlow только для чтения из UI
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<AddServiceEvent>()
    val events = _events.receiveAsFlow()

    // Дефолтная валюта, чтобы выбрать её при первом открытии
    val defaultCurrency = settingsRepository.defaultCurrency
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(AppConstants.Ui.SNACKBAR_LONG_DURATION),
            null
        )

    // Реактивный список для адаптера
    val currencyList = getCurrencyListUseCase()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(AppConstants.Ui.SNACKBAR_LONG_DURATION),
            emptyList()
        )
    private var selectedCurrencyCode: String? = null

    init {
        // При старте загружаем валюту по умолчанию
        viewModelScope.launch {
            val defaultCurrencyCode = settingsRepository.defaultCurrency.first()
            _uiState.update { it.copy(selectedCurrency = defaultCurrencyCode) }
            selectedCurrencyCode = defaultCurrencyCode
        }
    }

    // Метод для обновления продолжительности
    fun setDuration(hour: Int, minute: Int) {
        _uiState.update { it.copy(selectedHour = hour, selectedMinute = minute) }
    }

    // Метод для обновления флага "цена от"
    fun setPriceFrom(isFrom: Boolean) {
        _uiState.update { it.copy(isPriceFrom = isFrom) }
    }

    // Метод для обновления валюты
    fun setCurrency(currency: String?) {
        _uiState.update { it.copy(selectedCurrency = currency) }
    }

    fun setSelectedCategory(categoryName: String) {
        _uiState.update { it.copy(selectedCategoryName = categoryName) }
    }

    fun onCurrencyItemSelected(item: CurrencyListItem) {
        when (item) {
            is CurrencyListItem.ActionAdd -> {
                viewModelScope.launch {
                    // Откатываем визуал на предыдущую выбранную валюту
                    _events.send(AddServiceEvent.RestoreCurrencySelection(selectedCurrencyCode ?: defaultCurrency.value))
                    _events.send(AddServiceEvent.ShowAddCurrencyDialog)
                }
            }
            is CurrencyListItem.ActionDelete -> {
                viewModelScope.launch {
                    // 1. Откатываем визуал спиннера назад
                    _events.send(AddServiceEvent.RestoreCurrencySelection(selectedCurrencyCode ?: defaultCurrency.value))
                    // 2. Берем список кастомных валют и кидаем эвент на открытие диалога
                    val customCurrencies = settingsRepository.customCurrencies.first().toTypedArray()
                    _events.send(AddServiceEvent.ShowDeleteCurrencyDialog(customCurrencies))
                }
            }
            is CurrencyListItem.Currency -> {
                val newCurrencyCode = item.code
                selectedCurrencyCode = newCurrencyCode
                _uiState.update { it.copy(selectedCurrency = newCurrencyCode) }

                viewModelScope.launch {
                    val savedDefaultCode = settingsRepository.defaultCurrency.first()
                    // Если выбрали новую валюту, предлагаем сделать её дефолтной
                    if (newCurrencyCode != savedDefaultCode) {
                        // Обрати внимание: теперь передаем String, а не Enum
                        _events.send(AddServiceEvent.AskToSetDefaultCurrency(newCurrencyCode))
                    }
                }
            }
        }
    }

    // Метод для сохранения новой дефолтной валюты
    fun setNewDefaultCurrency(currencyCode: String) {
        viewModelScope.launch {
            settingsRepository.setDefaultCurrency(currencyCode)
        }
    }

    fun onDefaultCurrencyConfirmed() {
        selectedCurrencyCode?.let { setNewDefaultCurrency(it) }
    }

    fun onDefaultCurrencyDeclined() {
        // Ничего не делаем, просто оставляем текущую валюту только для этой услуги
    }

    fun saveCustomCurrency(currency: String) {
        val cleanCurrency = currency.trim().uppercase()

        viewModelScope.launch {
            settingsRepository.addCustomCurrency(cleanCurrency)
        }
    }

    fun saveService(service: ServiceEntity) {
        viewModelScope.launch {
            // 1. Выполняем проверку на дубликат
            val duplicate = serviceDao.findServiceByDetails(
                name = service.name,
                categoryName = service.categoryName,
                isPriceFrom = service.isPriceFrom,
                price = service.price,
                currency = service.currency,
                durationMinutes = service.durationMinutes,
                idToExclude = service.id // Исключаем текущий ID
            )

            // 2. Если дубликат найден, отправляем событие с ошибкой
            if (duplicate != null) {
                _events.send(
                    AddServiceEvent.ShowToast(
                        UiText.StringResource(
                            R.string.service_already_exists
                        )
                    )
                )
                return@launch
            }

            // 3. Если дубликатов нет, сохраняем и отправляем событие навигации
            if (service.id == 0L) {
                serviceDao.insertService(service)
            } else {
                serviceDao.updateService(service)
            }
            _events.send(AddServiceEvent.NavigateBackWithResult)
        }
    }

    fun deleteCustomCurrency(currency: String) {
        viewModelScope.launch {
            settingsRepository.removeCustomCurrency(currency)

            // Если мы удалили ту валюту, которая сейчас была выбрана в форме,
            // безопасно откатываемся на дефолтную, чтобы форма не сломалась
            if (selectedCurrencyCode == currency) {
                val fallbackCurrency = settingsRepository.defaultCurrency.first()
                selectedCurrencyCode = fallbackCurrency
                _uiState.update { it.copy(selectedCurrency = fallbackCurrency) }
            }
        }
    }
}