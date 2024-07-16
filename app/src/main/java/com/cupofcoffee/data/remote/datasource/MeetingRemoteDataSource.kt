package com.cupofcoffee.data.remote.datasource

import android.util.Log
import com.cupofcoffee.data.remote.model.CommentDTO
import com.cupofcoffee.data.remote.model.MeetingDTO
import com.cupofcoffee.data.remote.service.MeetingService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class MeetingRemoteDataSource(
    private val meetingService: MeetingService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val refreshIntervalMs: Long = 3000L
) {

    suspend fun insert(meetingDTO: MeetingDTO) = withContext(ioDispatcher) {
        meetingService.insert(meetingDTO).name
    }

    suspend fun getMeeting(id: String) = withContext(ioDispatcher) {
        meetingService.getMeeting(id)
    }

    suspend fun getMeetingInFlow(id: String) = withContext(ioDispatcher) {
        flow {
            try {
                while (true) {
                    emit(meetingService.getMeeting(id))
                    delay(refreshIntervalMs)
                }
            } catch (e: Exception) {
                emit(null)
            }
        }
    }

    suspend fun getMeetingsByIds(ids: List<String>) = withContext(ioDispatcher) {
        ids.associateWith { id -> meetingService.getMeeting(id) }
    }

    suspend fun getCommentsByIdsInFlow(ids: List<String>): Flow<Map<String, MeetingDTO>> =
        withContext(ioDispatcher) {
            flow {
                while (true) {
                    emit(tryGetComments(ids = ids))
                    delay(refreshIntervalMs)
                }
            }
        }

    private suspend fun tryGetComments(ids: List<String>): Map<String, MeetingDTO> {
        return try {
            ids.associateWith { id ->
                meetingService.getMeeting(id)
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    suspend fun update(id: String, meetingDTO: MeetingDTO) = withContext(ioDispatcher) {
        meetingService.update(id, meetingDTO)
    }

    suspend fun delete(id: String) = withContext(ioDispatcher) {
        meetingService.delete(id)
    }
}