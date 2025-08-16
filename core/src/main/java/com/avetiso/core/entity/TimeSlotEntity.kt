package com.avetiso.core.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "time_slots")
data class TimeSlotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    // Храним время начала в минутах от полуночи (например, 10:30 = 10 * 60 + 30 = 630)
    val startTimeMinutes: Int,
)