package com.cupofcoffee.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cupofcoffee.CupOfCoffeeApplication
import com.cupofcoffee.data.repository.MeetingRepositoryImpl
import com.cupofcoffee.data.repository.PlaceRepositoryImpl
import com.cupofcoffee.data.repository.UserRepositoryImpl
import com.cupofcoffee.ui.model.MeetingEntry
import com.cupofcoffee.ui.model.UserEntry
import com.cupofcoffee.ui.model.asMeetingDTO
import com.cupofcoffee.ui.model.asMeetingEntity
import com.cupofcoffee.ui.model.asPlaceDTO
import com.cupofcoffee.ui.model.asPlaceEntity
import com.cupofcoffee.ui.model.asUserEntity
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userRepositoryImpl: UserRepositoryImpl,
    private val meetingRepositoryImpl: MeetingRepositoryImpl,
    private val placeRepositoryImpl: PlaceRepositoryImpl
) : ViewModel() {

    fun deleteUserData(uid: String) {
        viewModelScope.launch {
            val user = userRepositoryImpl.getLocalUserById(uid)
            user.userModel.attendedMeetingIds.keys.map { meetingId ->
                cancelMeeting(uid, meetingId)
            }
            user.userModel.madeMeetingIds.keys.map { meetingId ->
                val meetingEntry = meetingRepositoryImpl.getLocalMeeting(meetingId)
                deleteMeeting(meetingEntry)
                deleteMadeMeetingsInPlace(meetingEntry.meetingModel.placeId, meetingId)
            }
            deleteUser(user)
        }
    }

    private suspend fun cancelMeeting(uid: String, meetingId: String) {
        val meetingEntry = meetingRepositoryImpl.getLocalMeeting(meetingId)
        meetingEntry.meetingModel.personIds.remove(uid)
        meetingRepositoryImpl.updateRemote(meetingId, meetingEntry.asMeetingDTO())
        meetingRepositoryImpl.updateLocal(meetingEntry.asMeetingEntity())
    }

    private suspend fun deleteMeeting(meetingEntry: MeetingEntry) {
        meetingRepositoryImpl.deleteLocal(meetingEntry.asMeetingEntity())
        meetingRepositoryImpl.deleteRemote(meetingEntry.id)
    }

    private suspend fun deleteMadeMeetingsInPlace(placeId: String, meetingId: String) {
        val place = placeRepositoryImpl.getLocalPlaceById(placeId).placeModel
        place.meetingIds.remove(meetingId)
        if (place.meetingIds.isEmpty()) {
            placeRepositoryImpl.deleteLocal(place.asPlaceEntity(placeId))
            placeRepositoryImpl.deleteRemote(placeId)
        } else {
            placeRepositoryImpl.updateLocal(place.asPlaceEntity(placeId))
            placeRepositoryImpl.updateRemote(placeId, place.asPlaceDTO())
        }
    }

    private suspend fun deleteUser(userEntry: UserEntry) {
        userRepositoryImpl.deleteLocal(userEntry.asUserEntity())
        userRepositoryImpl.deleteRemote(userEntry.id)
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