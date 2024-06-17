package com.cupofcoffee.data.module

import android.content.Context
import androidx.room.RoomDatabase
import com.cupofcoffee.CupOfCoffeeApplication
import com.cupofcoffee.data.local.CupOfCoffeeDatabase
import com.cupofcoffee.data.remote.MeetingDataSource
import com.cupofcoffee.data.remote.PlaceDataSource
import com.cupofcoffee.data.remote.UserDataSource
import com.cupofcoffee.data.repository.MeetingRepositoryImpl
import com.cupofcoffee.data.repository.PlaceRepositoryImpl
import com.cupofcoffee.data.repository.UserRepositoryImpl

object RepositoryModule {

    private val meetingDataSource = MeetingDataSource(NetworkModule.getMeetingService())
    private val placeDataSource = PlaceDataSource(NetworkModule.getPlaceService())
    private val userDataSource = UserDataSource(NetworkModule.getUserService())

    private lateinit var database: CupOfCoffeeDatabase

    private val meetingDao by lazy { LocalModule.provideMeetingDao(database) }
    private val placeDao by lazy { LocalModule.providePlaceDao(database) }
    private val userDao by lazy { LocalModule.provideUserDao(database) }

    fun initDatabase(context: Context) {
        database = LocalModule.provideDatabase(context)
    }

    fun getMeetingRepository() = MeetingRepositoryImpl(meetingDao, meetingDataSource)
    fun getPlaceRepository() = PlaceRepositoryImpl(placeDao, placeDataSource)
    fun getUserRepository() = UserRepositoryImpl(userDao, userDataSource)
}