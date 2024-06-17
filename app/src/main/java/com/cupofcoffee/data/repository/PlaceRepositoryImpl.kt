package com.cupofcoffee.data.repository

import com.cupofcoffee.data.local.PlaceDao
import com.cupofcoffee.data.remote.PlaceDTO
import com.cupofcoffee.data.remote.PlaceDataSource

class PlaceRepositoryImpl(
    private val placeDao: PlaceDao,
    private val placeDataSource: PlaceDataSource
) {

    suspend fun insertLocal(caption: String, placeDTO: PlaceDTO) =
        placeDataSource.insert(caption, placeDTO)

    suspend fun insertRemote(caption: String, placeDTO: PlaceDTO) =
        placeDataSource.insert(caption, placeDTO)

    suspend fun getLocalPlaceById(position: String) = placeDataSource.getPlaceById(position)

    suspend fun getRemotePlaceById(position: String) = placeDataSource.getPlaceById(position)

    suspend fun updateLocal(id: String, placeDTO: PlaceDTO) = placeDataSource.update(id, placeDTO)

    suspend fun updateRemote(id: String, placeDTO: PlaceDTO) = placeDataSource.update(id, placeDTO)

    suspend fun deleteLocal(id: String) = placeDataSource.delete(id)

    suspend fun deleteRemote(id: String) = placeDataSource.delete(id)

    fun getLocalPlaces() = placeDataSource.getPlaces()

    fun getRemotePlaces() = placeDataSource.getPlaces()
}