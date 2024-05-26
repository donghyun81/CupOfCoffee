package com.cupofcoffee.data.repository

import com.cupofcoffee.data.remote.PlaceDTO
import com.cupofcoffee.data.remote.PlaceDataSource

class PlaceRepositoryImpl(private val placeDataSource: PlaceDataSource) {

    suspend fun insert(caption: String, placeDTO: PlaceDTO) =
        placeDataSource.insert(caption, placeDTO)

    suspend fun getPlaceById(position: String) = placeDataSource.getPlaceById(position)

    suspend fun getPlaces() = placeDataSource.getPlaces()
}