package com.avetiso.core.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.avetiso.core.AppConstants
import com.avetiso.core.entity.ClientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClient(client: ClientEntity)

    @Update
    suspend fun updateClient(client: ClientEntity)

    @Delete
    suspend fun deleteClient(client: ClientEntity)

    @Query("SELECT * FROM " + AppConstants.Data.TABLE_CLIENTS + " ORDER BY name ASC")
    fun getAllClients(): Flow<List<ClientEntity>>

    @Query("SELECT * FROM " + AppConstants.Data.TABLE_CLIENTS + " WHERE name LIKE '%' || :query || '%' OR phoneNumber LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchClients(query: String): Flow<List<ClientEntity>>

    @Query("SELECT * FROM " + AppConstants.Data.TABLE_CLIENTS + " WHERE name = :name AND phoneNumber = :phoneNumber AND id != :idToExclude LIMIT 1")
    suspend fun findByNameAndPhone(name: String, phoneNumber: String, idToExclude: Long): ClientEntity?

    @Query("SELECT * FROM " + AppConstants.Data.TABLE_CLIENTS + " WHERE name = :name AND instagram = :instagram AND id != :idToExclude LIMIT 1")
    suspend fun findByNameAndInstagram(name: String, instagram: String, idToExclude: Long): ClientEntity?

    @Query("SELECT * FROM " + AppConstants.Data.TABLE_CLIENTS + " WHERE phoneNumber = :phoneNumber AND instagram = :instagram AND id != :idToExclude LIMIT 1")
    suspend fun findByPhoneAndInstagram(phoneNumber: String, instagram: String, idToExclude: Long): ClientEntity?

    @Query("SELECT * FROM " + AppConstants.Data.TABLE_CLIENTS + " WHERE id = :id")
    suspend fun getClientById(id: Long): ClientEntity?

    @Query("SELECT * FROM " + AppConstants.Data.TABLE_CLIENTS + " WHERE id = :id")
    fun getClientFlowById(id: Long): Flow<ClientEntity?>
}