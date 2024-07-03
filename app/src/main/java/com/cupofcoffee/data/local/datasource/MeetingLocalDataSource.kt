package com.cupofcoffee.data.local.datasource

import com.cupofcoffee.data.local.dao.MeetingDao
import com.cupofcoffee.data.local.model.MeetingEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MeetingLocalDataSource(
    private val meetingDao: MeetingDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend fun insert(meetingEntity: MeetingEntity) = withContext(ioDispatcher) {
        meetingDao.insert(meetingEntity)
    }

    suspend fun getMeeting(id: String): MeetingEntity = withContext(ioDispatcher) {
        meetingDao.getMeeting(id)
    }

    suspend fun getMeetingsByIds(ids: List<String>): List<MeetingEntity> = withContext(ioDispatcher) {
            meetingDao.getMeetingsByIds(ids)
        }


    suspend fun getAllMeetings(): List<MeetingEntity> = withContext(ioDispatcher) {
        meetingDao.getAllMeetings()
    }

    suspend fun update(meetingEntity: MeetingEntity) = withContext(ioDispatcher) {
        meetingDao.update(meetingEntity)
    }

    suspend fun delete(meetingEntity: MeetingEntity) = withContext(ioDispatcher) {
        meetingDao.delete(meetingEntity)
    }
}