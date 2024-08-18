package com.cupofcoffee0801.ui.makemeeting

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import com.cupofcoffee0801.ui.toCurrentDate
import com.cupofcoffee0801.ui.toCurrentTime
import com.cupofcoffee0801.util.NetworkUtil
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

const val POSITION_COUNT = 10

data class MakeMeetingUiState(
    val placeName: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val content: String = "",
    val date: String = "",
    val time: String = "",
    val isError: Boolean = false,
    val isComplete: Boolean = false
)

@HiltViewModel
class MakeMeetingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val meetingRepository: MeetingRepository,
    private val placeRepository: PlaceRepository,
    private val userRepository: UserRepository,
    private val networkUtil: NetworkUtil
) : ViewModel() {

    private val args = MakeMeetingFragmentArgs.fromSavedStateHandle(savedStateHandle)

    private val _isButtonClicked: MutableLiveData<Boolean> = MutableLiveData(false)
    val isButtonClicked: LiveData<Boolean> get() = _isButtonClicked

    private val _uiState: MutableLiveData<MakeMeetingUiState> =
        MutableLiveData(MakeMeetingUiState())

    val uiState: LiveData<MakeMeetingUiState> = _uiState

    init {
        initUiState()
    }

    private fun initUiState() {
        viewModelScope.launch {
            val meetingId = args.meetingId
            if (meetingId == null) createMeeting()
            else loadMeeting(meetingId)
        }
    }

    private fun createMeeting() {
        val calendar = Calendar.getInstance()
        _uiState.postValue(
            MakeMeetingUiState(
                args.placeName!!,
                args.placePosition!!.latitude,
                args.placePosition!!.longitude,
                date = calendar.toCurrentDate(),
                time = calendar.toCurrentTime()
            )
        )
    }

    private suspend fun loadMeeting(meetingId: String) {
        try {
            val meeting = meetingRepository.getMeeting(meetingId).meetingModel
            _uiState.postValue(
                MakeMeetingUiState(
                    placeName = meeting.caption,
                    lat = meeting.lat,
                    lng = meeting.lng,
                    content = meeting.content,
                    date = meeting.date,
                    time = meeting.time
                )
            )
        } catch (e: Exception) {
            _uiState.postValue(
                MakeMeetingUiState(
                    isError = true
                )
            )
        }
    }

    fun updateContent(content: String) {
        _uiState.postValue(
            uiState.value?.copy(content = content)
        )
    }

    fun updateDate(date: String) {
        _uiState.postValue(
            uiState.value?.copy(date = date)
        )
    }

    fun updateTime(time: String) {
        _uiState.postValue(
            uiState.value?.copy(time = time)
        )
    }

    fun onButtonClicked() {
        _isButtonClicked.value = true
    }

    fun isNetworkConnected() = networkUtil.isConnected()

    fun saveMeeting(uiState: MakeMeetingUiState) {
        val meeting = getMeeting(uiState)
        val place = getPlace(uiState)
        viewModelScope.launch {
            if (args.meetingId == null) {
                val meetingId = meetingRepository.insertRemote(meeting.asMeetingDTO()).id
                meetingRepository.insertLocal(meeting.asMeetingEntity(meetingId))
                savePlace(meetingId, place)
                updateUserMeeting(meetingId)
            } else meetingRepository.update(MeetingEntry(args.meetingId!!, meeting))
            _uiState.postValue(
                uiState.copy(isComplete = true)
            )
        }
    }

    fun getMeeting(uiState: MakeMeetingUiState): MeetingModel {
        val uid = Firebase.auth.uid!!
        return MeetingModel(
            caption = uiState.placeName,
            lat = uiState.lat,
            lng = uiState.lng,
            managerId = uid,
            personIds = mutableMapOf(uid to true),
            placeId = convertPlaceId(
                uiState.lat,
                uiState.lng
            ),
            date = uiState.date,
            time = uiState.time,
            createDate = Date().time,
            content = uiState.content
        )
    }

    private fun getPlace(uiState: MakeMeetingUiState) = PlaceModel(
        caption = uiState.placeName,
        lat = uiState.lat,
        lng = uiState.lng
    )

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