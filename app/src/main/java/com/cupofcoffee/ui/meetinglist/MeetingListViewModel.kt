package com.cupofcoffee.ui.meetinglist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cupofcoffee.CupOfCoffeeApplication
import com.cupofcoffee.data.DataResult
import com.cupofcoffee.data.DataResult.Companion.error
import com.cupofcoffee.data.DataResult.Companion.loading
import com.cupofcoffee.data.DataResult.Companion.success
import com.cupofcoffee.data.remote.model.asUserEntry
import com.cupofcoffee.data.repository.MeetingRepositoryImpl
import com.cupofcoffee.data.repository.PlaceRepositoryImpl
import com.cupofcoffee.data.repository.UserRepositoryImpl
import com.cupofcoffee.ui.model.MeetingEntry
import com.cupofcoffee.ui.model.PlaceEntry
import com.cupofcoffee.ui.model.asMeetingEntity
import com.cupofcoffee.ui.model.asMeetingListEntry
import com.cupofcoffee.util.NetworkUtil
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
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
    val uiState: LiveData<DataResult<MeetingListUiState>> = _uiState

    init {
        viewModelScope.launch {
            initUiState()
        }
    }

    fun isNetworkConnected() = networkUtil.isConnected()

    private suspend fun initUiState() {
        try {
            val placeEntry = placeRepositoryImpl.getPlaceById(placeId, networkUtil.isConnected())!!
            val meetingEntriesWithPeople = convertMeetingEntriesWithPeople(placeEntry)
            _uiState.value = success(MeetingListUiState(placeEntry, meetingEntriesWithPeople))
        } catch (e: Exception) {
            _uiState.value = error(e)
        }
    }

    private suspend fun convertMeetingEntriesWithPeople(placeEntry: PlaceEntry): List<MeetingEntryWithPeople> {
        val meetingIds = placeEntry.placeModel.meetingIds.keys.toList()
        val meetings = meetingRepositoryImpl.getMeetingsByIds(meetingIds, networkUtil.isConnected())
        addLocalMeetings(meetings)
        return if (networkUtil.isConnected()) meetings.map { meetingEntry ->
            convertMeetingListEntry(meetingEntry)
        }
        else meetings.map { meetingEntry ->
            meetingEntry.asMeetingListEntry(emptyList())
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