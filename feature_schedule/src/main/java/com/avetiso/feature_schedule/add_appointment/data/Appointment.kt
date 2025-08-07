package com.avetiso.feature_schedule.add_appointment.data

import java.time.LocalTime

data class Appointment(
    val time: LocalTime,
    val serviceName: String,
    val clientName: String,
    val durationMinutes: Long
)