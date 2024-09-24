package com.example.data.repository

import com.example.data.model.Meeting
import com.example.data.model.asPlace
import com.example.data.model.asMeetingDTO
import com.example.data.model.asMeetingEntity
import com.example.database.datasource.MeetingLocalDataSource
import com.example.database.model.MeetingEntity
import com.example.network.datasource.MeetingRemoteDataSource
import com.example.network.model.MeetingDTO
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
        if (isConnected) meetingRemoteDataSource.getMeeting(id).asPlace(id)
        else meetingLocalDataSource.getMeeting(id).asPlace()

    override suspend fun getMeetingInFlow(id: String, isConnected: Boolean) =
        if (isConnected) meetingRemoteDataSource.getMeetingInFlow(id).map {
            it?.asPlace(id)
        }
        else meetingLocalDataSource.getMeetingInFlow(id).map { it.asPlace() }


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
            meetingDTO.asPlace(id)
        }

    private fun List<MeetingEntity>.convertMeetingEntries() =
        map { it.asPlace() }
}