package com.avetiso.navigation.routers

import androidx.navigation.NavController

/**
 * Контракт навигации для модуля Schedule.
 * Позволяет переходить к расписанию из любой части приложения, не зная о реализации фрагмента.
 */
interface ScheduleNavigator {
    fun navigateToSchedule(navController: NavController)

    // Единый контракт для перехода на создание/редактирование записи из любого модуля
    fun navigateToAddEditAppointment(navController: NavController, appointmentId: Long, selectedDate: String?)
}