package com.cupofcoffee.data.remote

class PlaceDataSource(private val placeService: PlaceService) {

    suspend fun insert(caption: String, placeDTO: PlaceDTO) = placeService.insert(caption, placeDTO)

    suspend fun getPlaceById(id: String): PlaceDTO? {
        return try {
            placeService.getPlaceById(id)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getPlaces(): Map<String, PlaceDTO> {
        return try {
            placeService.getPlaces()
        } catch (e: Exception) {
            emptyMap()
        }
    }
}