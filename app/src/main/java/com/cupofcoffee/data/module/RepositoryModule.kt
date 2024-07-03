package com.cupofcoffee.data.module

import android.content.Context
import com.cupofcoffee.data.local.CupOfCoffeeDatabase
import com.cupofcoffee.data.remote.datasource.MeetingRemoteDataSource
import com.cupofcoffee.data.remote.datasource.PlaceRemoteDataSource
import com.cupofcoffee.data.remote.datasource.UserRemoteDataSource
import com.cupofcoffee.data.repository.MeetingRepositoryImpl
import com.cupofcoffee.data.repository.PlaceRepositoryImpl
import com.cupofcoffee.data.repository.UserRepositoryImpl

object RepositoryModule {

    private val meetingRemoteDataSource = MeetingRemoteDataSource(NetworkModule.getMeetingService())
    private val placeDataSource = PlaceRemoteDataSource(NetworkModule.getPlaceService())
    private val userRemoteDataSource = UserRemoteDataSource(NetworkModule.getUserService())

    private lateinit var database: CupOfCoffeeDatabase

    private val meetingLocalDataSource by lazy { LocalModule.provideMeetingLocalDataSource(database) }
    private val placeLocalDataSource by lazy { LocalModule.providePlaceLocalDataSource(database) }
    private val userLocalDataSource by lazy { LocalModule.provideUserLocalDataSource(database) }

    fun initDatabase(context: Context) {
        database = LocalModule.provideDatabase(context)
    }

    fun getMeetingRepository() =
        MeetingRepositoryImpl(meetingLocalDataSource, meetingRemoteDataSource)

    fun getPlaceRepository() = PlaceRepositoryImpl(placeLocalDataSource, placeDataSource)
    fun getUserRepository() = UserRepositoryImpl(userLocalDataSource, userRemoteDataSource)
}