package com.cupofcoffee0801.data.repository

import com.cupofcoffee0801.data.local.datasource.MeetingLocalDataSource
import com.cupofcoffee0801.data.local.model.MeetingEntity
import com.cupofcoffee0801.data.local.model.asMeetingEntry
import com.cupofcoffee0801.data.remote.datasource.MeetingRemoteDataSource
import com.cupofcoffee0801.data.remote.model.MeetingDTO
import com.cupofcoffee0801.data.remote.model.asMeetingEntry
import com.cupofcoffee0801.ui.model.MeetingEntry
import com.cupofcoffee0801.ui.model.asMeetingDTO
import com.cupofcoffee0801.ui.model.asMeetingEntity
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
            .sortedByDate()
        else meetingLocalDataSource.getMeetingsByIds(ids).convertMeetingEntries().sortedByDate()

    suspend fun getMeetingsByIdsInFlow(ids: List<String>, isConnected: Boolean = true) =
        if (isConnected) meetingRemoteDataSource.getMeetingsByIdsInFlow(ids).map { meetingEntries ->
            meetingEntries.convertMeetingEntries().sortedByDate()
        }
        else meetingLocalDataSource.getMeetingsByIdsInFlow(ids).map {
            it.convertMeetingEntries().sortedByDate()
        }

    suspend fun getAllLocalMeetings(): List<MeetingEntity> =
        meetingLocalDataSource.getAllMeetings()

    suspend fun getRemoteMeetingsByIds(ids: List<String>) =
        meetingRemoteDataSource.getMeetingsByIds(ids)

    suspend fun update(meetingEntry: MeetingEntry) {
        meetingEntry.apply {
            meetingLocalDataSource.update(asMeetingEntity())
            meetingRemoteDataSource.update(id, asMeetingDTO())
        }
    }

    suspend fun updateLocal(meetingEntity: MeetingEntity) {
        meetingLocalDataSource.update(meetingEntity)
    }

    suspend fun deleteLocal(id: String) =
        meetingLocalDataSource.delete(id)

    suspend fun delete(id: String) {
        meetingLocalDataSource.delete(id)
        meetingRemoteDataSource.delete(id)
    }

    private fun List<MeetingEntry>.sortedByDate() =
        sortedWith(compareBy<MeetingEntry> { it.meetingModel.date }.thenBy { it.meetingModel.time })


    private fun Map<String, MeetingDTO>.convertMeetingEntries() =
        map { entry ->
            val (id, meetingDTO) = entry
            meetingDTO.asMeetingEntry(id)
        }

    private fun List<MeetingEntity>.convertMeetingEntries() =
        map { it.asMeetingEntry() }
}