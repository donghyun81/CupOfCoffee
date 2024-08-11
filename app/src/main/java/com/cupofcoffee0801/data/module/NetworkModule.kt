package com.cupofcoffee0801.data.module

import com.cupofcoffee0801.BuildConfig
import com.cupofcoffee0801.data.remote.service.CommentService
import com.cupofcoffee0801.data.remote.service.MeetingService
import com.cupofcoffee0801.data.remote.service.PlaceService
import com.cupofcoffee0801.data.remote.service.UserService
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
    fun provideRemoteBuilder() = Retrofit.Builder()
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