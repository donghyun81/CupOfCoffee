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
import com.cupofcoffee.data.remote.toPlaceEntry
import com.cupofcoffee.data.remote.toPlaceModel
import com.cupofcoffee.data.repository.MeetingRepositoryImpl
import com.cupofcoffee.data.repository.PlaceRepositoryImpl
import com.cupofcoffee.ui.model.MeetingEntry
import com.cupofcoffee.ui.model.MeetingModel
import com.cupofcoffee.ui.model.PlaceEntry
import com.cupofcoffee.ui.model.PlaceModel
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import kotlinx.coroutines.launch

class HomeViewModel(private val placeRepositoryImpl: PlaceRepositoryImpl) : ViewModel() {

    private val _places: MutableLiveData<List<PlaceEntry>> = MutableLiveData()
    private val places: LiveData<List<PlaceEntry>> = _places

    private val _makers: MutableLiveData<List<Marker>?> = MutableLiveData()
    val marker: LiveData<List<Marker>?> = _makers

    init {
        initMeetings()
    }

    private fun initMeetings() {
        viewModelScope.launch {
            _places.value = placeRepositoryImpl.getPlaces().map {
                val (id, placeDTO) = it
                placeDTO.toPlaceEntry(id)
            }
            initMarkers()
        }
    }

    private fun initMarkers() {
        _makers.value = places.value?.map { placeEntry -> placeEntry.toMarker() }
    }

    private fun PlaceEntry.toMarker() = Marker().apply {
        position = LatLng(placeModel.lat, placeModel.lng)
        tag = id
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                HomeViewModel(
                    placeRepositoryImpl = CupOfCoffeeApplication.placeRepository
                )
            }
        }
    }
}