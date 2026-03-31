package com.avetiso.core.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.avetiso.core.AppConstants
import com.avetiso.core.entity.AppointmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppointmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: AppointmentEntity)

    @Query("SELECT * FROM " + AppConstants.Data.TABLE_APPOINTMENTS + " ORDER BY date ASC, startTimeMinutes ASC")
    fun getAllAppointments(): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM " + AppConstants.Data.TABLE_APPOINTMENTS + " WHERE date = :date ORDER BY startTimeMinutes ASC")
    fun getAppointmentsForDate(date: String): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM " + AppConstants.Data.TABLE_APPOINTMENTS + " WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC, startTimeMinutes ASC")
    fun getAppointmentsBetweenDatesFlow(startDate: String, endDate: String): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM " + AppConstants.Data.TABLE_APPOINTMENTS + " WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getAppointmentsBetweenDatesSync(startDate: String, endDate: String): List<AppointmentEntity>

    @Query("SELECT * FROM " + AppConstants.Data.TABLE_APPOINTMENTS + " WHERE id = :id")
    suspend fun getAppointmentById(id: Long): AppointmentEntity?

    @Query("SELECT * FROM " + AppConstants.Data.TABLE_APPOINTMENTS + " WHERE date = :date")
    suspend fun getAppointmentsForDateSync(date: String): List<AppointmentEntity>

    @Query("SELECT DISTINCT date FROM " + AppConstants.Data.TABLE_APPOINTMENTS + " WHERE date LIKE :yearMonth || '%'")
    fun getEventDatesForMonth(yearMonth: String): Flow<List<String>>

    @Update
    suspend fun updateAppointment(appointment: AppointmentEntity)

    @Delete
    suspend fun deleteAppointment(appointment: AppointmentEntity)
}