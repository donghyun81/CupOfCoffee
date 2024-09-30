package com.example.home

import android.net.ConnectivityManager
import android.net.Network
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.common.util.NetworkUtil
import com.example.data.model.Place
import com.example.data.model.asPlaceEntity
import com.example.data.repository.PlaceRepository
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val placeRepository: PlaceRepository,
    private val networkUtil: NetworkUtil
) : ViewModel() {

    private val _uiState: MutableLiveData<HomeUiState> =
        MutableLiveData(HomeUiState(isLoading = true))
    val uiState: LiveData<HomeUiState> get() = _uiState

    private var currentJob: Job? = null

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            initMarkersJob()
        }

        override fun onLost(network: Network) {
            initMarkersJob()
        }
    }

    init {
        initMarkersJob()
        networkUtil.registerNetworkCallback(networkCallback)
    }

    override fun onCleared() {
        super.onCleared()
        currentJob?.cancel()
    }

    fun initMarkersJob(){
        currentJob?.cancel()
        currentJob = initMarkers()
    }

    private fun initMarkers() = viewModelScope.launch {
        val placesFlow = placeRepository.getAllPlacesInFlow(networkUtil.isConnected())
        placesFlow
            .distinctUntilChanged()
            .collect { places ->
            try {
                places.forEach { place ->
                    placeRepository.insertLocal(
                        place.asPlaceEntity()
                    )
                }
                _uiState.postValue(
                    HomeUiState(places.map { it.toMarker() })
                )
            } catch (e: Exception) {
                _uiState.postValue(HomeUiState(isError = true))
            }
        }
    }

    fun updateShowedMarkers(markers: List<Marker>) {
        val currentUiState = _uiState.value
        _uiState.postValue(currentUiState!!.copy(showedMarkers = markers))
    }

    private fun Place.toMarker() = Marker().apply {
        position = LatLng(lat, lng)
        tag = id
        icon = OverlayImage
            .fromResource(R.drawable.cup_of_coffee_mini)
    }
}