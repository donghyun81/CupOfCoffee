package com.cupofcoffee.ui.meetingdetail

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
import com.cupofcoffee.data.DataResult.Companion.loading
import com.cupofcoffee.data.DataResult.Companion.success
import com.cupofcoffee.data.repository.MeetingRepositoryImpl
import com.cupofcoffee.util.NetworkUtil
import kotlinx.coroutines.launch

class MeetingDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val meetingRepositoryImpl: MeetingRepositoryImpl,
    private val networkUtil: NetworkUtil
) : ViewModel() {

    private val meetingId =
        MeetingDetailFragmentArgs.fromSavedStateHandle(savedStateHandle).meetingId

    private val _meetingDetailUiState: MutableLiveData<DataResult<MeetingDetailUiState>> =
        MutableLiveData(
            loading()
        )

    val meetingDetailUiState: LiveData<DataResult<MeetingDetailUiState>> =
        _meetingDetailUiState

    init {
        initUiState()
    }

    private fun initUiState() {
        viewModelScope.launch {
            val meetingEntry =
                meetingRepositoryImpl.getMeeting(meetingId, networkUtil.isConnected())
            try {
                _meetingDetailUiState.postValue(success(MeetingDetailUiState(meetingEntry)))
            } catch (e: Exception) {
                _meetingDetailUiState.postValue(error(e))
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MeetingDetailViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    meetingRepositoryImpl = CupOfCoffeeApplication.meetingRepository,
                    networkUtil = CupOfCoffeeApplication.networkUtil
                )
            }
        }
    }
}