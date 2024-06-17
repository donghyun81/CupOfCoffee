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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(private val placeRepositoryImpl: PlaceRepositoryImpl) : ViewModel() {

    private val _uiState: MutableLiveData<HomeUiState> = MutableLiveData(HomeUiState())
    val uiState: LiveData<HomeUiState> = _uiState
    private val _makers: MutableLiveData<List<Marker>?> = MutableLiveData()

    init {
        initMeetings()
    }

    private fun initMeetings() {
        viewModelScope.launch {
            initMarkers()
        }
    }

    private suspend fun initMarkers() {
        viewModelScope.launch {
            val places = withContext(Dispatchers.IO) {
                placeRepositoryImpl.getLocalPlaces()
            }
            places.collect { places ->
                _uiState.value = _uiState.value?.copy(
                    markers = places.map { place ->
                        place.toMarker()
                    }
                )
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