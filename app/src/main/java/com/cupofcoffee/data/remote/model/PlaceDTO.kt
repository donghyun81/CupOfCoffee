package com.cupofcoffee.data.remote.model

import com.cupofcoffee.data.local.model.PlaceEntity
import com.cupofcoffee.ui.model.PlaceEntry
import com.cupofcoffee.ui.model.PlaceModel
import kotlinx.serialization.Serializable

@Serializable
data class PlaceDTO(
    val caption: String,
    val lat: Double,
    val lng: Double,
    val meetingIds: MutableMap<String, Boolean> = mutableMapOf()
)

fun PlaceDTO.asPlaceEntry(id: String) = PlaceEntry(id, PlaceModel(caption, lat, lng, meetingIds))

fun PlaceDTO.asPlaceEntity(id: String) = PlaceEntity(id, caption, lat, lng, meetingIds)