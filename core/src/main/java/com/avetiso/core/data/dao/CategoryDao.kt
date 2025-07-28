package com.avetiso.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.avetiso.core.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Insert
    suspend fun insertCategory(category: CategoryEntity)

    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>
}