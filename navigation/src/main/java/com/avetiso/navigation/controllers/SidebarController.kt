package com.avetiso.navigation.controllers

/**
 * Интерфейс для управления боковой шторкой (Drawer).
 * Любая Activity, которая хочет поддерживать открытие меню из фрагментов,
 * должна реализовать этот интерфейс.
 */
interface SidebarController {
    fun openSideDrawer()
    fun closeSideDrawer()
}