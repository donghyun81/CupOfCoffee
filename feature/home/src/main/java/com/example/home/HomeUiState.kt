package com.example.home

import com.naver.maps.map.overlay.Marker

data class HomeUiState(
    val markers: List<Marker> = emptyList(),
    val showedMarkers: List<Marker> = emptyList(),
    val isError: Boolean = false,
    val isLoading: Boolean = false
)