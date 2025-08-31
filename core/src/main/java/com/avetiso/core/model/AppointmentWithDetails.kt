package com.avetiso.core.model

import com.avetiso.core.entity.AppointmentEntity
import com.avetiso.core.entity.ClientEntity
import com.avetiso.core.entity.ServiceEntity

data class AppointmentWithDetails(
    val appointment: AppointmentEntity,
    val client: ClientEntity,
    val services: List<ServiceEntity>
)