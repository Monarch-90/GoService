package com.avetiso.core.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "services")
data class ServiceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val categoryName: String,
    val isPriceFrom: Boolean,
    val price: Double,
    val currency: String,
    val durationMinutes: Int,
) : Parcelable