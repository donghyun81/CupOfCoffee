package com.cupofcoffee.data.repository

import com.cupofcoffee.data.local.UserDao
import com.cupofcoffee.data.local.UserEntity
import com.cupofcoffee.data.local.asUserEntry
import com.cupofcoffee.data.remote.UserDTO
import com.cupofcoffee.data.remote.UserDataSource
import kotlinx.coroutines.flow.map

class UserRepositoryImpl(
    private val userDao: UserDao,
    private val userDataSource: UserDataSource
) {

    suspend fun insertLocal(userEntity: UserEntity) = userDao.insert(userEntity)

    suspend fun insertRemote(id: String, userDTO: UserDTO) = userDataSource.insert(id, userDTO)

    fun getLocalUserByIdInFlow(id: String) = userDao.getUserByIdInFlow(id).map { it.asUserEntry() }

    fun getLocalUserById(id: String) = userDao.getUserById(id).asUserEntry()

    suspend fun getRemoteUserById(id: String) = userDataSource.getUserById(id)

    suspend fun updateLocal(userEntity: UserEntity) =
        userDao.update(userEntity)

    suspend fun updateRemote(id: String, userDTO: UserDTO) =
        userDataSource.update(id, userDTO)

    suspend fun deleteLocal(userEntity: UserEntity) = userDao.delete(userEntity)

    suspend fun deleteRemote(id: String) = userDataSource.delete(id)
}