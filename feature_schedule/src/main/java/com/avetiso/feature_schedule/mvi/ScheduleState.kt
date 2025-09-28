package com.avetiso.feature_schedule.mvi

sealed interface ScheduleState {
    data object Idle : ScheduleState      // Исходное состояние, ничего не происходит
    data object Success : ScheduleState   // Перенос прошел успешно
    data class Error(val message: String) : ScheduleState // Произошла ошибка
}