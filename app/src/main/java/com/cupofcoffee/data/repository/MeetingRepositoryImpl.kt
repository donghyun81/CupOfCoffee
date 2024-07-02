package com.cupofcoffee.data.repository

import com.cupofcoffee.data.local.datasource.MeetingLocalDataSource
import com.cupofcoffee.data.local.model.MeetingEntity
import com.cupofcoffee.data.remote.datasource.MeetingRemoteDataSource
import com.cupofcoffee.data.remote.model.MeetingDTO

class MeetingRepositoryImpl(
    private val meetingLocalDataSource: MeetingLocalDataSource,
    private val meetingRemoteDataSource: MeetingRemoteDataSource
) {

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