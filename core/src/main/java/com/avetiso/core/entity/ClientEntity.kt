package com.avetiso.core.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.avetiso.core.AppConstants
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = AppConstants.Data.TABLE_CLIENTS)
data class ClientEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phoneNumber: String,
    val instagram: String,
    // Пока что реализуем простыми строками. В будущем можно усложнить.
    val countryCode: String = "",
    val socialLink: String = "",
    val source: String = "", // Источник привлечения
    val discount: Int = 0,   // Личная скидка в %
    val note: String = "",    // Примечание
    val customFields: Map<String, String> = emptyMap(), // Кастомное поле
) : Parcelable