package com.cupofcoffee.data.repository

import com.cupofcoffee.data.local.datasource.PlaceLocalDataSource
import com.cupofcoffee.data.local.model.PlaceEntity
import com.cupofcoffee.data.local.model.asMeetingEntry
import com.cupofcoffee.data.remote.datasource.PlaceRemoteDataSource
import com.cupofcoffee.data.remote.model.PlaceDTO
import com.cupofcoffee.data.remote.model.asPlaceEntry
import com.cupofcoffee.ui.model.PlaceEntry
import com.cupofcoffee.ui.model.asPlaceDTO
import com.cupofcoffee.ui.model.asPlaceEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PlaceRepositoryImpl(
    private val placeLocalDataSource: PlaceLocalDataSource,
    private val placeRemoteDataSource: PlaceRemoteDataSource
) {

    suspend fun insertLocal(placeEntity: PlaceEntity) =
        placeLocalDataSource.insert(placeEntity)

    suspend fun insertRemote(id: String, placeDTO: PlaceDTO) =
        placeRemoteDataSource.insert(id, placeDTO)

    suspend fun insert(placeEntry: PlaceEntry) {
        placeEntry.apply {
            placeLocalDataSource.insert(placeModel.asPlaceEntity(id))
            placeRemoteDataSource.insert(id, placeModel.asPlaceDTO())
        }
    }

    suspend fun getPlaceById(id: String, isNetworkConnected: Boolean = true) =
        if (isNetworkConnected) placeRemoteDataSource.getPlaceById(id)?.asPlaceEntry(id)
        else placeLocalDataSource.getPlaceById(id).asMeetingEntry()

    suspend fun update(placeEntry: PlaceEntry) {
        placeEntry.apply {
            placeRemoteDataSource.update(id, placeModel.asPlaceDTO())
            placeLocalDataSource.update(placeModel.asPlaceEntity(id))
        }
    }

    suspend fun delete(placeEntry: PlaceEntry) {
        placeEntry.apply {
            placeRemoteDataSource.delete(id)
            placeLocalDataSource.delete(placeModel.asPlaceEntity(id))
        }
    }

    suspend fun getAllLocalPlacesInFlow() =
        placeLocalDataSource.getAllPlacesInFlow().map { places ->
            places.convertPlaceEntries()
        }

    suspend fun getAllRemotePlacesInFlow() =
        placeRemoteDataSource.getAllPlacesInFlow().map { places ->
            places.convertPlaceEntries()
        }


    suspend fun getAllRemotePlaces() = placeRemoteDataSource.getAllPlaces()

    suspend fun getAllPlacesInFlow(isNetworkConnected: Boolean): Flow<List<PlaceEntry>> {
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
            placeDTO.asPlaceEntry(id)
        }

    private fun List<PlaceEntity>.convertPlaceEntries() =
        map { it.asMeetingEntry() }
}