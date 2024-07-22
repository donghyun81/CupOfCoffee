package com.cupofcoffee.ui.user.usermettings

import android.util.Log
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
import com.cupofcoffee.data.DataResult.Companion.success
import com.cupofcoffee.data.local.model.UserEntity
import com.cupofcoffee.data.local.model.asUserEntry
import com.cupofcoffee.data.repository.CommentRepositoryImpl
import com.cupofcoffee.data.repository.MeetingRepositoryImpl
import com.cupofcoffee.data.repository.PlaceRepositoryImpl
import com.cupofcoffee.data.repository.UserRepositoryImpl
import com.cupofcoffee.ui.meetingdetail.MeetingDetailUiState
import com.cupofcoffee.ui.model.MeetingEntry
import com.cupofcoffee.ui.model.MeetingsCategory
import com.cupofcoffee.ui.model.UserEntry
import com.cupofcoffee.ui.model.asMeetingEntity
import com.cupofcoffee.util.NetworkUtil
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


private const val CATEGORY_TAG = "category"

class UserMeetingsViewModel(
    savedStateHandle: SavedStateHandle,
    private val userRepositoryImpl: UserRepositoryImpl,
    private val meetingRepositoryImpl: MeetingRepositoryImpl,
    private val placeRepositoryImpl: PlaceRepositoryImpl,
    private val commentRepositoryImpl: CommentRepositoryImpl,
    private val networkUtil: NetworkUtil
) : ViewModel() {

    private val category = savedStateHandle.get<MeetingsCategory>(CATEGORY_TAG)!!

    private val _uiState: MutableLiveData<DataResult<UserMeetingsUiState>> =
        MutableLiveData()
    val uiState: LiveData<DataResult<UserMeetingsUiState>> = _uiState

    init {
        viewModelScope.launch {
            setMeetings()
        }
    }

    fun isNetworkConnected() = networkUtil.isConnected()

    private suspend fun setMeetings() {
        val uid = Firebase.auth.uid ?: return
        val user = userRepositoryImpl.getLocalUserByIdInFlow(id = uid)
        user.flatMapLatest { userEntry: UserEntry? ->
            userEntry!!
            when (category) {
                MeetingsCategory.ATTENDED_MEETINGS -> {
                    getMeetingEntries(userEntry.userModel.attendedMeetingIds.keys.toList())
                        .map { value: List<MeetingEntry> ->
                            UserMeetingsUiState(value)
                        }
                }

                MeetingsCategory.MADE_MEETINGS ->
                    getMeetingEntries(userEntry.userModel.madeMeetingIds.keys.toList())
                        .map { value: List<MeetingEntry> ->
                            UserMeetingsUiState(value)
                        }
            }
        }.collect {
            _uiState.postValue(success(it))
        }
    }

    private suspend fun getMeetingEntries(meetingIds: List<String>) =
        meetingRepositoryImpl.getMeetingsByIdsInFlow(meetingIds, networkUtil.isConnected())

    private suspend fun updateMeetings(meetingEntriesInFlow: Flow<List<MeetingEntry>>) {
        try {
            meetingEntriesInFlow.collect { meetingEntries ->
                _uiState.value = success(UserMeetingsUiState(meetingEntries))
            }
        } catch (e: Exception) {
            _uiState.value = error(e)
        }
    }

    fun deleteMeeting(meetingEntry: MeetingEntry) {
        viewModelScope.launch {
            val placeId = meetingEntry.meetingModel.placeId
            updatePlace(placeId, meetingEntry.id)
            updateUser(meetingEntry.id)
            deleteComments(meetingEntry.meetingModel.commentIds.keys.toList())
            meetingRepositoryImpl.deleteLocal(meetingEntry.asMeetingEntity())
            meetingRepositoryImpl.deleteRemote(meetingEntry.id)
        }
    }

    private suspend fun updatePlace(placeId: String, meetingId: String) {
        val placeEntry =
            placeRepositoryImpl.getPlaceById(placeId, networkUtil.isConnected()) ?: return
        with(placeEntry) {
            placeModel.meetingIds.remove(meetingId)
            if (placeModel.meetingIds.isEmpty()) {
                placeRepositoryImpl.delete(this)
            } else {
                placeRepositoryImpl.update(placeEntry)
            }
        }
    }

    private suspend fun updateUser(meetingId: String) {
        val uid = Firebase.auth.uid!!
        val user = userRepositoryImpl.getLocalUserById(uid)
        user.userModel.madeMeetingIds.remove(meetingId)
        userRepositoryImpl.update(user)
    }

    private suspend fun deleteComments(commentIds: List<String>) {
        commentIds.forEach { id ->
            commentRepositoryImpl.delete(id = id)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                UserMeetingsViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    userRepositoryImpl = CupOfCoffeeApplication.userRepository,
                    meetingRepositoryImpl = CupOfCoffeeApplication.meetingRepository,
                    placeRepositoryImpl = CupOfCoffeeApplication.placeRepository,
                    commentRepositoryImpl = CupOfCoffeeApplication.commentRepository,
                    networkUtil = CupOfCoffeeApplication.networkUtil
                )
            }
        }
    }
}