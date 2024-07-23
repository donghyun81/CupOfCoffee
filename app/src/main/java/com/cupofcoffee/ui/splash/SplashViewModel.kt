package com.cupofcoffee.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cupofcoffee.CupOfCoffeeApplication
import com.cupofcoffee.data.repository.PreferencesRepositoryImpl

class SplashViewModel(
    preferencesRepositoryImpl: PreferencesRepositoryImpl
) : ViewModel() {

    val isAutoLoginFlow = preferencesRepositoryImpl.isAutoLoginFlow.asLiveData()

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                SplashViewModel(
                    preferencesRepositoryImpl = CupOfCoffeeApplication.preferencesRepositoryImpl
                )
            }
        }
    }
}