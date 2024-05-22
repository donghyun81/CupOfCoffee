package com.cupofcoffee.data.module

import com.cupofcoffee.BuildConfig
import com.cupofcoffee.data.remote.MeetingService
import com.cupofcoffee.data.remote.PlaceService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit

@OptIn(ExperimentalSerializationApi::class)
private val json = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    explicitNulls = false
}
object NetworkModule {

    private const val BASE_URL = BuildConfig.BASE_URL
    private val contentType = "application/json".toMediaType()

    private val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    fun getMeetingService(): MeetingService = instance.create(MeetingService::class.java)
    fun getPlaceService(): PlaceService = instance.create(PlaceService::class.java)
}