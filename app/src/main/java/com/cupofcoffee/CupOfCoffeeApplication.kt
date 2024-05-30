package com.cupofcoffee

import android.app.Application
import com.cupofcoffee.data.module.RepositoryModule
import com.cupofcoffee.data.repository.MeetingRepositoryImpl
import com.cupofcoffee.data.repository.PlaceRepositoryImpl


class CupOfCoffeeApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        meetingRepository = RepositoryModule.getMeetingRepository()
        placeRepository = RepositoryModule.getPlaceRepository()
    }

    companion object {
        lateinit var meetingRepository: MeetingRepositoryImpl
        lateinit var placeRepository: PlaceRepositoryImpl
    }
}