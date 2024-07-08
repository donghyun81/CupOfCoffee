package com.cupofcoffee.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cupofcoffee.CupOfCoffeeApplication
import com.cupofcoffee.data.local.model.MeetingEntity
import com.cupofcoffee.data.local.model.asPlaceDTO
import com.cupofcoffee.data.local.model.asUserEntry
import com.cupofcoffee.data.repository.MeetingRepositoryImpl
import com.cupofcoffee.data.repository.PlaceRepositoryImpl
import com.cupofcoffee.data.repository.UserRepositoryImpl
import com.cupofcoffee.ui.model.MeetingEntry
import com.cupofcoffee.ui.model.UserEntry
import com.cupofcoffee.ui.model.asMeetingDTO
import com.cupofcoffee.ui.model.asMeetingEntity
import com.cupofcoffee.ui.model.asUserEntity
import com.cupofcoffee.util.NetworkUtil

class SettingsViewModel(
    private val userRepositoryImpl: UserRepositoryImpl,
    private val meetingRepositoryImpl: MeetingRepositoryImpl,
    private val placeRepositoryImpl: PlaceRepositoryImpl,
    private val networkUtil: NetworkUtil
) : ViewModel() {

    suspend fun deleteUserData(uid: String) {
        val user = userRepositoryImpl.getLocalUserById(uid).asUserEntry()
        user.userModel.attendedMeetingIds.keys.map { meetingId ->
            cancelMeeting(uid, meetingId)
        }
        user.userModel.madeMeetingIds.keys.map { meetingId ->
            val meetingEntry =
                meetingRepositoryImpl.getMeeting(meetingId, networkUtil.isConnected())
            deleteMeeting(meetingEntry)
            deleteMadeMeetingsInPlace(meetingEntry.id, meetingId)
        }
        deleteUser(user)
    }

    private suspend fun cancelMeeting(uid: String, meetingId: String) {
        val meetingEntry =
            meetingRepositoryImpl.getMeeting(meetingId, networkUtil.isConnected())
        meetingEntry.meetingModel.personIds.remove(uid)
        meetingRepositoryImpl.updateRemote(meetingId, meetingEntry.asMeetingDTO())
        meetingRepositoryImpl.updateLocal(meetingEntry.asMeetingEntity())
    }

    private suspend fun deleteMeeting(meetingEntry: MeetingEntry) {
        meetingRepositoryImpl.deleteLocal(meetingEntry.asMeetingEntity())
        meetingRepositoryImpl.deleteRemote(meetingEntry.id)
    }

    private suspend fun deleteMadeMeetingsInPlace(placeId: String, meetingId: String) {
        val placeEntity = placeRepositoryImpl.getLocalPlaceById(placeId)
        placeEntity.meetingIds.remove(meetingId)
        if (placeEntity.meetingIds.isEmpty()) {
            placeRepositoryImpl.deleteLocal(placeEntity)
            placeRepositoryImpl.deleteRemote(placeId)
        } else {
            placeRepositoryImpl.updateLocal(placeEntity)
            placeRepositoryImpl.updateRemote(placeId, placeEntity.asPlaceDTO())
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
                    placeRepositoryImpl = CupOfCoffeeApplication.placeRepository,
                    networkUtil = CupOfCoffeeApplication.networkUtil
                )
            }
        }
    }
}