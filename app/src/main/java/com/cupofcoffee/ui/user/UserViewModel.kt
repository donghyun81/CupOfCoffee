package com.cupofcoffee.ui.user

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cupofcoffee.CupOfCoffeeApplication
import com.cupofcoffee.data.remote.toUserEntry
import com.cupofcoffee.data.repository.UserRepositoryImpl
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserViewModel(
    private val userRepositoryImpl: UserRepositoryImpl
) : ViewModel() {

    private val _uiState: MutableLiveData<UserUiState> = MutableLiveData(UserUiState())
    val uiState: LiveData<UserUiState> = _uiState

    init {
        initUser()
    }

    private fun initUser() {
        val uid = Firebase.auth.uid!!
        userRepositoryImpl.getRemoteUserByIdInFlow(uid).collect { userDTO ->
            if (userDTO == null) return@collect
            _user.value = userDTO.toUserEntry(uid)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                UserViewModel(
                    userRepositoryImpl = CupOfCoffeeApplication.userRepository
                )
            }
        }
    }
}