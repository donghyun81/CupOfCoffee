package com.cupofcoffee.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cupofcoffee.CupOfCoffeeApplication
import com.cupofcoffee.data.remote.model.asMeetingEntity
import com.cupofcoffee.data.remote.model.asPlaceEntity
import com.cupofcoffee.data.remote.model.asUserEntity
import com.cupofcoffee.data.repository.MeetingRepositoryImpl
import com.cupofcoffee.data.repository.PlaceRepositoryImpl
import com.cupofcoffee.data.repository.UserRepositoryImpl
import kotlinx.coroutines.delay

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
            delay(2000)
            val placeDTOs = placeRepositoryImpl.getAllRemotePlaces()
            placeDTOs.forEach { placeRepositoryImpl.insertLocal(it.value.asPlaceEntity(it.key)) }

            val meetingIds = meetingRepositoryImpl.getAllLocalMeetings().map { it.id }
            val meetingDTOs = meetingRepositoryImpl.getRemoteMeetingsByIds(meetingIds)

            meetingIds.forEach { id ->
                if (meetingDTOs.keys.contains(id).not()) {
                    meetingRepositoryImpl.deleteLocal(id)
                }
            }
            meetingDTOs.forEach {
                val (id, meetingDTO) = it
                meetingRepositoryImpl.updateLocal(meetingDTO.asMeetingEntity(id))
            }

            val userIds = userRepositoryImpl.getAllUsers().map { it.id }
            val userDTOs = userRepositoryImpl.getRemoteUsersByIds(userIds)

            userIds.forEach { id ->
                if (userDTOs.keys.contains(id).not()) {
                    userRepositoryImpl.deleteLocal(id)
                }
            }
            userDTOs.forEach {
                val (id, userDTO) = it
                userRepositoryImpl.updateLocal(userDTO.asUserEntity(id))
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}