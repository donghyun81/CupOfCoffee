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
import com.cupofcoffee.ui.model.UserEntry
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class UserViewModel(
    private val userRepositoryImpl: UserRepositoryImpl
) : ViewModel() {

    private val _user: MutableLiveData<UserEntry> = MutableLiveData()
    val user: LiveData<UserEntry> = _user

    init {
        viewModelScope.launch {
            initUser()
        }
    }

    private suspend fun initUser() {
        val uid = Firebase.auth.uid!!
        userRepositoryImpl.getUserByIdInFlow(uid).collect { userDTO ->
            _user.value = userDTO?.toUserEntry(uid)
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