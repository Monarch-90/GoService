package com.avetiso.core.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.avetiso.core.data.dao.AppointmentDao
import com.avetiso.core.data.dao.CategoryDao
import com.avetiso.core.data.dao.ClientDao
import com.avetiso.core.data.dao.ServiceDao
import com.avetiso.core.data.dao.TimeSlotDao
import com.avetiso.core.entity.AppointmentEntity
import com.avetiso.core.entity.CategoryEntity
import androidx.room.TypeConverters
import com.avetiso.core.entity.ClientEntity
import com.avetiso.core.entity.ServiceEntity
import com.avetiso.core.entity.TimeSlotEntity

@Database(
    entities = [ServiceEntity::class, CategoryEntity::class, TimeSlotEntity::class, ClientEntity::class, AppointmentEntity::class],
    version = 1, exportSchema = true
)
@TypeConverters(MyTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun serviceDao(): ServiceDao
    abstract fun categoryDao(): CategoryDao
    abstract fun timeSlotDao(): TimeSlotDao
    abstract fun clientDao(): ClientDao
    abstract fun appointmentDao(): AppointmentDao
}