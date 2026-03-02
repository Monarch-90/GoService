package com.avetiso.feature_schedule.add_appointment.ui

import android.content.Context
import com.avetiso.core.model.AppointmentStatus
import com.avetiso.feature_schedule.R
import com.avetiso.core.R as CoreR

/**
 * Превращает Enum статус в ID строки (из strings.xml).
 * Обрати внимание: 'else' больше не нужен, код полностью безопасен.
 */
fun AppointmentStatus.toStatusLabelRes(): Int {
    return when (this) {
        AppointmentStatus.ACTIVE -> R.string.status_active
        AppointmentStatus.COMPLETED -> R.string.status_completed
        AppointmentStatus.CANCELLED -> R.string.status_cancelled
        AppointmentStatus.RESCHEDULED -> R.string.status_rescheduled
        AppointmentStatus.NO_SHOW -> R.string.status_no_show
    }
}

/**
 * Превращает Enum статус в ID цвета.
 */
fun AppointmentStatus.toStatusColorRes(): Int {
    return when (this) {
        AppointmentStatus.ACTIVE -> CoreR.color.green
        AppointmentStatus.COMPLETED -> CoreR.color.main_dark
        AppointmentStatus.CANCELLED -> CoreR.color.grey
        AppointmentStatus.RESCHEDULED -> CoreR.color.orange_coral
        AppointmentStatus.NO_SHOW -> CoreR.color.red
    }
}

/**
 * Возвращает массив готовых строк (на русском) для показа в диалоге выбора.
 * Использует встроенный в Kotlin метод .entries для перебора всех вариантов.
 */
fun Context.getStatusLabelsArray(): Array<String> {
    return AppointmentStatus.entries.map { status ->
        this.getString(status.toStatusLabelRes())
    }.toTypedArray()
}

/**
 * По индексу нажатия (в диалоге) возвращает готовый объект Enum.
 */
fun getStatusByIndex(index: Int): AppointmentStatus {
    return AppointmentStatus.entries.getOrElse(index) { AppointmentStatus.ACTIVE }
}