package com.cupofcoffee0801.ui.home

import android.net.ConnectivityManager
import android.net.Network
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cupofcoffee0801.R
import com.cupofcoffee0801.data.DataResult
import com.cupofcoffee0801.data.DataResult.Companion.success
import com.cupofcoffee0801.data.repository.PlaceRepository
import com.cupofcoffee0801.data.repository.PlaceRepositoryImpl
import com.cupofcoffee0801.ui.model.PlaceEntry
import com.cupofcoffee0801.ui.model.asPlaceEntity
import com.cupofcoffee0801.util.NetworkUtil
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val placeRepository: PlaceRepository,
    private val networkUtil: NetworkUtil
) : ViewModel() {

    private val _uiState: MutableLiveData<DataResult<HomeUiState>> =
        MutableLiveData(DataResult.Loading)
    val uiState: LiveData<DataResult<HomeUiState>> get() = _uiState

    var currentJob: Job? = null

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
        networkUtil.registerNetworkCallback(networkCallback)
    }

    override fun onCleared() {
        super.onCleared()
        currentJob?.cancel()
    }

    fun initMarkers() = viewModelScope.launch {
        val placesFlow = placeRepository.getAllPlacesInFlow(networkUtil.isConnected())
        placesFlow.collect { places ->
            try {
                places.forEach { place ->
                    placeRepository.insertLocal(
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

    fun updateShowedMarkers(markers: List<Marker>) {
        val currentUiState = _uiState.value
        if (currentUiState is DataResult.Success) {
            _uiState.postValue(success(currentUiState.data.copy(showedMakers = markers)))
        }
    }

    private fun PlaceEntry.toMarker() = Marker().apply {
        position = LatLng(placeModel.lat, placeModel.lng)
        tag = id
        icon = OverlayImage
            .fromResource(R.drawable.cup_of_coffee_mini)
    }
}