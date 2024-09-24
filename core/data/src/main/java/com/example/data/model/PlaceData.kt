package com.example.data.model

data class PlaceData(
    val caption: String,
    val lat: Double,
    val lng: Double,
    val meetingIds: MutableMap<String, Boolean> = mutableMapOf()
)

fun PlaceData.asPlace(id: String) = Place(id, caption, lat, lng, meetingIds)