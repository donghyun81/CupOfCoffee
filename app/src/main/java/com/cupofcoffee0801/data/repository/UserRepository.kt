package com.cupofcoffee0801.data.repository

import com.cupofcoffee0801.data.local.model.UserEntity
import com.cupofcoffee0801.data.remote.model.UserDTO
import com.cupofcoffee0801.ui.model.UserEntry
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    suspend fun insertLocal(userEntity: UserEntity)

    suspend fun insertRemote(id: String, userDTO: UserDTO): String

    suspend fun getUser(id: String, isNetworkConnected: Boolean = true): UserEntry?

    fun getLocalUserByIdInFlow(id: String): Flow<UserEntry?>

    suspend fun getLocalUserById(id: String): UserEntry?
    suspend fun getRemoteUserById(id: String): UserEntry?

    suspend fun getRemoteUsersByIds(ids: List<String>): Map<String, UserDTO>
    suspend fun update(userEntry: UserEntry)

    suspend fun updateLocal(userEntity: UserEntity)
    suspend fun delete(id: String)
    suspend fun deleteLocal(id: String)

    suspend fun getAllUsers(): List<UserEntity>
}