package com.avetiso.goservice.navigation

import androidx.core.net.toUri
import androidx.navigation.NavController
import com.avetiso.core.entity.ClientEntity
import com.avetiso.navigation.ClientsNavigator
import javax.inject.Inject

class ClientsNavigatorImpl @Inject constructor() : ClientsNavigator {

    override fun navigateToAddEditClient(navController: NavController, clientToEdit: ClientEntity?) {
        // Создаем Deep Link URI
        val deepLinkUri = "goservice://clients/add_edit".toUri()

        // Передаем аргументы через NavController, он сам найдет нужный deep link
        // и безопасно прикрепит к нему аргументы.
        navController.navigate(deepLinkUri)

        if (clientToEdit != null) {
            navController.previousBackStackEntry?.savedStateHandle?.set("clientToEdit", clientToEdit)
        }
    }
}