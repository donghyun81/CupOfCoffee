package com.example.makemeeting

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
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
    val snackBarMessage: String = MAKE_NETWORK_MESSAGE,
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

    private val _meetingData = mutableStateOf(MeetingData())
    val meetingData: State<MeetingData> = _meetingData

    private val _placeData = mutableStateOf(PlaceData())
    val placeData: State<PlaceData> = _placeData

    private val _sideEffect = MutableSharedFlow<MakeMeetingSideEffect>(replay = 1)
    val sideEffect: SharedFlow<MakeMeetingSideEffect> = _sideEffect.asSharedFlow()

    fun handleIntent(intent: MakeMeetingIntent) {
        when (intent) {
            MakeMeetingIntent.InitData -> {
                viewModelScope.launch {
                    val meetingId = args.meetingId
                    if (meetingId == null) initMeeting()
                    else loadMeeting(meetingId)
                    initPlace()
                    _uiState.value = uiState.value.copy(isLoading = false)
                }
            }

            is MakeMeetingIntent.MakeMeeting -> {
                _uiState.value = uiState.value.copy(isSaveButtonEnable = false)
                if (isNetworkConnected()) {
                    viewModelScope.launch {
                        saveMeeting()
                        _sideEffect.tryEmit(MakeMeetingSideEffect.NavigateUp)
                    }
                } else {
                    _sideEffect.tryEmit(MakeMeetingSideEffect.ShowSnackBar(uiState.value.snackBarMessage))
                    _uiState.value = uiState.value.copy(isSaveButtonEnable = true)
                }
            }

            is MakeMeetingIntent.EnterContent -> updateContent(intent.content)
            is MakeMeetingIntent.EditDate -> updateDate(intent.date)
            is MakeMeetingIntent.EditTime -> updateTime(intent.time)
            is MakeMeetingIntent.ShowSnackBar -> _sideEffect.tryEmit(
                MakeMeetingSideEffect.ShowSnackBar(
                    intent.message
                )
            )
        }
    }

    private fun initMeeting() {
        val calendar = Calendar.getInstance()
        val uid = Firebase.auth.uid!!
        _meetingData.value =
            MeetingData(
                placeName = args.placeName ?: "",
                managerId = uid,
                personIds = mutableMapOf(uid to true),
                placeId = convertPlaceId(args.lat!!.toDouble(), args.lng!!.toDouble()),
                date = calendar.toCurrentDate(),
                time = calendar.toCurrentTime(),
            )
    }

    private suspend fun loadMeeting(meetingId: String) {
        try {
            val meeting = meetingRepository.getMeeting(meetingId, isNetworkConnected())
            _meetingData.value = meeting.asMeetingData()
        } catch (e: Exception) {
            _uiState.value = MakeMeetingUiState(isError = true)
        }
    }

    private fun initPlace() {
        _placeData.value =
            PlaceData(
                placeName = args.placeName!!,
                lat = args.lat!!.toDouble(),
                lng = args.lng!!.toDouble()
            )
    }

    private fun updateTime(time: String) {
        _meetingData.value = meetingData.value.copy(time = time)
    }

    private fun updateDate(date: String) {
        _meetingData.value = meetingData.value.copy(date = date)
    }

    private fun updateContent(content: String) {
        _meetingData.value = meetingData.value.copy(content = content)
    }

    fun isNetworkConnected() = networkUtil.isConnected()

    private suspend fun saveMeeting() {
        val curMeeting = _meetingData.value.copy(
            createDate = Date().time
        )
        val meetingId = args.meetingId
        if (meetingId == null) {
            val newMeetingId = meetingRepository.insertRemote(curMeeting.asMeetingDTO()).id
            meetingRepository.insertLocal(curMeeting.asMeetingEntity(newMeetingId))
            savePlace(newMeetingId, placeData.value)
            updateUserMeeting(newMeetingId)
        } else meetingRepository.update(curMeeting.asMeeting(meetingId))
    }

    private suspend fun updateUserMeeting(meetingId: String) {
        val uid = Firebase.auth.uid ?: return
        val userEntry = userRepository.getLocalUserById(uid)!!
        addUserMadeMeeting(userEntry, meetingId)
    }

    private suspend fun addUserMadeMeeting(user: User, meetingId: String) {
        user.madeMeetingIds[meetingId] = true
        userRepository.update(user)
    }

    private suspend fun savePlace(meetingId: String, placeData: PlaceData) {
        val placeId = convertPlaceId(placeData.lat, placeData.lng)
        val prvPlaceEntry = placeRepository.getPlaceById(placeId)
        if (prvPlaceEntry != null) updatePlace(meetingId, prvPlaceEntry)
        else createPlace(meetingId, placeData.asPlace(placeId))
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
}