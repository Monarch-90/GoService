package com.avetiso.feature_schedule.add_appointment.mvi

import com.avetiso.core.entity.ServiceEntity
import com.avetiso.core.entity.TimeSlotEntity

data class AddAppointmentState(
    val currentStep: Int = 0, // Шаги от 0 до 2
    val isNextButtonEnabled: Boolean = false,
    val selectedServices: Set<ServiceEntity> = emptySet(),
    val selectedTimeSlots: Set<TimeSlotEntity> = emptySet(),
)