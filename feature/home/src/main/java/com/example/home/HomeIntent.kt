package com.example.home

import com.naver.maps.geometry.LatLng

sealed class HomeIntent {
    data class SymbolClick(val caption: String, val latLng: LatLng) : HomeIntent()

    data class MarkerClick(val placeId: String) : HomeIntent()

    data object InitData : HomeIntent()
}