package com.cupofcoffee.data.remote.datasource

import com.cupofcoffee.data.remote.model.MeetingDTO
import com.cupofcoffee.data.remote.service.MeetingService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MeetingRemoteDataSource(
    private val meetingService: MeetingService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun insert(meetingDTO: MeetingDTO) = withContext(ioDispatcher) {
        meetingService.insert(meetingDTO).name
    }

    suspend fun getMeeting(id: String) = meetingService.getMeeting(id)

    suspend fun update(id: String, meetingDTO: MeetingDTO) = withContext(ioDispatcher) {
        meetingService.update(id, meetingDTO)
    }

    suspend fun delete(id: String) = withContext(ioDispatcher) {
        meetingService.delete(id)
    }
}