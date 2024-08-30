package com.cupofcoffee0801.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cupofcoffee0801.data.remote.model.PlaceDTO
import com.cupofcoffee0801.ui.model.Place

@Entity("places")
data class PlaceEntity(
    @PrimaryKey
    val id: String,
    val caption: String,
    val lat: Double,
    val lng: Double,
    val meetingIds: MutableMap<String, Boolean> = mutableMapOf()
)

fun PlaceEntity.asMeetingEntry() = Place(id,caption, lat, lng, meetingIds)

fun PlaceEntity.asPlaceDTO() = PlaceDTO(caption, lat, lng, meetingIds)