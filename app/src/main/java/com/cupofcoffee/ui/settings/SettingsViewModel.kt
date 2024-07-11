package com.cupofcoffee.ui.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cupofcoffee.CupOfCoffeeApplication
import com.cupofcoffee.data.local.model.asPlaceDTO
import com.cupofcoffee.data.local.model.asUserEntry
import com.cupofcoffee.data.repository.MeetingRepositoryImpl
import com.cupofcoffee.data.repository.PlaceRepositoryImpl
import com.cupofcoffee.data.repository.UserRepositoryImpl
import com.cupofcoffee.ui.model.MeetingEntry
import com.cupofcoffee.ui.model.UserEntry
import com.cupofcoffee.ui.model.asUserEntity
import com.cupofcoffee.util.NetworkUtil

class SettingsViewModel(
    private val userRepositoryImpl: UserRepositoryImpl,
    private val meetingRepositoryImpl: MeetingRepositoryImpl,
    private val placeRepositoryImpl: PlaceRepositoryImpl,
    private val networkUtil: NetworkUtil
) : ViewModel() {

    fun isConnected() = networkUtil.isConnected()

    suspend fun deleteUserData(uid: String) {
        val user = userRepositoryImpl.getLocalUserById(uid)
        user.userModel.attendedMeetingIds.keys.map { meetingId ->
            cancelMeeting(uid, meetingId)
        }
        user.userModel.madeMeetingIds.keys.map { meetingId ->
            val meetingEntry =
                meetingRepositoryImpl.getMeeting(meetingId, networkUtil.isConnected())
            deleteMeeting(meetingEntry)
            deleteMadeMeetingsInPlace(meetingEntry.meetingModel.placeId, meetingId)
        }
        deleteUser(user)
    }

    private suspend fun cancelMeeting(uid: String, meetingId: String) {
        val meetingEntry =
            meetingRepositoryImpl.getMeeting(meetingId, networkUtil.isConnected())
        meetingEntry.meetingModel.personIds.remove(uid)
        meetingRepositoryImpl.update(meetingEntry)
    }

    private suspend fun deleteMeeting(meetingEntry: MeetingEntry) {
        meetingRepositoryImpl.delete(meetingEntry)
    }

    private suspend fun deleteMadeMeetingsInPlace(placeId: String, meetingId: String) {
        Log.d("12345", placeId)
        val placeEntry = placeRepositoryImpl.getPlaceById(placeId, isConnected())!!
        placeEntry.placeModel.meetingIds.remove(meetingId)
        if (placeEntry.placeModel.meetingIds.isEmpty()) {
            placeRepositoryImpl.delete(placeEntry)
        } else {
            placeRepositoryImpl.update(placeEntry)
        }
    }

    private suspend fun deleteUser(userEntry: UserEntry) {
        userRepositoryImpl.delete(userEntry)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                SettingsViewModel(
                    userRepositoryImpl = CupOfCoffeeApplication.userRepository,
                    meetingRepositoryImpl = CupOfCoffeeApplication.meetingRepository,
                    placeRepositoryImpl = CupOfCoffeeApplication.placeRepository,
                    networkUtil = CupOfCoffeeApplication.networkUtil
                )
            }
        }
    }
}