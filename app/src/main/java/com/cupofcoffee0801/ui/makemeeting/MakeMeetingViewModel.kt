package com.cupofcoffee0801.ui.makemeeting

import android.net.ConnectivityManager
import android.net.Network
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cupofcoffee0801.data.DataResult
import com.cupofcoffee0801.data.DataResult.Companion.error
import com.cupofcoffee0801.data.DataResult.Companion.loading
import com.cupofcoffee0801.data.DataResult.Companion.success
import com.cupofcoffee0801.data.repository.MeetingRepository
import com.cupofcoffee0801.data.repository.PlaceRepository
import com.cupofcoffee0801.data.repository.UserRepository
import com.cupofcoffee0801.ui.model.MeetingEntry
import com.cupofcoffee0801.ui.model.MeetingModel
import com.cupofcoffee0801.ui.model.PlaceEntry
import com.cupofcoffee0801.ui.model.PlaceModel
import com.cupofcoffee0801.ui.model.UserEntry
import com.cupofcoffee0801.ui.model.asMeetingDTO
import com.cupofcoffee0801.ui.model.asMeetingEntity
import com.cupofcoffee0801.util.NetworkUtil
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

const val POSITION_COUNT = 10

@HiltViewModel
class MakeMeetingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val meetingRepository: MeetingRepository,
    private val placeRepository: PlaceRepository,
    private val userRepository: UserRepository,
    private val networkUtil: NetworkUtil
) : ViewModel() {

    val args = MakeMeetingFragmentArgs.fromSavedStateHandle(savedStateHandle)

    private val _uiState: MutableLiveData<DataResult<MakeMeetingUiState>> =
        MutableLiveData(loading())
    val uiState: LiveData<DataResult<MakeMeetingUiState>> get() = _uiState

    private val _isButtonClicked: MutableLiveData<Boolean> = MutableLiveData(false)
    val isButtonClicked: LiveData<Boolean> get() = _isButtonClicked


    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            initUiState()
        }

        override fun onLost(network: Network) {
            initUiState()
        }
    }

    init {
        networkUtil.registerNetworkCallback(networkCallback)
    }

    fun onButtonClicked() {
        _isButtonClicked.value = true
    }

    private fun initUiState() {
        viewModelScope.launch {
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
                    val meeting = meetingRepository.getMeeting(args.meetingId!!)
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

    }

    fun isNetworkConnected() = networkUtil.isConnected()

    suspend fun saveMeeting(meetingModel: MeetingModel, placeModel: PlaceModel) {
        if (args.meetingId == null) {
            val meetingId = meetingRepository.insertRemote(meetingModel.asMeetingDTO())
            meetingRepository.insertLocal(meetingModel.asMeetingEntity(meetingId))
            savePlace(meetingId, placeModel)
            updateUserMeeting(meetingId)
        } else {
            meetingRepository.update(MeetingEntry(args.meetingId!!, meetingModel))
        }
    }

    private suspend fun updateUserMeeting(meetingId: String) {
        val uid = Firebase.auth.uid ?: return
        val userEntry = userRepository.getLocalUserById(uid)!!
        addUserMadeMeeting(userEntry, meetingId)
    }

    private suspend fun addUserMadeMeeting(userEntry: UserEntry, meetingId: String) {
        userEntry.userModel.madeMeetingIds[meetingId] = true
        userRepository.update(userEntry)
    }

    private suspend fun savePlace(meetingId: String, placeModel: PlaceModel) {
        val placeId = convertPlaceId(placeModel.lat, placeModel.lng)
        val prvPlaceEntry = placeRepository.getPlaceById(placeId)
        if (prvPlaceEntry != null) updatePlace(meetingId, prvPlaceEntry)
        else createPlace(meetingId, PlaceEntry(placeId, placeModel))
    }

    fun convertPlaceId(lat: Double, lng: Double): String {
        return (lat.toString().take(POSITION_COUNT) + lng.toString()
            .take(POSITION_COUNT)).filter { it != '.' }
    }


    private suspend fun createPlace(meetingId: String, placeEntry: PlaceEntry) {
        placeEntry.placeModel.meetingIds[meetingId] = true
        placeRepository.insert(placeEntry)
    }

    private suspend fun updatePlace(meetingId: String, prvPlaceEntry: PlaceEntry) {
        prvPlaceEntry.placeModel.meetingIds[meetingId] = true
        placeRepository.update(prvPlaceEntry)
    }
}