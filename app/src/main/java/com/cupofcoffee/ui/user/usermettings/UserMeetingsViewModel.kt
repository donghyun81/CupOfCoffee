package com.cupofcoffee.ui.user.usermettings

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
import com.cupofcoffee.data.local.toEntry
import com.cupofcoffee.data.remote.toPlaceEntity
import com.cupofcoffee.data.repository.MeetingRepositoryImpl
import com.cupofcoffee.data.repository.PlaceRepositoryImpl
import com.cupofcoffee.data.repository.UserRepositoryImpl
import com.cupofcoffee.ui.model.MeetingEntry
import com.cupofcoffee.ui.model.MeetingsCategory
import com.cupofcoffee.ui.model.toMeetingEntity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


private const val CATEGORY_TAG = "category"

class UserMeetingsViewModel(
    savedStateHandle: SavedStateHandle,
    private val userRepositoryImpl: UserRepositoryImpl,
    private val meetingRepositoryImpl: MeetingRepositoryImpl,
    private val placeRepositoryImpl: PlaceRepositoryImpl
) : ViewModel() {

    private val category = savedStateHandle.get<MeetingsCategory>(CATEGORY_TAG)!!

    private val _uiState: MutableLiveData<UserMeetingsUiState> =
        MutableLiveData(UserMeetingsUiState())
    val uiState: LiveData<UserMeetingsUiState> = _uiState

    init {
        viewModelScope.launch {
            setMeetings()
        }
    }

    private suspend fun setMeetings() {
        val uid = Firebase.auth.uid ?: return
        val user = userRepositoryImpl.getLocalUserByIdInFlow(id = uid)
        user.collect { userEntity ->
            when (category) {
                MeetingsCategory.ATTENDED_MEETINGS -> {
                    val meetings = getMeetingEntries(userEntity.attendedMeetingIds.keys)
                    updateMeetings(meetings)
                }

                MeetingsCategory.MADE_MEETINGS -> {
                    val meetings = getMeetingEntries(userEntity.madeMeetingIds.keys)
                    updateMeetings(meetings)
                }
            }
        }
    }

    private suspend fun getMeetingEntries(meetingIds: Set<String>) =
        withContext(Dispatchers.IO) {
            meetingIds.map { id ->
                meetingRepositoryImpl.getLocalMeeting(id).toEntry()
            }
        }

    private suspend fun updateMeetings(meetingEntries: List<MeetingEntry>) {
        withContext(Dispatchers.Main) {
            _uiState.value = _uiState.value?.copy(
                meetings = meetingEntries
            )
        }
    }

    suspend fun deleteMeeting(meetingEntry: MeetingEntry) {
        val placeId = meetingEntry.meetingModel.placeId
        updatePlace(placeId, meetingEntry.id)
        updateUser(meetingEntry.id)
        meetingRepositoryImpl.deleteLocal(meetingEntry.toMeetingEntity())
        meetingRepositoryImpl.deleteRemote(meetingEntry.id)
    }

    private suspend fun updatePlace(placeId: String, meetingId: String) {
        val placeDTO = placeRepositoryImpl.getRemotePlaceById(placeId) ?: return
        placeDTO.meetingIds.remove(meetingId)
        if (placeDTO.meetingIds.isEmpty()) {
            placeRepositoryImpl.deleteLocal(placeDTO.toPlaceEntity(placeId))
            placeRepositoryImpl.deleteRemote(placeId)
        } else {
            placeRepositoryImpl.updateLocal(placeDTO.toPlaceEntity(placeId))
            placeRepositoryImpl.updateRemote(placeId, placeDTO)
        }
    }

    private suspend fun updateUser(meetingId: String) {
        val uid = Firebase.auth.uid!!
        val user = userRepositoryImpl.getRemoteUserById(uid)
        user.madeMeetingIds.remove(meetingId)
        user.attendedMeetingIds.remove(meetingId)
        userRepositoryImpl.updateRemote(uid, user)
        userRepositoryImpl.updateRemote(uid, user)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                UserMeetingsViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    userRepositoryImpl = CupOfCoffeeApplication.userRepository,
                    meetingRepositoryImpl = CupOfCoffeeApplication.meetingRepository,
                    placeRepositoryImpl = CupOfCoffeeApplication.placeRepository
                )
            }
        }
    }
}