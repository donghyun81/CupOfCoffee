package com.cupofcoffee0801.ui.model

import com.cupofcoffee0801.data.local.model.PlaceEntity
import com.cupofcoffee0801.data.remote.model.PlaceDTO

data class PlaceData(
    val caption: String,
    val lat: Double,
    val lng: Double,
    val meetingIds: MutableMap<String, Boolean> = mutableMapOf()
)

fun PlaceData.asPlaceEntity(id: String) = PlaceEntity(id, caption, lat, lng, meetingIds)
fun PlaceData.asPlaceDTO() = PlaceDTO(caption, lat, lng, meetingIds)

fun PlaceData.asPlace(id: String) = Place(id, caption, lat, lng, meetingIds)