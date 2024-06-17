package com.cupofcoffee.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cupofcoffee.CupOfCoffeeApplication
import com.cupofcoffee.data.repository.UserRepositoryImpl
import com.cupofcoffee.ui.model.UserEntry
import com.cupofcoffee.ui.model.toUserDTO
import com.cupofcoffee.ui.model.toUserEntity

class LoginViewModel(private val userRepository: UserRepositoryImpl) : ViewModel() {

    suspend fun insertUser(userEntry: UserEntry) {
        with(userEntry) {
            userRepository.insertLocal(userModel.toUserEntity(id))
            userRepository.insertRemote(id, userModel.toUserDTO())
        }
    }

    suspend fun updateUser(userEntry: UserEntry) {
        with(userEntry) {
            userRepository.updateLocal(userModel.toUserEntity(id))
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                LoginViewModel(CupOfCoffeeApplication.userRepository)
            }
        }
    }
}