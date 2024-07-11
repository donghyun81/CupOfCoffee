package com.cupofcoffee.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cupofcoffee.CupOfCoffeeApplication
import com.cupofcoffee.data.repository.UserRepositoryImpl
import com.cupofcoffee.ui.model.UserEntry
import com.cupofcoffee.ui.model.asUserDTO
import com.cupofcoffee.ui.model.asUserEntity

class LoginViewModel(private val userRepository: UserRepositoryImpl) : ViewModel() {

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
                LoginViewModel(CupOfCoffeeApplication.userRepository)
            }
        }
    }
}