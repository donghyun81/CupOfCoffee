package com.cupofcoffee.ui.model

import com.cupofcoffee.data.remote.PlaceDTO

data class PlaceModel(
    val caption: String,
    val lat: Double,
    val lng: Double,
    val meetingIds: MutableMap<String, Boolean> = mutableMapOf()
)

fun PlaceModel.toPlaceDTO() = PlaceDTO(caption, lat, lng, meetingIds)