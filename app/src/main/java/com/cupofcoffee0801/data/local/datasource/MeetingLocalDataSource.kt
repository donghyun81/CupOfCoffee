package com.cupofcoffee0801.data.local.datasource

import com.cupofcoffee0801.data.local.dao.MeetingDao
import com.cupofcoffee0801.data.local.model.MeetingEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MeetingLocalDataSource @Inject constructor(
    private val meetingDao: MeetingDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend fun insert(meetingEntity: MeetingEntity) = withContext(ioDispatcher) {
        meetingDao.insert(meetingEntity)
    }

    suspend fun getMeeting(id: String): MeetingEntity = withContext(ioDispatcher) {
        meetingDao.getMeeting(id)
    }

    suspend fun getMeetingInFlow(id: String): Flow<MeetingEntity> = withContext(ioDispatcher) {
        meetingDao.getMeetingInFlow(id)
    }

    suspend fun getMeetingsByIds(ids: List<String>): List<MeetingEntity> =
        withContext(ioDispatcher) {
            meetingDao.getMeetingsByIds(ids)
        }

    suspend fun getMeetingsByIdsInFlow(ids: List<String>): Flow<List<MeetingEntity>> =
        withContext(ioDispatcher) {
            meetingDao.getMeetingsByIdsInFlow(ids)
        }


    suspend fun getAllMeetings(): List<MeetingEntity> = withContext(ioDispatcher) {
        meetingDao.getAllMeetings()
    }

    suspend fun update(meetingEntity: MeetingEntity) = withContext(ioDispatcher) {
        meetingDao.update(meetingEntity)
    }

    suspend fun delete(id: String) = withContext(ioDispatcher) {
        meetingDao.delete(id)
    }
}