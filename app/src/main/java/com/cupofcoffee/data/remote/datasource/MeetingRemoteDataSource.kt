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

    suspend fun getMeeting(id: String) = withContext(ioDispatcher) {
        meetingService.getMeeting(id)
    }

    suspend fun getMeetingsByIds(ids: List<String>) = withContext(ioDispatcher) {
        ids.associateWith { id -> meetingService.getMeeting(id) }
    }

    suspend fun update(id: String, meetingDTO: MeetingDTO) = withContext(ioDispatcher) {
        meetingService.update(id, meetingDTO)
    }

    suspend fun delete(id: String) = withContext(ioDispatcher) {
        meetingService.delete(id)
    }
}