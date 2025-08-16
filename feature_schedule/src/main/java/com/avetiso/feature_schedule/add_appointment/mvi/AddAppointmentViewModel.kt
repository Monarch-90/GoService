package com.avetiso.feature_schedule.add_appointment.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avetiso.feature_schedule.add_appointment.ui.ADD_APPOINTMENT_PAGE_COUNT
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddAppointmentViewModel : ViewModel() {

    private val _state = MutableStateFlow(AddAppointmentState())
    val state = _state.asStateFlow()

    // канал для одноразовых событий навигации
    private val _navigationChannel = Channel<Unit>()
    val navigationEvents = _navigationChannel.receiveAsFlow()

    fun handleEvent(event: AddAppointmentEvent) {
        when (event) {
            is AddAppointmentEvent.NextButtonClicked -> {
                val currentStep = _state.value.currentStep
                if (currentStep < ADD_APPOINTMENT_PAGE_COUNT - 1) {
                    val nextStep = currentStep + 1
                    _state.update {
                        it.copy(
                            currentStep = nextStep,
                            isNextButtonEnabled = isStepComplete(nextStep, it) // Проверяем сразу
                        )
                    }
                } else {
                    // TODO: Логика сохранения записи
                }
            }

            is AddAppointmentEvent.BackPressed -> {
                val currentStep = _state.value.currentStep
                if (currentStep > 0) {
                    val previousStep = currentStep - 1
                    _state.update {
                        it.copy(
                            currentStep = previousStep,
                            // ✅ ГЛАВНОЕ ИСПРАВЛЕНИЕ:
                            // При возврате на шаг назад, мы ПЕРЕСЧИТЫВАЕМ состояние кнопки
                            // на основе уже имеющихся данных в state.
                            isNextButtonEnabled = isStepComplete(previousStep, it)
                        )
                    }
                }
            }

            // Управляем выбором услуг здесь
            is AddAppointmentEvent.ServiceSelected -> {
                _state.update { currentState ->
                    val newSelection = currentState.selectedServices.toMutableSet()
                    if (event.isSelected) {
                        newSelection.add(event.service)
                    } else {
                        newSelection.remove(event.service)
                    }
                    // Обновляем и список услуг, и состояние кнопки
                    currentState.copy(
                        selectedServices = newSelection,
                        isNextButtonEnabled = newSelection.isNotEmpty()
                    )
                }
            }

            is AddAppointmentEvent.TimeSlotClicked -> {
                _state.update { currentState ->
                    val newSelection = currentState.selectedTimeSlots.toMutableSet()
                    // Если слот уже есть в наборе, удаляем (снятие выделения).
                    // Иначе - добавляем (выделение).
                    if (newSelection.contains(event.timeSlot)) {
                        newSelection.remove(event.timeSlot)
                    } else {
                        newSelection.add(event.timeSlot)
                    }
                    val newState = currentState.copy(selectedTimeSlots = newSelection)
                    newState.copy(
                        isNextButtonEnabled = isStepComplete(
                            newState.currentStep,
                            newState
                        )
                    )
                }
            }

            is AddAppointmentEvent.ClearTimeSlotSelection -> {
                _state.update { currentState ->
                    val newState = currentState.copy(selectedTimeSlots = emptySet())
                    newState.copy(
                        isNextButtonEnabled = isStepComplete(
                            newState.currentStep,
                            newState
                        )
                    )
                }
            }

            is AddAppointmentEvent.NavigateToAddService -> {
                viewModelScope.launch {
                    _navigationChannel.send(Unit)
                }
            }

            is AddAppointmentEvent.ClearSelection -> {
                _state.update { currentState ->
                    currentState.copy(
                        selectedServices = emptySet(), // Очищаем список выделенных услуг
                        isNextButtonEnabled = false      // Выключаем кнопку "Далее"
                    )
                }
            }
        }
    }

    /**
     * Проверяет, завершен ли шаг.
     * Позже сюда нужно будет добавить проверки для шага 2 и 3.
     */
    private fun isStepComplete(step: Int, state: AddAppointmentState): Boolean {
        return when (step) {
            0 -> state.selectedServices.isNotEmpty()
            1 -> state.selectedTimeSlots.isNotEmpty()
            2 -> false // TODO: Добавить логику для шага 3 (выбран ли клиент)
            else -> false
        }
    }
}