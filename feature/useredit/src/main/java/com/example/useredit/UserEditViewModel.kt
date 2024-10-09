package com.example.useredit

import android.net.Uri
import android.os.Build
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.common.util.NetworkUtil
import com.example.data.model.User
import com.example.data.repository.CommentRepositoryImpl
import com.example.data.repository.UserRepositoryImpl
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

const val EDIT_USER_NETWORK_MESSAGE = "사용자 정보를 수정하기 위해서 네트워크 연결이 필요합니다!"

@HiltViewModel
class UserEditViewModel @Inject constructor(
    private val userRepositoryImpl: UserRepositoryImpl,
    private val commentRepositoryImpl: CommentRepositoryImpl,
    private val networkUtil: NetworkUtil
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserEditUiState(isLoading = true))
    val uiState = _uiState.asStateFlow()

    private val _sideEffect = MutableSharedFlow<UserEditSideEffect>(replay = 1)
    val sideEffect = _sideEffect.asSharedFlow()

    fun handleIntent(intent: UserEditIntent) {
        when (intent) {
            is UserEditIntent.EditUserProfile -> {
                handleImagePickerResult(intent.profileImageUri)
            }

            is UserEditIntent.EnterNickName -> {
                updateContentUri(intent.name)
            }

            UserEditIntent.InitData -> {
                initUiState()
            }

            is UserEditIntent.RequestAlbumAccessPermission -> {
                requestAlbumAccessPermission(intent.permissionLauncher)
            }

            UserEditIntent.UserEdit -> {
                if (networkUtil.isConnected()) {
                    viewModelScope.launch {
                        editUser()
                        _sideEffect.tryEmit(UserEditSideEffect.NavigateUp)
                    }
                } else _sideEffect.tryEmit(UserEditSideEffect.ShowSnackBar(EDIT_USER_NETWORK_MESSAGE))
            }
        }
    }

    private fun initUiState() {
        viewModelScope.launch {
            val uid = Firebase.auth.uid!!
            try {
                val user = userRepositoryImpl.getLocalUserById(uid)!!
                _uiState.value =
                    UserEditUiState(
                        userId = user.id,
                        nickname = user.nickname,
                        contentUri = user.profileImageWebUrl
                    )
            } catch (e: Exception) {
                _uiState.value = UserEditUiState(isError = true)
            }
        }
    }

    fun updateNickname(nickname: String) {
        _uiState.value = uiState.value.copy(nickname = nickname)
    }

    private fun requestAlbumAccessPermission(permissionLauncher: ManagedActivityResultLauncher<String, Boolean>) {
        val permissionId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }
        permissionLauncher.launch(permissionId)
    }

    private fun handleImagePickerResult(uri: Uri?) {
        uri?.let {
            updateContentUri(it.toString())
        }
    }

    private fun updateContentUri(contentUri: String?) {
        _uiState.value = uiState.value.copy(contentUri = contentUri)
    }

    private suspend fun editUser() {
        val contentUri = uiState.value.contentUri
        val currentUserEntry = getCurrentUser(contentUri)
        delay(2000L)
        updateUser(currentUserEntry)
        updateUserComments(currentUserEntry)
        _uiState.value = uiState.value.copy(isCompleted = true)
    }

    private suspend fun getCurrentUser(contentUri: String?): User {
        val uid = Firebase.auth.uid!!
        val user = userRepositoryImpl.getUser(uid)!!
        val storageReference = FirebaseStorage.getInstance().reference
        val ref = storageReference.child("images/$uid")
        val imageUri = contentUri.let { Uri.parse(it) }
        return try {
            ref.putFile(imageUri).await()
            val uri = ref.downloadUrl.await()
            user.copy(
                nickname = _uiState.value.nickname ?: user.nickname,
                profileImageWebUrl = uri.toString()
            )
        } catch (e: Exception) {
            user.copy(
                nickname = _uiState.value.nickname ?: user.nickname
            )
        }
    }

    private suspend fun updateUser(user: User) {
        userRepositoryImpl.update(user)
    }

    private suspend fun updateUserComments(user: User) {
        commentRepositoryImpl.getCommentsByUserId(user.id).map {
            val (id, commentDTO) = it
            val currentComment =
                commentDTO.copy(profileImageWebUrl = user.profileImageWebUrl)
            commentRepositoryImpl.update(id, currentComment)
        }
    }
}