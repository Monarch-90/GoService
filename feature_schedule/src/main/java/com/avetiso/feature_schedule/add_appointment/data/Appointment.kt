package com.avetiso.feature_schedule.add_appointment.data

import com.avetiso.core.model.AppointmentStatus

data class Appointment(
    val id: Long,
    val time: String,
    val date: String,
    val serviceNames: String,
    val clientName: String,
    val price: String,
    val hasDiscount: Boolean,
    val status: AppointmentStatus,
    val note: String,
)