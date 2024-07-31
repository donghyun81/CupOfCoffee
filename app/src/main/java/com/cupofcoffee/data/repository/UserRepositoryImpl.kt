package com.cupofcoffee.data.repository

import com.cupofcoffee.data.local.datasource.UserLocalDataSource
import com.cupofcoffee.data.local.model.UserEntity
import com.cupofcoffee.data.local.model.asUserEntry
import com.cupofcoffee.data.remote.datasource.UserRemoteDataSource
import com.cupofcoffee.data.remote.model.UserDTO
import com.cupofcoffee.data.remote.model.asUserEntry
import com.cupofcoffee.ui.model.UserEntry
import com.cupofcoffee.ui.model.asUserDTO
import com.cupofcoffee.ui.model.asUserEntity
import kotlinx.coroutines.flow.map

class UserRepositoryImpl(
    private val userLocalDataSource: UserLocalDataSource,
    private val userRemoteDataSource: UserRemoteDataSource,
) {

    suspend fun insertLocal(userEntity: UserEntity) = userLocalDataSource.insert(userEntity)

    suspend fun insertRemote(id: String, userDTO: UserDTO) =
        userRemoteDataSource.insert(id, userDTO)

    fun getLocalUserByIdInFlow(id: String) =
        userLocalDataSource.getUserByIdInFlow(id).map { it?.asUserEntry() }

    suspend fun getLocalUserById(id: String) = userLocalDataSource.getUserById(id).asUserEntry()

    suspend fun getRemoteUserById(id: String) =
        userRemoteDataSource.getUserById(id)?.asUserEntry(id)

    suspend fun getRemoteUsersByIds(ids: List<String>) = userRemoteDataSource.getUsersByIds(ids)

    suspend fun update(userEntry: UserEntry) {
        userEntry.apply {
            userRemoteDataSource.update(id, asUserDTO())
            userLocalDataSource.update(asUserEntity())
        }
    }

    suspend fun updateLocal(userEntity: UserEntity) {
        userLocalDataSource.update(userEntity)
    }

    suspend fun delete(id: String) {
        userRemoteDataSource.delete(id)
        userLocalDataSource.delete(id)
    }

    suspend fun deleteLocal(id: String) {
        userLocalDataSource.delete(id)
    }

    suspend fun getAllUsers(): List<UserEntity> = userLocalDataSource.getAllUsers()
}