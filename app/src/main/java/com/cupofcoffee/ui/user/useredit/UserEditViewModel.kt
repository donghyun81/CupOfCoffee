package com.cupofcoffee.ui.user.useredit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cupofcoffee.CupOfCoffeeApplication
import com.cupofcoffee.data.DataResult
import com.cupofcoffee.data.local.model.asUserEntry
import com.cupofcoffee.data.repository.UserRepositoryImpl
import com.cupofcoffee.ui.user.UserUiState
import com.cupofcoffee.ui.user.UserViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class UserEditViewModel(
    private val userRepositoryImpl: UserRepositoryImpl
) : ViewModel() {

    private val _uiState: MutableLiveData<DataResult<UserEditUiState>> =
        MutableLiveData(DataResult.loading())
    val uiState: LiveData<DataResult<UserEditUiState>> = _uiState

    init {
        viewModelScope.launch {
            initUser()
        }
    }

    private suspend fun initUser() {
        val uid = Firebase.auth.uid!!
        try {
            val user = userRepositoryImpl.getLocalUserById(uid)
            _uiState.value = DataResult.success(UserEditUiState(userEntry = user))
        } catch (e: Exception) {
            DataResult.error(e)
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