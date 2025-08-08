package com.avetiso.core.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.avetiso.core.entity.ServiceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceDao {
    @Insert
    suspend fun insertService(service: ServiceEntity)

    @Query("SELECT * FROM services")
    fun getAllServices(): Flow<List<ServiceEntity>>

    // Поиск услуги
    @Query("SELECT * FROM services WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchServices(query: String): Flow<List<ServiceEntity>>

    @Update
    suspend fun updateService(service: ServiceEntity)

    @Delete
    suspend fun deleteService(service: ServiceEntity)
}