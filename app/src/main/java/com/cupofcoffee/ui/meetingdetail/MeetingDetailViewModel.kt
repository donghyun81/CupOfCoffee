package com.cupofcoffee.ui.meetingdetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cupofcoffee.CupOfCoffeeApplication
import com.cupofcoffee.data.DataResult
import com.cupofcoffee.data.DataResult.Companion.error
import com.cupofcoffee.data.DataResult.Companion.loading
import com.cupofcoffee.data.DataResult.Companion.success
import com.cupofcoffee.data.repository.CommentRepositoryImpl
import com.cupofcoffee.data.repository.MeetingRepositoryImpl
import com.cupofcoffee.data.repository.UserRepositoryImpl
import com.cupofcoffee.util.NetworkUtil
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MeetingDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val meetingRepositoryImpl: MeetingRepositoryImpl,
    private val commentRepositoryImpl: CommentRepositoryImpl,
    private val userRepositoryImpl: UserRepositoryImpl,
    private val networkUtil: NetworkUtil
) : ViewModel() {

    val meetingId =
        MeetingDetailFragmentArgs.fromSavedStateHandle(savedStateHandle).meetingId

    private val _meetingDetailUiState: MutableLiveData<DataResult<MeetingDetailUiState>> =
        MutableLiveData(
            loading()
        )

    val meetingDetailUiState: LiveData<DataResult<MeetingDetailUiState>> = _meetingDetailUiState

    init {
        initUiState()
    }

    private fun initUiState() {
        viewModelScope.launch {
            try {
                val meetingEntryInFlow =
                    meetingRepositoryImpl.getMeetingInFlow(meetingId, networkUtil.isConnected())
                val userEntry = userRepositoryImpl.getLocalUserById(Firebase.auth.uid!!)
                meetingEntryInFlow
                    .flatMapLatest { meetingEntry ->
                        meetingEntry!!
                        getCommentsInFlow(meetingEntry.meetingModel.commentIds.keys.toList()).map { commentEntries ->
                            MeetingDetailUiState(
                                userEntry,
                                meetingEntry,
                                commentEntries,
                                meetingEntry.meetingModel.managerId == userEntry.id
                            )
                        }
                    }
                    .collect { meetingDetailUiState ->
                        _meetingDetailUiState.postValue(success(meetingDetailUiState))
                    }
            } catch (e: Exception) {
                _meetingDetailUiState.postValue(error(e))
            }
        }
    }

    private suspend fun getCommentsInFlow(ids: List<String>) =
        commentRepositoryImpl.getCommentsByIdsInFlow(ids)

    fun deleteComment(commentId: String) {
        viewModelScope.launch {
            val meetingEntry =
                meetingRepositoryImpl.getMeeting(meetingId, networkUtil.isConnected())
            meetingEntry.meetingModel.commentIds.remove(commentId)
            meetingRepositoryImpl.update(meetingEntry)
            commentRepositoryImpl.delete(commentId)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MeetingDetailViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    meetingRepositoryImpl = CupOfCoffeeApplication.meetingRepository,
                    commentRepositoryImpl = CupOfCoffeeApplication.commentRepository,
                    userRepositoryImpl = CupOfCoffeeApplication.userRepository,
                    networkUtil = CupOfCoffeeApplication.networkUtil
                )
            }
        }
    }
}