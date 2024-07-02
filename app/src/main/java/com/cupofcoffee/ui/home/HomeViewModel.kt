package com.cupofcoffee.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cupofcoffee.CupOfCoffeeApplication
import com.cupofcoffee.data.DataResult
import com.cupofcoffee.data.local.model.PlaceEntity
import com.cupofcoffee.data.repository.PlaceRepositoryImpl
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.overlay.Marker
import kotlinx.coroutines.launch

class HomeViewModel(private val placeRepositoryImpl: PlaceRepositoryImpl) : ViewModel() {

    private val _uiState: MutableLiveData<DataResult<HomeUiState>> =
        MutableLiveData(DataResult.Loading)
    val uiState: LiveData<DataResult<HomeUiState>> = _uiState

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
            val placesFlow = placeRepositoryImpl.getLocalPlacesInFlow()
            placesFlow.collect { places ->
                try {
                    _uiState.value = DataResult.Success(HomeUiState(places.map { it.toMarker() }))
                } catch (e: Exception) {
                    _uiState.value = DataResult.Error(e)
                }
            }
        }
    }

    private fun PlaceEntity.toMarker() = Marker().apply {
        position = LatLng(lat, lng)
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