package com.cupofcoffee.data.remote.datasource

import android.util.Log
import com.cupofcoffee.data.module.AuthTokenManager
import com.cupofcoffee.data.module.AuthTokenManager.getAuthToken
import com.cupofcoffee.data.remote.model.PlaceDTO
import com.cupofcoffee.data.remote.service.PlaceService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class PlaceRemoteDataSource(
    private val placeService: PlaceService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val refreshIntervalMs: Long = 3000
) {

    suspend fun insert(id: String, placeDTO: PlaceDTO) = withContext(ioDispatcher) {
        placeService.insert(
            id = id,
            authToken = getAuthToken()!!,
            placeDTO = placeDTO
        )
    }

    suspend fun getPlaceById(id: String) = withContext(ioDispatcher) {
        try {
            placeService.getPlaceById(
                id = id,
                authToken = getAuthToken()!!,
            )
        } catch (e: Exception) {
            null
        }
    }

    suspend fun update(id: String, placeDTO: PlaceDTO) = withContext(ioDispatcher) {
        placeService.update(
            id = id,
            authToken = getAuthToken()!!,
            placeDTO = placeDTO
        )
    }

    suspend fun delete(id: String) = withContext(ioDispatcher) {
        placeService.delete(
            id = id,
            authToken = getAuthToken()!!
        )
    }

    suspend fun getAllPlaces(): Map<String, PlaceDTO> = withContext(ioDispatcher) {
        try {
            placeService.getPlaces(getAuthToken()!!)
        } catch (e: Exception) {
            emptyMap()
        }
    }

    suspend fun getAllPlacesInFlow(): Flow<Map<String, PlaceDTO>> = withContext(ioDispatcher) {
        flow {
            while (true) {
                emit(tryGetPlaces())
                delay(refreshIntervalMs)
            }
        }
    }

    private suspend fun tryGetPlaces(): Map<String, PlaceDTO> {
        return try {
            placeService.getPlaces(getAuthToken()!!)
        } catch (e: Exception) {
            emptyMap()
        }
    }
}