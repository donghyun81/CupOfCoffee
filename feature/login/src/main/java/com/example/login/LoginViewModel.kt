package com.example.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.common.util.NetworkUtil
import com.example.data.model.User
import com.example.data.model.asMeetingEntity
import com.example.data.model.asUserDTO
import com.example.data.model.asUserEntity
import com.example.data.repository.MeetingRepository
import com.example.data.repository.UserRepository
import com.example.model.NaverUser
import com.example.model.asUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.navercorp.nid.oauth.NidOAuthLogin
import com.navercorp.nid.profile.NidProfileCallback
import com.navercorp.nid.profile.data.NidProfileResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val NAVER_ID_TO_EMAIL_COUNT = 7
private const val EMPTY_NAME = "익명"

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val meetingsRepository: MeetingRepository,
    private val networkUtil: NetworkUtil
) : ViewModel() {

    private val _loginUiState = MutableLiveData(LoginUiState())
    val loginUiState: LiveData<LoginUiState> get() = _loginUiState

    private val auth = Firebase.auth

    private val _snackbarEvent = Channel<String>()
    val snackbarEvent = _snackbarEvent.receiveAsFlow()

    fun showSnackBar(message: String) {
        viewModelScope.launch {
            _snackbarEvent.send(message)
        }
    }


    fun handleIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.LoginButtonClicked -> {
                    _loginUiState.value = _loginUiState.value!!.copy(isLoginButtonEnable = false)
            }
        }
    }

    fun isNetworkConnected() = networkUtil.isConnected()

    fun disableLoginButton() {
        _loginUiState.value = _loginUiState.value!!.copy(isLoginButtonEnable = true)
    }

    fun loginNaver() {
        _loginUiState.value = LoginUiState(isLoading = true)
        NidOAuthLogin().callProfileApi(object : NidProfileCallback<NidProfileResponse> {
            override fun onSuccess(result: NidProfileResponse) {
                val naverUser = result.profile?.run {
                    val naverUser = NaverUser(
                        this.id!!,
                        name ?: EMPTY_NAME,
                        nickname,
                        profileImage
                    )
                    naverUser
                } ?: return
                loginAccount(naverUser)
            }

            override fun onFailure(httpStatus: Int, message: String) {
                _loginUiState.value = LoginUiState(isError = true)
            }

            override fun onError(errorCode: Int, message: String) {
                _loginUiState.value = LoginUiState(isError = true)
            }
        })
    }

    private fun loginAccount(naverUser: NaverUser) {
        with(naverUser) {
            auth.signInWithEmailAndPassword(id.toNaverEmail(), id)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = Firebase.auth.uid!!
                        viewModelScope.launch {
                            delay(2000L)
                            loginUser(uid)
                            _loginUiState.value = LoginUiState(isComplete = true)
                        }
                    } else {
                        createAccount(naverUser)
                    }
                }
        }
    }

    private fun createAccount(naverUser: NaverUser) {
        with(naverUser) {
            auth.createUserWithEmailAndPassword(id.toNaverEmail(), id)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = Firebase.auth.uid!!
                        viewModelScope.launch {
                            delay(2000L)
                            insertUser(naverUser.asUser(uid))
                            _loginUiState.value = LoginUiState(isComplete = true)
                        }
                    } else {
                        _loginUiState.value = LoginUiState(isError = true)
                    }
                }
        }
    }

    private suspend fun insertUser(user: User) {
        with(user) {
            userRepository.insertLocal(asUserEntity())
            userRepository.insertRemote(id, asUserDTO())
        }
    }

    private suspend fun loginUser(id: String) {
        val userEntry = userRepository.getRemoteUserById(id)!!
        if (isNetworkConnected()) {
            insertUserMeetings(userEntry)
        }
        userRepository.insertLocal(userEntry.asUserEntity())
    }

    private suspend fun insertUserMeetings(user: User) {
        val madeMeetings =
            meetingsRepository.getMeetingsByIds(user.madeMeetingIds.keys.toList())
        val attendMeetings =
            meetingsRepository.getMeetingsByIds(user.attendedMeetingIds.keys.toList())
        madeMeetings.forEach { madeMeeting ->
            meetingsRepository.insertLocal(
                madeMeeting.asMeetingEntity()
            )
        }
        attendMeetings.forEach { attendMeeting ->
            meetingsRepository.insertLocal(
                attendMeeting.asMeetingEntity()
            )
        }
    }

    private fun String.toNaverEmail() = "${this.take(NAVER_ID_TO_EMAIL_COUNT)}@naver.com"
}