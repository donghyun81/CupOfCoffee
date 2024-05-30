package com.cupofcoffee.data.remote

class MeetingDataSource(private val meetingService: MeetingService) {

    suspend fun insert(meetingDTO: MeetingDTO) = meetingService.insert(meetingDTO).name

    suspend fun getMeeting(id: String) = meetingService.getMeeting(id)

    suspend fun addPeopleId(id: String, meetingDTO: MeetingDTO) =
        meetingService.addPeopleId(id, meetingDTO)
}