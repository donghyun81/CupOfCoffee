package com.cupofcoffee.data.repository

import com.cupofcoffee.data.local.datasource.UserLocalDataSource
import com.cupofcoffee.data.local.model.UserEntity
import com.cupofcoffee.data.remote.datasource.UserRemoteDataSource
import com.cupofcoffee.data.remote.model.UserDTO

class UserRepositoryImpl(
    private val userLocalDataSource: UserLocalDataSource,
    private val userRemoteDataSource: UserRemoteDataSource,
) {

    suspend fun insertLocal(userEntity: UserEntity) = userLocalDataSource.insert(userEntity)

    suspend fun insertRemote(id: String, userDTO: UserDTO) =
        userRemoteDataSource.insert(id, userDTO)

    fun getLocalUserByIdInFlow(id: String) =
        userLocalDataSource.getUserByIdInFlow(id)

    suspend fun getLocalUserById(id: String) = userLocalDataSource.getUserById(id)

    suspend fun getRemoteUsersByIds(ids: List<String>) = userRemoteDataSource.getUsersByIds(ids)

    suspend fun updateLocal(userEntity: UserEntity) =
        userLocalDataSource.update(userEntity)

    suspend fun updateRemote(id: String, userDTO: UserDTO) =
        userRemoteDataSource.update(id, userDTO)

    suspend fun deleteLocal(userEntity: UserEntity) = userLocalDataSource.delete(userEntity)

    suspend fun deleteRemote(id: String) = userRemoteDataSource.delete(id)

    suspend fun getAllUsers(): List<UserEntity> = userLocalDataSource.getAllUsers()
}