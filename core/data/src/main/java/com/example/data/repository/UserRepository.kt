package com.example.data.repository

import com.example.data.model.User
import com.example.database.model.UserEntity
import com.example.network.model.UserDTO
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    suspend fun insertLocal(userEntity: UserEntity)

    suspend fun insertRemote(id: String, userDTO: UserDTO): String

    suspend fun getUser(id: String, isNetworkConnected: Boolean = true): User?

    fun getLocalUserByIdInFlow(id: String): Flow<User?>

    suspend fun getLocalUserById(id: String): User?
    suspend fun getRemoteUserById(id: String): User?

    suspend fun getRemoteUsersByIds(ids: List<String>): Map<String, UserDTO>
    suspend fun update(user: User)

    suspend fun updateLocal(userEntity: UserEntity)
    suspend fun delete(id: String)
    suspend fun deleteLocal(id: String)

    suspend fun getAllUsers(): List<UserEntity>
}