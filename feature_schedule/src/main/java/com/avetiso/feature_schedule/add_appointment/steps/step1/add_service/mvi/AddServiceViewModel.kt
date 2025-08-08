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

@HiltViewModel
class AddServiceViewModel @Inject constructor(
    private val serviceDao: ServiceDao,
) : ViewModel() {

    // Приватный MutableStateFlow для хранения и изменения состояния
    private val _uiState = MutableStateFlow(AddServiceState())

    // Публичный StateFlow только для чтения из UI
    val uiState = _uiState.asStateFlow()

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
            // Если id == 0, значит, это новая сущность.
            // Если id != 0, значит, мы редактируем существующую.
            if (service.id == 0L) {
                serviceDao.insertService(service)
            } else {
                serviceDao.updateService(service)
            }
        }
    }
}