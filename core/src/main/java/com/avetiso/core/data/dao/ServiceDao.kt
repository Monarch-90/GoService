package com.avetiso.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.avetiso.core.entity.ServiceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceDao {
    @Insert
    suspend fun insertService(service: ServiceEntity)

    @Query("SELECT * FROM services")
    fun getAllServices(): Flow<List<ServiceEntity>>
}