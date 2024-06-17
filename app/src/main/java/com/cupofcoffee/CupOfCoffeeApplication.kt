package com.cupofcoffee

import android.app.Application
import android.content.Context
import com.cupofcoffee.data.module.LocalModule
import com.cupofcoffee.data.module.RepositoryModule
import com.cupofcoffee.data.repository.MeetingRepositoryImpl
import com.cupofcoffee.data.repository.PlaceRepositoryImpl
import com.cupofcoffee.data.repository.UserRepositoryImpl


class CupOfCoffeeApplication : Application() {


    override fun onCreate() {
        super.onCreate()
        RepositoryModule.initDatabase(applicationContext)
    }

    companion object {

        val meetingRepository: MeetingRepositoryImpl by lazy {
            RepositoryModule.getMeetingRepository()
        }

        val placeRepository: PlaceRepositoryImpl by lazy {
            RepositoryModule.getPlaceRepository()
        }

        val userRepository: UserRepositoryImpl by lazy {
            RepositoryModule.getUserRepository()
        }
    }
}