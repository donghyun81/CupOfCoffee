package com.cupofcoffee0801.ui.user

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cupofcoffee0801.data.DataResult
import com.cupofcoffee0801.data.DataResult.Companion.error
import com.cupofcoffee0801.data.DataResult.Companion.loading
import com.cupofcoffee0801.data.DataResult.Companion.success
import com.cupofcoffee0801.data.repository.UserRepositoryImpl
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepositoryImpl: UserRepositoryImpl
) : ViewModel() {

    private val _dataResult: MutableLiveData<DataResult<UserUiState>> = MutableLiveData(loading())
    val dataResult: LiveData<DataResult<UserUiState>> get() = _dataResult

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
        val userInFlow = userRepositoryImpl.getLocalUserByIdInFlow(uid)
        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            userInFlow.collect { user ->
                if (Firebase.auth.uid == null) return@collect
                try {
                    user ?: return@collect
                    _dataResult.value = success(
                        UserUiState(
                            user = user,
                            attendedMeetingsCount = user.attendedMeetingIds.count(),
                            madeMeetingsCount = user.madeMeetingIds.count()
                        )
                    )
                } catch (e: Exception) {
                    error(e)
                }
            }
        }
    }
}