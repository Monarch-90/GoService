package com.avetiso.feature_schedule.add_appointment.data

import java.time.LocalTime

data class Appointment(
    val id: Long,
    val time: String,
    val date: String,
    val serviceNames: String,
    val clientName: String,
    val price: String,
    val hasDiscount: Boolean,
    val status: String,
    val note: String,
)