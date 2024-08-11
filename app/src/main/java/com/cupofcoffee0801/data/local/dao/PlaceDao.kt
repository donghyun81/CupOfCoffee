package com.cupofcoffee0801.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.cupofcoffee0801.data.local.model.PlaceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(placeEntity: PlaceEntity)

    @Query("SELECT * FROM places Where id = :id")
    suspend fun getPlaceById(id: String): PlaceEntity

    @Update
    suspend fun update(placeEntity: PlaceEntity)


    @Delete
    suspend fun delete(placeEntity: PlaceEntity)

    @Query("DELETE FROM places")
    fun deleteAll()

    @Query("SELECT * From places")
    fun getAllPlaces(): List<PlaceEntity>

    @Query("SELECT * FROM places")
    fun getAllPlacesInFlow(): Flow<List<PlaceEntity>>
}