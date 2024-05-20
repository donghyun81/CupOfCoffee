package com.cupofcoffee.data.module

import com.cupofcoffee.data.remote.MeetingDataSource
import com.cupofcoffee.data.repository.MeetingRepositoryImpl

object RepositoryModule {

    private val meetingDataSource = MeetingDataSource(NetworkModule.getMeetingService())

    fun getMeetingRepository() = MeetingRepositoryImpl(meetingDataSource)
}