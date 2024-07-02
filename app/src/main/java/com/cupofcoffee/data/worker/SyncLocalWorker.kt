package com.cupofcoffee.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cupofcoffee.CupOfCoffeeApplication
import com.cupofcoffee.data.remote.model.asPlaceEntity
import com.cupofcoffee.data.repository.PlaceRepositoryImpl

class SyncLocalWorker(
    context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    private val placeRepositoryImpl: PlaceRepositoryImpl = CupOfCoffeeApplication.placeRepository

    override suspend fun doWork(): Result {
        return try {
            val places = placeRepositoryImpl.getAllRemotePlaces()
            places.forEach { placeRepositoryImpl.insertLocal(it.value.asPlaceEntity(it.key)) }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}