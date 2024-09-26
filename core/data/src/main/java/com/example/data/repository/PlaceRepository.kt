package com.example.data.repository


import com.example.data.model.Place
import com.example.database.model.PlaceEntity
import com.example.network.model.PlaceDTO
import kotlinx.coroutines.flow.Flow

interface PlaceRepository {

    suspend fun insertLocal(placeEntity: PlaceEntity)

    suspend fun insertRemote(id: String, placeDTO: PlaceDTO)

    suspend fun insert(place: Place)

    suspend fun getPlaceById(id: String, isNetworkConnected: Boolean = true): Place?
    suspend fun update(place: Place)

    suspend fun delete(place: Place)

    suspend fun deleteAllLocalPlaces()

    suspend fun getAllRemotePlaces(): Map<String, PlaceDTO>

    suspend fun getAllPlacesInFlow(isNetworkConnected: Boolean): Flow<List<Place>>
}