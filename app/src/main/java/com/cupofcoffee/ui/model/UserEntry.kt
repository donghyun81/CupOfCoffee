package com.cupofcoffee.ui.model

data class UserEntry(
    val id: String,
    val userModel: UserModel
)

fun UserEntry.asUserEntity() = userModel.asUserEntity(id)

fun UserEntry.asUserDTO() = userModel.asUserDTO()