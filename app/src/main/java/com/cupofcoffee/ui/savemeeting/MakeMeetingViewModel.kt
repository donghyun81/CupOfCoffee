package com.cupofcoffee.ui.savemeeting

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cupofcoffee.CupOfCoffeeApplication
import com.cupofcoffee.data.remote.PlaceDTO
import com.cupofcoffee.data.remote.toUserEntry
import com.cupofcoffee.data.repository.MeetingRepositoryImpl
import com.cupofcoffee.data.repository.PlaceRepositoryImpl
import com.cupofcoffee.data.repository.UserRepositoryImpl
import com.cupofcoffee.ui.model.MeetingModel
import com.cupofcoffee.ui.model.PlaceModel
import com.cupofcoffee.ui.model.UserEntry
import com.cupofcoffee.ui.model.toMeetingDTO
import com.cupofcoffee.ui.model.toPlaceDTO
import com.cupofcoffee.ui.model.toUserDTO
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
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
        viewModelScope.launch {
            val meetingId = meetingRepositoryImpl.insert(meetingModel.toMeetingDTO())
            savePlace(meetingId, placeModel = placeModel)
            updateUserMeeting(meetingId)
        }
    }

    private suspend fun updateUserMeeting(meetingId: String) {
        val uid = Firebase.auth.uid ?: return
        userRepositoryImpl.getUserByIdInFlow(uid).collect { userDTO ->
            val user = userDTO.toUserEntry(uid)
            addUserMadeMeeting(user, meetingId)
            addUserAttendedMeeting(user, meetingId)
        }
    }

    private suspend fun addUserMadeMeeting(userEntry: UserEntry, meetingId: String) {
        userEntry.userModel.madeMeetingIds.add(meetingId)
        userRepositoryImpl.update(userEntry.id, userDTO = userEntry.userModel.toUserDTO())
    }

    private suspend fun addUserAttendedMeeting(userEntry: UserEntry, meetingId: String) {
        userEntry.userModel.attendedMeetingIds.add(meetingId)
        userRepositoryImpl.update(userEntry.id, userDTO = userEntry.userModel.toUserDTO())
    }

    private suspend fun savePlace(meetingId: String, placeModel: PlaceModel) {
        val placeId = convertPlaceId()
        val prvPlaceDTO = placeRepositoryImpl.getPlaceById(placeId)
        if (prvPlaceDTO != null) updatePlace(placeId, meetingId, prvPlaceDTO)
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
        placeRepositoryImpl.insert(placeId, placeModel.toPlaceDTO())
    }

    private suspend fun updatePlace(placeId: String, meetingId: String, prvPlaceDTO: PlaceDTO) {
        prvPlaceDTO.meetingIds[meetingId] = true
        placeRepositoryImpl.update(placeId, prvPlaceDTO)
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