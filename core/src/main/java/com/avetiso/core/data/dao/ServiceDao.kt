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

    @Query("SELECT * FROM services WHERE id IN (:ids)")
    suspend fun getServicesByIds(ids: List<Long>): List<ServiceEntity>

    @Update
    suspend fun updateService(service: ServiceEntity)

    @Delete
    suspend fun deleteService(service: ServiceEntity)

    /**
     * Ищет услугу с точным совпадением всех полей.
     * @param idToExclude ID услуги, которую нужно исключить из поиска (важно при редактировании).
     * @return ServiceEntity, если найдена, иначе null.
     */
    @Query("""
        SELECT * FROM services 
        WHERE name = :name 
        AND categoryName = :categoryName 
        AND isPriceFrom = :isPriceFrom 
        AND price = :price 
        AND currency = :currency 
        AND durationMinutes = :durationMinutes
        AND id != :idToExclude
        LIMIT 1
    """)
    suspend fun findServiceByDetails(
        name: String,
        categoryName: String,
        isPriceFrom: Boolean,
        price: Double,
        currency: String,
        durationMinutes: Int,
        idToExclude: Long
    ): ServiceEntity?
}