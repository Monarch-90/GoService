package com.avetiso.goservice

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.avetiso.goservice.databinding.ActivityMainBinding
import com.avetiso.navigation.controllers.SidebarController
import com.avetiso.navigation.routers.SidebarNavigator
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), SidebarController {
    private var binding: ActivityMainBinding? = null
    private var navController: NavController? = null
    @Inject
    lateinit var sidebarNavigator: SidebarNavigator

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        val activityBinding = ActivityMainBinding.inflate(layoutInflater)
        binding = activityBinding

        setContentView(activityBinding.root)

        setupWindowInsets(activityBinding)
        setupNavigation(activityBinding)
        setupSideMenu(activityBinding)
    }

    private fun setupWindowInsets(binding: ActivityMainBinding) {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.navHostFragment.updatePadding(top = systemBars.top)
            binding.bottomNavView.updatePadding(bottom = systemBars.bottom)
            insets
        }
    }

    private fun setupNavigation(binding: ActivityMainBinding) {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        navController?.let {
            binding.bottomNavView.setupWithNavController(it)
        }
    }

    private fun setupSideMenu(binding: ActivityMainBinding) {
        binding.sideNavView.setNavigationItemSelectedListener { menuItem ->
            val controller = navController ?: return@setNavigationItemSelectedListener false

            when (menuItem.itemId) {
                // Используем ID из menu_sidebar (проверь, что в xml меню id называются именно так)
                R.id.nav_settings -> {
                    sidebarNavigator.navigateToSettings(controller)
                    closeSideDrawer()
                    true
                }
                R.id.nav_about -> {
                    sidebarNavigator.navigateToAbout(controller)
                    closeSideDrawer()
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