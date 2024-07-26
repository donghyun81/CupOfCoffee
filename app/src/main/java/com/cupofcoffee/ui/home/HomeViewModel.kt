package com.cupofcoffee.ui.home

import android.net.ConnectivityManager
import android.net.Network
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cupofcoffee.CupOfCoffeeApplication
import com.cupofcoffee.R
import com.cupofcoffee.data.DataResult
import com.cupofcoffee.data.repository.PlaceRepositoryImpl
import com.cupofcoffee.ui.model.PlaceEntry
import com.cupofcoffee.ui.model.asPlaceEntity
import com.cupofcoffee.util.NetworkUtil
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class HomeViewModel(
    private val placeRepositoryImpl: PlaceRepositoryImpl,
    private val networkUtil: NetworkUtil
) : ViewModel() {

    private val _uiState: MutableLiveData<DataResult<HomeUiState>> =
        MutableLiveData(DataResult.Loading)
    val uiState: LiveData<DataResult<HomeUiState>> = _uiState

    private var currentJob: Job? = null

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            currentJob?.cancel()
            currentJob = initMarkers()
        }

        override fun onLost(network: Network) {
            currentJob?.cancel()
            currentJob = initMarkers()
        }
    }

    init {
        initMarkers()
        networkUtil.registerNetworkCallback(networkCallback)
    }

    override fun onCleared() {
        super.onCleared()
        currentJob?.cancel()
    }

    fun initMarkers() = viewModelScope.launch {
        val placesFlow = placeRepositoryImpl.getAllPlacesInFlow(networkUtil.isConnected())
        placesFlow.collect { places ->
            try {
                places.forEach { place ->
                    placeRepositoryImpl.insertLocal(
                        place.placeModel.asPlaceEntity(
                            place.id
                        )
                    )
                }
                _uiState.postValue(
                    DataResult.Success(
                        HomeUiState(places.map { it.toMarker() })
                    )
                )
            } catch (e: Exception) {
                _uiState.postValue(DataResult.Error(e))
            }
        }
    }

    private fun PlaceEntry.toMarker() = Marker().apply {
        position = LatLng(placeModel.lat, placeModel.lng)
        tag = id
        icon = OverlayImage
            .fromResource(R.drawable.cup_of_coffee_mini)
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