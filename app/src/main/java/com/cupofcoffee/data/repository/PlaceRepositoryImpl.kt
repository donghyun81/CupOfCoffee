package com.cupofcoffee.data.repository

import com.cupofcoffee.data.local.PlaceDao
import com.cupofcoffee.data.local.PlaceEntity
import com.cupofcoffee.data.remote.PlaceDTO
import com.cupofcoffee.data.remote.PlaceDataSource

class PlaceRepositoryImpl(
    private val placeDao: PlaceDao,
    private val placeDataSource: PlaceDataSource
) {

    suspend fun insertLocal(placeEntity: PlaceEntity) =
        placeDao.insert(placeEntity)

    suspend fun insertRemote(id: String, placeDTO: PlaceDTO) =
        placeDataSource.insert(id, placeDTO)

    suspend fun getLocalPlaceById(position: String) = placeDao.getPlaceById(position)

    suspend fun getRemotePlaceById(position: String) = placeDataSource.getPlaceById(position)

    suspend fun updateLocal(placeEntity: PlaceEntity) = placeDao.update(placeEntity)

    suspend fun updateRemote(id: String, placeDTO: PlaceDTO) = placeDataSource.update(id, placeDTO)

    suspend fun deleteLocal(placeEntity: PlaceEntity) = placeDao.delete(placeEntity)

    suspend fun deleteRemote(id: String) = placeDataSource.delete(id)

    fun getLocalPlaces() = placeDao.getAllPlaces()

    fun getRemotePlaces() = placeDataSource.getPlaces()
}