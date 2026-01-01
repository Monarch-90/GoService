package com.avetiso.goservice.navigation

import androidx.core.net.toUri
import androidx.navigation.NavController
import com.avetiso.core.AppConstants
import com.avetiso.navigation.ClientsNavigator
import javax.inject.Inject

class ClientsNavigatorImpl @Inject constructor() : ClientsNavigator {

    override fun navigateToAddEditClient(navController: NavController, clientId: Long?) {
        val deepLinkUri = if (clientId != null) {
            // Если есть ID, строим URI для редактирования
            "${AppConstants.DeepLinks.CLIENTS_EDIT}$clientId".toUri()
        } else {
            // Если ID нет, используем URI для создания
            AppConstants.DeepLinks.CLIENTS_ADD.toUri()
        }
        navController.navigate(deepLinkUri)
    }
}