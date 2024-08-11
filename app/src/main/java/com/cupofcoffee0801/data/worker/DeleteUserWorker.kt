package com.cupofcoffee0801.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cupofcoffee0801.data.repository.CommentRepository
import com.cupofcoffee0801.data.repository.MeetingRepository
import com.cupofcoffee0801.data.repository.PlaceRepository
import com.cupofcoffee0801.data.repository.UserRepository
import com.cupofcoffee0801.ui.model.MeetingEntry
import com.cupofcoffee0801.util.NetworkUtil
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class DeleteUserWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val placeRepository: PlaceRepository,
    private val meetingRepository: MeetingRepository,
    private val userRepository: UserRepository,
    private val commentRepository: CommentRepository,
    private val networkUtil: NetworkUtil
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val userId = inputData.getString("userId") ?: return Result.failure()
            deleteUserData(userId)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private suspend fun deleteUserData(uid: String) {
        val user = userRepository.getLocalUserById(uid)!!
        val storageRef = Firebase.storage.reference.child("images/$uid")
        storageRef.delete().addOnSuccessListener {
        }.addOnFailureListener {
        }
        user.userModel.attendedMeetingIds.keys.map { meetingId ->
            cancelMeeting(uid, meetingId)
        }
        user.userModel.madeMeetingIds.keys.map { meetingId ->
            val meetingEntry =
                meetingRepository.getMeeting(meetingId, networkUtil.isConnected())
            deleteMeeting(meetingEntry)
            deleteMadeMeetingsInPlace(meetingEntry.meetingModel.placeId, meetingId)
        }
        deleteUserComments(uid)
        deleteUser(uid)
    }

    private suspend fun cancelMeeting(uid: String, meetingId: String) {
        val meetingEntry =
            meetingRepository.getMeeting(meetingId, networkUtil.isConnected())
        meetingEntry.meetingModel.personIds.remove(uid)
        meetingRepository.update(meetingEntry)
    }

    private suspend fun deleteMadeMeetingsInPlace(placeId: String, meetingId: String) {
        val placeEntry = placeRepository.getPlaceById(placeId)!!
        placeEntry.placeModel.meetingIds.remove(meetingId)
        if (placeEntry.placeModel.meetingIds.isEmpty()) placeRepository.delete(placeEntry)
        else placeRepository.update(placeEntry)
    }

    private suspend fun deleteUserComments(userId: String) {
        val commentsByUserId =
            commentRepository.getCommentsByUserId(userId)
        commentsByUserId.keys.forEach { id ->
            commentRepository.delete(id)
        }
    }

    private suspend fun deleteMeeting(meetingEntry: MeetingEntry) {
        meetingRepository.delete(meetingEntry.id)
        deleteMeetingComments(meetingEntry.meetingModel.commentIds.keys.toList())
    }

    private suspend fun deleteMeetingComments(commentIds: List<String>) {
        commentIds.forEach { id ->
            commentRepository.delete(id = id)
        }
    }

    private suspend fun deleteUser(id: String) {
        userRepository.delete(id)
    }
}