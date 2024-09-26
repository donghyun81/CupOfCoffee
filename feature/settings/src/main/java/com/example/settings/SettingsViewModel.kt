package com.example.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import com.example.common.util.NetworkUtil
import com.example.datastore.PreferencesRepositoryImpl
import com.example.work.DeleteUserWorker
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepositoryImpl,
    private val networkUtil: NetworkUtil
) : ViewModel() {

    private val _uiState: MutableLiveData<SettingsUiState> =
        MutableLiveData(SettingsUiState(isLoading = true))
    val uiState: LiveData<SettingsUiState> get() = _uiState

    init {
        viewModelScope.launch {
            initUiState()
        }
    }

    private suspend fun initUiState() {
        preferencesRepository.isAutoLoginFlow.collect { isAutoLogin ->
            try {
                _uiState.value = SettingsUiState(isAutoLogin = isAutoLogin)
            } catch (e: Exception) {
                _uiState.postValue(SettingsUiState(isError = true))
            }
        }
    }

    fun convertIsAutoLogin() {
        viewModelScope.launch {
            preferencesRepository.toggleAutoLogin()
        }
    }

    fun isNetworkConnected() = networkUtil.isConnected()

    fun getDeleteUserWorker(): OneTimeWorkRequest {
        val inputData = Data.Builder()
            .putString("userId", Firebase.auth.uid!!)
            .build()
        return OneTimeWorkRequestBuilder<DeleteUserWorker>()
            .setInputData(inputData)
            .build()
    }
}