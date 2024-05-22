package com.cupofcoffee.data.remote

import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

class MeetingDataSource(private val meetingService: MeetingService) {

    suspend fun insert(meetingDTO: MeetingDTO) = meetingService.insert(meetingDTO).name

    suspend fun getMeetings(): Map<String, MeetingDTO> {
        return try {
            meetingService.getMeetings()
        } catch (e: Exception) {
            emptyMap()
        }
    }
}