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
import com.cupofcoffee.data.DataResult
import com.cupofcoffee.data.local.model.PlaceEntity
import com.cupofcoffee.data.remote.model.asPlaceEntry
import com.cupofcoffee.data.repository.PlaceRepositoryImpl
import com.cupofcoffee.ui.model.PlaceEntry
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
        placeRepositoryImpl.connectWebSocket()
        val placesFlow = placeRepositoryImpl.placesUpdates
        placesFlow.collect { places ->
            Log.d("12345", places.toString())
            try {
                _uiState.value =
                    DataResult.Success(HomeUiState(
                        places.map { it.value.asPlaceEntry(it.key) }.map { it.toMarker() }
                    )
                    )
                Log.d("12345", places.toString())
            } catch (e: Exception) {
                Log.d("12345", "에러")
                _uiState.value = DataResult.Error(e)
            }
        }
    }

    fun closeWebSocket() {
        placeRepositoryImpl.closeWebSocket()
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