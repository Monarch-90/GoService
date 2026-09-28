package com.avetiso.feature_appointments.model

import com.avetiso.core.entity.ui.Appointment

data class AppointmentContentItem(
    val appointment: Appointment
) : AppointmentsListItem
