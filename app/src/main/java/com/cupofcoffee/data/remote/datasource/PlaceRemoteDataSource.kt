package com.cupofcoffee.data.remote.datasource

import com.cupofcoffee.data.remote.model.PlaceDTO
import com.cupofcoffee.data.remote.service.PlaceService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PlaceRemoteDataSource(
    private val placeService: PlaceService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
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
        placeService.getPlaces()
    }
}