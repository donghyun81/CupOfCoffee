package com.cupofcoffee.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

private const val MEETING_PATH = "meetings"

interface MeetingService {

    @POST("$MEETING_PATH.json")
    suspend fun insert(@Body meetingDTO: MeetingDTO)
}