package com.cupofcoffee0801.ui.user.useredit

import com.cupofcoffee0801.ui.model.UserEntry

data class UserEditUiState(
    val userEntry: UserEntry,
    val contentUri: String?
)