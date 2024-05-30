package com.cupofcoffee.data.remote

import android.util.Log
import com.cupofcoffee.ui.model.PlaceEntry
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class PlaceDataSource(
    private val placeService: PlaceService,
    private val refreshIntervalMs: Long = 1000
) {

    suspend fun insert(caption: String, placeDTO: PlaceDTO) = placeService.insert(caption, placeDTO)

    suspend fun getPlaceById(id: String): PlaceDTO? {
        return try {
            placeService.getPlaceById(id)
        } catch (e: Exception) {
            null
        }
    }

    fun getPlaces(): Flow<List<PlaceEntry>> {
        return flow {
            while (true) {
                val latestNews = placeService.getPlaces().map { entry ->
                    val (id, place) = entry
                    place.toPlaceEntry(id)
                }
                emit(latestNews) // Emits the result of the request to the flow
                delay(refreshIntervalMs) // Suspends the coroutine for some time
            }
        }
    }
}