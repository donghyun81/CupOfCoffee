package com.cupofcoffee.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cupofcoffee.CupOfCoffeeApplication
import com.cupofcoffee.data.repository.PlaceRepositoryImpl
import com.cupofcoffee.ui.model.PlaceEntry
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.overlay.Marker
import kotlinx.coroutines.launch

class HomeViewModel(private val placeRepositoryImpl: PlaceRepositoryImpl) : ViewModel() {

    private val places = placeRepositoryImpl.getPlaces()
    private val _makers: MutableLiveData<List<Marker>?> = MutableLiveData()
    val marker: LiveData<List<Marker>?> = _makers

    init {
        initMeetings()
    }

    private fun initMeetings() {
        viewModelScope.launch {
            initMarkers()
        }
    }

    private suspend fun initMarkers() {
        places.collect { placesEntry ->
            _makers.value = placesEntry.map { placeEntry ->
                placeEntry.toMarker()
            }
        }
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