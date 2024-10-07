package com.example.home

import com.naver.maps.geometry.LatLng

sealed class HomeSideEffect {

    data class NavigateMakeMeeting(val caption: String, val latLng: LatLng) : HomeSideEffect()

    data class NavigateMeetingPlace(val placeId:String) : HomeSideEffect()
}