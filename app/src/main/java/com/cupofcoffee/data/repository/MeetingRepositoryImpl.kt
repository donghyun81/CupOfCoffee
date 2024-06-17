package com.cupofcoffee.data.repository

import com.cupofcoffee.data.local.MeetingDao
import com.cupofcoffee.data.local.MeetingEntity
import com.cupofcoffee.data.remote.MeetingDTO
import com.cupofcoffee.data.remote.MeetingDataSource

class MeetingRepositoryImpl(
    private val meetingDao: MeetingDao,
    private val meetingDataSource: MeetingDataSource
) {

    suspend fun insertLocal(meetingEntity: MeetingEntity) = meetingDao.insert(meetingEntity)
    suspend fun insertRemote(meetingDTO: MeetingDTO) = meetingDataSource.insert(meetingDTO)

    suspend fun getLocalMeeting(id: String) = meetingDao.getMeeting(id)

    suspend fun getRemoteMeeting(id: String) = meetingDataSource.getMeeting(id)

    suspend fun updateLocal(meetingEntity: MeetingEntity) =
        meetingDao.update(meetingEntity)

    suspend fun updateRemote(id: String, meetingDTO: MeetingDTO) =
        meetingDataSource.update(id, meetingDTO)

    suspend fun deleteLocal(meetingEntity: MeetingEntity) = meetingDao.delete(meetingEntity)

    suspend fun deleteRemote(id: String) = meetingDataSource.delete(id)
}