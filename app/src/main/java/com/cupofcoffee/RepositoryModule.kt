package com.cupofcoffee

object RepositoryModule {

    private val networkModule = NetworkModule

    private val meetingDataSource = MeetingDataSource(networkModule.getMeetingService())

    fun getMeetingRepository() = MeetingRepositoryImpl(meetingDataSource)
}