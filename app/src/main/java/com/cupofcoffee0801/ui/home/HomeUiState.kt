package com.cupofcoffee0801.ui.home

import com.naver.maps.map.overlay.Marker

data class HomeUiState(
    val markers: List<Marker> = emptyList(),
    val showedMakers: List<Marker> = emptyList()
)