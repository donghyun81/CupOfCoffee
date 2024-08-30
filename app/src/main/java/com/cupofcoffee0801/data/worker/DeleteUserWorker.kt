package com.cupofcoffee0801.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cupofcoffee0801.data.repository.CommentRepository
import com.cupofcoffee0801.data.repository.MeetingRepository
import com.cupofcoffee0801.data.repository.PlaceRepository
import com.cupofcoffee0801.data.repository.UserRepository
import com.cupofcoffee0801.ui.model.Meeting
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
        user.attendedMeetingIds.keys.map { meetingId ->
            cancelMeeting(uid, meetingId)
        }
        user.madeMeetingIds.keys.map { meetingId ->
            val meeting =
                meetingRepository.getMeeting(meetingId, networkUtil.isConnected())
            deleteMeeting(meeting)
            deleteMadeMeetingsInPlace(meeting.placeId, meetingId)
        }
        deleteUserComments(uid)
        deleteUser(uid)
    }

    private suspend fun cancelMeeting(uid: String, meetingId: String) {
        val meeting =
            meetingRepository.getMeeting(meetingId, networkUtil.isConnected())
        meeting.personIds.remove(uid)
        meetingRepository.update(meeting)
    }

    private suspend fun deleteMadeMeetingsInPlace(placeId: String, meetingId: String) {
        val place = placeRepository.getPlaceById(placeId)!!
        place.meetingIds.remove(meetingId)
        if (place.meetingIds.isEmpty()) placeRepository.delete(place)
        else placeRepository.update(place)
    }

    private suspend fun deleteUserComments(userId: String) {
        val commentsByUserId =
            commentRepository.getCommentsByUserId(userId)
        commentsByUserId.keys.forEach { id ->
            commentRepository.delete(id)
        }
    }

    private suspend fun deleteMeeting(meeting: Meeting) {
        meetingRepository.delete(meeting.id)
        deleteMeetingComments(meeting.commentIds.keys.toList())
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