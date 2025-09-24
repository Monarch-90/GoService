package com.avetiso.core.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "appointments")
data class AppointmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // Храним в формате "YYYY-MM-DD" для простоты запросов
    val startTimeMinutes: Int,
    val totalDurationMinutes: Int, // Общая продолжительность всех услуг

    val clientName: String,
    val clientPhoneNumber: String,
    val clientInstagram: String,
    val discountPercent: Int,
    val status: String,

    val servicesJson: String,
    val note: String = "", // Заметка к записи
)