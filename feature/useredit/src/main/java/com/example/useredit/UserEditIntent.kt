package com.example.useredit

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher

sealed class UserEditIntent {
    data class EnterNickName(val name: String) : UserEditIntent()

    data class RequestAlbumAccessPermission(val permissionLauncher: ManagedActivityResultLauncher<String, Boolean>) :
        UserEditIntent()

    data class EditUserProfile(val profileImageUri: Uri?) : UserEditIntent()

    data object UserEdit : UserEditIntent()

    data object InitData : UserEditIntent()
}