package com.cupofcoffee.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cupofcoffee.CupOfCoffeeApplication
import com.cupofcoffee.data.local.asMeetingDTO
import com.cupofcoffee.data.local.asPlaceDTO
import com.cupofcoffee.data.local.asUserDTO
import com.cupofcoffee.data.repository.MeetingRepositoryImpl
import com.cupofcoffee.data.repository.PlaceRepositoryImpl
import com.cupofcoffee.data.repository.UserRepositoryImpl

class SyncRemoteWorker(
    context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    private val meetingRepositoryImpl: MeetingRepositoryImpl = CupOfCoffeeApplication.meetingRepository
    private val placeRepositoryImpl: PlaceRepositoryImpl = CupOfCoffeeApplication.placeRepository
    private val userRepositoryImpl: UserRepositoryImpl = CupOfCoffeeApplication.userRepository

    override suspend fun doWork(): Result {
        return try {
            val meetings = meetingRepositoryImpl.getAllMeetings()
            val places = placeRepositoryImpl.getAllLocalPlaces()
            val users = userRepositoryImpl.getAllUsers()

            meetings.filter { it.isSynced.not() }
                .forEach { meetingRepositoryImpl.insertRemote(it.asMeetingDTO()) }

            places.filter { it.isSynced.not() }
                .forEach { placeRepositoryImpl.insertRemote(it.id, it.asPlaceDTO()) }

            users.filter { it.isSynced.not() }
                .forEach { userRepositoryImpl.insertRemote(it.id, it.asUserDTO()) }

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}