package com.cupofcoffee.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cupofcoffee.CupOfCoffeeApplication
import com.cupofcoffee.data.remote.toMeetingEntity
import com.cupofcoffee.data.remote.toPlaceEntity
import com.cupofcoffee.data.repository.MeetingRepositoryImpl
import com.cupofcoffee.data.repository.PlaceRepositoryImpl
import com.cupofcoffee.data.repository.UserRepositoryImpl
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userRepositoryImpl: UserRepositoryImpl,
    private val meetingRepositoryImpl: MeetingRepositoryImpl,
    private val placeRepositoryImpl: PlaceRepositoryImpl
) : ViewModel() {

    fun deleteUserData(uid: String) {
        viewModelScope.launch {
            val user = userRepositoryImpl.getRemoteUserById(uid)
            user.attendedMeetingIds.keys.map { meetingId ->
                cancelMeeting(uid, meetingId)
            }
            user.madeMeetingIds.keys.map { meetingId ->
                val meeting = meetingRepositoryImpl.getRemoteMeeting(meetingId)
                deleteMeeting(meetingId)
                deleteMadeMeetingsInPlace(meeting.placeId, meetingId)
            }
            deleteUser(uid)
        }
    }

    private suspend fun cancelMeeting(uid: String, meetingId: String) {
        val meetingDTO = meetingRepositoryImpl.getRemoteMeeting(meetingId)
        meetingDTO.personIds.remove(uid)
        meetingRepositoryImpl.updateRemote(meetingId, meetingDTO)
        meetingRepositoryImpl.updateLocal(meetingDTO.toMeetingEntity(meetingId))
    }

    private suspend fun deleteMeeting(meetingId: String) {
        meetingRepositoryImpl.deleteRemote(meetingId)
    }

    private suspend fun deleteMadeMeetingsInPlace(placeId: String, meetingId: String) {
        val place = placeRepositoryImpl.getRemotePlaceById(placeId)!!
        place.meetingIds.remove(meetingId)
        if (place.meetingIds.isEmpty()) {
            placeRepositoryImpl.deleteLocal(place.toPlaceEntity(placeId))
            placeRepositoryImpl.deleteRemote(placeId)
        } else {
            placeRepositoryImpl.updateLocal(place.toPlaceEntity(placeId))
            placeRepositoryImpl.updateRemote(placeId, place)
        }
    }

    private suspend fun deleteUser(uid: String) {
        userRepositoryImpl.deleteRemote(uid)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                SettingsViewModel(
                    userRepositoryImpl = CupOfCoffeeApplication.userRepository,
                    meetingRepositoryImpl = CupOfCoffeeApplication.meetingRepository,
                    placeRepositoryImpl = CupOfCoffeeApplication.placeRepository
                )
            }
        }
    }
}