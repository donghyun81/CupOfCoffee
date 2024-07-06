package com.cupofcoffee.data.repository

import com.cupofcoffee.data.local.datasource.MeetingLocalDataSource
import com.cupofcoffee.data.local.model.MeetingEntity
import com.cupofcoffee.data.remote.datasource.MeetingRemoteDataSource
import com.cupofcoffee.data.remote.model.MeetingDTO
import com.cupofcoffee.data.remote.websocket.MeetingWebSocketManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

class MeetingRepositoryImpl(
    private val meetingLocalDataSource: MeetingLocalDataSource,
    private val meetingRemoteDataSource: MeetingRemoteDataSource
) {

    private val meetingChannel = Channel<List<MeetingDTO>>()
    val meetingUpdates: Flow<List<MeetingDTO>> = meetingChannel.receiveAsFlow()

    val webSocketManager = MeetingWebSocketManager(meetingChannel)

    fun connectWebSocket() {
        webSocketManager.connect()
    }

    fun closeWebSocket() {
        webSocketManager.close()
    }

    suspend fun insertLocal(meetingEntity: MeetingEntity) =
        meetingLocalDataSource.insert(meetingEntity)

    suspend fun insertRemote(meetingDTO: MeetingDTO) = meetingRemoteDataSource.insert(meetingDTO)

    suspend fun getLocalMeeting(id: String) = meetingLocalDataSource.getMeeting(id)

    suspend fun getLocalMeetingsByIds(ids: List<String>) =
        meetingLocalDataSource.getMeetingsByIds(ids)

    suspend fun getAllMeetings(): List<MeetingEntity> =
        meetingLocalDataSource.getAllMeetings()

    suspend fun updateLocal(meetingEntity: MeetingEntity) =
        meetingLocalDataSource.update(meetingEntity)

    suspend fun updateRemote(id: String, meetingDTO: MeetingDTO) =
        meetingRemoteDataSource.update(id, meetingDTO)

    suspend fun deleteLocal(meetingEntity: MeetingEntity) =
        meetingLocalDataSource.delete(meetingEntity)

    suspend fun deleteRemote(id: String) = meetingRemoteDataSource.delete(id)
}