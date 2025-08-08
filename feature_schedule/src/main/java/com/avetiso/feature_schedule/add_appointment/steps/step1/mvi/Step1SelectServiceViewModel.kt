package com.avetiso.feature_schedule.add_appointment.steps.step1.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avetiso.core.data.dao.ServiceDao
import com.avetiso.core.entity.ServiceEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class Step1SelectServiceViewModel @Inject constructor(
    private val serviceDao: ServiceDao,
) : ViewModel() {

    private val _state = MutableStateFlow(Step1State())
    val state = _state.asStateFlow()

    init {
        // Мы "слушаем" изменения нашего состояния...
        state
            // ...нас интересует только изменение поля `searchQuery`...
            .map { it.searchQuery }
            // ...убираем слишком быстрые повторные запросы (например, при быстром наборе текста)...
            .debounce(300L)
            // ...и для каждого нового запроса отменяем старый и выполняем новый.
            .flatMapLatest { query ->
                if (query.isBlank()) {
                    // Если запрос пустой, показываем все услуги
                    serviceDao.getAllServices()
                } else {
                    // Если что-то введено, ищем в БД
                    serviceDao.searchServices(query)
                }
            }
            .onEach { services ->
                // Обновляем список услуг в состоянии
                _state.update { it.copy(availableServices = services) }
            }
            .launchIn(viewModelScope)
    }

    fun handleEvent(event: Step1Event) {
        when (event) {
            is Step1Event.ServiceSelected -> {
                _state.update { currentState ->
                    val newSelection = currentState.selectedServices.toMutableSet()
                    if (event.isSelected) {
                        newSelection.add(event.service)
                    } else {
                        newSelection.remove(event.service)
                    }
                    currentState.copy(selectedServices = newSelection)
                }
            }
            // Обрабатываем новое событие. Поиск услуг
            is Step1Event.SearchQueryChanged -> {
                _state.update { it.copy(searchQuery = event.query) }
            }
        }
    }

    fun deleteService(service: ServiceEntity) {
        viewModelScope.launch {
            serviceDao.deleteService(service)
        }
    }
}