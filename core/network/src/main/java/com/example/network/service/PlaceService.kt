package com.example.network.service

import com.example.network.model.PlaceDTO
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

private const val PlACE_PATH = "places"


interface PlaceService {

    @PUT("$PlACE_PATH/{id}.json")
    suspend fun insert(
        @Path("id") id: String,
        @Query("auth") authToken: String,
        @Body placeDTO: PlaceDTO
    )

    @GET("$PlACE_PATH/{id}.json")
    suspend fun getPlaceById(
        @Path("id") id: String,
        @Query("auth") authToken: String,
    ): PlaceDTO?

    @PATCH("$PlACE_PATH/{id}.json")
    suspend fun update(
        @Path("id") id: String,
        @Query("auth") authToken: String,
        @Body placeDTO: PlaceDTO
    )

    @GET("$PlACE_PATH.json")
    suspend fun getPlaces(
        @Query("auth") authToken: String
    ): Map<String, PlaceDTO>

    @DELETE("$PlACE_PATH/{id}.json")
    suspend fun delete(
        @Path("id") id: String,
        @Query("auth") authToken: String
    )
}