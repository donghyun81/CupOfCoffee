package com.cupofcoffee.ui.makemeeting

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
import com.cupofcoffee.data.DataResult.Companion.loading
import com.cupofcoffee.data.DataResult.Companion.success
import com.cupofcoffee.data.repository.MeetingRepositoryImpl
import com.cupofcoffee.data.repository.PlaceRepositoryImpl
import com.cupofcoffee.data.repository.UserRepositoryImpl
import com.cupofcoffee.ui.model.MeetingEntry
import com.cupofcoffee.ui.model.MeetingModel
import com.cupofcoffee.ui.model.PlaceEntry
import com.cupofcoffee.ui.model.PlaceModel
import com.cupofcoffee.ui.model.UserEntry
import com.cupofcoffee.ui.model.asMeetingDTO
import com.cupofcoffee.ui.model.asMeetingEntity
import com.cupofcoffee.util.NetworkUtil
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

const val POSITION_COUNT = 10

class MakeMeetingViewModel(
    savedStateHandle: SavedStateHandle,
    private val meetingRepositoryImpl: MeetingRepositoryImpl,
    private val placeRepositoryImpl: PlaceRepositoryImpl,
    private val userRepositoryImpl: UserRepositoryImpl,
    private val networkUtil: NetworkUtil
) : ViewModel() {

    val args = MakeMeetingFragmentArgs.fromSavedStateHandle(savedStateHandle)

    private val _uiState: MutableLiveData<DataResult<MakeMeetingUiState>> =
        MutableLiveData(loading())
    val uiState: LiveData<DataResult<MakeMeetingUiState>> = _uiState

    init {
        viewModelScope.launch {
            initUiState()
        }
    }

    private suspend fun initUiState() {
        try {
            val isNewMeeting = args.meetingId == null
            if (isNewMeeting) _uiState.postValue(
                success(
                    MakeMeetingUiState(
                        args.placeName!!,
                        args.placePosition!!.latitude,
                        args.placePosition!!.longitude,
                        meetingEntry = null
                    )
                )
            )
            else {
                val meeting = meetingRepositoryImpl.getMeeting(args.meetingId!!)
                _uiState.postValue(
                    success(
                        MakeMeetingUiState(
                            placeName = meeting.meetingModel.caption,
                            lat = meeting.meetingModel.lat,
                            lng = meeting.meetingModel.lng,
                            meetingEntry = meeting
                        )
                    )
                )
            }
        } catch (e: Exception) {
            _uiState.postValue(error(e))
        }
    }

    fun isNetworkConnected() = networkUtil.isConnected()

    suspend fun saveMeeting(meetingModel: MeetingModel, placeModel: PlaceModel) {
        if (args.meetingId == null) {
            val meetingId = meetingRepositoryImpl.insertRemote(meetingModel.asMeetingDTO())
            meetingRepositoryImpl.insertLocal(meetingModel.asMeetingEntity(meetingId))
            savePlace(meetingId, placeModel)
            updateUserMeeting(meetingId)
        } else {
            meetingRepositoryImpl.update(MeetingEntry(args.meetingId!!, meetingModel))
        }
    }

    private suspend fun updateUserMeeting(meetingId: String) {
        val uid = Firebase.auth.uid ?: return
        val userEntry = userRepositoryImpl.getLocalUserById(uid)
        addUserMadeMeeting(userEntry, meetingId)
    }

    private suspend fun addUserMadeMeeting(userEntry: UserEntry, meetingId: String) {
        userEntry.userModel.madeMeetingIds[meetingId] = true
        userRepositoryImpl.update(userEntry)
    }

    private suspend fun savePlace(meetingId: String, placeModel: PlaceModel) {
        val placeId = convertPlaceId(placeModel.lat, placeModel.lng)
        val prvPlaceEntry = placeRepositoryImpl.getPlaceById(placeId)
        if (prvPlaceEntry != null) updatePlace(meetingId, prvPlaceEntry)
        else createPlace(meetingId, PlaceEntry(placeId, placeModel))
    }

    fun convertPlaceId(lat: Double, lng: Double): String {
        return (lat.toString().take(POSITION_COUNT) + lng.toString()
            .take(POSITION_COUNT)).filter { it != '.' }
    }


    private suspend fun createPlace(meetingId: String, placeEntry: PlaceEntry) {
        placeEntry.placeModel.meetingIds[meetingId] = true
        placeRepositoryImpl.insert(placeEntry)
    }

    private suspend fun updatePlace(meetingId: String, prvPlaceEntry: PlaceEntry) {
        prvPlaceEntry.placeModel.meetingIds[meetingId] = true
        placeRepositoryImpl.update(prvPlaceEntry)
    }


    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MakeMeetingViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    meetingRepositoryImpl = CupOfCoffeeApplication.meetingRepository,
                    placeRepositoryImpl = CupOfCoffeeApplication.placeRepository,
                    userRepositoryImpl = CupOfCoffeeApplication.userRepository,
                    networkUtil = CupOfCoffeeApplication.networkUtil
                )
            }
        }
    }
}