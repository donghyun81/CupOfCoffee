package com.cupofcoffee0801

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.cupofcoffee0801.DestinationLabels.COMMENT_EDIT_FRAGMENT
import com.cupofcoffee0801.DestinationLabels.LOGIN_FRAGMENT
import com.cupofcoffee0801.DestinationLabels.MEETING_DETAIL_FRAGMENT
import com.cupofcoffee0801.DestinationLabels.SETTINGS_FRAGMENT
import com.cupofcoffee0801.DestinationLabels.SPLASH_FRAGMENT
import com.cupofcoffee0801.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var onBackPressedCallback: OnBackPressedCallback
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setNavigation()
        handleDeepLink(intent)
    }

    private fun setNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fv_main) as NavHostFragment
        navController = navHostFragment.navController
        val bottomNavigation = binding.bnvHome
        setNavigationVisibility(navController)
        bottomNavigation.setupWithNavController(navController)
    }

    private fun handleDeepLink(intent: Intent?) {
        intent?.data?.let {
            try {
                navController.handleDeepLink(intent)
            } catch (e: Exception) {
                navController.navigate(R.id.splashFragment)
            }
        }
    }

    private fun setNavigationVisibility(navController: NavController) {
        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bnvHome.visibility = when (destination.label) {
                SPLASH_FRAGMENT,
                LOGIN_FRAGMENT,
                MEETING_DETAIL_FRAGMENT,
                SETTINGS_FRAGMENT,
                COMMENT_EDIT_FRAGMENT -> View.GONE

                else -> View.VISIBLE
            }
            if (destination.id == R.id.homeFragment || destination.id == R.id.userFragment)
                onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
            else onBackPressedCallback.remove()
        }
    }
}