package com.example.database.datasource

import com.example.common.di.IoDispatcher
import com.example.database.model.UserEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UserLocalDataSource @Inject constructor(
    private val dao: com.example.database.dao.UserDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun insert(userEntity: UserEntity) = withContext(ioDispatcher) {
        dao.insert(userEntity)
    }

    fun getUserByIdInFlow(id: String): Flow<UserEntity?> =
        dao.getUserByIdInFlow(id)

    suspend fun getUserById(id: String): UserEntity? = withContext(ioDispatcher) {
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