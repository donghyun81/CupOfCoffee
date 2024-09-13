package com.cupofcoffee0801.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cupofcoffee0801.data.remote.model.UserDTO
import com.cupofcoffee0801.ui.model.User

@Entity("users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val name: String? = null,
    val nickname: String? = null,
    val profileImageWebUrl: String? = null,
    val madeMeetingIds: MutableMap<String, Boolean> = mutableMapOf(),
    val attendedMeetingIds: MutableMap<String, Boolean> = mutableMapOf()
)

fun UserEntity.asUserEntry() =
    User(id,name, nickname, profileImageWebUrl, madeMeetingIds, attendedMeetingIds)

fun UserEntity.asUserDTO() =
    UserDTO(name, nickname, profileImageWebUrl, madeMeetingIds)