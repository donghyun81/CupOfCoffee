package com.example.makemeeting

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.common.toCurrentDate
import com.example.common.toCurrentTime
import com.example.common.util.NetworkUtil
import com.example.data.model.MeetingData
import com.example.data.model.Place
import com.example.data.model.PlaceData
import com.example.data.model.User
import com.example.data.model.asMeeting
import com.example.data.model.asMeetingDTO
import com.example.data.model.asMeetingData
import com.example.data.model.asMeetingEntity
import com.example.data.model.asPlace
import com.example.data.repository.MeetingRepository
import com.example.data.repository.PlaceRepository
import com.example.data.repository.UserRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

const val POSITION_COUNT = 10
const val MAKE_NETWORK_MESSAGE = "모임을 생성을 위해서 네트워크 연결이 필요합니다!"


data class MakeMeetingUiState(
    val isError: Boolean = false,
    val isLoading: Boolean = false,
    val meetingData: MeetingData = MeetingData(),
    val placeData: PlaceData = PlaceData(),
    val isSaveButtonEnable: Boolean = true
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

    private val _uiState: MutableStateFlow<MakeMeetingUiState> =
        MutableStateFlow(MakeMeetingUiState(isLoading = true))
    val uiState: StateFlow<MakeMeetingUiState> = _uiState.asStateFlow()

    private val _sideEffect = MutableSharedFlow<MakeMeetingSideEffect>(replay = 1)
    val sideEffect: SharedFlow<MakeMeetingSideEffect> = _sideEffect.asSharedFlow()

    fun handleIntent(intent: MakeMeetingIntent) {
        when (intent) {
            MakeMeetingIntent.InitData -> {
                viewModelScope.launch {
                    handleInitData()
                    _uiState.value = uiState.value.copy(isLoading = false)
                }
            }

            is MakeMeetingIntent.MakeMeeting -> {
                _uiState.value = uiState.value.copy(isSaveButtonEnable = false)
                handleMakeMeeting()
            }
            is MakeMeetingIntent.EnterContent -> updateContent(intent.content)
            is MakeMeetingIntent.EditDate -> updateDate(intent.date)
            is MakeMeetingIntent.EditTime -> updateTime(intent.time)
        }
    }

    private suspend fun handleInitData() {
        val meetingId = args.meetingId
        if (meetingId == null) initMeeting() else loadMeeting(meetingId)
        initPlace()
    }

    private fun handleMakeMeeting() {
        if (networkUtil.isConnected()) {
            viewModelScope.launch {
                saveMeeting()
                _sideEffect.tryEmit(MakeMeetingSideEffect.NavigateUp)
            }
        } else {
            showSnackBar(MAKE_NETWORK_MESSAGE)
            _uiState.value = uiState.value.copy(isSaveButtonEnable = true)
        }
    }

    private fun initMeeting() {
        val calendar = Calendar.getInstance()
        val uid = Firebase.auth.uid ?: return
        _uiState.value = uiState.value.copy(
            meetingData = MeetingData(
                placeName = args.placeName ?: "",
                managerId = uid,
                personIds = mutableMapOf(uid to true),
                placeId = convertPlaceId(args.lat!!.toDouble(), args.lng!!.toDouble()),
                date = calendar.toCurrentDate(),
                time = calendar.toCurrentTime(),
            )
        )
    }

    private suspend fun loadMeeting(meetingId: String) {
        try {
            val meeting = meetingRepository.getMeeting(meetingId, networkUtil.isConnected())
            _uiState.value = uiState.value.copy(meetingData = meeting.asMeetingData())
        } catch (e: Exception) {
            _uiState.value = uiState.value.copy(isError = true, isLoading = false)
        }
    }

    private fun initPlace() {
        _uiState.value = uiState.value.copy(
            placeData = PlaceData(
                placeName = args.placeName ?: "",
                lat = args.lat?.toDouble() ?: 0.0,
                lng = args.lng?.toDouble() ?: 0.0
            )
        )
    }

    private fun updateTime(time: String) {
        updateMeetingData { it.copy(time = time) }
    }

    private fun updateDate(date: String) {
        updateMeetingData { it.copy(date = date) }
    }

    private fun updateContent(content: String) {
        updateMeetingData { it.copy(content = content) }
    }

    private fun updateMeetingData(update: (MeetingData) -> MeetingData) {
        val curMeeting = update(uiState.value.meetingData)
        _uiState.value = uiState.value.copy(meetingData = curMeeting)
    }

    private suspend fun saveMeeting() {
        val curMeeting = _uiState.value.meetingData.copy(createDate = Date().time)
        val meetingId = args.meetingId
        if (meetingId == null) {
            saveNewMeeting(curMeeting)
        } else {
            meetingRepository.update(curMeeting.asMeeting(meetingId))
        }
    }

    private suspend fun saveNewMeeting(meeting: MeetingData) {
        val newMeetingId = meetingRepository.insertRemote(meeting.asMeetingDTO()).id
        meetingRepository.insertLocal(meeting.asMeetingEntity(newMeetingId))
        savePlace(newMeetingId, _uiState.value.placeData)
        updateUserMeeting(newMeetingId)
    }

    private suspend fun updateUserMeeting(meetingId: String) {
        val uid = Firebase.auth.uid ?: return
        userRepository.getLocalUserById(uid)?.let { user ->
            addUserMadeMeeting(user, meetingId)
        }
    }

    private suspend fun addUserMadeMeeting(user: User, meetingId: String) {
        user.madeMeetingIds[meetingId] = true
        userRepository.update(user)
    }

    private suspend fun savePlace(meetingId: String, placeData: PlaceData) {
        val placeId = convertPlaceId(placeData.lat, placeData.lng)
        val prvPlaceEntry = placeRepository.getPlaceById(placeId)
        if (prvPlaceEntry != null) {
            updatePlace(meetingId, prvPlaceEntry)
        } else {
            createPlace(meetingId, placeData.asPlace(placeId))
        }
    }

    private fun convertPlaceId(lat: Double, lng: Double): String {
        return (lat.toString().take(POSITION_COUNT) + lng.toString()
            .take(POSITION_COUNT)).filter { it != '.' }
    }

    private suspend fun createPlace(meetingId: String, place: Place) {
        place.meetingIds[meetingId] = true
        placeRepository.insert(place)
    }

    private suspend fun updatePlace(meetingId: String, prvPlace: Place) {
        prvPlace.meetingIds[meetingId] = true
        placeRepository.update(prvPlace)
    }

    private fun showSnackBar(message: String) {
        _sideEffect.tryEmit(MakeMeetingSideEffect.ShowSnackBar(message))
    }
}