package com.cupofcoffee.data.remote.datasource

import com.cupofcoffee.data.remote.model.PlaceDTO
import com.cupofcoffee.data.remote.model.asPlaceEntry
import com.cupofcoffee.data.remote.service.PlaceService
import com.cupofcoffee.ui.model.PlaceEntry
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
        placeService.insert(id, placeDTO)
    }

    suspend fun getPlaceById(id: String) = withContext(ioDispatcher) {
        try {
            placeService.getPlaceById(id)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun update(id: String, placeDTO: PlaceDTO) = withContext(ioDispatcher) {
        placeService.update(id, placeDTO)
    }

    suspend fun delete(id: String) = withContext(ioDispatcher) {
        placeService.delete(id)
    }

    suspend fun getAllPlaces(): Map<String, PlaceDTO> = withContext(ioDispatcher) {
        try {
            placeService.getPlaces()
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
            placeService.getPlaces()
        } catch (e: Exception) {
            emptyMap()
        }
    }
}