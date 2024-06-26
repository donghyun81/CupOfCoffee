package com.cupofcoffee.ui.model

import com.cupofcoffee.data.local.PlaceEntity
import com.cupofcoffee.data.remote.PlaceDTO

data class PlaceModel(
    val caption: String,
    val lat: Double,
    val lng: Double,
    val meetingIds: MutableMap<String, Boolean> = mutableMapOf(),
    val isSynced: Boolean = true
    )

fun PlaceModel.asPlaceEntity(id: String) = PlaceEntity(id, caption, lat, lng, meetingIds)
fun PlaceModel.asPlaceDTO() = PlaceDTO(caption, lat, lng, meetingIds)