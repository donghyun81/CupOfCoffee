package com.cupofcoffee.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cupofcoffee.CupOfCoffeeApplication
import com.cupofcoffee.data.remote.model.asPlaceEntity
import com.cupofcoffee.data.remote.model.asUserEntity
import com.cupofcoffee.data.remote.model.asUserEntry
import com.cupofcoffee.data.repository.MeetingRepositoryImpl
import com.cupofcoffee.data.repository.PlaceRepositoryImpl
import com.cupofcoffee.data.repository.UserRepositoryImpl
import com.cupofcoffee.ui.model.asMeetingDTO
import com.cupofcoffee.ui.model.asMeetingEntity

class SyncLocalWorker(
    context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    private val placeRepositoryImpl: PlaceRepositoryImpl = CupOfCoffeeApplication.placeRepository
    private val meetingRepositoryImpl: MeetingRepositoryImpl =
        CupOfCoffeeApplication.meetingRepository
    private val userRepositoryImpl: UserRepositoryImpl = CupOfCoffeeApplication.userRepository

    override suspend fun doWork(): Result {
        return try {
            val placeDTOs = placeRepositoryImpl.getAllRemotePlaces()
            placeDTOs.forEach { placeRepositoryImpl.insertLocal(it.value.asPlaceEntity(it.key)) }

            val meetingIds = meetingRepositoryImpl.getAllLocalMeetings().map { it.id }
            val meetingEntries = meetingRepositoryImpl.getMeetingsByIds(meetingIds)
            meetingEntries.forEach { meetingRepositoryImpl.update(it) }

            val userIds = userRepositoryImpl.getAllUsers().map { it.id }
            val userDTOs = userRepositoryImpl.getRemoteUsersByIds(userIds)
            userDTOs.forEach { userRepositoryImpl.update(it.value.asUserEntry(it.key)) }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}