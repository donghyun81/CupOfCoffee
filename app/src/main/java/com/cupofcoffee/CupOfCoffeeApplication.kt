package com.cupofcoffee

import android.app.Application


class CupOfCoffeeApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        meetingRepository = RepositoryModule.getMeetingRepository()
    }

    companion object {
        lateinit var meetingRepository: MeetingRepositoryImpl
    }
}