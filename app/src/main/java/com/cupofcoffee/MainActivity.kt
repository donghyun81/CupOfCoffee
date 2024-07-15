package com.cupofcoffee

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
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
        setNavigationVisibility(navController)
        bottomNavigation.setupWithNavController(navController = navController)
    }

    private fun setNavigationVisibility(navController: NavController) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bnvHome.visibility = when (destination.id) {
                R.id.loginFragment -> View.GONE
                R.id.meetingDetailFragment -> View.GONE
                R.id.settingsFragment -> View.GONE
                R.id.commentEditFragment -> View.GONE
                else -> View.VISIBLE
            }
        }
    }
}