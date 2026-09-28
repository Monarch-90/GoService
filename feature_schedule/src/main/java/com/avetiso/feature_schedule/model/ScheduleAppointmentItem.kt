package com.avetiso.feature_schedule.model

import com.avetiso.core.entity.ui.Appointment

data class ScheduleAppointmentItem(
    val appointment: Appointment
) : ScheduleListItem
