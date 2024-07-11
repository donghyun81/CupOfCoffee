package com.cupofcoffee

import android.app.Application
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.cupofcoffee.data.module.RepositoryModule
import com.cupofcoffee.data.repository.MeetingRepositoryImpl
import com.cupofcoffee.data.repository.PlaceRepositoryImpl
import com.cupofcoffee.data.repository.UserRepositoryImpl
import com.cupofcoffee.data.worker.SyncLocalWorker
import com.cupofcoffee.util.NetworkUtil


class CupOfCoffeeApplication : Application() {


    override fun onCreate() {
        super.onCreate()
        RepositoryModule.initDatabase(applicationContext)

        networkUtil = NetworkUtil(applicationContext)

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val localWorker = OneTimeWorkRequestBuilder<SyncLocalWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(applicationContext)
            .beginWith(localWorker)
            .enqueue()
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

        lateinit var networkUtil: NetworkUtil
    }
}