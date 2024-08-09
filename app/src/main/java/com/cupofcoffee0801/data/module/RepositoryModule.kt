package com.cupofcoffee0801.data.module

import com.cupofcoffee0801.data.repository.CommentRepository
import com.cupofcoffee0801.data.repository.CommentRepositoryImpl
import com.cupofcoffee0801.data.repository.MeetingRepository
import com.cupofcoffee0801.data.repository.MeetingRepositoryImpl
import com.cupofcoffee0801.data.repository.PlaceRepository
import com.cupofcoffee0801.data.repository.PlaceRepositoryImpl
import com.cupofcoffee0801.data.repository.PreferencesRepository
import com.cupofcoffee0801.data.repository.PreferencesRepositoryImpl
import com.cupofcoffee0801.data.repository.UserRepository
import com.cupofcoffee0801.data.repository.UserRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Singleton
    @Binds
    abstract fun bindMeetingRepository(
        meetingRepositoryImpl: MeetingRepositoryImpl
    ): MeetingRepository

    @Singleton
    @Binds
    abstract fun bindPlaceRepository(
        placeRepositoryImpl: PlaceRepositoryImpl
    ): PlaceRepository

    @Singleton
    @Binds
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    @Singleton
    @Binds
    abstract fun bindCommentRepository(
        commentRepositoryImpl: CommentRepositoryImpl
    ): CommentRepository

    @Singleton
    @Binds
    abstract fun bindPreferencesRepository(preferencesRepositoryImpl: PreferencesRepositoryImpl): PreferencesRepository
}