package com.avetiso.feature_schedule.data

import java.time.LocalTime

data class Appointment(
    val time: LocalTime,
    val serviceName: String,
    val clientName: String,
    val durationMinutes: Long
)