package com.cupofcoffee

class MeetingRepositoryImpl(private val meetingDataSource: MeetingDataSource) {

    suspend fun insert(meetingDTO: MeetingDTO) = meetingDataSource.insert(meetingDTO)
}