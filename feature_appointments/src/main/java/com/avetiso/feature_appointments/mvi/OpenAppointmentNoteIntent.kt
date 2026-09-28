package com.avetiso.feature_appointments.mvi

data class OpenAppointmentNoteIntent(
    val appointmentId: Long,
    val currentNote: String
) : AppointmentsIntent
