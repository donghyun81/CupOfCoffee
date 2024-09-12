package com.cupofcoffee0801.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cupofcoffee0801.data.repository.MeetingRepository
import com.cupofcoffee0801.data.repository.UserRepository
import com.cupofcoffee0801.ui.model.NaverUser
import com.cupofcoffee0801.ui.model.User
import com.cupofcoffee0801.ui.model.asMeetingEntity
import com.cupofcoffee0801.ui.model.asUser
import com.cupofcoffee0801.ui.model.asUserDTO
import com.cupofcoffee0801.ui.model.asUserEntity
import com.cupofcoffee0801.util.NetworkUtil
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.navercorp.nid.oauth.NidOAuthLogin
import com.navercorp.nid.profile.NidProfileCallback
import com.navercorp.nid.profile.data.NidProfileResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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

    private val _isButtonClicked: MutableLiveData<Boolean> = MutableLiveData(false)
    val isButtonClicked: LiveData<Boolean> get() = _isButtonClicked

    private val _loginUiState = MutableLiveData(LoginUiState())
    val loginUiState: LiveData<LoginUiState> get() = _loginUiState

    private val auth = Firebase.auth

    fun isNetworkConnected() = networkUtil.isConnected()

    fun switchButtonClicked() {
        _isButtonClicked.value = _isButtonClicked.value?.not()
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