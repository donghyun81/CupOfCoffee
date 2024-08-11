package com.cupofcoffee0801.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteIdWrapper(@SerialName("name") val id: String)