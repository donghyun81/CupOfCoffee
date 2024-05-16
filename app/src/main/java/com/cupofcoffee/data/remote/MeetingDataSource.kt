package com.cupofcoffee.data.remote

class MeetingDataSource(private val meetingService: MeetingService) {

    suspend fun insert(meetingDTO: MeetingDTO) = meetingService.insert(meetingDTO)
}