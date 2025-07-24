package com.avetiso.goservice

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.avetiso.goservice.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activityBinding = ActivityMainBinding.inflate(layoutInflater)
        binding = activityBinding
        setContentView(activityBinding.root)

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