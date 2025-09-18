package com.avetiso.core.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// Этот класс хранит копию данных услуги на момент создания записи
@Parcelize
data class ServiceSnapshot(
    val id: Long,
    val name: String,
    val categoryName: String,
    val isPriceFrom: Boolean,
    val price: Double,
    val currency: String,
    val durationMinutes: Int,
) : Parcelable