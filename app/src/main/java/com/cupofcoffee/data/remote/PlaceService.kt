package com.cupofcoffee.data.remote

import kotlinx.coroutines.flow.Flow
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

private const val PlACE_PATH = "places"

interface PlaceService {

    @PUT("$PlACE_PATH/{id}.json")
    suspend fun insert(@Path("id") caption: String, @Body place: PlaceDTO)

    @GET("$PlACE_PATH/{id}.json")
    suspend fun getPlaceById(
        @Path("id") id: String
    ): PlaceDTO?

    @GET("$PlACE_PATH.json")
    suspend fun getPlaces(): Map<String, PlaceDTO>

}