package com.cupofcoffee.data.remote

import com.cupofcoffee.ui.model.PlaceEntry
import com.cupofcoffee.ui.model.PlaceModel
import kotlinx.serialization.Serializable

@Serializable
data class PlaceDTO(
    val caption: String,
    val lat: Double,
    val lng: Double,
    val meetingIds: Map<String, Boolean> = emptyMap()
)

fun PlaceDTO.toPlaceEntry(id: String) = PlaceEntry(id, PlaceModel(caption, lat, lng, meetingIds))