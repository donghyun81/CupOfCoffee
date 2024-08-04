package com.cupofcoffee0801.data.remote.model

import com.cupofcoffee0801.data.local.model.PlaceEntity
import com.cupofcoffee0801.ui.model.PlaceEntry
import com.cupofcoffee0801.ui.model.PlaceModel
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