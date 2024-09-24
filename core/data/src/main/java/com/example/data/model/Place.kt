package com.example.data.model

import com.example.database.model.PlaceEntity
import com.example.network.model.PlaceDTO

data class Place(
    val id: String,
    val caption: String,
    val lat: Double,
    val lng: Double,
    val meetingIds: MutableMap<String, Boolean> = mutableMapOf()
)

fun Place.asPlaceEntity() = PlaceEntity(id, caption, lat, lng, meetingIds)
fun Place.asPlaceDTO() = PlaceDTO(caption, lat, lng, meetingIds)

fun PlaceDTO.asPlace(id: String) = Place(id,caption, lat, lng, meetingIds)

fun PlaceDTO.asPlaceEntity(id: String) = PlaceEntity(id, caption, lat, lng, meetingIds)

fun PlaceEntity.asPlace() = Place(id,caption, lat, lng, meetingIds)

fun PlaceEntity.asPlaceDTO() = PlaceDTO(caption, lat, lng, meetingIds)