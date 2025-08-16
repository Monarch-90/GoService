package com.avetiso.feature_schedule.add_appointment.mvi

import com.avetiso.core.entity.ServiceEntity
import com.avetiso.core.entity.TimeSlotEntity

sealed interface AddAppointmentEvent {
    data object NextButtonClicked : AddAppointmentEvent
    data object NavigateToAddService : AddAppointmentEvent
    data object BackPressed : AddAppointmentEvent
    object ClearSelection : AddAppointmentEvent

    data class ServiceSelected(val service: ServiceEntity, val isSelected: Boolean) :
        AddAppointmentEvent

    data class TimeSlotClicked(val timeSlot: TimeSlotEntity) : AddAppointmentEvent
    // Новое событие для очистки выбора слотов
    data object ClearTimeSlotSelection : AddAppointmentEvent
}