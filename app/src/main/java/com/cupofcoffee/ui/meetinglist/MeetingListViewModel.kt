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
import com.cupofcoffee.data.repository.MeetingRepositoryImpl
import com.cupofcoffee.data.repository.PlaceRepositoryImpl
import com.cupofcoffee.ui.model.MeetingEntry
import com.cupofcoffee.ui.model.PlaceEntry
import com.cupofcoffee.ui.model.toMeetingDTO
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class MeetingListViewModel(
    savedStateHandle: SavedStateHandle,
    private val meetingRepositoryImpl: MeetingRepositoryImpl,
    private val placeRepositoryImpl: PlaceRepositoryImpl
) : ViewModel() {

    private val placeId = MeetingListFragmentArgs.fromSavedStateHandle(savedStateHandle).placeId

    private val _meetings: MutableLiveData<List<MeetingEntry>> = MutableLiveData()
    val meetings: LiveData<List<MeetingEntry>> = _meetings

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
        _meetings.value = meetingIds?.map { meetingId ->
            meetingRepositoryImpl.getMeeting(meetingId).toMeetingEntry(meetingId)
        }
    }

    suspend fun applyMeeting(meetingEntry: MeetingEntry) {
        with(meetingEntry) {
            meetingRepositoryImpl.addPeopleId(
                id,
                meetingModel.apply { peopleId.add(Firebase.auth.uid!!) }.toMeetingDTO()
            )
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MeetingListViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    meetingRepositoryImpl = CupOfCoffeeApplication.meetingRepository,
                    placeRepositoryImpl = CupOfCoffeeApplication.placeRepository
                )
            }
        }
    }
}