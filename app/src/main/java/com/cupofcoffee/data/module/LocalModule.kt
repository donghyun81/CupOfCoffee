package com.cupofcoffee.data.module

import android.content.Context
import com.cupofcoffee.data.local.CupOfCoffeeDatabase
import com.cupofcoffee.data.local.datasource.MeetingLocalDataSource
import com.cupofcoffee.data.local.datasource.PlaceLocalDataSource
import com.cupofcoffee.data.local.datasource.UserLocalDataSource

object LocalModule {
    fun provideDatabase(context: Context) = CupOfCoffeeDatabase.from(context)

    fun provideMeetingLocalDataSource(database: CupOfCoffeeDatabase) =
        MeetingLocalDataSource(database.meetingDao())

    fun providePlaceLocalDataSource(database: CupOfCoffeeDatabase) =
        PlaceLocalDataSource(database.placeDao())

    fun provideUserLocalDataSource(database: CupOfCoffeeDatabase) =
        UserLocalDataSource(database.userDao())
}