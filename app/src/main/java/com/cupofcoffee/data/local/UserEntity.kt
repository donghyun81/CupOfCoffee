package com.cupofcoffee.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cupofcoffee.ui.model.UserEntry
import com.cupofcoffee.ui.model.UserModel

@Entity("users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val name: String? = null,
    val nickname: String? = null,
    val profileImageWebUrl: String? = null,
    val madeMeetingIds: MutableList<String> = mutableListOf(),
    val attendedMeetingIds: MutableList<String> = mutableListOf()
)

fun UserEntity.toUserEntry() =
    UserEntry(id, UserModel(name, nickname, profileImageWebUrl, madeMeetingIds))