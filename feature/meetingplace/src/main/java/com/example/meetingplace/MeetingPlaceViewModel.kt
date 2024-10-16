package com.example.meetingplace

import android.net.ConnectivityManager
import android.net.Network
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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

const val APPLY_MESSAGE = "모임 참가를 위해서 네트워크 연결이 필요합니다!"
const val NO_CANCEL_MY_MEETING = "내 모임은 모임 상세에서 삭제 해주세요!"
const val CANCEL_NETWORK_MESSAGE = "모임을 취소하기 위해서 네트워크 연결이 필요합니다!"


@HiltViewModel
class MeetingPlaceViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val meetingRepository: MeetingRepository,
    private val placeRepository: PlaceRepository,
    private val userRepository: UserRepository,
    private val networkUtil: NetworkUtil
) : ViewModel() {

    private val placeId = MeetingPlaceFragmentArgs.fromSavedStateHandle(savedStateHandle).placeId

    private val _uiState: MutableStateFlow<MeetingPlaceUiState> =
        MutableStateFlow(MeetingPlaceUiState(isLoading = true))
    val uiState: MutableStateFlow<MeetingPlaceUiState> get() = _uiState

    private val _sideEffect = Channel<MeetingPlaceSideEffect>()
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

    init {
        if (isNetworkConnected().not()) initUiState()
        networkUtil.registerNetworkCallback(networkCallback)
    }

    private fun isNetworkConnected() = networkUtil.isConnected()

    fun handleIntent(intent: MeetingPlaceIntent) {
        when (intent) {
            is MeetingPlaceIntent.AttendedCancelClick -> {
                viewModelScope.launch {
                    if (networkUtil.isConnected()) {
                        cancelMeeting(intent.meetingId)
                    }
                    val snackBarMessage =
                        if (intent.isMyMeeting) NO_CANCEL_MY_MEETING else CANCEL_NETWORK_MESSAGE
                    _uiState.value = uiState.value.copy(snackBarMessage = snackBarMessage)
                    _sideEffect.send(MeetingPlaceSideEffect.ShowSnackBar(uiState.value.snackBarMessage))
                }
            }

            is MeetingPlaceIntent.MeetingApplyClick -> {
                _uiState.value = uiState.value.copy(snackBarMessage = APPLY_MESSAGE)
                applyMeeting(intent.meetingId)
            }

            is MeetingPlaceIntent.MeetingDetailClick -> {
                viewModelScope.launch {
                    _sideEffect.send(MeetingPlaceSideEffect.NavigateMeetingDetail(intent.meetingId))
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun initUiState() {
        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            try {
                val place = placeRepository.getPlaceById(placeId, networkUtil.isConnected())!!
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
                users.values.map { MeetingPlaceUserUiModel(it.nickname, it.profileImageWebUrl) }
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

    private fun applyMeeting(meetingId: String) {
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

    private suspend fun cancelMeeting(meetingId: String) {
        deleteUserToMeeting(meetingId)
        deleteAttendedMeetingToUser(meetingId)
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