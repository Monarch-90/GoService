package com.avetiso.feature_schedule.mvi

sealed interface ScheduleEvent {
    data class ShowToast(val message: String) : ScheduleEvent
}