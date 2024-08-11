package com.cupofcoffee0801.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cupofcoffee0801.data.repository.MeetingRepository
import com.cupofcoffee0801.data.repository.UserRepository
import com.cupofcoffee0801.ui.model.UserEntry
import com.cupofcoffee0801.ui.model.asMeetingEntity
import com.cupofcoffee0801.ui.model.asUserDTO
import com.cupofcoffee0801.ui.model.asUserEntity
import com.cupofcoffee0801.util.NetworkUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val meetingsRepository: MeetingRepository,
    private val networkUtil: NetworkUtil
) : ViewModel() {

    private val _isButtonClicked: MutableLiveData<Boolean> = MutableLiveData(false)
    val isButtonClicked: LiveData<Boolean> get() = _isButtonClicked

    fun isNetworkConnected() = networkUtil.isConnected()

    fun switchButtonClicked() {
        _isButtonClicked.value = _isButtonClicked.value?.not()
    }

    suspend fun insertUser(userEntry: UserEntry) {
        with(userEntry) {
            userRepository.insertLocal(userModel.asUserEntity(id))
            userRepository.insertRemote(id, userModel.asUserDTO())
        }
    }

    suspend fun loginUser(id: String) {
        val userEntry = userRepository.getRemoteUserById(id)!!
        if (isNetworkConnected()) {
            insertUserMeetings(userEntry)
        }
        userRepository.insertLocal(userEntry.asUserEntity())
    }

    private suspend fun insertUserMeetings(userEntry: UserEntry) {
        val madeMeetings =
            meetingsRepository.getMeetingsByIds(userEntry.userModel.madeMeetingIds.keys.toList())
        val attendMeetings =
            meetingsRepository.getMeetingsByIds(userEntry.userModel.attendedMeetingIds.keys.toList())
        madeMeetings.forEach { madeMeeting ->
            meetingsRepository.insertLocal(
                madeMeeting.meetingModel.asMeetingEntity(
                    madeMeeting.id
                )
            )
        }
        attendMeetings.forEach { attendMeeting ->
            meetingsRepository.insertLocal(
                attendMeeting.meetingModel.asMeetingEntity(
                    attendMeeting.id
                )
            )
        }
    }
}