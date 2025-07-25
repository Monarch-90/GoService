package com.avetiso.feature_schedule.add_appointment

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AddAppointmentViewModel : ViewModel() {

    private val _state = MutableStateFlow(AddAppointmentState())
    val state = _state.asStateFlow()

    fun handleEvent(event: AddAppointmentEvent) {
        when (event) {
            is AddAppointmentEvent.NextButtonClicked -> {
                val currentStep = _state.value.currentStep
                if (currentStep < ADD_APPOINTMENT_PAGE_COUNT - 1) {
                    _state.update { it.copy(currentStep = currentStep + 1) }
                } else {
                    // TODO: Логика сохранения записи
                }
            }
            // Пока просто заглушка, позже будем использовать
            is AddAppointmentEvent.StepDataChanged -> {
                _state.update { it.copy(isNextButtonEnabled = event.isStepComplete) }
            }
        }
    }
}