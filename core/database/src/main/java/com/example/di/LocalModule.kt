package com.example.di

import android.content.Context
import com.example.database.CupOfCoffeeDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalModule {
    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context) = CupOfCoffeeDatabase.from(context)

    @Provides
    fun provideMeetingDao(database: CupOfCoffeeDatabase) = database.meetingDao()

    @Provides
    fun providePlaceDao(database: CupOfCoffeeDatabase) = database.placeDao()

    @Provides
    fun provideUserDao(database: CupOfCoffeeDatabase) = database.userDao()
}