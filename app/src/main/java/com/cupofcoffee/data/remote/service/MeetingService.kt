package com.cupofcoffee.data.remote.service

import com.cupofcoffee.data.remote.RemoteIdWrapper
import com.cupofcoffee.data.remote.model.MeetingDTO
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

private const val MEETING_PATH = "meetings"

interface MeetingService {

    @POST("$MEETING_PATH.json")
    suspend fun insert(
        @Query("auth") authToken: String,
        @Body meetingDTO: MeetingDTO
    ): RemoteIdWrapper

    @GET("$MEETING_PATH/{id}.json")
    suspend fun getMeeting(
        @Path("id") id: String,
        @Query("auth") authToken: String,
    ): MeetingDTO

    @PATCH("$MEETING_PATH/{id}.json")
    suspend fun update(
        @Path("id") id: String,
        @Query("auth") authToken: String,
        @Body meetingDTO: MeetingDTO
    )

    @DELETE("$MEETING_PATH/{id}.json")
    suspend fun delete(

        @Path("id") id: String, @Query("auth") authToken: String,
    )
}