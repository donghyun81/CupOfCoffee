package com.cupofcoffee.data.module

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

    fun getMeetingRepository() = MeetingRepositoryImpl(meetingDataSource)
    fun getPlaceRepository() = PlaceRepositoryImpl(placeDataSource)
    fun getUserRepository() = UserRepositoryImpl(userDataSource)
}