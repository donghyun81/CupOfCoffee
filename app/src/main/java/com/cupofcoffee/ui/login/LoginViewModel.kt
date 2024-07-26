package com.cupofcoffee.ui.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cupofcoffee.CupOfCoffeeApplication
import com.cupofcoffee.data.repository.MeetingRepositoryImpl
import com.cupofcoffee.data.repository.PreferencesRepositoryImpl
import com.cupofcoffee.data.repository.UserRepositoryImpl
import com.cupofcoffee.ui.model.UserEntry
import com.cupofcoffee.ui.model.asMeetingEntity
import com.cupofcoffee.ui.model.asUserDTO
import com.cupofcoffee.ui.model.asUserEntity
import com.cupofcoffee.util.NetworkUtil
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay

class LoginViewModel(
    private val userRepository: UserRepositoryImpl,
    private val meetingsRepositoryImpl: MeetingRepositoryImpl,
    private val networkUtil: NetworkUtil
) : ViewModel() {

    fun isNetworkConnected() = networkUtil.isConnected()

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