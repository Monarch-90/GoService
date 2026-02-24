package com.avetiso.navigation.routers

import androidx.navigation.NavController

/**
 * Контракт навигации для модуля Schedule.
 * Позволяет переходить к расписанию из любой части приложения, не зная о реализации фрагмента.
 */
interface ScheduleNavigator {
    fun navigateToSchedule(navController: NavController)
}