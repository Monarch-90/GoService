package com.avetiso.feature_schedule.add_appointment.steps.step1.mvi

import com.avetiso.core.entity.ServiceEntity

sealed interface Step1Event {
    data class ServiceSelected(val service: ServiceEntity, val isSelected: Boolean) : Step1Event
}