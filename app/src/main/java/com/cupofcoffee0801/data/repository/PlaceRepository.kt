package com.cupofcoffee0801.data.repository


import com.cupofcoffee0801.data.local.model.PlaceEntity
import com.cupofcoffee0801.data.remote.model.PlaceDTO
import com.cupofcoffee0801.ui.model.PlaceEntry
import kotlinx.coroutines.flow.Flow

interface PlaceRepository {

    suspend fun insertLocal(placeEntity: PlaceEntity)

    suspend fun insertRemote(id: String, placeDTO: PlaceDTO)

    suspend fun insert(placeEntry: PlaceEntry)

    suspend fun getPlaceById(id: String, isNetworkConnected: Boolean = true): PlaceEntry?
    suspend fun update(placeEntry: PlaceEntry)

    suspend fun delete(placeEntry: PlaceEntry)

    suspend fun deleteAllLocalPlaces()

    suspend fun getAllRemotePlaces(): Map<String, PlaceDTO>

    suspend fun getAllPlacesInFlow(isNetworkConnected: Boolean): Flow<List<PlaceEntry>>
}