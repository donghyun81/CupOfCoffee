package com.example.network.di

import com.example.network.BuildConfig
import com.example.network.service.CommentService
import com.example.network.service.MeetingService
import com.example.network.service.PlaceService
import com.example.network.service.UserService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import javax.inject.Singleton

@OptIn(ExperimentalSerializationApi::class)
private val json = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    explicitNulls = false
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = BuildConfig.BASE_URL
    private val contentType = "application/json".toMediaType()

    @Singleton
    @Provides
    fun provideRemoteBuilder(): Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(json.asConverterFactory(contentType))
        .build()

    @Provides
    fun getMeetingService(
        remoteBuilder: Retrofit
    ): MeetingService = remoteBuilder.create(MeetingService::class.java)

    @Provides
    fun getPlaceService(
        remoteBuilder: Retrofit
    ): PlaceService = remoteBuilder.create(PlaceService::class.java)

    @Provides
    fun getUserService(
        remoteBuilder: Retrofit
    ): UserService = remoteBuilder.create(UserService::class.java)

    @Provides
    fun getCommentService(
        remoteBuilder: Retrofit
    ): CommentService = remoteBuilder.create(CommentService::class.java)
}