package com.avetiso.feature_appointments.mvi

import com.avetiso.feature_appointments.model.AppointmentsListItem

data class AppointmentsState(
    val isLoading: Boolean = true,
    val items: List<AppointmentsListItem> = emptyList()
)