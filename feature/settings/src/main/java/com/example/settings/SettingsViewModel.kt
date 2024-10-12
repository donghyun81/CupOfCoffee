package com.example.settings

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.common.util.NetworkUtil
import com.example.datastore.PreferencesRepositoryImpl
import com.example.work.DeleteUserWorker
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.navercorp.nid.NaverIdLoginSDK
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val CANCEL_MEMBERSHIP_NETWORK_MESSAGE = "회원탈퇴를 위해서 인터넷 연결이 필요합니다!"

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepositoryImpl,
    private val networkUtil: NetworkUtil,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState: MutableLiveData<SettingsUiState> =
        MutableLiveData(SettingsUiState(isLoading = true))
    val uiState: LiveData<SettingsUiState> get() = _uiState

    private val _sideEffect = Channel<SettingsSideEffect>()
    val sideEffect = _sideEffect.receiveAsFlow()

    fun handleIntent(intent: SettingsIntent) {
        when (intent) {
            SettingsIntent.CancelMembership -> {
                viewModelScope.launch {
                    if (networkUtil.isConnected())
                        cancelMembership()
                    else _sideEffect.send(
                        SettingsSideEffect.ShowSnackBar(
                            CANCEL_MEMBERSHIP_NETWORK_MESSAGE
                        )
                    )
                }

            }

            SettingsIntent.InitData -> {
                viewModelScope.launch {
                    initUiState()
                }
            }

            SettingsIntent.Logout -> {
                viewModelScope.launch {
                    logout()
                    _sideEffect.send(SettingsSideEffect.NavigateLogin)
                }
            }

            SettingsIntent.SwitchAutoLogin -> {
                convertIsAutoLogin()
            }
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

    private fun convertIsAutoLogin() {
        viewModelScope.launch {
            preferencesRepository.toggleAutoLogin()
        }
    }

    private fun logout() {
        NaverIdLoginSDK.logout()
        Firebase.auth.signOut()
    }

    private suspend fun cancelMembership() {
        val user = Firebase.auth.currentUser!!
        val deleteUserWorker = getDeleteUserWorker()
        logout()
        WorkManager.getInstance(context).enqueue(deleteUserWorker)
        delay(2000)
        user.delete().addOnCompleteListener {
            viewModelScope.launch {
                _sideEffect.send(SettingsSideEffect.NavigateLogin)
            }
        }
    }

    private fun getDeleteUserWorker(): OneTimeWorkRequest {
        val inputData = Data.Builder()
            .putString("userId", Firebase.auth.uid!!)
            .build()
        return OneTimeWorkRequestBuilder<DeleteUserWorker>()
            .setInputData(inputData)
            .build()
    }
}