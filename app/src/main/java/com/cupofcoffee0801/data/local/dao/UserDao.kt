package com.cupofcoffee0801.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.cupofcoffee0801.data.local.model.UserEntity
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

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT * From users")
    fun getAllUsers(): List<UserEntity>
}