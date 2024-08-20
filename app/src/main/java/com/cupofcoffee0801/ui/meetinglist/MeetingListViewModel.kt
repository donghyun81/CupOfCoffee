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
import com.cupofcoffee0801.ui.model.MeetingEntry
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

data class MeetingListUiState(
    val placeCaption: String = "",
    val meetingsInPlace: List<MeetingInPlace> = emptyList(),
    val isError: Boolean = false,
    val isLoading: Boolean = false
)

data class MeetingInPlace(
    val id: String,
    val content: String,
    val date: String,
    val time: String,
    val isAttendedMeeting: Boolean,
    val userInMeeting: List<UserInMeeting>
)

data class UserInMeeting(
    val nickName: String?,
    val profilesUrl: String?
)

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
                val placeEntry =
                    placeRepository.getPlaceById(placeId, networkUtil.isConnected())!!
                val meetingsInFlow =
                    meetingRepository.getMeetingsByIdsInFlow(placeEntry.placeModel.meetingIds.keys.toList())
                meetingsInFlow.flatMapLatest { meetings ->
                    addLocalMeetings(meetings)
                    flow { emit(meetings.map { convertMeetingInPlace(it) }) }
                }.collect {
                    _uiState.value = MeetingListUiState(
                        placeCaption = placeEntry.placeModel.caption,
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

    private suspend fun addLocalMeetings(meetings: List<MeetingEntry>) {
        if (isNetworkConnected().not()) return
        meetings.forEach { meeting ->
            meetingRepository.insertLocal(meeting.asMeetingEntity())
        }
    }

    private suspend fun convertMeetingInPlace(meeting: MeetingEntry): MeetingInPlace {
        return if (isNetworkConnected().not()) MeetingInPlace(
            meeting.id,
            meeting.meetingModel.content,
            meeting.meetingModel.date,
            meeting.meetingModel.time,
            false,
            emptyList()
        )
        else {
            val users =
                userRepository.getRemoteUsersByIds(meeting.meetingModel.personIds.keys.toList())
            val userInMeeting = users.values.map { UserInMeeting(it.name, it.profileImageWebUrl) }
            val isAttendedMeeting = users.keys.contains(Firebase.auth.uid)
            return MeetingInPlace(
                meeting.id,
                meeting.meetingModel.content,
                meeting.meetingModel.date,
                meeting.meetingModel.time,
                isAttendedMeeting,
                userInMeeting
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
        meeting.meetingModel.personIds[Firebase.auth.uid!!] = true
        meetingRepository.update(meeting)
    }

    private suspend fun addAttendedMeetingToUser(meetingId: String) {
        val uid = Firebase.auth.uid!!
        val userEntry = userRepository.getLocalUserById(uid)!!
        userEntry.userModel.attendedMeetingIds[meetingId] = true
        userRepository.update(userEntry)
    }

    fun cancelMeeting(meetingId: String) {
        viewModelScope.launch {
            deleteUserToMeeting(meetingId)
            deleteAttendedMeetingToUser(meetingId)
        }
    }

    private suspend fun deleteUserToMeeting(meetingId: String) {
        val meetingEntry = meetingRepository.getMeeting(meetingId)
        meetingEntry.meetingModel.personIds.remove(Firebase.auth.uid!!)
        meetingRepository.update(meetingEntry)
    }

    private suspend fun deleteAttendedMeetingToUser(meetingId: String) {
        val uid = Firebase.auth.uid!!
        val userEntry = userRepository.getLocalUserById(uid)!!
        userEntry.userModel.attendedMeetingIds.remove(meetingId)
        userRepository.update(userEntry)
    }
}