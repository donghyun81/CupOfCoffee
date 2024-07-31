package com.cupofcoffee0801.ui.meetinglist

import android.net.ConnectivityManager
import android.net.Network
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cupofcoffee0801.CupOfCoffeeApplication
import com.cupofcoffee0801.data.DataResult
import com.cupofcoffee0801.data.DataResult.Companion.error
import com.cupofcoffee0801.data.DataResult.Companion.loading
import com.cupofcoffee0801.data.DataResult.Companion.success
import com.cupofcoffee0801.data.remote.model.asUserEntry
import com.cupofcoffee0801.data.repository.MeetingRepositoryImpl
import com.cupofcoffee0801.data.repository.PlaceRepositoryImpl
import com.cupofcoffee0801.data.repository.UserRepositoryImpl
import com.cupofcoffee0801.ui.model.MeetingEntry
import com.cupofcoffee0801.ui.model.PlaceEntry
import com.cupofcoffee0801.ui.model.asMeetingEntity
import com.cupofcoffee0801.ui.model.asMeetingListEntry
import com.cupofcoffee0801.util.NetworkUtil
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class MeetingListViewModel(
    savedStateHandle: SavedStateHandle,
    private val meetingRepositoryImpl: MeetingRepositoryImpl,
    private val placeRepositoryImpl: PlaceRepositoryImpl,
    private val userRepositoryImpl: UserRepositoryImpl,
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
                    placeRepositoryImpl.getPlaceById(placeId, networkUtil.isConnected())!!
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
        val meetings =
            meetingRepositoryImpl.getMeetingsByIdsInFlow(meetingIds, networkUtil.isConnected())
        return meetings.flatMapLatest { meetings ->
            addLocalMeetings(meetings)
            if (networkUtil.isConnected()) flow { emit(meetings.map { convertMeetingListEntry(it) }) }
            else flow { emit(meetings.map { it.asMeetingListEntry(emptyList()) }) }
        }
    }

    private suspend fun addLocalMeetings(meetings: List<MeetingEntry>) {
        meetings.forEach { meeting ->
            meetingRepositoryImpl.insertLocal(meeting.asMeetingEntity())
        }
    }

    private suspend fun convertMeetingListEntry(meeting: MeetingEntry): MeetingEntryWithPeople {
        val users =
            userRepositoryImpl.getRemoteUsersByIds(meeting.meetingModel.personIds.keys.toList())
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
        meetingRepositoryImpl.update(meetingEntry)
    }

    private suspend fun addAttendedMeetingToUser(meetingId: String) {
        val uid = Firebase.auth.uid!!
        val userEntry = userRepositoryImpl.getLocalUserById(uid)
        userEntry.userModel.attendedMeetingIds[meetingId] = true
        userRepositoryImpl.update(userEntry)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MeetingListViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    meetingRepositoryImpl = CupOfCoffeeApplication.meetingRepository,
                    placeRepositoryImpl = CupOfCoffeeApplication.placeRepository,
                    userRepositoryImpl = CupOfCoffeeApplication.userRepository,
                    networkUtil = CupOfCoffeeApplication.networkUtil
                )
            }
        }
    }
}