package com.cupofcoffee.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import retrofit2.http.DELETE

@Dao
interface MeetingDao {

    @Insert
    suspend fun insert(meetingEntity: MeetingEntity)

    @Query("SELECT * From meetings Where id = :id")
    suspend fun getMeeting(id: String): MeetingEntity

    @Update
    suspend fun update(meetingEntity: MeetingEntity)

    @Delete
    suspend fun delete(meetingEntity: MeetingEntity)
}