package com.cupofcoffee.ui.savemeeting

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cupofcoffee.CupOfCoffeeApplication
import com.cupofcoffee.data.remote.MeetingDTO
import com.cupofcoffee.data.repository.MeetingRepositoryImpl
import com.cupofcoffee.ui.model.MeetingModel
import com.cupofcoffee.ui.model.toMeetingDTO
import kotlinx.coroutines.launch

class SaveMeetingViewModel(
    private val meetingRepositoryImpl: MeetingRepositoryImpl,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val args = SaveMeetingFragmentArgs.fromSavedStateHandle(savedStateHandle)

    fun saveMeeting(meetingDTO: MeetingDTO) {
        viewModelScope.launch {
            meetingRepositoryImpl.insert(meetingDTO)
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