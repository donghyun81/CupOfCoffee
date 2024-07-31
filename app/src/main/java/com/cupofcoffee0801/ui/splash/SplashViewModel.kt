package com.cupofcoffee0801.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cupofcoffee0801.CupOfCoffeeApplication
import com.cupofcoffee0801.data.repository.PreferencesRepositoryImpl
import com.cupofcoffee0801.data.repository.UserRepositoryImpl
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SplashViewModel(
    preferencesRepositoryImpl: PreferencesRepositoryImpl,
    private val userRepositoryImpl: UserRepositoryImpl,
) : ViewModel() {

    val isAutoLoginFlow = preferencesRepositoryImpl.isAutoLoginFlow.asLiveData()

    suspend fun isUserDeleted(): Boolean {
        val uid = Firebase.auth.uid ?: return true
        val isUserDeleted = userRepositoryImpl.getRemoteUserById(uid) == null
        if (isUserDeleted) Firebase.auth.signOut()
        return isUserDeleted
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                SplashViewModel(
                    preferencesRepositoryImpl = CupOfCoffeeApplication.preferencesRepositoryImpl,
                    userRepositoryImpl = CupOfCoffeeApplication.userRepository
                )
            }
        }
    }
}