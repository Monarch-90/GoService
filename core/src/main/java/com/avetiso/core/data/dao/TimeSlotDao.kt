package com.avetiso.core.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.avetiso.core.entity.TimeSlotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TimeSlotDao {

    @Query("SELECT * FROM time_slots ORDER BY startTimeMinutes ASC")
    fun getAllTimeSlots(): Flow<List<TimeSlotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimeSlot(timeSlot: TimeSlotEntity)

    @Delete
    suspend fun deleteTimeSlot(timeSlot: TimeSlotEntity)
}