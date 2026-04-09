package com.avetiso.feature_appointments.mvi

import com.avetiso.core.models.AppointmentStatus

sealed interface AppointmentsIntent {
    data class OnDeleteClicked(val appointmentId: Long) : AppointmentsIntent
    data object ConfirmDelete : AppointmentsIntent
    data class OnEditClicked(val appointmentId: Long) : AppointmentsIntent
    data class OnNoteClicked(val appointmentId: Long, val currentNote: String) : AppointmentsIntent
    data class SaveNote(val newNote: String) : AppointmentsIntent
    data class ChangeStatus(val appointmentId: Long, val status: AppointmentStatus, val newDate: String? = null) : AppointmentsIntent
    data class UpdateSearchQuery(val query: String) : AppointmentsIntent
}