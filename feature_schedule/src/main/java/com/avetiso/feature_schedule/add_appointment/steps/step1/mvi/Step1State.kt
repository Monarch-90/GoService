package com.avetiso.feature_schedule.add_appointment.steps.step1.mvi

import com.avetiso.core.entity.ServiceEntity

data class Step1State(
    val availableServices: List<ServiceEntity> = emptyList(), // Доступные услуги
    val selectedServices: Set<ServiceEntity> = emptySet(), // Выделенные услуги
    val searchQuery: String = "", // Поле, для хранения текста, из поиска услуги
)