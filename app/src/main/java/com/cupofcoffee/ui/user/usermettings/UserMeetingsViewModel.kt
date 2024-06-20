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
import com.cupofcoffee.data.remote.asPlaceEntity
import com.cupofcoffee.data.repository.MeetingRepositoryImpl
import com.cupofcoffee.data.repository.PlaceRepositoryImpl
import com.cupofcoffee.data.repository.UserRepositoryImpl
import com.cupofcoffee.ui.model.MeetingEntry
import com.cupofcoffee.ui.model.MeetingsCategory
import com.cupofcoffee.ui.model.asMeetingEntity
import com.cupofcoffee.ui.model.asUserDTO
import com.cupofcoffee.ui.model.asUserEntity
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
        user.collect { userEntry ->
            userEntry ?: return@collect
            when (category) {
                MeetingsCategory.ATTENDED_MEETINGS -> {
                    val meetings = getMeetingEntries(userEntry.userModel.attendedMeetingIds.keys)
                    updateMeetings(meetings)
                }

                MeetingsCategory.MADE_MEETINGS -> {
                    val meetings = getMeetingEntries(userEntry.userModel.madeMeetingIds.keys)
                    updateMeetings(meetings)
                }
            }
        }
    }

    private suspend fun getMeetingEntries(meetingIds: Set<String>) =
        withContext(Dispatchers.IO) {
            meetingIds.map { id ->
                meetingRepositoryImpl.getLocalMeeting(id)
            }
        }

    private suspend fun updateMeetings(meetingEntries: List<MeetingEntry>) {
        withContext(Dispatchers.Main) {
            _uiState.value = _uiState.value?.copy(
                meetings = meetingEntries
            )
        }
    }

    fun deleteMeeting(meetingEntry: MeetingEntry) {
        viewModelScope.launch(Dispatchers.IO) {
            val placeId = meetingEntry.meetingModel.placeId
            updatePlace(placeId, meetingEntry.id)
            updateUser(meetingEntry.id)
            meetingRepositoryImpl.deleteLocal(meetingEntry.asMeetingEntity())
            meetingRepositoryImpl.deleteRemote(meetingEntry.id)
        }
    }

    private suspend fun updatePlace(placeId: String, meetingId: String) {
        val placeDTO = placeRepositoryImpl.getRemotePlaceById(placeId) ?: return
        placeDTO.meetingIds.remove(meetingId)
        if (placeDTO.meetingIds.isEmpty()) {
            placeRepositoryImpl.deleteLocal(placeDTO.asPlaceEntity(placeId))
            placeRepositoryImpl.deleteRemote(placeId)
        } else {
            placeRepositoryImpl.updateLocal(placeDTO.asPlaceEntity(placeId))
            placeRepositoryImpl.updateRemote(placeId, placeDTO)
        }
    }

    private suspend fun updateUser(meetingId: String) {
        val uid = Firebase.auth.uid!!
        val user = userRepositoryImpl.getLocalUserById(uid)
        user.userModel.madeMeetingIds.remove(meetingId)
        userRepositoryImpl.updateLocal(user.asUserEntity())
        userRepositoryImpl.updateRemote(uid, user.asUserDTO())
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