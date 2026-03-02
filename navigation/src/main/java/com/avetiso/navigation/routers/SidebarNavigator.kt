package com.avetiso.navigation.routers

import androidx.navigation.NavController

/**
 * Контракт навигации для модуля Sidebar (бывший Settings).
 * Описывает переходы на экраны, относящиеся к боковому меню и настройкам.
[cite_start]* [cite: 9] - пример аналогичного контракта ClientsNavigator.
 */
interface SidebarNavigator {

    /**
     * Переход на экран настроек.
     * @param navController Контроллер навигации.
     */
    fun navigateToSettings(navController: NavController)

    /**
     * Переход на экран "О приложении".
     * @param navController Контроллер навигации.
     */
    fun navigateToAbout(navController: NavController)
}