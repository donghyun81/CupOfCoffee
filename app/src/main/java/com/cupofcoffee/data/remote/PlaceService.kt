package com.cupofcoffee.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

private const val MEETING_PATH = "places"

interface PlaceService {

    @PUT("$MEETING_PATH/{id}.json")
    suspend fun insert(@Path("id") caption: String, @Body place: PlaceDTO)

    @GET("$MEETING_PATH/{id}.json")
    suspend fun getPlaceByCaption(
        @Path("id") position: String
    ): PlaceDTO?

}