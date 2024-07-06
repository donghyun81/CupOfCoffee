package com.cupofcoffee.data.repository

import com.cupofcoffee.data.local.datasource.PlaceLocalDataSource
import com.cupofcoffee.data.local.model.PlaceEntity
import com.cupofcoffee.data.remote.datasource.PlaceRemoteDataSource
import com.cupofcoffee.data.remote.model.PlaceDTO
import com.cupofcoffee.data.remote.websocket.PlaceWebSocketManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

class PlaceRepositoryImpl(
    private val placeLocalDataSource: PlaceLocalDataSource,
    private val placeDataSource: PlaceRemoteDataSource
) {

    private val placesChannel = Channel<Map<String, PlaceDTO>>()
    val placesUpdates: Flow<Map<String, PlaceDTO>> = placesChannel.receiveAsFlow()

    private val webSocketManager = PlaceWebSocketManager(placesChannel)

    fun connectWebSocket() {
        webSocketManager.connect()
    }

    fun closeWebSocket() {
        webSocketManager.close()
    }

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