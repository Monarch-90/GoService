package com.avetiso.feature_appointments.mvi

data class DeleteAppointmentClickedIntent(
    val appointmentId: Long
) : AppointmentsIntent
