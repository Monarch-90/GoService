package com.avetiso.feature_appointments.mvi

data class ShowAppointmentNoteDialogEvent(
    val currentNote: String
) : AppointmentsEvent
