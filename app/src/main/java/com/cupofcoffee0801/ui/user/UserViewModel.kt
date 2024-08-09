package com.cupofcoffee0801.ui.user

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cupofcoffee0801.CupOfCoffeeApplication
import com.cupofcoffee0801.data.DataResult
import com.cupofcoffee0801.data.DataResult.Companion.error
import com.cupofcoffee0801.data.DataResult.Companion.loading
import com.cupofcoffee0801.data.DataResult.Companion.success
import com.cupofcoffee0801.data.repository.UserRepositoryImpl
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

class UserViewModel @Inject constructor(
    private val userRepositoryImpl: UserRepositoryImpl
) : ViewModel() {

    private val _uiState: MutableLiveData<DataResult<UserUiState>> = MutableLiveData(loading())
    val uiState: LiveData<DataResult<UserUiState>> get() = _uiState

    var currentJob: Job? = null

    private val _isButtonClicked: MutableLiveData<Boolean> = MutableLiveData(false)
    val isButtonClicked: LiveData<Boolean> get() = _isButtonClicked

    init {
        initUser()
    }

    fun onButtonClicked() {
        _isButtonClicked.value = true
    }

    override fun onCleared() {
        super.onCleared()
        currentJob?.cancel()
    }

    fun initUser() {
        val uid = Firebase.auth.uid!!
        val user = userRepositoryImpl.getLocalUserByIdInFlow(uid)
        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            user.collect { userEntry ->
                if (Firebase.auth.uid == null) return@collect
                try {
                    userEntry ?: return@collect
                    _uiState.value = success(
                        UserUiState(
                            user = userEntry,
                            attendedMeetingsCount = userEntry.userModel.attendedMeetingIds.count(),
                            madeMeetingsCount = userEntry.userModel.madeMeetingIds.count()
                        )
                    )
                } catch (e: Exception) {
                    error(e)
                }
            }
        }
    }
}