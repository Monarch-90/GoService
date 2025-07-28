package com.avetiso.feature_schedule.add_appointment.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avetiso.feature_schedule.add_appointment.ADD_APPOINTMENT_PAGE_COUNT
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
                    _state.update {
                        it.copy(
                            currentStep = currentStep + 1,
//                            isNextButtonEnabled = false // При переходе на следующий шаг, сбрасываем состояние кнопки
                        )
                    }
                } else {
                    // TODO: Логика сохранения записи
                }
            }
            // Пока просто заглушка, позже будем использовать
            is AddAppointmentEvent.StepDataChanged -> {
                _state.update { it.copy(isNextButtonEnabled = event.isStepComplete) }
            }

            is AddAppointmentEvent.NavigateToAddService -> {
                viewModelScope.launch {
                    _navigationChannel.send(Unit)
                }
            }
        }
    }
}