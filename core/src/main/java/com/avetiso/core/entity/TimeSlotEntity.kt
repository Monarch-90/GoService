package com.avetiso.core.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.avetiso.core.AppConstants

@Entity(tableName = AppConstants.Data.TABLE_TIMESLOT)
data class TimeSlotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    // Храним время начала в минутах от полуночи (например, 10:30 = 10 * 60 + 30 = 630)
    val startTimeMinutes: Int,
)