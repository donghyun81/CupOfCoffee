package com.cupofcoffee.ui.user

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
import com.cupofcoffee.data.remote.toMeetingEntry
import com.cupofcoffee.data.remote.toUserModel
import com.cupofcoffee.data.repository.MeetingRepositoryImpl
import com.cupofcoffee.data.repository.UserRepositoryImpl
import com.cupofcoffee.ui.model.MeetingEntry
import com.cupofcoffee.ui.model.MeetingsCategory
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class UserMeetingsViewModel(
    savedStateHandle: SavedStateHandle,
    private val userRepositoryImpl: UserRepositoryImpl,
    private val meetingRepositoryImpl: MeetingRepositoryImpl
) : ViewModel() {

    private val category = savedStateHandle.get<MeetingsCategory>("category")!!

    private val _meetings: MutableLiveData<List<MeetingEntry>> = MutableLiveData()
    val meetings: LiveData<List<MeetingEntry>> = _meetings

    init {
        viewModelScope.launch {
            setMeetings()
        }
    }

    private suspend fun setMeetings() {
        val uid = Firebase.auth.uid ?: return
        val user = userRepositoryImpl.getUserById(id = uid).toUserModel()
        _meetings.value =
            when (category) {
                MeetingsCategory.ATTENDED_MEETINGS -> user.attendedMeetingIds.map { id ->
                    meetingRepositoryImpl.getMeeting(id).toMeetingEntry(id)
                }

                MeetingsCategory.MADE_MEETINGS -> user.madeMeetingIds.map { id ->
                    meetingRepositoryImpl.getMeeting(id).toMeetingEntry(id)
                }
            }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                UserMeetingsViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    userRepositoryImpl = CupOfCoffeeApplication.userRepository,
                    meetingRepositoryImpl = CupOfCoffeeApplication.meetingRepository
                )
            }
        }
    }
}