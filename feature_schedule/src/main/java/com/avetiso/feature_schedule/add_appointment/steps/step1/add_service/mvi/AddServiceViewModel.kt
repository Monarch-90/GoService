package com.avetiso.feature_schedule.add_appointment.steps.step1.add_service.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avetiso.core.data.dao.ServiceDao
import com.avetiso.core.entity.ServiceEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

@HiltViewModel
class AddServiceViewModel @Inject constructor(
    private val serviceDao: ServiceDao,
) : ViewModel() {

    // Приватный MutableStateFlow для хранения и изменения состояния
    private val _uiState = MutableStateFlow(AddServiceState())

    // Публичный StateFlow только для чтения из UI
    val uiState = _uiState.asStateFlow()

    private val _eventChannel = Channel<AddServiceEvent>()
    val events = _eventChannel.receiveAsFlow()

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
    fun setCurrency(currency: String) {
        _uiState.update { currentState ->
            currentState.copy(selectedCurrency = currency)
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
}