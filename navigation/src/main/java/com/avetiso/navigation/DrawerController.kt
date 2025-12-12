package com.avetiso.navigation

/**
 * Интерфейс для управления боковой шторкой (Drawer).
 * Любая Activity, которая хочет поддерживать открытие меню из фрагментов,
 * должна реализовать этот интерфейс.
 */
interface DrawerController {
    fun openSideDrawer()
    fun closeSideDrawer()
}