package com.cupofcoffee.data.repository

import com.cupofcoffee.data.local.datasource.PlaceLocalDataSource
import com.cupofcoffee.data.local.model.PlaceEntity
import com.cupofcoffee.data.remote.datasource.PlaceRemoteDataSource
import com.cupofcoffee.data.remote.model.PlaceDTO
import kotlinx.coroutines.flow.Flow

class PlaceRepositoryImpl(
    private val placeLocalDataSource: PlaceLocalDataSource,
    private val placeDataSource: PlaceRemoteDataSource
) {

    suspend fun insertLocal(placeEntity: PlaceEntity) =
        placeLocalDataSource.insert(placeEntity)

    suspend fun insertRemote(id: String, placeDTO: PlaceDTO) =
        placeDataSource.insert(id, placeDTO)

    suspend fun getLocalPlaceById(id: String) = placeLocalDataSource.getPlaceById(id)

    suspend fun getRemotePlaceById(id: String) = placeDataSource.getPlaceById(id)

    suspend fun updateLocal(placeEntity: PlaceEntity) = placeLocalDataSource.update(placeEntity)

    suspend fun updateRemote(id: String, placeDTO: PlaceDTO) = placeDataSource.update(id, placeDTO)

    suspend fun deleteLocal(placeEntity: PlaceEntity) = placeLocalDataSource.delete(placeEntity)

    suspend fun deleteRemote(id: String) = placeDataSource.delete(id)

    suspend fun getAllLocalPlaces() = placeLocalDataSource.getAllPlaces()

    suspend fun getAllRemotePlaces() = placeDataSource.getAllPlaces()

    fun getLocalPlacesInFlow(): Flow<List<PlaceEntity>> =
        placeLocalDataSource.getAllPlacesInFlow()
}