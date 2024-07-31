package com.cupofcoffee0801.data.module

import android.content.Context
import com.cupofcoffee0801.data.local.CupOfCoffeeDatabase
import com.cupofcoffee0801.data.remote.datasource.CommentRemoteDataSource
import com.cupofcoffee0801.data.remote.datasource.MeetingRemoteDataSource
import com.cupofcoffee0801.data.remote.datasource.PlaceRemoteDataSource
import com.cupofcoffee0801.data.remote.datasource.UserRemoteDataSource
import com.cupofcoffee0801.data.repository.CommentRepositoryImpl
import com.cupofcoffee0801.data.repository.MeetingRepositoryImpl
import com.cupofcoffee0801.data.repository.PlaceRepositoryImpl
import com.cupofcoffee0801.data.repository.PreferencesRepositoryImpl
import com.cupofcoffee0801.data.repository.UserRepositoryImpl

object RepositoryModule {

    private val meetingRemoteDataSource = MeetingRemoteDataSource(NetworkModule.getMeetingService())
    private val placeDataSource = PlaceRemoteDataSource(NetworkModule.getPlaceService())
    private val userRemoteDataSource = UserRemoteDataSource(NetworkModule.getUserService())
    private val commentRemoteDataSource = CommentRemoteDataSource(NetworkModule.getCommentService())

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

    fun getCommentRepository() = CommentRepositoryImpl(commentRemoteDataSource)

    fun getPreferencesRepository(context: Context) = PreferencesRepositoryImpl(context)
}