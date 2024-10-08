package com.example.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.database.model.MeetingEntity
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

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun delete(id: String)
}