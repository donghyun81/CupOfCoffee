package com.cupofcoffee0801.ui.model

import com.cupofcoffee0801.data.local.model.PlaceEntity
import com.cupofcoffee0801.data.remote.model.PlaceDTO

data class Place(
    val id: String,
    val caption: String,
    val lat: Double,
    val lng: Double,
    val meetingIds: MutableMap<String, Boolean> = mutableMapOf()
)

fun Place.asPlaceEntity() = PlaceEntity(id, caption, lat, lng, meetingIds)
fun Place.asPlaceDTO() = PlaceDTO(caption, lat, lng, meetingIds)