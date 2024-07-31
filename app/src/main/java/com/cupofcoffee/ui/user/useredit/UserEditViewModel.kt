package com.cupofcoffee.ui.user.useredit

import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cupofcoffee.CupOfCoffeeApplication
import com.cupofcoffee.data.DataResult
import com.cupofcoffee.data.DataResult.Companion.success
import com.cupofcoffee.data.repository.CommentRepositoryImpl
import com.cupofcoffee.data.repository.UserRepositoryImpl
import com.cupofcoffee.ui.model.UserEntry
import com.cupofcoffee.util.NetworkUtil
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import java.util.UUID

class UserEditViewModel(
    private val userRepositoryImpl: UserRepositoryImpl,
    private val commentRepositoryImpl: CommentRepositoryImpl,
    private val networkUtil: NetworkUtil
) : ViewModel() {

    private val _uiState: MutableLiveData<DataResult<UserEditUiState>> =
        MutableLiveData(DataResult.loading())
    val uiState: LiveData<DataResult<UserEditUiState>> get() = _uiState

    private val _isButtonClicked: MutableLiveData<Boolean> = MutableLiveData(false)
    val isButtonClicked: LiveData<Boolean> get() = _isButtonClicked


    init {
        initUiState()
    }

    fun onButtonClicked() {
        _isButtonClicked.value = true
    }

    private fun initUiState() {
        viewModelScope.launch {
            val uid = Firebase.auth.uid!!
            try {
                val user = userRepositoryImpl.getLocalUserById(uid)
                _uiState.value = success(
                    UserEditUiState(
                        userEntry = user,
                        contentUri = user.userModel.profileImageWebUrl
                    )
                )
            } catch (e: Exception) {
                DataResult.error(e)
            }
        }
    }

    fun isNetworkConnected() = networkUtil.isConnected()

    suspend fun updateUiState(contentUri: String?) {
        val uid = Firebase.auth.uid!!
        try {
            val user = userRepositoryImpl.getLocalUserById(uid)
            _uiState.value = success(
                UserEditUiState(
                    userEntry = user,
                    contentUri = contentUri
                )
            )
        } catch (e: Exception) {
            DataResult.error(e)
        }
    }

    suspend fun updateUser(userEntry: UserEntry) {
        userRepositoryImpl.update(userEntry)
    }

    suspend fun updateUserComments(userEntry: UserEntry) {
        commentRepositoryImpl.getCommentsByUserId().filterValues { it.userId == userEntry.id }.map {
            val (id, commentDTO) = it
            val currentComment =
                commentDTO.copy(profileImageWebUrl = userEntry.userModel.profileImageWebUrl)
            commentRepositoryImpl.update(id, currentComment)
        }
    }

    fun getImagePick() = Intent().apply {
        type = "image/*"
        action = Intent.ACTION_GET_CONTENT
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                UserEditViewModel(
                    userRepositoryImpl = CupOfCoffeeApplication.userRepository,
                    commentRepositoryImpl = CupOfCoffeeApplication.commentRepository,
                    networkUtil = CupOfCoffeeApplication.networkUtil
                )
            }
        }
    }
}