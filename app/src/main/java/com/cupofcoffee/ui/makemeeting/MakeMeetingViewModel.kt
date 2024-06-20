package com.cupofcoffee.ui.makemeeting

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cupofcoffee.CupOfCoffeeApplication
import com.cupofcoffee.data.local.toUserEntry
import com.cupofcoffee.data.remote.toPlaceEntry
import com.cupofcoffee.data.repository.MeetingRepositoryImpl
import com.cupofcoffee.data.repository.PlaceRepositoryImpl
import com.cupofcoffee.data.repository.UserRepositoryImpl
import com.cupofcoffee.ui.model.MeetingModel
import com.cupofcoffee.ui.model.PlaceModel
import com.cupofcoffee.ui.model.UserEntry
import com.cupofcoffee.ui.model.toMeetingDTO
import com.cupofcoffee.ui.model.toMeetingEntity
import com.cupofcoffee.ui.model.toPlaceDTO
import com.cupofcoffee.ui.model.toPlaceEntity
import com.cupofcoffee.ui.model.toUserDTO
import com.cupofcoffee.ui.model.toUserEntity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

const val POSITION_COUNT = 10

class MakeMeetingViewModel(
    private val meetingRepositoryImpl: MeetingRepositoryImpl,
    private val placeRepositoryImpl: PlaceRepositoryImpl,
    private val userRepositoryImpl: UserRepositoryImpl,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val args = MakeMeetingFragmentArgs.fromSavedStateHandle(savedStateHandle)

    fun saveMeeting(meetingModel: MeetingModel, placeModel: PlaceModel) {
        viewModelScope.launch(Dispatchers.IO) {
            val meetingId = meetingRepositoryImpl.insertRemote(meetingModel.toMeetingDTO())
            meetingRepositoryImpl.insertLocal(meetingModel.toMeetingEntity(meetingId))
            savePlace(meetingId, placeModel)
            updateUserMeeting(meetingId)
        }
    }

    private suspend fun updateUserMeeting(meetingId: String) {
        val uid = Firebase.auth.uid ?: return
        val userFlow = userRepositoryImpl.getLocalUserByIdInFlow(uid)
        userFlow.collect { userEntity ->
            val user = userEntity.toUserEntry()
            addUserMadeMeeting(user, meetingId)
        }
    }

    private suspend fun addUserMadeMeeting(userEntry: UserEntry, meetingId: String) {
        userEntry.userModel.madeMeetingIds[meetingId] = true
        val uid = userEntry.id
        userRepositoryImpl.updateLocal(userEntry.userModel.toUserEntity(uid))
        userRepositoryImpl.updateRemote(uid, userDTO = userEntry.userModel.toUserDTO())
    }

    private suspend fun savePlace(meetingId: String, placeModel: PlaceModel) {
        val placeId = convertPlaceId()
        val prvPlaceEntry = placeRepositoryImpl.getRemotePlaceById(placeId)?.toPlaceEntry(placeId)
        if (prvPlaceEntry != null) updatePlace(placeId, meetingId, prvPlaceEntry.placeModel)
        else createPlace(placeId, meetingId, placeModel)
    }

    fun convertPlaceId(): String {
        val lat = args.placePosition.latitude
        val lng = args.placePosition.longitude
        return (lat.toString().take(POSITION_COUNT) + lng.toString()
            .take(POSITION_COUNT)).filter { it != '.' }
    }


    private suspend fun createPlace(placeId: String, meetingId: String, placeModel: PlaceModel) {
        placeModel.meetingIds[meetingId] = true
        placeRepositoryImpl.insertLocal(placeModel.toPlaceEntity(placeId))
        placeRepositoryImpl.insertRemote(placeId, placeModel.toPlaceDTO())
    }

    private suspend fun updatePlace(placeId: String, meetingId: String, prvPlaceModel: PlaceModel) {
        prvPlaceModel.meetingIds[meetingId] = true
        placeRepositoryImpl.updateLocal(prvPlaceModel.toPlaceEntity(placeId))
        placeRepositoryImpl.updateRemote(placeId, prvPlaceModel.toPlaceDTO())
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MakeMeetingViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    meetingRepositoryImpl = CupOfCoffeeApplication.meetingRepository,
                    placeRepositoryImpl = CupOfCoffeeApplication.placeRepository,
                    userRepositoryImpl = CupOfCoffeeApplication.userRepository
                )
            }
        }
    }
}