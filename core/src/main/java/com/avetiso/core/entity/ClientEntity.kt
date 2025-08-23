package com.avetiso.core.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "clients")
data class ClientEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phoneNumber: String,
    val instagram: String,
    // Пока что реализуем простыми строками. В будущем можно усложнить.
    val countryCode: String = "+7",
    val socialLink: String = "",
    val source: String = "", // Источник привлечения
    val discount: Int = 0,   // Личная скидка в %
    val note: String = "",    // Примечание
) : Parcelable