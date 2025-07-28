package com.avetiso.feature_schedule.add_appointment.steps.step1.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avetiso.core.data.dao.ServiceDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class Step1SelectServiceViewModel @Inject constructor(
    serviceDao: ServiceDao
) : ViewModel() {

    private val _state = MutableStateFlow(Step1State())
    val state = _state.asStateFlow()

    init {
        // Подписываемся на все услуги из БД
        serviceDao.getAllServices()
            .onEach { services ->
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
        }
    }
}