package com.cupofcoffee

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.cupofcoffee.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setNavigation()
    }

    private fun setNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fv_main) as NavHostFragment
        val bottomNavigation = binding.bnvHome
        val navController = navHostFragment.navController
        bottomNavigation.setupWithNavController(navController = navController)
    }
}