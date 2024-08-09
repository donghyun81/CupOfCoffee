package com.cupofcoffee0801.data.repository

import com.cupofcoffee0801.data.local.model.MeetingEntity
import com.cupofcoffee0801.data.remote.model.MeetingDTO
import com.cupofcoffee0801.ui.model.MeetingEntry
import kotlinx.coroutines.flow.Flow

interface MeetingRepository {
    suspend fun insertLocal(meetingEntity: MeetingEntity)
    suspend fun insertRemote(meetingDTO: MeetingDTO): String
    suspend fun getMeeting(id: String, isConnected: Boolean = true): MeetingEntry
    suspend fun getMeetingInFlow(id: String, isConnected: Boolean = true): Flow<MeetingEntry?>
    suspend fun getMeetingsByIds(ids: List<String>, isConnected: Boolean = true): List<MeetingEntry>
    suspend fun getMeetingsByIdsInFlow(ids: List<String>, isConnected: Boolean = true)
            : Flow<List<MeetingEntry>>

    suspend fun getRemoteMeetingsByIds(ids: List<String>): Map<String, MeetingDTO>
    suspend fun getAllLocalMeetings(): List<MeetingEntity>
    suspend fun update(meetingEntry: MeetingEntry)
    suspend fun updateLocal(meetingEntity: MeetingEntity)
    suspend fun deleteLocal(id: String)
    suspend fun deleteRemote(id: String)
    suspend fun delete(id: String)
}