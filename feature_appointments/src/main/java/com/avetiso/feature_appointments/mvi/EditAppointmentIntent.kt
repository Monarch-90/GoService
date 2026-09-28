package com.avetiso.feature_appointments.mvi

data class EditAppointmentIntent(
    val appointmentId: Long
) : AppointmentsIntent
