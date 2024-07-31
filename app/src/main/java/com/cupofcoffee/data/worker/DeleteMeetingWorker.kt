package com.cupofcoffee.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cupofcoffee.CupOfCoffeeApplication
import com.cupofcoffee.data.repository.CommentRepositoryImpl
import com.cupofcoffee.data.repository.MeetingRepositoryImpl
import com.cupofcoffee.data.repository.PlaceRepositoryImpl
import com.cupofcoffee.data.repository.UserRepositoryImpl
import com.cupofcoffee.ui.model.MeetingEntry
import com.cupofcoffee.util.NetworkUtil
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.serialization.json.Json

class DeleteMeetingWorker(
    context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    private val placeRepositoryImpl: PlaceRepositoryImpl = CupOfCoffeeApplication.placeRepository
    private val meetingRepositoryImpl: MeetingRepositoryImpl =
        CupOfCoffeeApplication.meetingRepository
    private val userRepositoryImpl: UserRepositoryImpl = CupOfCoffeeApplication.userRepository
    private val commentRepositoryImpl: CommentRepositoryImpl =
        CupOfCoffeeApplication.commentRepository
    private val networkUtil: NetworkUtil = CupOfCoffeeApplication.networkUtil

    override suspend fun doWork(): Result {
        return try {
            val meetingEntry =
                inputData.getString("meetingEntry")?.let { Json.decodeFromString<MeetingEntry>(it) }
                    ?: return Result.failure()
            deleteMeeting(meetingEntry)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private suspend fun deleteMeeting(meetingEntry: MeetingEntry) {
        val placeId = meetingEntry.meetingModel.placeId
        updatePlace(placeId, meetingEntry.id)
        updateUser(meetingEntry.id)
        deleteComments(meetingEntry.meetingModel.commentIds.keys.toList())
        meetingRepositoryImpl.delete(meetingEntry.id)
    }

    private suspend fun updatePlace(placeId: String, meetingId: String) {
        val placeEntry =
            placeRepositoryImpl.getPlaceById(placeId, networkUtil.isConnected()) ?: return
        with(placeEntry) {
            placeModel.meetingIds.remove(meetingId)
            if (placeModel.meetingIds.isEmpty()) {
                placeRepositoryImpl.delete(this)
            } else {
                placeRepositoryImpl.update(placeEntry)
            }
        }
    }

    private suspend fun updateUser(meetingId: String) {
        val uid = Firebase.auth.uid!!
        val user = userRepositoryImpl.getLocalUserById(uid)
        user.userModel.madeMeetingIds.remove(meetingId)
        userRepositoryImpl.update(user)
    }

    private suspend fun deleteComments(commentIds: List<String>) {
        commentIds.forEach { id ->
            commentRepositoryImpl.delete(id = id)
        }
    }
}