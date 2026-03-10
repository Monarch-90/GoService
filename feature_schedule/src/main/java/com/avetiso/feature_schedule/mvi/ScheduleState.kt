package com.avetiso.feature_schedule.mvi

import androidx.annotation.StringRes

sealed interface ScheduleState {
    data object Idle : ScheduleState      // Исходное состояние, ничего не происходит
    data object Success : ScheduleState   // Перенос прошел успешно
    data class Error(@StringRes val messageResId: Int) : ScheduleState // Произошла ошибка
}