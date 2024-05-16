package com.cupofcoffee

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.launch

class SaveMeetingViewModel(
    private val meetingRepositoryImpl: MeetingRepositoryImpl,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val args = SaveMeetingFragmentArgs.fromSavedStateHandle(savedStateHandle)

    fun saveMeeting(meetingModel: MeetingModel) {
        viewModelScope.launch {
            meetingRepositoryImpl.insert(meetingModel.toMeetingDTO())
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                SaveMeetingViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    meetingRepositoryImpl = CupOfCoffeeApplication.meetingRepository
                )
            }
        }
    }
}