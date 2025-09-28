package com.avetiso.core.data

import androidx.room.TypeConverter
import com.avetiso.core.model.ServiceSnapshot
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MyTypeConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromLongList(value: List<Long>?): String? {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toLongList(value: String?): List<Long>? {
        if (value == null) {
            return null
        }
        val listType = object : TypeToken<List<Long>>() {}.type
        return gson.fromJson(value, listType)
    }
}