package com.avetiso.core.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.avetiso.core.data.dao.CategoryDao
import com.avetiso.core.data.dao.ServiceDao
import com.avetiso.core.entity.CategoryEntity
import com.avetiso.core.entity.ServiceEntity

@Database(entities = [ServiceEntity::class, CategoryEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun serviceDao(): ServiceDao
    abstract fun categoryDao(): CategoryDao // <-- ДОБАВИТЬ
}