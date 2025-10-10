package com.avetiso.goservice

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.avetiso.goservice.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

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

        binding?.bottomNavView?.let { bottomNavView ->
            // Стандартное подключение
            setupWithNavController(bottomNavView, navHostFragment.navController)

            // ПРАВИЛЬНЫЙ ОБРАБОТЧИК ДЛЯ СОХРАНЕНИЯ СОСТОЯНИЯ
            bottomNavView.setOnItemSelectedListener { item ->
                val builder = NavOptions.Builder()
                    .setLaunchSingleTop(true) // Не пересоздавать вкладку, если она уже на вершине стека
                    .setRestoreState(true) // ВОССТАНАВЛИВАТЬ СОСТОЯНИЕ при возвращении

                builder.setPopUpTo(
                    destinationId = navHostFragment.navController.graph.findStartDestination().id,
                    inclusive = false,
                    saveState = true
                )

                val options = builder.build()

                try {
                    // Выполняем навигацию с нашими опциями
                    navHostFragment.navController.navigate(item.itemId, null, options)
                    true
                } catch (e: IllegalArgumentException) {
                    // Иногда может быть ошибка, если кликнуть очень быстро
                    // при пересоздании. Просто игнорируем.
                    true
                }
            }
        }
    }

    // Этот метод нужен для корректной работы кнопки "назад"
    override fun onSupportNavigateUp(): Boolean {
        return navController?.navigateUp() ?: super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        binding = null
        super.onDestroy()
    }
}