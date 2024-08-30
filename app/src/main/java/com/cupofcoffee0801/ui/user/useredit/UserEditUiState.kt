package com.cupofcoffee0801.ui.user.useredit

import com.cupofcoffee0801.ui.model.User

data class UserEditUiState(
    val user: User,
    val contentUri: String?
)