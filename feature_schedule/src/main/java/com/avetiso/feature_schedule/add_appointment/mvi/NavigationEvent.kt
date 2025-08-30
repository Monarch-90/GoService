package com.avetiso.feature_schedule.add_appointment.mvi

sealed interface NavigationEvent {
    data object NavigateToAddService : NavigationEvent
    data object NavigateToSchedule : NavigationEvent
}