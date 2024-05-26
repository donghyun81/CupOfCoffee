package com.cupofcoffee.data.remote

import android.util.Log
import com.cupofcoffee.ui.model.MeetingModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

class MeetingDataSource(private val meetingService: MeetingService) {

    suspend fun insert(meetingDTO: MeetingDTO) = meetingService.insert(meetingDTO).name

    suspend fun getMeeting(id: String) = meetingService.getMeeting(id)

    suspend fun addPeopleId(id: String, meetingDTO: MeetingDTO) =
        meetingService.addPeopleId(id, meetingDTO)
}