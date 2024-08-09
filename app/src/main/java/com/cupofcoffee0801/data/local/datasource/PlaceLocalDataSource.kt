package com.cupofcoffee0801.data.local.datasource

import com.cupofcoffee0801.data.local.dao.PlaceDao
import com.cupofcoffee0801.data.local.model.PlaceEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PlaceLocalDataSource @Inject constructor(
    private val placeDao: PlaceDao,
) {

    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    suspend fun insert(placeEntity: PlaceEntity) = withContext(ioDispatcher) {
        placeDao.insert(placeEntity)
    }

    suspend fun getPlaceById(id: String): PlaceEntity = withContext(ioDispatcher) {
        placeDao.getPlaceById(id)
    }


    suspend fun update(placeEntity: PlaceEntity) = withContext(ioDispatcher) {
        placeDao.update(placeEntity)
    }

    suspend fun delete(placeEntity: PlaceEntity) = withContext(ioDispatcher) {
        placeDao.delete(placeEntity)
    }

    suspend fun getAllPlacesInFlow(): Flow<List<PlaceEntity>> = withContext(ioDispatcher) {
        placeDao.getAllPlacesInFlow()
    }

}