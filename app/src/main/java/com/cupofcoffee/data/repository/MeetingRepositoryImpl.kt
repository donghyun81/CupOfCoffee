package com.cupofcoffee.data.repository

import com.cupofcoffee.data.local.datasource.MeetingLocalDataSource
import com.cupofcoffee.data.local.model.MeetingEntity
import com.cupofcoffee.data.local.model.asMeetingEntry
import com.cupofcoffee.data.remote.datasource.MeetingRemoteDataSource
import com.cupofcoffee.data.remote.model.MeetingDTO
import com.cupofcoffee.data.remote.model.asMeetingEntry

class MeetingRepositoryImpl(
    private val meetingLocalDataSource: MeetingLocalDataSource,
    private val meetingRemoteDataSource: MeetingRemoteDataSource
) {

    suspend fun insertLocal(meetingEntity: MeetingEntity) =
        meetingLocalDataSource.insert(meetingEntity)

    suspend fun insertRemote(meetingDTO: MeetingDTO) = meetingRemoteDataSource.insert(meetingDTO)

    suspend fun getMeeting(id: String, isConnected: Boolean) =
        if (isConnected) meetingRemoteDataSource.getMeeting(id).asMeetingEntry(id)
        else meetingLocalDataSource.getMeeting(id).asMeetingEntry()

    suspend fun getMeetingsByIds(ids: List<String>, isConnected: Boolean) =
        if (isConnected) meetingRemoteDataSource.getMeetingsByIds(ids).convertMeetingEntries()
        else meetingLocalDataSource.getMeetingsByIds(ids).convertMeetingEntries()

    suspend fun getAllLocalMeetings(): List<MeetingEntity> =
        meetingLocalDataSource.getAllMeetings()

    suspend fun updateLocal(meetingEntity: MeetingEntity) =
        meetingLocalDataSource.update(meetingEntity)

    suspend fun updateRemote(id: String, meetingDTO: MeetingDTO) =
        meetingRemoteDataSource.update(id, meetingDTO)

    suspend fun deleteLocal(meetingEntity: MeetingEntity) =
        meetingLocalDataSource.delete(meetingEntity)

    suspend fun deleteRemote(id: String) = meetingRemoteDataSource.delete(id)

    private fun Map<String, MeetingDTO>.convertMeetingEntries() =
        map { entry ->
            val (id, meetingDTO) = entry
            meetingDTO.asMeetingEntry(id)
        }

    private fun List<MeetingEntity>.convertMeetingEntries() =
        map { it.asMeetingEntry() }
}