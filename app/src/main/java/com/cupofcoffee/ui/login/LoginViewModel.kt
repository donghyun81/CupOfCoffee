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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginViewModel(private val userRepository: UserRepositoryImpl) : ViewModel() {

    fun insertUser(userEntry: UserEntry) {
        viewModelScope.launch(Dispatchers.IO) {
            with(userEntry) {
                userRepository.insert(id, userModel.toUserDTO())
            }
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