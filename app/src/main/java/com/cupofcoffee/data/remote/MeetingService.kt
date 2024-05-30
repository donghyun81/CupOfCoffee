package com.cupofcoffee.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

private const val MEETING_PATH = "meetings"

interface MeetingService {

    @POST("$MEETING_PATH.json")
    suspend fun insert(@Body meetingDTO: MeetingDTO): RemoteIdWrapper

    @GET("$MEETING_PATH/{id}.json")
    suspend fun getMeeting(
        @Path("id") id: String
    ): MeetingDTO

    @PATCH("$MEETING_PATH/{id}.json")
    suspend fun addPeopleId(
        @Path("id") id: String,
        @Body meetingDTO: MeetingDTO
    )
}