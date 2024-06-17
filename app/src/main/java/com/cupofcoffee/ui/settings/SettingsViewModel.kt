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
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userRepositoryImpl: UserRepositoryImpl,
    private val meetingRepositoryImpl: MeetingRepositoryImpl,
    private val placeRepositoryImpl: PlaceRepositoryImpl
) : ViewModel() {

    fun deleteUserData(uid: String) {
        viewModelScope.launch {
            val user = userRepositoryImpl.getRemoteUserById(uid)
            user.attendedMeetingIds.map { meetingId ->
                val meeting = meetingRepositoryImpl.getRemoteMeeting(meetingId)
                meeting.peopleId.remove(uid)
            }
            user.madeMeetingIds.map { meetingId ->
                val meeting = meetingRepositoryImpl.getRemoteMeeting(meetingId)
                deleteMeeting(meetingId)
                deleteMadeMeetingsInPlace(meetingId)
            }
            deleteUser(uid)
        }
    }

    private suspend fun cancelMeeting(uid: String, meetingId: String) {
        val meeting = meetingRepositoryImpl.getMeeting(meetingId)
        meeting.personIds.remove(uid)
        meetingRepositoryImpl.update(meetingId, meeting)
    }

    private suspend fun deleteMeeting(meetingId: String) {
        meetingRepositoryImpl.deleteRemote(meetingId)
    }

    private suspend fun deleteMadeMeetingsInPlace(placeId: String, meetingId: String) {
        val place = placeRepositoryImpl.getRemotePlaceById(placeId)!!
        place.meetingIds.remove(meetingId)
        if (place.meetingIds.isEmpty()) placeRepositoryImpl.deleteRemote(placeId)
        else placeRepositoryImpl.updateRemote(placeId, place)
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