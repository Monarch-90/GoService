package com.avetiso.feature_appointments.mvi

data class NavigateToEditAppointmentEvent(
    val appointmentId: Long
) : AppointmentsEvent
