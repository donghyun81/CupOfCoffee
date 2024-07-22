package com.cupofcoffee.data.repository

import com.cupofcoffee.data.local.datasource.MeetingLocalDataSource
import com.cupofcoffee.data.local.model.MeetingEntity
import com.cupofcoffee.data.local.model.asMeetingEntry
import com.cupofcoffee.data.remote.datasource.MeetingRemoteDataSource
import com.cupofcoffee.data.remote.model.MeetingDTO
import com.cupofcoffee.data.remote.model.asMeetingEntry
import com.cupofcoffee.ui.model.MeetingEntry
import com.cupofcoffee.ui.model.asMeetingDTO
import com.cupofcoffee.ui.model.asMeetingEntity
import kotlinx.coroutines.flow.map

class MeetingRepositoryImpl(
    private val meetingLocalDataSource: MeetingLocalDataSource,
    private val meetingRemoteDataSource: MeetingRemoteDataSource
) {

    suspend fun insertLocal(meetingEntity: MeetingEntity) =
        meetingLocalDataSource.insert(meetingEntity)

    suspend fun insertRemote(meetingDTO: MeetingDTO) = meetingRemoteDataSource.insert(meetingDTO)

    suspend fun getMeeting(id: String, isConnected: Boolean = true) =
        if (isConnected) meetingRemoteDataSource.getMeeting(id).asMeetingEntry(id)
        else meetingLocalDataSource.getMeeting(id).asMeetingEntry()

    suspend fun getMeetingInFlow(id: String, isConnected: Boolean = true) =
        if (isConnected) meetingRemoteDataSource.getMeetingInFlow(id).map {
            it?.asMeetingEntry(id)
        }
        else meetingLocalDataSource.getMeetingInFlow(id).map { it.asMeetingEntry() }

    suspend fun getMeetingsByIds(ids: List<String>, isConnected: Boolean = true) =
        if (isConnected) meetingRemoteDataSource.getMeetingsByIds(ids).convertMeetingEntries()
        else meetingLocalDataSource.getMeetingsByIds(ids).convertMeetingEntries()

    suspend fun getMeetingsByIdsInFlow(ids: List<String>, isConnected: Boolean = true) =
        if (isConnected) meetingRemoteDataSource.getCommentsByIdsInFlow(ids).map {
            it.convertMeetingEntries()
        }
        else meetingLocalDataSource.getMeetingsByIdsInFlow(ids).map { it.convertMeetingEntries() }

    suspend fun getAllLocalMeetings(): List<MeetingEntity> =
        meetingLocalDataSource.getAllMeetings()

    suspend fun update(meetingEntry: MeetingEntry) {
        meetingEntry.apply {
            meetingLocalDataSource.update(asMeetingEntity())
            meetingRemoteDataSource.update(id, asMeetingDTO())
        }
    }

    suspend fun deleteLocal(meetingEntity: MeetingEntity) =
        meetingLocalDataSource.delete(meetingEntity)

    suspend fun deleteRemote(id: String) = meetingRemoteDataSource.delete(id)

    suspend fun delete(meetingEntry: MeetingEntry) {
        meetingLocalDataSource.delete(meetingEntry.asMeetingEntity())
        meetingRemoteDataSource.delete(meetingEntry.id)
    }

    private fun Map<String, MeetingDTO>.convertMeetingEntries() =
        map { entry ->
            val (id, meetingDTO) = entry
            meetingDTO.asMeetingEntry(id)
        }

    private fun List<MeetingEntity>.convertMeetingEntries() =
        map { it.asMeetingEntry() }
}