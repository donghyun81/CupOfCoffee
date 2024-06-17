package com.cupofcoffee.data.module

import android.content.Context
import com.cupofcoffee.data.local.CupOfCoffeeDatabase

object LocalModule {
    fun provideDatabase(context: Context) = CupOfCoffeeDatabase.from(context)

    fun provideMeetingDao(database: CupOfCoffeeDatabase) = database.meetingDao()

    fun providePlaceDao(database: CupOfCoffeeDatabase) = database.placeDao()

    fun provideUserDao(database: CupOfCoffeeDatabase) = database.userDao()
}