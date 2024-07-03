package com.cupofcoffee.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.cupofcoffee.data.local.dao.MeetingDao
import com.cupofcoffee.data.local.dao.PlaceDao
import com.cupofcoffee.data.local.dao.UserDao
import com.cupofcoffee.data.local.model.MeetingEntity
import com.cupofcoffee.data.local.model.PlaceEntity
import com.cupofcoffee.data.local.model.UserEntity

@Database(
    entities = [MeetingEntity::class, PlaceEntity::class, UserEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CupOfCoffeeDatabase : RoomDatabase() {

    abstract fun meetingDao(): MeetingDao

    abstract fun placeDao(): PlaceDao

    abstract fun userDao(): UserDao


    companion object {
        @Volatile
        private var INSTANCE: CupOfCoffeeDatabase? = null

        fun from(context: Context): CupOfCoffeeDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context, CupOfCoffeeDatabase::class.java, "db")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}