package com.cupofcoffee.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

private const val PlACE_PATH = "places"

interface PlaceService {

    @PUT("$PlACE_PATH/{id}.json")
    suspend fun insert(@Path("id") caption: String, @Body place: PlaceDTO)

    @GET("$PlACE_PATH/{id}.json")
    suspend fun getPlaceById(
        @Path("id") id: String
    ): PlaceDTO?

    @PATCH("$PlACE_PATH/{id}.json")
    suspend fun update(
        @Path("id") id: String,
        @Body place: PlaceDTO
    )

    @GET("$PlACE_PATH.json")
    suspend fun getPlaces(): Map<String, PlaceDTO>

    @DELETE("$PlACE_PATH/{id}.json")
    suspend fun delete(
        @Path("id") id: String
    )
}