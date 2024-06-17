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
import com.cupofcoffee.data.remote.toMeetingEntry
import com.cupofcoffee.data.remote.toPlaceEntry
import com.cupofcoffee.data.remote.toUserEntry
import com.cupofcoffee.data.repository.MeetingRepositoryImpl
import com.cupofcoffee.data.repository.PlaceRepositoryImpl
import com.cupofcoffee.data.repository.UserRepositoryImpl
import com.cupofcoffee.ui.model.MeetingEntry
import com.cupofcoffee.ui.model.PlaceEntry
import com.cupofcoffee.ui.model.PlaceModel
import com.cupofcoffee.ui.model.toMeetingDTO
import com.cupofcoffee.ui.model.toMeetingListEntry
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MeetingListViewModel(
    savedStateHandle: SavedStateHandle,
    private val meetingRepositoryImpl: MeetingRepositoryImpl,
    private val placeRepositoryImpl: PlaceRepositoryImpl,
    private val userRepositoryImpl: UserRepositoryImpl
) : ViewModel() {

    private val placeId = MeetingListFragmentArgs.fromSavedStateHandle(savedStateHandle).placeId

    private val _uiState: MutableLiveData<MeetingListUiState> =
        MutableLiveData(MeetingListUiState())
    val uiState: LiveData<MeetingListUiState> = _uiState

    init {
        initUiState()
    }

    private fun initUiState() {
        viewModelScope.launch(Dispatchers.IO) {
            val placeEntry = getPlaceEntry(placeId = placeId)
            val meetings = getMeetings(placeEntry.placeModel)
            val meetingEntriesWithPeople = getMeetingEntriesWithPeople(meetings)
            withContext(Dispatchers.Main) {
                updateUiState(placeEntry, meetingEntriesWithPeople)
            }
        }
    }

    private suspend fun getPlaceEntry(placeId: String) =
        placeRepositoryImpl.getLocalPlaceById(placeId)!!.toPlaceEntry(placeId)

    private suspend fun getMeetings(placeModel: PlaceModel) =
        placeModel.meetingIds.keys.map { meetingId ->
            meetingRepositoryImpl.getLocalMeeting(meetingId).to(meetingId) }
    private suspend fun initPlace() {
        _place.value = placeRepositoryImpl.getRemotePlaceById(placeId)?.toPlaceEntry(placeId)
    }

    private suspend fun initMeetings() {
        val meetingIds = placeRepositoryImpl.getRemotePlaceById(placeId)?.meetingIds?.keys
        val meetings = meetingIds?.map { meetingId ->
            meetingRepositoryImpl.getRemoteMeeting(meetingId).toMeetingEntry(meetingId)
        }
    }

    private suspend fun getMeetingEntriesWithPeople(meetings: List<MeetingEntry>) =
        meetings.map { meetingEntry ->
            val users =
                meetingEntry.meetingModel.personIds.keys.map { id ->
                    userRepositoryImpl.getRemoteUserById(id).toUserEntry(id)
                }
            meetingEntry.toMeetingListEntry(users)
        }

    private fun updateUiState(placeEntry: PlaceEntry, meetings: List<MeetingEntryWithPeople>) {
        _uiState.value = uiState.value?.copy(
            placeEntry = placeEntry,
            meetingEntriesWithPeople = meetings
        )
    }

    fun applyMeeting(meetingEntryWithPeople: MeetingEntryWithPeople) {
        viewModelScope.launch(Dispatchers.IO) {
            addMeetingUserId(meetingEntryWithPeople)
            addUserAttendedMeeting(meetingEntryWithPeople.id)
        }
    }

    private suspend fun addMeetingUserId(meetingEntryWithPeople: MeetingEntryWithPeople) {
        with(meetingEntryWithPeople) {
            meetingRepositoryImpl.updateRemote(
                id,
                meetingListModel.toMeetingModel()
                    .apply { personIds[Firebase.auth.uid!!] = true }
                    .toMeetingDTO()
            )
        }
    }

    private suspend fun addUserAttendedMeeting(meetingId: String) {
        val uid = Firebase.auth.uid!!
        val user = userRepositoryImpl.getRemoteUserById(uid)
        user.attendedMeetingIds[meetingId] = true
        userRepositoryImpl.updateRemote(uid, user)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MeetingListViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    meetingRepositoryImpl = CupOfCoffeeApplication.meetingRepository,
                    placeRepositoryImpl = CupOfCoffeeApplication.placeRepository,
                    userRepositoryImpl = CupOfCoffeeApplication.userRepository
                )
            }
        }
    }
        }