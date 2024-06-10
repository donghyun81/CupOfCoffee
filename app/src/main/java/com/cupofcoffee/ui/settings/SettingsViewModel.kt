package com.cupofcoffee.ui.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cupofcoffee.CupOfCoffeeApplication
import com.cupofcoffee.data.repository.MeetingRepositoryImpl
import com.cupofcoffee.data.repository.PlaceRepositoryImpl
import com.cupofcoffee.data.repository.UserRepositoryImpl
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userRepositoryImpl: UserRepositoryImpl,
    private val meetingRepositoryImpl: MeetingRepositoryImpl,
    private val placeRepositoryImpl: PlaceRepositoryImpl
) : ViewModel() {

    fun deleteUserData(uid: String) {
        viewModelScope.launch {
            val user = userRepositoryImpl.getUserById(uid)
            user.attendedMeetingIds.map { meetingId ->
                val meeting = meetingRepositoryImpl.getMeeting(meetingId)
                meeting.peopleId.remove(uid)
            }
            user.madeMeetingIds.map { meetingId ->
                val meeting = meetingRepositoryImpl.getMeeting(meetingId)
                deleteMeeting(meetingId)
                val placeId = meeting.placeId
                deleteMadeMeetingsInPlace(placeId, meetingId)
            }
            deleteUser(uid)
        }
    }

    private suspend fun deleteMeeting(meetingId: String) {
        meetingRepositoryImpl.delete(meetingId)
    }

    private suspend fun deleteMadeMeetingsInPlace(placeId: String, meetingId: String) {
        val place = placeRepositoryImpl.getPlaceById(placeId)!!
        place.meetingIds.remove(meetingId)
        if (place.meetingIds.isEmpty()) placeRepositoryImpl.delete(placeId)
        else placeRepositoryImpl.update(placeId, place)
    }

    private suspend fun deleteUser(uid: String) {
        userRepositoryImpl.delete(uid)
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