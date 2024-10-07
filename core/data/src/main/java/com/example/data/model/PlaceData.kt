package com.example.data.model

data class PlaceData(
    val placeName: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val meetingIds: MutableMap<String, Boolean> = mutableMapOf()
)

fun PlaceData.asPlace(id: String) = Place(id, placeName, lat, lng, meetingIds)