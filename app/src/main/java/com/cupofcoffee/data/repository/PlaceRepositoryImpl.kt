package com.cupofcoffee.data.repository

import com.cupofcoffee.data.local.datasource.PlaceLocalDataSource
import com.cupofcoffee.data.local.model.PlaceEntity
import com.cupofcoffee.data.local.model.asPlaceEntry
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

    }

    suspend fun getLocalPlaceById(id: String) = placeLocalDataSource.getPlaceById(id)

    suspend fun getPlaceById(id: String, isNetworkConnected: Boolean = true) =
        if (isNetworkConnected) placeRemoteDataSource.getPlaceById(id)?.asPlaceEntry(id)
        else placeLocalDataSource.getPlaceById(id).asPlaceEntry()

    suspend fun updateLocal(placeEntity: PlaceEntity) = placeLocalDataSource.update(placeEntity)

    suspend fun updateRemote(id: String, placeDTO: PlaceDTO) =
        placeRemoteDataSource.update(id, placeDTO)

    suspend fun update(placeEntry: PlaceEntry, isNetworkConnected: Boolean = true) {
        placeEntry.apply {
            if (isNetworkConnected) {
                placeRemoteDataSource.update(id, placeModel.asPlaceDTO())
                placeLocalDataSource.update(placeModel.asPlaceEntity(id))
            } else {
                placeModel.isSynced = false
                placeLocalDataSource.update(placeModel.asPlaceEntity(id))
            }
        }
    }

    suspend fun deleteLocal(placeEntity: PlaceEntity) = placeLocalDataSource.delete(placeEntity)

    suspend fun deleteRemote(id: String) = placeRemoteDataSource.delete(id)

    suspend fun delete(placeEntry: PlaceEntry) {
        placeEntry.apply {
            placeRemoteDataSource.delete(id)
            placeLocalDataSource.delete(placeModel.asPlaceEntity(id))
        }
    }

    suspend fun getAllLocalPlaces() = placeLocalDataSource.getAllPlaces()

    suspend fun getAllRemotePlaces() = placeRemoteDataSource.getAllPlaces()

    suspend fun getAllPlacesInFlow(networkConnected: Boolean): Flow<List<PlaceEntry>> {
        return if (networkConnected) {
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
        map { it.asPlaceEntry() }
}