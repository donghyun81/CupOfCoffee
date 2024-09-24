package com.example.network.datasource

import com.example.common.di.AuthTokenManager.getAuthToken
import com.example.common.di.IoDispatcher
import com.example.common.di.RefreshInterval
import com.example.network.model.PlaceDTO
import com.example.network.service.PlaceService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PlaceRemoteDataSource @Inject constructor(
    private val placeService: PlaceService,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @RefreshInterval private val refreshIntervalMs: Long
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