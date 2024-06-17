package com.cupofcoffee.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cupofcoffee.ui.model.PlaceEntry
import com.cupofcoffee.ui.model.PlaceModel

@Entity("places")
data class PlaceEntity(
    @PrimaryKey
    val id: String,
    val caption: String,
    val lat: Double,
    val lng: Double,
    val meetingIds: MutableMap<String, Boolean> = mutableMapOf()
)


fun PlaceEntity.toEntry() = PlaceEntry(id, PlaceModel(caption, lat, lng, meetingIds))