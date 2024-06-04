package com.cupofcoffee.ui.model

data class PlaceModel(
    val caption: String,
    val lat: Double,
    val lng: Double,
    val meetingIds: Map<String, Boolean> = mutableMapOf()
)