package com.epicorebiosystems.rehydrate.nordicsemi.uart

import com.epicorebiosystems.rehydrate.nordicsemi.uart.db.ConfigurationsDao
import com.epicorebiosystems.rehydrate.nordicsemi.uart.db.ConfigurationsDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DaoHiltModule {

    @Provides
    @Singleton
    internal fun provideDao(db: ConfigurationsDatabase): ConfigurationsDao {
        return db.dao()
    }
}
