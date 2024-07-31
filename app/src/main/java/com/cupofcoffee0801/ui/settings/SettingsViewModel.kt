package com.cupofcoffee0801.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cupofcoffee0801.CupOfCoffeeApplication
import com.cupofcoffee0801.data.DataResult
import com.cupofcoffee0801.data.DataResult.Companion.error
import com.cupofcoffee0801.data.DataResult.Companion.loading
import com.cupofcoffee0801.data.DataResult.Companion.success
import com.cupofcoffee0801.data.repository.CommentRepositoryImpl
import com.cupofcoffee0801.data.repository.MeetingRepositoryImpl
import com.cupofcoffee0801.data.repository.PlaceRepositoryImpl
import com.cupofcoffee0801.data.repository.PreferencesRepositoryImpl
import com.cupofcoffee0801.data.repository.UserRepositoryImpl
import com.cupofcoffee0801.util.NetworkUtil
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userRepositoryImpl: UserRepositoryImpl,
    private val meetingRepositoryImpl: MeetingRepositoryImpl,
    private val placeRepositoryImpl: PlaceRepositoryImpl,
    private val commentRepositoryImpl: CommentRepositoryImpl,
    private val preferencesRepositoryImpl: PreferencesRepositoryImpl,
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
        preferencesRepositoryImpl.isAutoLoginFlow.collect { isAutoLogin ->
            try {
                _uiState.postValue(success(SettingsUiState(isAutoLogin)))
            } catch (e: Exception) {
                _uiState.postValue(error(e))
            }
        }
    }

    fun convertIsAutoLogin() {
        viewModelScope.launch {
            preferencesRepositoryImpl.toggleAutoLogin()
        }
    }

    fun isConnected() = networkUtil.isConnected()

    suspend fun deleteUserData(uid: String) {
        val user = userRepositoryImpl.getLocalUserById(uid)
        val storageRef = Firebase.storage.reference.child("images/$uid")
        storageRef.delete().addOnSuccessListener {

        }.addOnFailureListener {
        }
        user.userModel.attendedMeetingIds.keys.map { meetingId ->
            cancelMeeting(uid, meetingId)
        }
        user.userModel.madeMeetingIds.keys.map { meetingId ->
            val meetingEntry =
                meetingRepositoryImpl.getMeeting(meetingId, networkUtil.isConnected())
            deleteMeeting(meetingId)
            deleteMadeMeetingsInPlace(meetingEntry.meetingModel.placeId, meetingId)
        }
        deleteComments(uid)
        deleteUser(uid)
    }

    private suspend fun cancelMeeting(uid: String, meetingId: String) {
        val meetingEntry =
            meetingRepositoryImpl.getMeeting(meetingId, networkUtil.isConnected())
        meetingEntry.meetingModel.personIds.remove(uid)
        meetingRepositoryImpl.update(meetingEntry)
    }

    private suspend fun deleteMeeting(id: String) {
        meetingRepositoryImpl.delete(id = id)
    }

    private suspend fun deleteMadeMeetingsInPlace(placeId: String, meetingId: String) {
        val placeEntry = placeRepositoryImpl.getPlaceById(placeId, isConnected())!!
        placeEntry.placeModel.meetingIds.remove(meetingId)
        if (placeEntry.placeModel.meetingIds.isEmpty()) placeRepositoryImpl.delete(placeEntry)
        else placeRepositoryImpl.update(placeEntry)
    }

    private suspend fun deleteComments(userId: String) {
        val commentIdsByUserId =
            commentRepositoryImpl.getCommentsByUserId().filterValues { it.userId == userId }.keys
        commentIdsByUserId.forEach { id ->
            commentRepositoryImpl.delete(id)
        }
    }

    private suspend fun deleteUser(id: String) {
        userRepositoryImpl.delete(id)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                SettingsViewModel(
                    userRepositoryImpl = CupOfCoffeeApplication.userRepository,
                    meetingRepositoryImpl = CupOfCoffeeApplication.meetingRepository,
                    placeRepositoryImpl = CupOfCoffeeApplication.placeRepository,
                    commentRepositoryImpl = CupOfCoffeeApplication.commentRepository,
                    preferencesRepositoryImpl = CupOfCoffeeApplication.preferencesRepositoryImpl,
                    networkUtil = CupOfCoffeeApplication.networkUtil
                )
            }
        }
    }
}