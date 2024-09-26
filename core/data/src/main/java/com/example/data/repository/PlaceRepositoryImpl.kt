package com.example.data.repository

import com.example.data.model.Place
import com.example.data.model.asPlace
import com.example.data.model.asPlaceDTO
import com.example.data.model.asPlaceEntity
import com.example.database.datasource.PlaceLocalDataSource
import com.example.database.model.PlaceEntity
import com.example.network.datasource.PlaceRemoteDataSource
import com.example.network.model.PlaceDTO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PlaceRepositoryImpl @Inject constructor(
    private val placeLocalDataSource: PlaceLocalDataSource,
    private val placeRemoteDataSource: PlaceRemoteDataSource
) : PlaceRepository {

    override suspend fun insertLocal(placeEntity: PlaceEntity) =
        placeLocalDataSource.insert(placeEntity)

    override suspend fun insertRemote(id: String, placeDTO: PlaceDTO) =
        placeRemoteDataSource.insert(id, placeDTO)

    override suspend fun insert(place: Place) {
        place.apply {
            placeLocalDataSource.insert(asPlaceEntity())
            placeRemoteDataSource.insert(id, asPlaceDTO())
        }
    }

    override suspend fun getPlaceById(id: String, isNetworkConnected: Boolean) =
        if (isNetworkConnected) placeRemoteDataSource.getPlaceById(id)?.asPlace(id)
        else placeLocalDataSource.getPlaceById(id).asPlace()

    override suspend fun update(place: Place) {
        place.apply {
            placeLocalDataSource.update(asPlaceEntity())
            placeRemoteDataSource.update(id,asPlaceDTO())
        }
    }

    override suspend fun delete(place: Place) {
        place.apply {
            placeRemoteDataSource.delete(id)
            placeLocalDataSource.delete(asPlaceEntity())
        }
    }

    override suspend fun deleteAllLocalPlaces() {
        placeLocalDataSource.deleteAll()
    }

    override suspend fun getAllRemotePlaces() = placeRemoteDataSource.getAllPlaces()

    override suspend fun getAllPlacesInFlow(isNetworkConnected: Boolean): Flow<List<Place>> {
        return if (isNetworkConnected) {
            placeRemoteDataSource.getAllPlacesInFlow().map { places ->
                places.convertPlaceEntries()
            }
        } else {
            placeLocalDataSource.getAllPlacesInFlow().map { places ->
                places.convertPlaceEntries()
            }
        }
    }

    private fun Map<String, PlaceDTO>.convertPlaceEntries() =
        map { entry ->
            val (id, placeDTO) = entry
            placeDTO.asPlace(id)
        }

    private fun List<PlaceEntity>.convertPlaceEntries() =
        map { it.asPlace() }
}