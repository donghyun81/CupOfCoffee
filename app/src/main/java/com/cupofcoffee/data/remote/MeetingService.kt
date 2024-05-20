package com.cupofcoffee.data.remote

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

private const val MEETING_PATH = "meetings"

interface MeetingService {

    @POST("$MEETING_PATH.json")
    suspend fun insert(@Body meetingDTO: MeetingDTO)

    @GET("$MEETING_PATH.json")
    suspend fun getMeetings(): Map<String,MeetingDTO>
}