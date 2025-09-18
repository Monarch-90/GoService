package com.avetiso.feature_schedule.add_appointment.mvi

sealed interface NavigationEvent {
    // Команда для перехода на экран добавления услуги
    data object NavigateToAddService : NavigationEvent

    // Команда для перехода на экран расписания
    data object NavigateToSchedule : NavigationEvent

    // Команда для показа всплывающего сообщения
    data class ShowToast(val message: String) : NavigationEvent
}