package com.avetiso.core.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "appointments")
data class AppointmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val clientId: Long,
    val date: String, // Храним в формате "YYYY-MM-DD" для простоты запросов
    val startTimeMinutes: Int, // Время начала записи в минутах от полуночи
    val totalDurationMinutes: Int, // Общая продолжительность всех услуг
    val serviceIds: List<Long>, // Список ID выбранных услуг
)