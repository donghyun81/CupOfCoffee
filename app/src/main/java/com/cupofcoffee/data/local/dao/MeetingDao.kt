package com.cupofcoffee.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.cupofcoffee.data.local.model.MeetingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MeetingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(meetingEntity: MeetingEntity)

    @Query("SELECT * From meetings Where id = :id")
    suspend fun getMeeting(id: String): MeetingEntity

    @Query("SELECT * From meetings Where id = :id")
    fun getMeetingInFlow(id: String): Flow<MeetingEntity>

    @Query("SELECT * From meetings Where id In (:ids)")
    suspend fun getMeetingsByIds(ids: List<String>): List<MeetingEntity>

    @Query("SELECT * From meetings Where id In (:ids)")
    fun getMeetingsByIdsInFlow(ids: List<String>): Flow<List<MeetingEntity>>

    @Query("SELECT * From meetings")
    fun getAllMeetings(): List<MeetingEntity>

    @Update
    suspend fun update(meetingEntity: MeetingEntity)

    @Delete
    suspend fun delete(meetingEntity: MeetingEntity)
}