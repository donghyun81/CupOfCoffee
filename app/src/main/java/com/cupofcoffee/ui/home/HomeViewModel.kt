package com.cupofcoffee.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cupofcoffee.CupOfCoffeeApplication
import com.cupofcoffee.data.remote.toMeetingEntry
import com.cupofcoffee.data.repository.MeetingRepositoryImpl
import com.cupofcoffee.ui.model.MeetingEntry
import com.cupofcoffee.ui.model.MeetingModel
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import kotlinx.coroutines.launch

class HomeViewModel(private val meetingRepositoryImpl: MeetingRepositoryImpl) : ViewModel() {

    private val _meetings: MutableLiveData<List<MeetingEntry>> = MutableLiveData()
    val meetings: LiveData<List<MeetingEntry>> = _meetings

    private val _makers: MutableLiveData<List<Marker>?> = MutableLiveData()
    val marker: LiveData<List<Marker>?> = _makers

    init {
        initMeetings()
    }

    private fun initMeetings() {
        viewModelScope.launch {
            _meetings.value = meetingRepositoryImpl.getMeetings()?.map {
                val (id, meetingDTO) = it
                meetingDTO.toMeetingEntry(id)
            }
            initMarkers()
        }
    }

    private fun initMarkers() {
        _makers.value = meetings.value?.map { meetingEntry -> meetingEntry.meetingModel.toMarker() }
    }

    private fun MeetingModel.toMarker() = Marker().apply { position = LatLng(lat, lng) }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                HomeViewModel(
                    meetingRepositoryImpl = CupOfCoffeeApplication.meetingRepository
                )
            }
        }
    }
}