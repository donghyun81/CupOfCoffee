package com.cupofcoffee0801.data.repository

import com.cupofcoffee0801.data.local.datasource.MeetingLocalDataSource
import com.cupofcoffee0801.data.local.model.MeetingEntity
import com.cupofcoffee0801.data.local.model.asMeetingEntry
import com.cupofcoffee0801.data.remote.datasource.MeetingRemoteDataSource
import com.cupofcoffee0801.data.remote.model.MeetingDTO
import com.cupofcoffee0801.data.remote.model.asMeeting
import com.cupofcoffee0801.ui.model.Meeting
import com.cupofcoffee0801.ui.model.asMeetingDTO
import com.cupofcoffee0801.ui.model.asMeetingEntity
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MeetingRepositoryImpl @Inject constructor(
    private val meetingLocalDataSource: MeetingLocalDataSource,
    private val meetingRemoteDataSource: MeetingRemoteDataSource
) : MeetingRepository {

    override suspend fun insertLocal(meetingEntity: MeetingEntity) =
        meetingLocalDataSource.insert(meetingEntity)

    override suspend fun insertRemote(meetingDTO: MeetingDTO) =
        meetingRemoteDataSource.insert(meetingDTO)

    override suspend fun getMeeting(id: String, isConnected: Boolean): Meeting =
        if (isConnected) meetingRemoteDataSource.getMeeting(id).asMeeting(id)
        else meetingLocalDataSource.getMeeting(id).asMeetingEntry()

    override suspend fun getMeetingInFlow(id: String, isConnected: Boolean) =
        if (isConnected) meetingRemoteDataSource.getMeetingInFlow(id).map {
            it?.asMeeting(id)
        }
        else meetingLocalDataSource.getMeetingInFlow(id).map { it.asMeetingEntry() }


    override suspend fun getMeetingsByIds(ids: List<String>, isConnected: Boolean) =
        if (isConnected) meetingRemoteDataSource.getMeetingsByIds(ids).convertMeetingEntries()
            .sortedByDate()
        else meetingLocalDataSource.getMeetingsByIds(ids).convertMeetingEntries().sortedByDate()

    override suspend fun getMeetingsByIdsInFlow(ids: List<String>, isConnected: Boolean) =
        if (isConnected) meetingRemoteDataSource.getMeetingsByIdsInFlow(ids)
            .map { meetingEntries ->
                meetingEntries.convertMeetingEntries().sortedByDate()
            }
        else meetingLocalDataSource.getMeetingsByIdsInFlow(ids).map {
            it.convertMeetingEntries().sortedByDate()
        }

    override suspend fun getRemoteMeetingsByIds(ids: List<String>): Map<String, MeetingDTO> =
        meetingRemoteDataSource.getMeetingsByIds(ids)

    override suspend fun getAllLocalMeetings(): List<MeetingEntity> =
        meetingLocalDataSource.getAllMeetings()


    override suspend fun update(meeting: Meeting) {
        meeting.apply {
            meetingLocalDataSource.update(asMeetingEntity())
            meetingRemoteDataSource.update(id, asMeetingDTO())
        }
    }

    override suspend fun updateLocal(meetingEntity: MeetingEntity) {
        meetingLocalDataSource.update(meetingEntity)
    }

    override suspend fun deleteLocal(id: String) =
        meetingLocalDataSource.delete(id)

    override suspend fun deleteRemote(id: String) =
        meetingRemoteDataSource.delete(id)

    override suspend fun delete(id: String) {
        meetingLocalDataSource.delete(id)
        meetingRemoteDataSource.delete(id)
    }

    private fun List<Meeting>.sortedByDate() =
        sortedWith(compareBy<Meeting> { it.date }.thenBy { it.time })


    private fun Map<String, MeetingDTO>.convertMeetingEntries() =
        map { entry ->
            val (id, meetingDTO) = entry
            meetingDTO.asMeeting(id)
        }

    private fun List<MeetingEntity>.convertMeetingEntries() =
        map { it.asMeetingEntry() }
}