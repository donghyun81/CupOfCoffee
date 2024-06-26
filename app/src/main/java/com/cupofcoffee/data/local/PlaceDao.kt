package com.cupofcoffee.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaceDao {

    @Insert
    suspend fun insert(placeEntity: PlaceEntity)

    @Query("SELECT * FROM places Where id = :id")
    suspend fun getPlaceById(id: String): PlaceEntity

    @Update
    suspend fun update(placeEntity: PlaceEntity)


    @Delete
    suspend fun delete(placeEntity: PlaceEntity)

    @Query("SELECT * From places")
    fun getAllPlaces(): List<PlaceEntity>

    @Query("SELECT * FROM places")
    fun getAllPlacesInFlow(): Flow<List<PlaceEntity>>
}