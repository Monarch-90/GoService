package com.avetiso.navigation

import androidx.navigation.NavController
import com.avetiso.core.entity.ClientEntity

/**
 * Абстракция для навигации к экранам, связанным с клиентами.
 */
interface ClientsNavigator {

    /**
     * Переходит на экран добавления/редактирования клиента.
     * @param navController Текущий NavController, который должен выполнить навигацию.
     * @param clientToEdit Клиент для редактирования или null, если создается новый.
     */
    fun navigateToAddEditClient(navController: NavController, clientToEdit: ClientEntity?)
}