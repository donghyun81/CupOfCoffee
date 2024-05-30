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
import com.cupofcoffee.data.repository.MeetingRepositoryImpl
import com.cupofcoffee.data.repository.PlaceRepositoryImpl
import com.cupofcoffee.ui.model.MeetingModel
import com.cupofcoffee.ui.model.PlaceModel
import com.cupofcoffee.ui.model.toMeetingDTO
import kotlinx.coroutines.launch

const val POSITION_COUNT = 10

class SaveMeetingViewModel(
    private val meetingRepositoryImpl: MeetingRepositoryImpl,
    private val placeRepositoryImpl: PlaceRepositoryImpl,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val args = SaveMeetingFragmentArgs.fromSavedStateHandle(savedStateHandle)

    fun saveMeeting(meetingModel: MeetingModel, placeModel: PlaceModel) {
        viewModelScope.launch {
            val meetingId = meetingRepositoryImpl.insert(meetingModel.toMeetingDTO())
            savePlace(meetingId, placeModel = placeModel)
        }
    }

    private suspend fun savePlace(meetingId: String, placeModel: PlaceModel) {
        val placeId = placeModel.run { convertPlaceId(lat, lng) }
        val prvPlaceDTO = placeRepositoryImpl.getPlaceById(placeId)
        if (prvPlaceDTO != null) updatePlace(placeId, meetingId, prvPlaceDTO)
        else createPlace(placeId, meetingId, placeModel)
    }

    private fun convertPlaceId(lat: Double, lng: Double) =
        (lat.toString().take(POSITION_COUNT) + lng.toString().take(POSITION_COUNT)).filter { it != '.' }

    private suspend fun createPlace(placeId: String, meetingId: String, placeModel: PlaceModel) {
        val placeDTO =
            placeModel.run { PlaceDTO(caption, lat, lng, meetingIds.plus(meetingId to true)) }
        placeRepositoryImpl.insert(placeId, placeDTO)
    }

    private suspend fun updatePlace(placeId: String, meetingId: String, prvPlaceDTO: PlaceDTO) {
        val placeDTO = prvPlaceDTO.run { PlaceDTO(caption, lat, lng, meetingIds.plus(meetingId to true)) }
        placeRepositoryImpl.insert(placeId, placeDTO)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                SaveMeetingViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    meetingRepositoryImpl = CupOfCoffeeApplication.meetingRepository,
                    placeRepositoryImpl = CupOfCoffeeApplication.placeRepository
                )
            }
        }
    }
}