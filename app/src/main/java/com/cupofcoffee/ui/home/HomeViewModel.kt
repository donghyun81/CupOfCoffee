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
import com.cupofcoffee.data.remote.model.PlaceDTO
import com.cupofcoffee.data.remote.model.asPlaceEntry
import com.cupofcoffee.data.repository.PlaceRepositoryImpl
import com.cupofcoffee.ui.model.PlaceEntry
import com.cupofcoffee.util.NetworkUtil
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.overlay.Marker
import kotlinx.coroutines.launch

class HomeViewModel(
    private val placeRepositoryImpl: PlaceRepositoryImpl,
    private val networkUtil: NetworkUtil
) : ViewModel() {

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
            val placesFlow = placeRepositoryImpl.getAllPlacesInFlow(networkUtil.isConnected())
            placesFlow.collect { places ->
                try {
                    _uiState.value = DataResult.Success(
                        HomeUiState(places.map { it.toMarker() })
                    )
                } catch (e: Exception) {
                    _uiState.value = DataResult.Error(e)
                }
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
                    placeRepositoryImpl = CupOfCoffeeApplication.placeRepository,
                    networkUtil = CupOfCoffeeApplication.networkUtil
                )
            }
        }
    }
}