package com.avetiso.core.data.converters

import androidx.room.TypeConverter
import com.avetiso.core.model.AppointmentStatus

class AppointmentStatusConverter {

    @TypeConverter
    fun toStatus(value: String): AppointmentStatus {
        return AppointmentStatus.fromKey(value)
    }

    @TypeConverter
    fun fromStatus(status: AppointmentStatus): String {
        return status.key
    }
}