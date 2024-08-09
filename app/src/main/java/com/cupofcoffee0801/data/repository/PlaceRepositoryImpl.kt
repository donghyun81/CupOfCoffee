package com.cupofcoffee0801.data.repository

import com.cupofcoffee0801.data.local.datasource.PlaceLocalDataSource
import com.cupofcoffee0801.data.local.model.PlaceEntity
import com.cupofcoffee0801.data.local.model.asMeetingEntry
import com.cupofcoffee0801.data.remote.datasource.PlaceRemoteDataSource
import com.cupofcoffee0801.data.remote.model.PlaceDTO
import com.cupofcoffee0801.data.remote.model.asPlaceEntry
import com.cupofcoffee0801.ui.model.PlaceEntry
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

    override suspend fun insert(placeEntry: PlaceEntry) {
        placeEntry.apply {
            placeLocalDataSource.insert(placeModel.asPlaceEntity(id))
            placeRemoteDataSource.insert(id, placeModel.asPlaceDTO())
        }
    }

    override suspend fun getPlaceById(id: String, isNetworkConnected: Boolean) =
        if (isNetworkConnected) placeRemoteDataSource.getPlaceById(id)?.asPlaceEntry(id)
        else placeLocalDataSource.getPlaceById(id).asMeetingEntry()

    override suspend fun update(placeEntry: PlaceEntry) {
        placeEntry.apply {
            placeRemoteDataSource.update(id, placeModel.asPlaceDTO())
            placeLocalDataSource.update(placeModel.asPlaceEntity(id))
        }
    }

    override suspend fun delete(placeEntry: PlaceEntry) {
        placeEntry.apply {
            placeRemoteDataSource.delete(id)
            placeLocalDataSource.delete(placeModel.asPlaceEntity(id))
        }
    }

    override suspend fun getAllRemotePlaces() = placeRemoteDataSource.getAllPlaces()

    override suspend fun getAllPlacesInFlow(isNetworkConnected: Boolean): Flow<List<PlaceEntry>> {
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