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
}