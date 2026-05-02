package com.avetiso.goservice

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.avetiso.goservice.databinding.ActivityMainBinding
import com.avetiso.goservice.mvi.MainIntent
import com.avetiso.goservice.mvi.MainSideEffect
import com.avetiso.goservice.mvi.MainViewModel
import com.avetiso.navigation.controllers.SidebarController
import com.avetiso.navigation.routers.SidebarNavigator
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), SidebarController {

    private val viewModel: MainViewModel by viewModels()
    private var _binding: ActivityMainBinding? = null
    private val binding get() = requireNotNull(_binding) { "Binding is null" }
    @Inject
    lateinit var sidebarNavigator: SidebarNavigator

    private val navController: NavController
        get() = binding.navHostFragment.getFragment<NavHostFragment>().navController

    // Сохраняем ссылку на listener, чтобы безопасно отписать его в onDestroy
    private val drawerListener = object : DrawerLayout.SimpleDrawerListener() {
        override fun onDrawerClosed(drawerView: View) {
            super.onDrawerClosed(drawerView)
            viewModel.processIntent(MainIntent.OnDrawerClosed)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        val activityBinding = ActivityMainBinding.inflate(layoutInflater)
        _binding = activityBinding

        setContentView(activityBinding.root)

        setupWindowInsets()
        setupNavigation()
        setupSideMenu()
        observeEffects()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.navHostFragment.updatePadding(top = systemBars.top)
            binding.bottomNavView.updatePadding(bottom = systemBars.bottom)
            insets
        }
    }

    private fun setupNavigation() {
        binding.bottomNavView.setupWithNavController(navController)
    }

    private fun setupSideMenu() {
        binding.drawerLayout.addDrawerListener(drawerListener)

        val navItemSelectedListener = NavigationView.OnNavigationItemSelectedListener { menuItem ->
            viewModel.processIntent(MainIntent.OnSidebarItemClicked(menuItem.itemId))
            // Возвращаем false: глушим стандартное "залипание" (checked state) элемента меню
            return@OnNavigationItemSelectedListener false
        }

        binding.sideNavView.setNavigationItemSelectedListener(navItemSelectedListener)
        binding.about.setNavigationItemSelectedListener(navItemSelectedListener)
    }

    private fun observeEffects() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effects.collect { effect ->
                    handleSideEffect(effect)
                }
            }
        }
    }

    private fun handleSideEffect(effect: MainSideEffect) {
        when (effect) {
            is MainSideEffect.CloseSidebar -> {
                binding.drawerLayout.closeDrawers()
            }
            is MainSideEffect.Navigate -> {
                when (effect.destinationId) {
                    R.id.nav_settings -> sidebarNavigator.navigateToSettings(navController)
                    R.id.nav_about -> sidebarNavigator.navigateToAbout(navController)
                    // Добавляем обработку нашего нового пункта меню
                    R.id.nav_contact_developers -> {
                        sidebarNavigator.navigateToContactDevelopers(navController)
                        // Не забудь закрыть шторку после клика, как ты это делаешь для остальных пунктов
                        closeSideDrawer()
                        true
                    }
                }
            }
        }
    }

    // Этот метод нужен для корректной работы кнопки "назад"
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun openSideDrawer() {
        binding.drawerLayout.openDrawer(GravityCompat.START)
    }

    override fun closeSideDrawer() {
        binding.drawerLayout.closeDrawer(GravityCompat.START)
    }

    override fun onDestroy() {
        _binding?.drawerLayout?.removeDrawerListener(drawerListener)
        _binding = null
        super.onDestroy()
    }
}