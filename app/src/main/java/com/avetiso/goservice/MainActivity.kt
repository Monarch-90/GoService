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
import com.avetiso.core.ads.AppOpenAdController
import com.avetiso.goservice.databinding.ActivityMainBinding
import com.avetiso.goservice.mvi.MainAdEffect
import com.avetiso.goservice.mvi.MainAdEffectHandledIntent
import com.avetiso.goservice.mvi.MainAdEffectType
import com.avetiso.goservice.mvi.MainAppLaunchedIntent
import com.avetiso.goservice.mvi.MainIntent
import com.avetiso.goservice.mvi.MainSideEffect
import com.avetiso.goservice.mvi.MainViewModel
import com.avetiso.navigation.controllers.SidebarController
import com.avetiso.navigation.routers.SidebarNavigator
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), SidebarController {

    private val viewModel: MainViewModel by viewModels()
    private var _binding: ActivityMainBinding? = null
    private val binding get() = requireNotNull(_binding) { "Binding is null" }

    @Inject
    lateinit var sidebarNavigator: SidebarNavigator

    @Inject
    lateinit var appOpenAdController: AppOpenAdController

    private val navController: NavController
        get() = binding.navHostFragment.getFragment<NavHostFragment>().navController

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
        observeViewModel()
        notifyAppLaunched(savedInstanceState)
    }

    private fun notifyAppLaunched(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            viewModel.processIntent(MainAppLaunchedIntent)
        }
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
            return@OnNavigationItemSelectedListener false
        }

        binding.sideNavView.setNavigationItemSelectedListener(navItemSelectedListener)
        binding.about.setNavigationItemSelectedListener(navItemSelectedListener)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.state
                        .map { state -> state.pendingAdEffects.firstOrNull() }
                        .distinctUntilChanged()
                        .collect { effect ->
                            effect ?: return@collect
                            handleAdEffect(effect)
                        }
                }

                launch {
                    viewModel.effects.collect { effect ->
                        handleSideEffect(effect)
                    }
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
                    R.id.nav_contact_developers -> {
                        sidebarNavigator.navigateToContactDevelopers(navController)
                        closeSideDrawer()
                    }
                }
            }
        }
    }

    private suspend fun handleAdEffect(effect: MainAdEffect) {
        when (effect.type) {
            MainAdEffectType.LOAD_APP_OPEN -> {
                appOpenAdController.loadAppOpenAd()
            }

            MainAdEffectType.SHOW_APP_OPEN -> {
                showAppOpenAdWhenResumed()
            }
        }

        acknowledgeAdEffect(effect)
    }

    private fun acknowledgeAdEffect(effect: MainAdEffect) {
        viewModel.processIntent(
            MainAdEffectHandledIntent(
                requestId = effect.requestId
            )
        )
    }

    private suspend fun showAppOpenAdWhenResumed() = coroutineScope {
        val showCompletion = CompletableDeferred<Unit>()

        val lifecycleJob = launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                appOpenAdController.showAppOpenAd(this@MainActivity)
                showCompletion.complete(Unit)
            }
        }

        try {
            showCompletion.await()
        } finally {
            lifecycleJob.cancel()
        }
    }

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


