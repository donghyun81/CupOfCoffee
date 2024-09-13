package com.cupofcoffee0801.data.remote.model

import com.cupofcoffee0801.data.local.model.PlaceEntity
import com.cupofcoffee0801.ui.model.Place
import kotlinx.serialization.Serializable

@Serializable
data class PlaceDTO(
    val caption: String,
    val lat: Double,
    val lng: Double,
    val meetingIds: MutableMap<String, Boolean> = mutableMapOf()
)

fun PlaceDTO.asPlace(id: String) = Place(id,caption, lat, lng, meetingIds)

fun PlaceDTO.asPlaceEntity(id: String) = PlaceEntity(id, caption, lat, lng, meetingIds)