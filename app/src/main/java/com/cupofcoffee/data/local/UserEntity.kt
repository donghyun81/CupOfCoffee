package com.cupofcoffee.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("users")
data class UserEntity(
    @PrimaryKey
    val id:String,
    val name: String? = null,
    val nickname: String? = null,
    val profileImageWebUrl: String? = null,
    val madeMeetingIds: MutableList<String> = mutableListOf(),
    val attendedMeetingIds: MutableList<String> = mutableListOf()
)