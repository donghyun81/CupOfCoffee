package com.cupofcoffee.data.remote

import com.cupofcoffee.ui.model.PlaceEntry
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class PlaceDataSource(
    private val placeService: PlaceService,
    private val refreshIntervalMs: Long = 1000
) {

    suspend fun insert(id: String, placeDTO: PlaceDTO) = placeService.insert(id, placeDTO)

    suspend fun getPlaceById(id: String): PlaceDTO? {
        return try {
            placeService.getPlaceById(id)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun update(id: String, placeDTO: PlaceDTO) = placeService.update(id, placeDTO)

    suspend fun delete(id: String) = placeService.delete(id)

    fun getPlaces(): Flow<List<PlaceEntry>> {
        return flow {
            while (true) {
                emit(tryGetPlaces())
                delay(refreshIntervalMs)
            }
        }
    }

    private suspend fun tryGetPlaces(): List<PlaceEntry> {
        return try {
            val latestNews = placeService.getPlaces().map { entry ->
                val (id, place) = entry
                place.asPlaceEntry(id)
            }
            latestNews
        } catch (e: Exception) {
            emptyList()
        }
    }
}