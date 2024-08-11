package com.cupofcoffee0801.data.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier

const val REFRESH_INTERVAL_MS = 3000L
@Module
@InstallIn(SingletonComponent::class)
object ConfigModule {
    @RefreshInterval
    @Provides
    fun provideRefreshInterval(): Long = REFRESH_INTERVAL_MS
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RefreshInterval