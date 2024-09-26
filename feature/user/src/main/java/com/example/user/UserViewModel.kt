package com.example.user

import android.net.ConnectivityManager
import android.net.Network
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import com.example.common.util.NetworkUtil
import com.example.data.model.Meeting
import com.example.data.repository.MeetingRepository
import com.example.data.repository.PlaceRepository
import com.example.data.repository.UserRepository
import com.example.work.DeleteMeetingWorker
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val meetingRepository: MeetingRepository,
    private val placeRepository: PlaceRepository,
    private val networkUtil: NetworkUtil
) : ViewModel() {

    private val _userUiState: MutableLiveData<UserUiState> =
        MutableLiveData(UserUiState(isLoading = true))
    val userUiState: LiveData<UserUiState> get() = _userUiState

    private var currentJob: Job? = null

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            initUiState()
        }

        override fun onLost(network: Network) {
            initUiState()
        }
    }

    fun isNetworkConnected() = networkUtil.isConnected()

    init {
        if (isNetworkConnected().not()) initUiState()
        networkUtil.registerNetworkCallback(networkCallback)
    }

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
                    _userUiState.value =
                        UserUiState(
                            userId = user.id,
                            nickName = user.nickname,
                            profileUrl = user.profileImageWebUrl,
                            attendedMeetings = attendedMeetings,
                            madeMeetings = madeMeetings
                        )
                } catch (e: Exception) {
                    _userUiState.value =
                        UserUiState(
                            isError = true
                        )
                }
            }
        }
    }

    fun getDeleteMeetingWorker(meetingId: String): OneTimeWorkRequest {
        val inputData = Data.Builder()
            .putString("meetingId", meetingId)
            .build()

        return OneTimeWorkRequestBuilder<DeleteMeetingWorker>()
            .setInputData(inputData)
            .build()
    }

    private suspend fun getMeetings(meetingIds: Set<String>): List<UserMeeting> {
        val meetings = meetingRepository.getMeetingsByIds(meetingIds.toList(), isNetworkConnected())
        return meetings.map {
            val placeName = placeRepository.getPlaceById(it.placeId, isNetworkConnected())!!.caption
            it.asUserMeeting(placeName)
        }
    }

    private fun Meeting.asUserMeeting(place: String) = UserMeeting(
        id, date, time, place, content
    )
}