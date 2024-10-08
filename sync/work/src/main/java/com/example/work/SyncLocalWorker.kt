package com.example.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.model.asMeetingEntity
import com.example.data.model.asPlaceEntity
import com.example.data.model.asUserEntity
import com.example.data.repository.MeetingRepository
import com.example.data.repository.PlaceRepository
import com.example.data.repository.UserRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay

@HiltWorker
class SyncLocalWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val placeRepository: PlaceRepository,
    private val meetingRepository: MeetingRepository,
    private val userRepository: UserRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            delay(2000)
            val placeDTOs = placeRepository.getAllRemotePlaces()
            placeRepository.deleteAllLocalPlaces()
            placeDTOs.forEach { placeRepository.insertLocal(it.value.asPlaceEntity(it.key)) }

            val meetingIds = meetingRepository.getAllLocalMeetings().map { it.id }
            val meetingDTOs = meetingRepository.getRemoteMeetingsByIds(meetingIds)

            meetingIds.forEach { id ->
                if (meetingDTOs.keys.contains(id).not()) {
                    meetingRepository.deleteLocal(id)
                }
            }
            meetingDTOs.forEach {
                val (id, meetingDTO) = it
                meetingRepository.updateLocal(meetingDTO.asMeetingEntity(id))
            }

            val userIds = userRepository.getAllUsers().map { it.id }
            val userDTOs = userRepository.getRemoteUsersByIds(userIds)

            userIds.forEach { id ->
                if (userDTOs.keys.contains(id).not()) {
                    userRepository.deleteLocal(id)
                }
            }
            userDTOs.forEach {
                val (id, userDTO) = it
                userRepository.updateLocal(userDTO.asUserEntity(id))
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}