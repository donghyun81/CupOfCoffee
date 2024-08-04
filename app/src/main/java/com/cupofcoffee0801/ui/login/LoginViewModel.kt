package com.cupofcoffee0801.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cupofcoffee0801.CupOfCoffeeApplication
import com.cupofcoffee0801.data.repository.MeetingRepositoryImpl
import com.cupofcoffee0801.data.repository.UserRepositoryImpl
import com.cupofcoffee0801.ui.model.UserEntry
import com.cupofcoffee0801.ui.model.asMeetingEntity
import com.cupofcoffee0801.ui.model.asUserDTO
import com.cupofcoffee0801.ui.model.asUserEntity
import com.cupofcoffee0801.util.NetworkUtil

class LoginViewModel(
    private val userRepository: UserRepositoryImpl,
    private val meetingsRepositoryImpl: MeetingRepositoryImpl,
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
            meetingsRepositoryImpl.getMeetingsByIds(userEntry.userModel.madeMeetingIds.keys.toList())
        val attendMeetings =
            meetingsRepositoryImpl.getMeetingsByIds(userEntry.userModel.attendedMeetingIds.keys.toList())
        madeMeetings.forEach { madeMeeting ->
            meetingsRepositoryImpl.insertLocal(
                madeMeeting.meetingModel.asMeetingEntity(
                    madeMeeting.id
                )
            )
        }
        attendMeetings.forEach { attendMeeting ->
            meetingsRepositoryImpl.insertLocal(
                attendMeeting.meetingModel.asMeetingEntity(
                    attendMeeting.id
                )
            )
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                LoginViewModel(
                    userRepository = CupOfCoffeeApplication.userRepository,
                    meetingsRepositoryImpl = CupOfCoffeeApplication.meetingRepository,
                    networkUtil = CupOfCoffeeApplication.networkUtil
                )
            }
        }
    }
}