package com.avetiso.goservice.navigation

import androidx.core.net.toUri
import androidx.navigation.NavController
import androidx.navigation.NavDeepLinkRequest
import com.avetiso.core.AppConstants
import com.avetiso.navigation.routers.ScheduleNavigator
import javax.inject.Inject

class ScheduleNavigatorImpl @Inject constructor() : ScheduleNavigator {

    override fun navigateToSchedule(navController: NavController) {
        val request = NavDeepLinkRequest.Builder
            .fromUri(AppConstants.DeepLinks.SCHEDULE_MAIN.toUri())
            .build()
        navController.navigate(request)
    }

    override fun navigateToAddEditAppointment(navController: NavController, appointmentId: Long, selectedDate: String?) {
        val baseUri = AppConstants.DeepLinks.SCHEDULE_ADD_EDIT
        // Если дата null, передаем пустую строку, чтобы не ломать required аргумент в графе
        val safeDate = selectedDate ?: ""

        val uriBuilder = baseUri.toUri().buildUpon()
            .appendQueryParameter("appointmentId", appointmentId.toString())
            .appendQueryParameter("selectedDate", safeDate)

        val request = NavDeepLinkRequest.Builder
            .fromUri(uriBuilder.build())
            .build()

        navController.navigate(request)
    }
}