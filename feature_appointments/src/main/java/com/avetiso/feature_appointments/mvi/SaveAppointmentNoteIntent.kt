package com.avetiso.feature_appointments.mvi

data class SaveAppointmentNoteIntent(
    val newNote: String
) : AppointmentsIntent
