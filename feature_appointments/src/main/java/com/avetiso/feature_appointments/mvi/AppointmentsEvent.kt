package com.avetiso.feature_appointments.mvi

import androidx.annotation.StringRes

sealed interface AppointmentsEvent {
    data class ShowDeleteDialog(val appointmentId: Long) : AppointmentsEvent
    data class ShowNoteDialog(val appointmentId: Long, val currentNote: String) : AppointmentsEvent
    data class NavigateToEdit(val appointmentId: Long) : AppointmentsEvent
    // Только безопасный ID строки!
    data class ShowToast(@StringRes val messageResId: Int) : AppointmentsEvent
}