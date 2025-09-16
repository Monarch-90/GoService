package com.avetiso.core.model

import com.avetiso.core.entity.AppointmentEntity
import com.avetiso.core.entity.ClientEntity
import com.avetiso.core.entity.ServiceEntity
import com.avetiso.core.entity.TimeSlotEntity

data class AppointmentWithDetails(
    val appointment: AppointmentEntity,
    val client: ClientEntity,
    val services: List<ServiceEntity>,
    val timeSlot: TimeSlotEntity,
)