package com.cupofcoffee0801.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cupofcoffee0801.CupOfCoffeeApplication
import com.cupofcoffee0801.data.repository.CommentRepositoryImpl
import com.cupofcoffee0801.data.repository.MeetingRepositoryImpl
import com.cupofcoffee0801.data.repository.PlaceRepositoryImpl
import com.cupofcoffee0801.data.repository.UserRepositoryImpl
import com.cupofcoffee0801.ui.model.MeetingEntry
import com.cupofcoffee0801.util.NetworkUtil
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.serialization.json.Json
import javax.inject.Inject

class DeleteMeetingWorker @Inject constructor(
    context: Context,
    workerParams: WorkerParameters,
    private val placeRepositoryImpl: PlaceRepositoryImpl,
    private val meetingRepositoryImpl: MeetingRepositoryImpl,
    private val userRepositoryImpl: UserRepositoryImpl,
    private val commentRepositoryImpl: CommentRepositoryImpl
) : CoroutineWorker(context, workerParams) {

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
        val user = userRepositoryImpl.getLocalUserById(uid)!!
        user.userModel.madeMeetingIds.remove(meetingId)
        userRepositoryImpl.update(user)
    }

    private suspend fun deleteComments(commentIds: List<String>) {
        commentIds.forEach { id ->
            commentRepositoryImpl.delete(id = id)
        }
    }
}