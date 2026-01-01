package com.avetiso.feature_schedule.add_appointment.steps.step1.add_service.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avetiso.core.data.dao.ServiceDao
import com.avetiso.core.data.repository.SettingsRepository
import com.avetiso.core.entity.ServiceEntity
import com.avetiso.core.model.AppCurrency
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow

@HiltViewModel
class AddServiceViewModel @Inject constructor(
    private val serviceDao: ServiceDao,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    // Приватный MutableStateFlow для хранения и изменения состояния
    private val _uiState = MutableStateFlow(AddServiceState())

    // Публичный StateFlow только для чтения из UI
    val uiState = _uiState.asStateFlow()

    private val _eventChannel = Channel<AddServiceEvent>()
    val events = _eventChannel.receiveAsFlow()
    private var pendingCurrencyForDefault: AppCurrency? = null

    init {
        // При старте загружаем валюту по умолчанию
        viewModelScope.launch {
            val defaultCurrency = settingsRepository.defaultCurrency.first()
            _uiState.update { it.copy(selectedCurrency = defaultCurrency) }
        }
    }

    // Метод для обновления продолжительности
    fun setDuration(hour: Int, minute: Int) {
        _uiState.update { currentState ->
            currentState.copy(selectedHour = hour, selectedMinute = minute)
        }
    }

    // Метод для обновления флага "цена от"
    fun setPriceFrom(isFrom: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(isPriceFrom = isFrom)
        }
    }

    // Метод для обновления валюты
    fun setCurrency(currency: String?) {
        _uiState.update { currentState ->
            currentState.copy(selectedCurrency = currency)
        }
    }

    // Метод вызывается, когда пользователь меняет значение в спиннере
    fun onCurrencySelectedInSpinner(newCurrencyCode: String) {

        // Обновляем UI
        _uiState.update { it.copy(selectedCurrency = newCurrencyCode) }

        // Если валюта изменилась и это не инициализация (простая проверка),
        // запускаем проверку, нужно ли показать диалог.
        // Нюанс: Спиннер вызывает onItemSelected даже при инициализации.
        // Чтобы избежать диалога при старте, можно проверить, отличается ли новая от сохраненной дефолтной.

        viewModelScope.launch {
            val savedDefaultCode = settingsRepository.defaultCurrency.first()
            if (newCurrencyCode != savedDefaultCode) {

                val currencyEnum = AppCurrency.fromCode(newCurrencyCode)
                // Запоминаем внутри ViewModel
                pendingCurrencyForDefault = currencyEnum
                // Отправляем событие во фрагмент, чтобы показать диалог
                _eventChannel.send(AddServiceEvent.AskToSetDefaultCurrency(currencyEnum))
            }
        }
    }

    // Метод для сохранения новой дефолтной валюты
    fun setNewDefaultCurrency(currency: AppCurrency) {
        viewModelScope.launch {
            settingsRepository.setDefaultCurrency(currency.name)
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
                _eventChannel.send(AddServiceEvent.ShowToast("Такая услуга уже существует"))
                return@launch
            }

            // 3. Если дубликатов нет, сохраняем и отправляем событие навигации
            if (service.id == 0L) {
                serviceDao.insertService(service)
            } else {
                serviceDao.updateService(service)
            }
            _eventChannel.send(AddServiceEvent.NavigateBackWithResult)
        }
    }

    // Вызывается фрагментом, когда пользователь нажал "ДА" в диалоге
    fun onDefaultCurrencyConfirmed() {
        val currency = pendingCurrencyForDefault
        if (currency != null) {
            setNewDefaultCurrency(currency)
        }
        pendingCurrencyForDefault = null
    }

    // Вызывается фрагментом, когда пользователь нажал "НЕТ" (опционально, для очистки)
    fun onDefaultCurrencyDeclined() {
        pendingCurrencyForDefault = null
    }

    fun setSelectedCategory(categoryName: String) {
        _uiState.update { it.copy(selectedCategoryName = categoryName) }
    }
}