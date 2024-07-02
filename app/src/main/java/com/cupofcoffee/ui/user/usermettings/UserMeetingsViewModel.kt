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
import com.cupofcoffee.data.DataResult
import com.cupofcoffee.data.DataResult.Companion.error
import com.cupofcoffee.data.DataResult.Companion.success
import com.cupofcoffee.data.local.model.MeetingEntity
import com.cupofcoffee.data.local.model.asMeetingEntry
import com.cupofcoffee.data.local.model.asUserDTO
import com.cupofcoffee.data.local.model.asUserEntry
import com.cupofcoffee.data.remote.model.asPlaceEntity
import com.cupofcoffee.data.repository.MeetingRepositoryImpl
import com.cupofcoffee.data.repository.PlaceRepositoryImpl
import com.cupofcoffee.data.repository.UserRepositoryImpl
import com.cupofcoffee.ui.model.MeetingEntry
import com.cupofcoffee.ui.model.MeetingsCategory
import com.cupofcoffee.ui.model.asMeetingEntity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch


private const val CATEGORY_TAG = "category"

class UserMeetingsViewModel(
    savedStateHandle: SavedStateHandle,
    private val userRepositoryImpl: UserRepositoryImpl,
    private val meetingRepositoryImpl: MeetingRepositoryImpl,
    private val placeRepositoryImpl: PlaceRepositoryImpl
) : ViewModel() {

    private val category = savedStateHandle.get<MeetingsCategory>(CATEGORY_TAG)!!

    private val _uiState: MutableLiveData<DataResult<UserMeetingsUiState>> =
        MutableLiveData()
    val uiState: LiveData<DataResult<UserMeetingsUiState>> = _uiState

    init {
        viewModelScope.launch {
            setMeetings()
        }
    }

    private suspend fun setMeetings() {
        val uid = Firebase.auth.uid ?: return
        val user = userRepositoryImpl.getLocalUserByIdInFlow(id = uid)
        user.collect { userEntity ->
            val userEntry = userEntity?.asUserEntry() ?: return@collect
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
        meetingRepositoryImpl.getLocalMeetingsByIds(meetingIds.toList())

    private fun updateMeetings(meetingEntities: List<MeetingEntity>) {
        try {
            val meetingEntries = meetingEntities.map { it.asMeetingEntry() }
            _uiState.value = success(UserMeetingsUiState(meetingEntries))
        } catch (e: Exception) {
            _uiState.value = error(e)
        }
    }

    fun deleteMeeting(meetingEntry: MeetingEntry) {
        viewModelScope.launch {
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
        user.madeMeetingIds.remove(meetingId)
        userRepositoryImpl.updateLocal(user)
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