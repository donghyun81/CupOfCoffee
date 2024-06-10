package com.cupofcoffee.data.remote

class MeetingDataSource(private val meetingService: MeetingService) {

    suspend fun insert(meetingDTO: MeetingDTO) = meetingService.insert(meetingDTO).name

    suspend fun getMeeting(id: String) = meetingService.getMeeting(id)

    suspend fun update(id: String, meetingDTO: MeetingDTO) =
        meetingService.update(id, meetingDTO)

    suspend fun delete(id: String) = meetingService.delete(id)

}