package com.cupofcoffee0801.data.repository

import com.cupofcoffee0801.data.local.datasource.PlaceLocalDataSource
import com.cupofcoffee0801.data.local.model.PlaceEntity
import com.cupofcoffee0801.data.local.model.asMeetingEntry
import com.cupofcoffee0801.data.remote.datasource.PlaceRemoteDataSource
import com.cupofcoffee0801.data.remote.model.PlaceDTO
import com.cupofcoffee0801.data.remote.model.asPlace
import com.cupofcoffee0801.ui.model.Place
import com.cupofcoffee0801.ui.model.asPlaceDTO
import com.cupofcoffee0801.ui.model.asPlaceEntity
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
        else placeLocalDataSource.getPlaceById(id).asMeetingEntry()

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
        map { it.asMeetingEntry() }
}