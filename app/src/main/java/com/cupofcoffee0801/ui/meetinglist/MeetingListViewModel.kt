package com.cupofcoffee0801.ui.meetinglist

import android.net.ConnectivityManager
import android.net.Network
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cupofcoffee0801.data.repository.MeetingRepository
import com.cupofcoffee0801.data.repository.PlaceRepository
import com.cupofcoffee0801.data.repository.UserRepository
import com.cupofcoffee0801.ui.model.Meeting
import com.cupofcoffee0801.ui.model.asMeetingEntity
import com.cupofcoffee0801.util.NetworkUtil
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MeetingListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val meetingRepository: MeetingRepository,
    private val placeRepository: PlaceRepository,
    private val userRepository: UserRepository,
    private val networkUtil: NetworkUtil
) : ViewModel() {

    private val placeId = MeetingListFragmentArgs.fromSavedStateHandle(savedStateHandle).placeId

    private val _uiState: MutableLiveData<MeetingListUiState> =
        MutableLiveData(MeetingListUiState(isLoading = true))
    val uiState: LiveData<MeetingListUiState> get() = _uiState

    private val _isButtonClicked: MutableLiveData<Boolean> = MutableLiveData(false)
    val isButtonClicked: LiveData<Boolean> get() = _isButtonClicked

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

    fun onButtonClicked() {
        _isButtonClicked.value = true
    }

    fun isNetworkConnected() = networkUtil.isConnected()

    fun initUiState() {
        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            try {
                val place =
                    placeRepository.getPlaceById(placeId, networkUtil.isConnected())!!
                val meetingsInFlow =
                    meetingRepository.getMeetingsByIdsInFlow(place.meetingIds.keys.toList())
                meetingsInFlow.flatMapLatest { meetings ->
                    addLocalMeetings(meetings)
                    flow { emit(meetings.map { convertMeetingInPlace(it) }) }
                }.collect {
                    _uiState.value = MeetingListUiState(
                        placeCaption = place.caption,
                        meetingsInPlace = it,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = MeetingListUiState(
                    isError = true,
                    isLoading = false
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

    private suspend fun convertMeetingInPlace(meeting: Meeting): MeetingListMeetingUiModel {
        val isMyMeeting = Firebase.auth.uid == meeting.managerId
        val isAttendedMeeting = meeting.personIds.keys.contains(Firebase.auth.uid)
        return if (isNetworkConnected().not()) MeetingListMeetingUiModel(
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
            val meetingListUserUiModel = users.values.map { MeetingListUserUiModel(it.name, it.profileImageWebUrl) }
            return MeetingListMeetingUiModel(
                meeting.id,
                meeting.content,
                meeting.date,
                meeting.time,
                isAttendedMeeting,
                isMyMeeting,
                meetingListUserUiModel
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