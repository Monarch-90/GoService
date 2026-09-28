package com.avetiso.feature_appointments.mvi

data class UpdateAppointmentsSearchQueryIntent(
    val query: String
) : AppointmentsIntent
