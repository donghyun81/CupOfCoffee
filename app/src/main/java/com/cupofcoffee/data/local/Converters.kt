package com.cupofcoffee.data.local

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {

    @TypeConverter
    fun fromStringToList(value: String): MutableList<String> = Json.decodeFromString(value)

    @TypeConverter
    fun listToString(value: MutableList<String>): String = Json.encodeToString(value)

    @TypeConverter
    fun fromStringToMap(value: String): MutableMap<String, Boolean> = Json.decodeFromString(value)

    @TypeConverter
    fun mapToString(value: MutableMap<String, Boolean>): String = Json.encodeToString(value)
}