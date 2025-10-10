package com.avetiso.goservice.navigation

import androidx.core.net.toUri
import androidx.navigation.NavController
import com.avetiso.navigation.ClientsNavigator
import javax.inject.Inject

class ClientsNavigatorImpl @Inject constructor() : ClientsNavigator {

    override fun navigateToAddEditClient(navController: NavController, clientId: Long?) {
        val deepLinkUri = if (clientId != null) {
            // Если есть ID, строим URI для редактирования
            "goservice://clients/edit/$clientId".toUri()
        } else {
            // Если ID нет, используем URI для создания
            "goservice://clients/add".toUri()
        }
        navController.navigate(deepLinkUri)
    }
}