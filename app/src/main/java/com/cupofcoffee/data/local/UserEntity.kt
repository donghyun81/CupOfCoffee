package com.cupofcoffee.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cupofcoffee.data.remote.UserDTO
import com.cupofcoffee.ui.model.UserEntry
import com.cupofcoffee.ui.model.UserModel

@Entity("users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val name: String? = null,
    val nickname: String? = null,
    val profileImageWebUrl: String? = null,
    val madeMeetingIds: MutableMap<String, Boolean> = mutableMapOf(),
    val attendedMeetingIds: MutableMap<String, Boolean> = mutableMapOf(),
)

fun UserEntity.toUserEntry() =
    UserEntry(id, UserModel(name, nickname, profileImageWebUrl, madeMeetingIds, attendedMeetingIds))

fun UserEntity.toUserDTO() =
    UserDTO(name, nickname, profileImageWebUrl, madeMeetingIds, attendedMeetingIds)