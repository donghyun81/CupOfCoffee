package com.cupofcoffee0801.data.module

import android.content.Context
import com.cupofcoffee0801.data.local.CupOfCoffeeDatabase
import com.cupofcoffee0801.data.local.datasource.MeetingLocalDataSource
import com.cupofcoffee0801.data.local.datasource.PlaceLocalDataSource
import com.cupofcoffee0801.data.local.datasource.UserLocalDataSource
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