package com.cupofcoffee.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.cupofcoffee.data.local.model.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userEntity: UserEntity)

    @Query("SELECT * From users where id = :id")
    fun getUserByIdInFlow(id: String): Flow<UserEntity?>

    @Query("SELECT * From users where id = :id")
    fun getUserById(id: String): UserEntity

    @Update
    suspend fun update(userEntity: UserEntity): Int

    @Delete
    suspend fun delete(userEntity: UserEntity)

    @Query("SELECT * From users Where id In (:ids)")
    suspend fun getUsersByIds(ids: List<String>): List<UserEntity>

    @Query("SELECT * From users")
    fun getAllUsers(): List<UserEntity>
}