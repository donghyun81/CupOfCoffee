package com.cupofcoffee0801.data.repository

import android.util.Log
import com.cupofcoffee0801.data.local.datasource.UserLocalDataSource
import com.cupofcoffee0801.data.local.model.UserEntity
import com.cupofcoffee0801.data.local.model.asUserEntry
import com.cupofcoffee0801.data.remote.datasource.UserRemoteDataSource
import com.cupofcoffee0801.data.remote.model.UserDTO
import com.cupofcoffee0801.data.remote.model.asUserEntry
import com.cupofcoffee0801.ui.model.UserEntry
import com.cupofcoffee0801.ui.model.asUserDTO
import com.cupofcoffee0801.ui.model.asUserEntity
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

    override suspend fun update(userEntry: UserEntry) {
        userEntry.apply {
            userRemoteDataSource.update(id, asUserDTO())
            userLocalDataSource.update(asUserEntity())
        }
    }

    override suspend fun updateLocal(userEntity: UserEntity) {
        userLocalDataSource.update(userEntity)
    }

    override suspend fun delete(id: String) {
        Log.d("123456","바바보7")
        userRemoteDataSource.delete(id)
        Log.d("123456","바바보8")
        userLocalDataSource.delete(id)
        Log.d("123456","바바보9")
    }

    override suspend fun deleteLocal(id: String) {
        userLocalDataSource.delete(id)
    }

    override suspend fun getAllUsers(): List<UserEntity> = userLocalDataSource.getAllUsers()
}