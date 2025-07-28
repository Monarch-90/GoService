package com.avetiso.feature_schedule.add_appointment.steps.step1.mvi

import com.avetiso.core.entity.ServiceEntity

data class Step1State(
    val availableServices: List<ServiceEntity> = emptyList(),
    val selectedServices: Set<ServiceEntity> = emptySet(),
)