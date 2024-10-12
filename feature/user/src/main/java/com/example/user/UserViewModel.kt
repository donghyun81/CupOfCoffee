package com.example.user

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.common.util.NetworkUtil
import com.example.data.model.Meeting
import com.example.data.repository.MeetingRepository
import com.example.data.repository.PlaceRepository
import com.example.data.repository.UserRepository
import com.example.work.DeleteMeetingWorker
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val meetingRepository: MeetingRepository,
    private val placeRepository: PlaceRepository,
    private val networkUtil: NetworkUtil,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserUiState(isLoading = true))
    val uiState = _uiState.asStateFlow()

    private val _sideEffect = Channel<UserSideEffect>()
    val sideEffect = _sideEffect.receiveAsFlow()

    private var currentJob: Job? = null

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            initUiState()
        }

        override fun onLost(network: Network) {
            initUiState()
        }
    }

    fun handleIntent(intent: UserIntent) {
        when (intent) {
            is UserIntent.DeleteMeetingClick -> {
                deleteMeeting(intent.meetingId)
            }

            is UserIntent.DetailMeetingClick -> {
                viewModelScope.launch {
                    _sideEffect.send(UserSideEffect.NavigateMeetingDetail(intent.meetingId))
                }
            }

            is UserIntent.UpdateMeetingClick -> {
                viewModelScope.launch {
                    _sideEffect.send(UserSideEffect.NavigateMakeMeeting(intent.meetingId))
                }
            }

            UserIntent.InitData -> {
                if (isNetworkConnected().not()) initUiState()
                networkUtil.registerNetworkCallback(networkCallback)
            }

            UserIntent.SettingClick -> {
                viewModelScope.launch {
                    _sideEffect.send(UserSideEffect.NavigateSettings)
                }
            }

            UserIntent.UserEditClick -> {
                viewModelScope.launch {
                    _sideEffect.send(UserSideEffect.NavigateUserEdit)
                }
            }
        }
    }

    private fun isNetworkConnected() = networkUtil.isConnected()

    override fun onCleared() {
        super.onCleared()
        currentJob?.cancel()
    }

    private fun initUiState() {
        val uid = Firebase.auth.uid!!
        val userInFlow = userRepository.getLocalUserByIdInFlow(uid)

        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            userInFlow.collect { user ->
                if (Firebase.auth.uid == null) return@collect
                try {
                    user ?: return@collect
                    val attendedMeetings = getMeetings(user.attendedMeetingIds.keys)
                    val madeMeetings = getMeetings(user.madeMeetingIds.keys)
                    _uiState.value =
                        UserUiState(
                            userId = user.id,
                            nickName = user.nickname,
                            profileUrl = user.profileImageWebUrl,
                            attendedMeetings = attendedMeetings,
                            madeMeetings = madeMeetings
                        )
                } catch (e: Exception) {
                    _uiState.value =
                        UserUiState(
                            isError = true
                        )
                }
            }
        }
    }

    private fun deleteMeeting(meetingId: String) {
        val inputData = Data.Builder()
            .putString("meetingId", meetingId)
            .build()

        val deleteMeetingWorker = OneTimeWorkRequestBuilder<DeleteMeetingWorker>()
            .setInputData(inputData)
            .build()
        WorkManager.getInstance(context).enqueue(deleteMeetingWorker)
    }

    private suspend fun getMeetings(meetingIds: Set<String>): List<UserMeeting> {
        val meetings =
            meetingRepository.getMeetingsByIds(meetingIds.toList(), networkUtil.isConnected())
        return meetings.map {
            val placeName =
                placeRepository.getPlaceById(it.placeId, networkUtil.isConnected())!!.caption
            it.asUserMeeting(placeName)
        }
    }

    private fun Meeting.asUserMeeting(place: String) = UserMeeting(
        id, date, time, place, content
    )
}