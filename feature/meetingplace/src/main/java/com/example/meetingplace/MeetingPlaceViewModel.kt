package com.example.meetingplace

import android.net.ConnectivityManager
import android.net.Network
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.common.util.NetworkUtil
import com.example.data.model.Meeting
import com.example.data.model.asMeetingEntity
import com.example.data.repository.MeetingRepository
import com.example.data.repository.PlaceRepository
import com.example.data.repository.UserRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MeetingPlaceViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val meetingRepository: MeetingRepository,
    private val placeRepository: PlaceRepository,
    private val userRepository: UserRepository,
    private val networkUtil: NetworkUtil
) : ViewModel() {

    private val placeId = MeetingPlaceFragmentArgs.fromSavedStateHandle(savedStateHandle).placeId

    private val _uiState: MutableLiveData<MeetingPlaceUiState> =
        MutableLiveData(MeetingPlaceUiState(isLoading = true))
    val uiState: LiveData<MeetingPlaceUiState> get() = _uiState

    private var currentJob: Job? = null


    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            initUiState()
        }

        override fun onLost(network: Network) {
            initUiState()
        }
    }

    init {
        if (isNetworkConnected().not()) initUiState()
        networkUtil.registerNetworkCallback(networkCallback)
    }

    fun isNetworkConnected() = networkUtil.isConnected()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun initUiState() {
        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            try {
                Log.d("12345", placeId)
                val place =
                    placeRepository.getPlaceById(placeId, networkUtil.isConnected())!!
                val meetingsInFlow =
                    meetingRepository.getMeetingsByIdsInFlow(
                        place.meetingIds.keys.toList(),
                        isNetworkConnected()
                    )
                meetingsInFlow.flatMapLatest { meetings ->
                    addLocalMeetings(meetings)
                    flow { emit(meetings.map { convertMeetingInPlace(it) }) }
                }.collect {
                    _uiState.value = MeetingPlaceUiState(
                        placeCaption = place.caption,
                        meetingsInPlace = it,
                    )
                }
            } catch (e: Exception) {
                _uiState.value = MeetingPlaceUiState(
                    isError = true,
                )
            }
        }
    }

    private suspend fun addLocalMeetings(meetings: List<Meeting>) {
        if (isNetworkConnected().not()) return
        meetings.forEach { meeting ->
            meetingRepository.insertLocal(meeting.asMeetingEntity())
        }
    }

    private suspend fun convertMeetingInPlace(meeting: Meeting): MeetingPlaceMeetingUiModel {
        val isMyMeeting = Firebase.auth.uid == meeting.managerId
        val isAttendedMeeting = meeting.personIds.keys.contains(Firebase.auth.uid)
        return if (isNetworkConnected().not()) MeetingPlaceMeetingUiModel(
            meeting.id,
            meeting.content,
            meeting.date,
            meeting.time,
            isAttendedMeeting,
            isMyMeeting,
            emptyList()
        )
        else {
            val users =
                userRepository.getRemoteUsersByIds(meeting.personIds.keys.toList())
            val meetingPlaceUserUiModel =
                users.values.map { MeetingPlaceUserUiModel(it.name, it.profileImageWebUrl) }
            return MeetingPlaceMeetingUiModel(
                meeting.id,
                meeting.content,
                meeting.date,
                meeting.time,
                isAttendedMeeting,
                isMyMeeting,
                meetingPlaceUserUiModel
            )
        }
    }

    fun applyMeeting(meetingId: String) {
        viewModelScope.launch {
            addUserToMeeting(meetingId)
            addAttendedMeetingToUser(meetingId)
        }
    }

    private suspend fun addUserToMeeting(meetingId: String) {
        val meeting = meetingRepository.getMeeting(meetingId)
        meeting.personIds[Firebase.auth.uid!!] = true
        meetingRepository.update(meeting)
    }

    private suspend fun addAttendedMeetingToUser(meetingId: String) {
        val uid = Firebase.auth.uid!!
        val user = userRepository.getLocalUserById(uid)!!
        user.attendedMeetingIds[meetingId] = true
        userRepository.update(user)
    }

    fun cancelMeeting(meetingId: String) {
        viewModelScope.launch {
            deleteUserToMeeting(meetingId)
            deleteAttendedMeetingToUser(meetingId)
        }
    }

    private suspend fun deleteUserToMeeting(meetingId: String) {
        val meeting = meetingRepository.getMeeting(meetingId)
        meeting.personIds.remove(Firebase.auth.uid!!)
        meetingRepository.update(meeting)
    }

    private suspend fun deleteAttendedMeetingToUser(meetingId: String) {
        val uid = Firebase.auth.uid!!
        val user = userRepository.getLocalUserById(uid)!!
        user.attendedMeetingIds.remove(meetingId)
        userRepository.update(user)
    }
}