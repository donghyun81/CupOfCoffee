package com.example.data.repository

import com.example.data.model.User
import com.example.data.model.asUserDTO
import com.example.data.model.asUserEntity
import com.example.data.model.asUserEntry
import com.example.database.datasource.UserLocalDataSource
import com.example.database.model.UserEntity
import com.example.network.datasource.UserRemoteDataSource
import com.example.network.model.UserDTO
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userLocalDataSource: UserLocalDataSource,
    private val userRemoteDataSource: UserRemoteDataSource,
) : UserRepository {

    override suspend fun insertLocal(userEntity: UserEntity) =
        userLocalDataSource.insert(userEntity)

    override suspend fun insertRemote(id: String, userDTO: UserDTO) =
        userRemoteDataSource.insert(id, userDTO)

    override suspend fun getUser(id: String, isNetworkConnected: Boolean) =
        if (isNetworkConnected) userRemoteDataSource.getUserById(id)?.asUserEntry(id)
        else userLocalDataSource.getUserById(id)?.asUserEntry()

    override fun getLocalUserByIdInFlow(id: String) =
        userLocalDataSource.getUserByIdInFlow(id).map { it?.asUserEntry() }

    override suspend fun getLocalUserById(id: String) =
        userLocalDataSource.getUserById(id)?.asUserEntry()

    override suspend fun getRemoteUserById(id: String) =
        userRemoteDataSource.getUserById(id)?.asUserEntry(id)

    override suspend fun getRemoteUsersByIds(ids: List<String>) =
        userRemoteDataSource.getUsersByIds(ids)

    override suspend fun update(user: User) {
        user.apply {
            userLocalDataSource.update(asUserEntity())
            userRemoteDataSource.update(id, asUserDTO())
        }
    }

    override suspend fun updateLocal(userEntity: UserEntity) {
        userLocalDataSource.update(userEntity)
    }

    override suspend fun delete(id: String) {
        userLocalDataSource.delete(id)
        userRemoteDataSource.delete(id)
    }

    override suspend fun deleteLocal(id: String) {
        userLocalDataSource.delete(id)
    }

    override suspend fun getAllUsers(): List<UserEntity> = userLocalDataSource.getAllUsers()
}