package com.avetiso.core.models

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

// Этот класс хранит копию данных услуги на момент создания записи
@Keep
@Parcelize
data class ServiceSnapshot(
    val id: Long,
    val name: String,
    val categoryName: String,
    val isPriceFrom: Boolean,
    val price: Double,
    val currency: String?,
    val durationMinutes: Int,
) : Parcelable