package com.cupofcoffee.data.local.datasource

import com.cupofcoffee.data.DataResult
import com.cupofcoffee.data.DataResult.Error
import com.cupofcoffee.data.DataResult.Success
import com.cupofcoffee.data.local.dao.UserDao
import com.cupofcoffee.data.local.model.UserEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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

    suspend fun delete(userEntity: UserEntity) = withContext(ioDispatcher) {
        dao.delete(userEntity)
    }

    suspend fun getUsersByIds(ids: List<String>) = withContext(ioDispatcher) {
        dao.getUsersByIds(ids)
    }

    suspend fun getAllUsers() = withContext(ioDispatcher) {
        dao.getAllUsers()
    }
}