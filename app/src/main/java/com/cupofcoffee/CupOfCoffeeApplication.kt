package com.cupofcoffee

import android.app.Application
import com.cupofcoffee.data.module.RepositoryModule
import com.cupofcoffee.data.repository.MeetingRepositoryImpl


class CupOfCoffeeApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        meetingRepository = RepositoryModule.getMeetingRepository()
    }

    companion object {
        lateinit var meetingRepository: MeetingRepositoryImpl
    }
}