package com.cupofcoffee0801.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cupofcoffee0801.data.DataResult
import com.cupofcoffee0801.data.DataResult.Companion.error
import com.cupofcoffee0801.data.DataResult.Companion.loading
import com.cupofcoffee0801.data.DataResult.Companion.success
import com.cupofcoffee0801.data.repository.CommentRepository
import com.cupofcoffee0801.data.repository.CommentRepositoryImpl
import com.cupofcoffee0801.data.repository.MeetingRepository
import com.cupofcoffee0801.data.repository.MeetingRepositoryImpl
import com.cupofcoffee0801.data.repository.PlaceRepository
import com.cupofcoffee0801.data.repository.PlaceRepositoryImpl
import com.cupofcoffee0801.data.repository.PreferencesRepositoryImpl
import com.cupofcoffee0801.data.repository.UserRepository
import com.cupofcoffee0801.data.repository.UserRepositoryImpl
import com.cupofcoffee0801.util.NetworkUtil
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val meetingRepository: MeetingRepository,
    private val placeRepository: PlaceRepository,
    private val commentRepository: CommentRepository,
    private val preferencesRepository: PreferencesRepositoryImpl,
    private val networkUtil: NetworkUtil
) : ViewModel() {

    private val _uiState: MutableLiveData<DataResult<SettingsUiState>> = MutableLiveData(loading())
    val uiState: LiveData<DataResult<SettingsUiState>> get() = _uiState

    private val _isButtonClicked: MutableLiveData<Boolean> = MutableLiveData(false)
    val isButtonClicked: LiveData<Boolean> get() = _isButtonClicked


    init {
        viewModelScope.launch {
            initUiState()
        }
    }

    fun onButtonClicked() {
        _isButtonClicked.value = true
    }

    private suspend fun initUiState() {
        preferencesRepository.isAutoLoginFlow.collect { isAutoLogin ->
            try {
                _uiState.postValue(success(SettingsUiState(isAutoLogin)))
            } catch (e: Exception) {
                _uiState.postValue(error(e))
            }
        }
    }

    fun convertIsAutoLogin() {
        viewModelScope.launch {
            preferencesRepository.toggleAutoLogin()
        }
    }

    fun isConnected() = networkUtil.isConnected()

    suspend fun deleteUserData(uid: String) {
        val user = userRepository.getLocalUserById(uid)!!
        val storageRef = Firebase.storage.reference.child("images/$uid")
        storageRef.delete().addOnSuccessListener {

        }.addOnFailureListener {
        }
        user.userModel.attendedMeetingIds.keys.map { meetingId ->
            cancelMeeting(uid, meetingId)
        }
        user.userModel.madeMeetingIds.keys.map { meetingId ->
            val meetingEntry =
                meetingRepository.getMeeting(meetingId, networkUtil.isConnected())
            deleteMeeting(meetingId)
            deleteMadeMeetingsInPlace(meetingEntry.meetingModel.placeId, meetingId)
        }
        deleteComments(uid)
        deleteUser(uid)
    }

    private suspend fun cancelMeeting(uid: String, meetingId: String) {
        val meetingEntry =
            meetingRepository.getMeeting(meetingId, networkUtil.isConnected())
        meetingEntry.meetingModel.personIds.remove(uid)
        meetingRepository.update(meetingEntry)
    }

    private suspend fun deleteMeeting(id: String) {
        meetingRepository.delete(id = id)
    }

    private suspend fun deleteMadeMeetingsInPlace(placeId: String, meetingId: String) {
        val placeEntry = placeRepository.getPlaceById(placeId, isConnected())!!
        placeEntry.placeModel.meetingIds.remove(meetingId)
        if (placeEntry.placeModel.meetingIds.isEmpty()) placeRepository.delete(placeEntry)
        else placeRepository.update(placeEntry)
    }

    private suspend fun deleteComments(userId: String) {
        val commentsByUserId =
            commentRepository.getCommentsByUserId(userId)
        commentsByUserId.keys.forEach { id ->
            commentRepository.delete(id)
        }
    }

    private suspend fun deleteUser(id: String) {
        userRepository.delete(id)
    }
}