package com.example.data.repository

import com.example.data.model.Meeting
import com.example.database.model.MeetingEntity
import com.example.network.RemoteIdWrapper
import com.example.network.model.MeetingDTO
import kotlinx.coroutines.flow.Flow

interface MeetingRepository {
    suspend fun insertLocal(meetingEntity: MeetingEntity)
    suspend fun insertRemote(meetingDTO: MeetingDTO): RemoteIdWrapper
    suspend fun getMeeting(id: String, isConnected: Boolean = true): Meeting
    suspend fun getMeetingInFlow(id: String, isConnected: Boolean = true): Flow<Meeting?>
    suspend fun getMeetingsByIds(ids: List<String>, isConnected: Boolean = true): List<Meeting>
    suspend fun getMeetingsByIdsInFlow(ids: List<String>, isConnected: Boolean = true)
            : Flow<List<Meeting>>

    suspend fun getRemoteMeetingsByIds(ids: List<String>): Map<String, MeetingDTO>
    suspend fun getAllLocalMeetings(): List<MeetingEntity>
    suspend fun update(meeting: Meeting)
    suspend fun updateLocal(meetingEntity: MeetingEntity)
    suspend fun deleteLocal(id: String)
    suspend fun deleteRemote(id: String)
    suspend fun delete(id: String)
}