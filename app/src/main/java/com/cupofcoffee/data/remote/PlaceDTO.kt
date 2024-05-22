package com.cupofcoffee.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class PlaceDTO(
    val caption: String,
    val lat: Double,
    val lng: Double,
    val meetingIds: Map<String,Boolean> = emptyMap()
)