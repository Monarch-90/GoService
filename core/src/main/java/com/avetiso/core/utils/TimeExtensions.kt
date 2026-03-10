package com.avetiso.core.utils

import com.avetiso.core.AppConstants
import com.avetiso.core.entity.TimeSlotEntity
import java.util.Locale

/**
 * Расширение для TimeSlotEntity.
 * Преобразует минуты (Int) в строковый формат времени "ЧЧ:ММ".
 * * Пример:
 * 630 -> "10:30"
 * 60  -> "01:00"
 */
val TimeSlotEntity.formattedTime: String
    get() {
        val hours = this.startTimeMinutes / 60
        val minutes = this.startTimeMinutes % 60
        // %02d добавляет ноль в начале, если цифра одна (9 -> 09)
        return String.format(Locale.getDefault(), AppConstants.Format.TIME_HH_MM, hours, minutes)
    }