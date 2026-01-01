package com.avetiso.core.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.avetiso.core.AppConstants
import com.avetiso.core.entity.ClientEntity
import com.avetiso.core.entity.TimeSlotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TimeSlotDao {

    @Query("SELECT * FROM " + AppConstants.Data.TABLE_TIMESLOT + " ORDER BY startTimeMinutes ASC")
    fun getAllTimeSlots(): Flow<List<TimeSlotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimeSlot(timeSlot: TimeSlotEntity)

    @Delete
    suspend fun deleteTimeSlot(timeSlot: TimeSlotEntity)

    @Query("SELECT * FROM " + AppConstants.Data.TABLE_TIMESLOT + " WHERE id IN (:ids)")
    suspend fun getTimeSlotsByIds(ids: List<Long>): List<TimeSlotEntity>
}