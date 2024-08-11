package com.cupofcoffee0801.data.remote.datasource

import com.cupofcoffee0801.data.module.AuthTokenManager.getAuthToken
import com.cupofcoffee0801.data.module.IoDispatcher
import com.cupofcoffee0801.data.module.RefreshInterval
import com.cupofcoffee0801.data.remote.model.MeetingDTO
import com.cupofcoffee0801.data.remote.service.MeetingService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MeetingRemoteDataSource @Inject constructor(
    private val meetingService: MeetingService,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @RefreshInterval private val refreshIntervalMs: Long
) {

    suspend fun insert(meetingDTO: MeetingDTO) = withContext(ioDispatcher) {
        meetingService.insert(getAuthToken()!!, meetingDTO)
    }

    suspend fun getMeeting(id: String) = withContext(ioDispatcher) {
        meetingService.getMeeting(
            id = id,
            authToken = getAuthToken()!!
        )
    }

    suspend fun getMeetingInFlow(id: String) = withContext(ioDispatcher) {
        flow {
            try {
                while (true) {
                    emit(
                        meetingService.getMeeting(
                            id = id,
                            authToken = getAuthToken()!!
                        )
                    )
                    delay(refreshIntervalMs)
                }
            } catch (e: Exception) {
                emit(null)
            }
        }
    }

    suspend fun getMeetingsByIds(ids: List<String>) = withContext(ioDispatcher) {
        ids.mapNotNull { id ->
            try {
                id to meetingService.getMeeting(
                    id = id,
                    authToken = getAuthToken()!!
                )
            } catch (e: Exception) {
                null
            }
        }.toMap()
    }

    suspend fun getMeetingsByIdsInFlow(ids: List<String>): Flow<Map<String, MeetingDTO>> =
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
                meetingService.getMeeting(
                    id = id,
                    authToken = getAuthToken()!!
                )
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    suspend fun update(id: String, meetingDTO: MeetingDTO) = withContext(ioDispatcher) {
        meetingService.update(
            id = id,
            authToken = getAuthToken()!!,
            meetingDTO = meetingDTO
        )
    }

    suspend fun delete(id: String) = withContext(ioDispatcher) {
        meetingService.delete(
            id = id,
            authToken = getAuthToken()!!
        )
    }
}