package com.cupofcoffee.ui.home

import com.naver.maps.map.overlay.Marker

data class HomeUiState(
    val markers: List<Marker> = emptyList()
)