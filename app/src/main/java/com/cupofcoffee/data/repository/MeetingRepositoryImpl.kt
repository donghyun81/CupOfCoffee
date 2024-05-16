package com.cupofcoffee.data.repository

import com.cupofcoffee.data.remote.MeetingDTO
import com.cupofcoffee.data.remote.MeetingDataSource

class MeetingRepositoryImpl(private val meetingDataSource: MeetingDataSource) {

    suspend fun insert(meetingDTO: MeetingDTO) = meetingDataSource.insert(meetingDTO)
}