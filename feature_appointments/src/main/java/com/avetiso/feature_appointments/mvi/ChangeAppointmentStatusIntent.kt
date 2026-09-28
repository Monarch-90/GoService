package com.avetiso.feature_appointments.mvi

import com.avetiso.core.models.AppointmentStatus

data class ChangeAppointmentStatusIntent(
    val appointmentId: Long,
    val status: AppointmentStatus,
    val newDate: String? = null
) : AppointmentsIntent
