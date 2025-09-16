package com.avetiso.core.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "appointments",
    foreignKeys = [
        ForeignKey(
            entity = ClientEntity::class,
            parentColumns = ["id"],
            childColumns = ["clientId"],
            onDelete = ForeignKey.CASCADE
        ),
        // ДОБАВЛЯЕМ ВНЕШНИЙ КЛЮЧ К ТАБЛИЦЕ time_slots
        ForeignKey(
            entity = TimeSlotEntity::class,
            parentColumns = ["id"],
            childColumns = ["timeSlotId"],
            onDelete = ForeignKey.CASCADE // Если слот удалится, запись тоже удалится
        )
    ]
)
data class AppointmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val clientId: Long,
    val date: String, // Храним в формате "YYYY-MM-DD" для простоты запросов
    val timeSlotId: Long,
    val totalDurationMinutes: Int, // Общая продолжительность всех услуг
    val serviceIds: List<Long>, // Список ID выбранных услуг
    val note: String = "", // Заметка к записи
)