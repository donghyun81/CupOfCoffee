package com.cupofcoffee0801.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import com.cupofcoffee0801.data.DataResult
import com.cupofcoffee0801.data.DataResult.Companion.error
import com.cupofcoffee0801.data.DataResult.Companion.loading
import com.cupofcoffee0801.data.DataResult.Companion.success
import com.cupofcoffee0801.data.repository.PreferencesRepositoryImpl
import com.cupofcoffee0801.data.worker.DeleteUserWorker
import com.cupofcoffee0801.util.NetworkUtil
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

    private val _dataResult: MutableLiveData<DataResult<SettingsUiState>> = MutableLiveData(loading())
    val dataResult: LiveData<DataResult<SettingsUiState>> get() = _dataResult

    private val _isButtonClicked: MutableLiveData<Boolean> = MutableLiveData(false)
    val isButtonClicked: LiveData<Boolean> get() = _isButtonClicked


    init {
        viewModelScope.launch {
            initUiState()
        }
    }

    fun onButtonClicked() {
        _isButtonClicked.value = true
    }

    private suspend fun initUiState() {
        preferencesRepository.isAutoLoginFlow.collect { isAutoLogin ->
            try {
                _dataResult.postValue(success(SettingsUiState(isAutoLogin)))
            } catch (e: Exception) {
                _dataResult.postValue(error(e))
            }
        }
    }

    fun convertIsAutoLogin() {
        viewModelScope.launch {
            preferencesRepository.toggleAutoLogin()
        }
    }

    fun isConnected() = networkUtil.isConnected()

    fun getDeleteUserWorker(): OneTimeWorkRequest {
        val inputData = Data.Builder()
            .putString("userId", Firebase.auth.uid!!)
            .build()
        return OneTimeWorkRequestBuilder<DeleteUserWorker>()
            .setInputData(inputData)
            .build()
    }
}