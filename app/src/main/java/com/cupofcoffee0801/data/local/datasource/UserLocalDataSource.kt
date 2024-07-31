package com.cupofcoffee0801.data.local.datasource

import com.cupofcoffee0801.data.local.dao.UserDao
import com.cupofcoffee0801.data.local.model.UserEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class UserLocalDataSource(
    private val dao: UserDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend fun insert(userEntity: UserEntity) = withContext(ioDispatcher) {
        dao.insert(userEntity)
    }

    fun getUserByIdInFlow(id: String): Flow<UserEntity?> =
        dao.getUserByIdInFlow(id)

    suspend fun getUserById(id: String): UserEntity = withContext(ioDispatcher) {
        dao.getUserById(id)
    }


    suspend fun update(userEntity: UserEntity): Int = withContext(ioDispatcher) {
        dao.update(userEntity)
    }

    suspend fun delete(id: String) = withContext(ioDispatcher) {
        dao.delete(id)
    }

    suspend fun getAllUsers() = withContext(ioDispatcher) {
        dao.getAllUsers()
    }
}