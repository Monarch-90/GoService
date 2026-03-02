package com.avetiso.goservice.navigation

import androidx.core.net.toUri
import androidx.navigation.NavController
import androidx.navigation.NavDeepLinkRequest
import com.avetiso.core.AppConstants
import com.avetiso.navigation.routers.SidebarNavigator
import javax.inject.Inject

/**
 * Реализация навигации для модуля Sidebar.
 * Использует DeepLinks для перехода, обеспечивая слабую связность (Decoupling).
 */
class SidebarNavigatorImpl @Inject constructor() : SidebarNavigator {

    override fun navigateToSettings(navController: NavController) {
        val request = NavDeepLinkRequest.Builder
            .fromUri(AppConstants.DeepLinks.SIDEBAR_SETTINGS.toUri())
            .build()

        // Используем navigateSafe (если есть extension) или стандартный navigate
        navController.navigate(request)
    }

    override fun navigateToAbout(navController: NavController) {
        val request = NavDeepLinkRequest.Builder
            .fromUri(AppConstants.DeepLinks.SIDEBAR_ABOUT.toUri())
            .build()

        navController.navigate(request)
    }
}