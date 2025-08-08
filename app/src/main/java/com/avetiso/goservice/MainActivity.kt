package com.avetiso.goservice

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.avetiso.goservice.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null

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


        // Находим NavController
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Подключаем BottomNavigationView к NavController
        activityBinding.bottomNavView.setupWithNavController(navController)
    }

    override fun onDestroy() {
        binding = null
        super.onDestroy()
    }
}