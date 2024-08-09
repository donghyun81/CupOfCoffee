package com.cupofcoffee0801.ui.meetinglist

import android.net.ConnectivityManager
import android.net.Network
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cupofcoffee0801.data.DataResult
import com.cupofcoffee0801.data.DataResult.Companion.error
import com.cupofcoffee0801.data.DataResult.Companion.loading
import com.cupofcoffee0801.data.DataResult.Companion.success
import com.cupofcoffee0801.data.remote.model.asUserEntry
import com.cupofcoffee0801.data.repository.MeetingRepository
import com.cupofcoffee0801.data.repository.MeetingRepositoryImpl
import com.cupofcoffee0801.data.repository.PlaceRepository
import com.cupofcoffee0801.data.repository.PlaceRepositoryImpl
import com.cupofcoffee0801.data.repository.UserRepository
import com.cupofcoffee0801.data.repository.UserRepositoryImpl
import com.cupofcoffee0801.ui.model.MeetingEntry
import com.cupofcoffee0801.ui.model.PlaceEntry
import com.cupofcoffee0801.ui.model.asMeetingEntity
import com.cupofcoffee0801.ui.model.asMeetingListEntry
import com.cupofcoffee0801.util.NetworkUtil
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
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

    private val _uiState: MutableLiveData<DataResult<MeetingListUiState>> =
        MutableLiveData(loading())
    val uiState: LiveData<DataResult<MeetingListUiState>> get() = _uiState

    private val _isButtonClicked: MutableLiveData<Boolean> = MutableLiveData(false)
    val isButtonClicked: LiveData<Boolean> get() = _isButtonClicked

    var currentJob: Job? = null


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

    private fun initUiState() {
        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            try {
                val placeEntry =
                    placeRepository.getPlaceById(placeId, networkUtil.isConnected())!!
                val meetingEntriesWithPeopleInFlow = convertMeetingEntriesWithPeople(placeEntry)
                meetingEntriesWithPeopleInFlow.collect { meetingEntriesWithPeople ->
                    if (Firebase.auth.uid == null) return@collect
                    _uiState.value =
                        success(MeetingListUiState(placeEntry, meetingEntriesWithPeople))
                }
            } catch (e: Exception) {
                _uiState.value = error(e)
            }
        }
    }

    private suspend fun convertMeetingEntriesWithPeople(placeEntry: PlaceEntry): Flow<List<MeetingEntryWithPeople>> {
        val meetingIds = placeEntry.placeModel.meetingIds.keys.toList()
        val meetingsInFlow =
            meetingRepository.getMeetingsByIdsInFlow(meetingIds, networkUtil.isConnected())
        return meetingsInFlow.flatMapLatest { meetings ->
            addLocalMeetings(meetings)
            if (networkUtil.isConnected()) flow { emit(meetings.map { convertMeetingListEntry(it) }) }
            else flow { emit(meetings.map { it.asMeetingListEntry(emptyList()) }) }
        }
    }

    private suspend fun addLocalMeetings(meetings: List<MeetingEntry>) {
        if (networkUtil.isConnected().not()) return
        meetings.forEach { meeting ->
            meetingRepository.insertLocal(meeting.asMeetingEntity())
        }
    }

    private suspend fun convertMeetingListEntry(meeting: MeetingEntry): MeetingEntryWithPeople {
        val users =
            userRepository.getRemoteUsersByIds(meeting.meetingModel.personIds.keys.toList())
        return meeting.asMeetingListEntry(users.map { it.value.asUserEntry(it.key) })
    }

    fun applyMeeting(meetingEntryWithPeople: MeetingEntryWithPeople) {
        viewModelScope.launch {
            addUserToMeeting(meetingEntryWithPeople)
            addAttendedMeetingToUser(meetingEntryWithPeople.id)
        }
    }

    private suspend fun addUserToMeeting(meetingEntryWithPeople: MeetingEntryWithPeople) {
        val meetingEntry = meetingEntryWithPeople.asMeetingEntry()
        meetingEntry.meetingModel.personIds[Firebase.auth.uid!!] = true
        meetingRepository.update(meetingEntry)
    }

    private suspend fun addAttendedMeetingToUser(meetingId: String) {
        val uid = Firebase.auth.uid!!
        val userEntry = userRepository.getLocalUserById(uid)!!
        userEntry.userModel.attendedMeetingIds[meetingId] = true
        userRepository.update(userEntry)
    }

    fun cancelMeeting(meetingEntryWithPeople: MeetingEntryWithPeople) {
        viewModelScope.launch {
            deleteUserToMeeting(meetingEntryWithPeople)
            deleteAttendedMeetingToUser(meetingEntryWithPeople.id)
        }
    }

    private suspend fun deleteUserToMeeting(meetingEntryWithPeople: MeetingEntryWithPeople) {
        val meetingEntry = meetingEntryWithPeople.asMeetingEntry()
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