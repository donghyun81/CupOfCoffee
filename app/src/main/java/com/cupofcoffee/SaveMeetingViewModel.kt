package com.cupofcoffee

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.launch

class SaveMeetingViewModel(
    private val meetingRepositoryImpl: MeetingRepositoryImpl
) : ViewModel() {


    fun saveMeeting(meetingModel: MeetingModel) {
        val meetingDTO = meetingModel.toMeetingDTO()
        viewModelScope.launch {
            meetingRepositoryImpl.insert(meetingDTO)
        }
    }


    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                SaveMeetingViewModel(
                    meetingRepositoryImpl = CupOfCoffeeApplication.meetingRepository
                )
            }
        }
    }
}