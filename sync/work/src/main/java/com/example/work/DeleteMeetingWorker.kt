package com.example.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.common.util.NetworkUtil
import com.example.data.repository.CommentRepository
import com.example.data.repository.MeetingRepository
import com.example.data.repository.PlaceRepository
import com.example.data.repository.UserRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class DeleteMeetingWorker @AssistedInject constructor(
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
            val meetingId = inputData.getString("meetingId") ?: return Result.failure()
            deleteMeeting(meetingId)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private suspend fun deleteMeeting(meetingId: String) {
        val meeting = meetingRepository.getMeeting(meetingId)
        val placeId = meeting.placeId
        updatePlace(placeId, meeting.id)
        updateUser(meeting.id)
        deleteComments(meeting.commentIds.keys.toList())
        meetingRepository.delete(meeting.id)
    }

    private suspend fun updatePlace(placeId: String, meetingId: String) {
        val place =
            placeRepository.getPlaceById(placeId, networkUtil.isConnected()) ?: return
        with(place) {
            meetingIds.remove(meetingId)
            if (meetingIds.isEmpty()) {
                placeRepository.delete(this)
            } else {
                placeRepository.update(place)
            }
        }
    }

    private suspend fun updateUser(meetingId: String) {
        val uid = Firebase.auth.uid!!
        val user = userRepository.getLocalUserById(uid)!!
        user.madeMeetingIds.remove(meetingId)
        userRepository.update(user)
    }

    private suspend fun deleteComments(commentIds: List<String>) {
        commentIds.forEach { id ->
            commentRepository.delete(id = id)
        }
    }
}