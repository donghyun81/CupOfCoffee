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
import com.cupofcoffee.ui.model.PlaceEntry
import com.cupofcoffee.ui.model.toMeetingDTO
import com.cupofcoffee.ui.model.toMeetingListEntry
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class MeetingListViewModel(
    savedStateHandle: SavedStateHandle,
    private val meetingRepositoryImpl: MeetingRepositoryImpl,
    private val placeRepositoryImpl: PlaceRepositoryImpl,
    private val userRepositoryImpl: UserRepositoryImpl
) : ViewModel() {

    private val placeId = MeetingListFragmentArgs.fromSavedStateHandle(savedStateHandle).placeId

    private val _meetings: MutableLiveData<List<MeetingListEntry>> = MutableLiveData()
    val meetings: LiveData<List<MeetingListEntry>> = _meetings

    private val _place: MutableLiveData<PlaceEntry> = MutableLiveData()
    val place: LiveData<PlaceEntry> = _place

    init {
        viewModelScope.launch {
            initPlace()
            initMeetings()
        }
    }

    private suspend fun initPlace() {
        _place.value = placeRepositoryImpl.getPlaceById(placeId)?.toPlaceEntry(placeId)
    }

    private suspend fun initMeetings() {
        val meetingIds = placeRepositoryImpl.getPlaceById(placeId)?.meetingIds?.keys
        val meetings = meetingIds?.map { meetingId ->
            meetingRepositoryImpl.getMeeting(meetingId).toMeetingEntry(meetingId)
        }
        val meetingListModel = meetings?.map { meetingEntry ->
            val users =
                meetingEntry.meetingModel.peopleId.map { id ->
                    userRepositoryImpl.getUserById(id).toUserEntry(id)
                }
            meetingEntry.toMeetingListEntry(users)
        }
        _meetings.value = meetingListModel ?: emptyList()
    }

    suspend fun applyMeeting(meetingListEntry: MeetingListEntry) {
        addMeetingUserId(meetingListEntry)
        addUserAttendedMeeting(meetingListEntry.id)
    }

    private suspend fun addMeetingUserId(meetingListEntry: MeetingListEntry) {
        with(meetingListEntry) {
            meetingRepositoryImpl.update(
                id,
                meetingListModel.toMeetingModel()
                    .apply { peopleId.add(Firebase.auth.uid!!) }
                    .toMeetingDTO()
            )
        }
    }

    private suspend fun addUserAttendedMeeting(meetingId: String) {
        val uid = Firebase.auth.uid!!
        val user = userRepositoryImpl.getUserById(uid)
        user.attendedMeetingIds.add(meetingId)
        userRepositoryImpl.update(uid, user)
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