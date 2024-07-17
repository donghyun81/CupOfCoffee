package com.cupofcoffee.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cupofcoffee.CupOfCoffeeApplication
import com.cupofcoffee.data.repository.PreferencesRepositoryImpl
import com.cupofcoffee.data.repository.UserRepositoryImpl
import com.cupofcoffee.ui.model.UserEntry
import com.cupofcoffee.ui.model.asUserDTO
import com.cupofcoffee.ui.model.asUserEntity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginViewModel(
    private val userRepository: UserRepositoryImpl,
    private val preferencesRepositoryImpl: PreferencesRepositoryImpl
) : ViewModel() {

    val isAutoLoginFlow = preferencesRepositoryImpl.isAutoLoginFlow.asLiveData()

    suspend fun insertUser(userEntry: UserEntry) {
        with(userEntry) {
            userRepository.insertLocal(userModel.asUserEntity(id))
            userRepository.insertRemote(id, userModel.asUserDTO())
        }
    }

    suspend fun loginUser(id: String) {
        val userEntry = userRepository.getRemoteUserById(id)!!
        userRepository.insertLocal(userEntry.asUserEntity())
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                LoginViewModel(
                    userRepository = CupOfCoffeeApplication.userRepository,
                    preferencesRepositoryImpl = CupOfCoffeeApplication.preferencesRepositoryImpl
                )
            }
        }
    }
}