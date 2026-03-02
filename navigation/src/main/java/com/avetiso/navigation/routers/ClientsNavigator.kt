package com.avetiso.navigation.routers

import androidx.navigation.NavController

/**
 * Абстракция для навигации к экранам, связанным с клиентами.
 */
interface ClientsNavigator {

    /**
     * Переходит на экран добавления/редактирования клиента.
     * @param navController Текущий NavController, который должен выполнить навигацию.
     * @param clientToEdit Клиент для редактирования или null, если создается новый.
     */
    fun navigateToAddEditClient(navController: NavController, clientId: Long?)
}