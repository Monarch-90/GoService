package com.avetiso.goservice

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupWithNavController
import androidx.navigation.ui.setupWithNavController
import com.avetiso.goservice.databinding.ActivityMainBinding
import com.avetiso.navigation.DrawerController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), DrawerController {

    private var binding: ActivityMainBinding? = null
    private var navController: NavController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        val activityBinding = ActivityMainBinding.inflate(layoutInflater)
        binding = activityBinding

        setContentView(activityBinding.root)

        ViewCompat.setOnApplyWindowInsetsListener(activityBinding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            activityBinding.navHostFragment.updatePadding(top = systemBars.top)
            activityBinding.bottomNavView.updatePadding(bottom = systemBars.bottom)
            insets
        }

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        if (navController != null) {
            activityBinding.bottomNavView.setupWithNavController(navController!!)
        }

        // ✅ НАСТРОЙКА БОКОВОГО МЕНЮ (РУЧНАЯ ОБРАБОТКА)
        binding?.sideNavView?.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.settingsFragment -> {
                    // 1. Пытаемся найти экшен или назначение
                    try {
                        // Опции для очистки стека, чтобы не плодить фрагменты настроек
                        val navOptions = NavOptions.Builder()
                            .setLaunchSingleTop(true)
                            .build()

                        navController?.navigate(R.id.settingsFragment, null, navOptions)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    // 2. Закрываем шторку
                    binding?.drawerLayout?.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_about -> {
                    // Тут позже сделаем диалог "О приложении"
                    // Пока просто закроем шторку и покажем Тост
                    android.widget.Toast.makeText(this, "О приложении", android.widget.Toast.LENGTH_SHORT).show()
                    binding?.drawerLayout?.closeDrawer(GravityCompat.START)
                    true
                }
                else -> false
            }
        }
    }

    // Этот метод нужен для корректной работы кнопки "назад"
    override fun onSupportNavigateUp(): Boolean {
        return navController?.navigateUp() ?: super.onSupportNavigateUp()
    }

    override fun openSideDrawer() {
        binding?.drawerLayout?.openDrawer(GravityCompat.START)
    }

    override fun closeSideDrawer() {
        binding?.drawerLayout?.closeDrawer(GravityCompat.START)
    }

    override fun onDestroy() {
        binding = null
        super.onDestroy()
    }
}