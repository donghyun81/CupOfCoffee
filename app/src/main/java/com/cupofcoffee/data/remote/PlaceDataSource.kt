package com.cupofcoffee.data.remote

class PlaceDataSource(private val placeService: PlaceService) {

    suspend fun insert(caption: String, placeDTO: PlaceDTO) = placeService.insert(caption, placeDTO)

    suspend fun getPlaceByPosition(position: String): PlaceDTO? {
        return try {
            placeService.getPlaceByCaption(position)
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