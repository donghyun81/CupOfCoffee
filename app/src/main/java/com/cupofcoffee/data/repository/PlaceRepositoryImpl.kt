package com.cupofcoffee.data.repository

import com.cupofcoffee.data.remote.PlaceDTO
import com.cupofcoffee.data.remote.PlaceDataSource

class PlaceRepositoryImpl(private val placeDataSource: PlaceDataSource) {

    suspend fun insert(caption: String, placeDTO: PlaceDTO) =
        placeDataSource.insert(caption, placeDTO)

    suspend fun getPlaceById(position: String) = placeDataSource.getPlaceById(position)

    suspend fun update(id: String, placeDTO: PlaceDTO) = placeDataSource.update(id, placeDTO)

    suspend fun delete(id: String) = placeDataSource.delete(id)

    fun getPlaces() = placeDataSource.getPlaces()
}