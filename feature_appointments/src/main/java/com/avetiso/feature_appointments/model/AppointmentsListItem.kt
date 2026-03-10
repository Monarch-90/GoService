package com.avetiso.feature_appointments.model

import com.avetiso.core.entity.ui.Appointment // Твой путь к перенесенному классу Appointment

sealed interface AppointmentsListItem {
    data class DateHeader(
        val date: String // Сюда будем передавать отформатированную дату, например "12 март 2026"
    ) : AppointmentsListItem

    data class AppointmentItem(
        val appointment: Appointment
    ) : AppointmentsListItem
}