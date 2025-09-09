package com.epicorebiosystems.rehydrate.nordicsemi.uart

import android.content.Context
import androidx.room.Room
import com.epicorebiosystems.rehydrate.nordicsemi.uart.db.ConfigurationsDatabase
import com.epicorebiosystems.rehydrate.nordicsemi.uart.db.MIGRATION_1_2
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DbHiltModule {

    @Provides
    @Singleton
    internal fun provideDB(@ApplicationContext context: Context): ConfigurationsDatabase {
        return Room.databaseBuilder(
            context,
            ConfigurationsDatabase::class.java, "toolbox_uart.db"
        ).addMigrations(MIGRATION_1_2).build()
    }
}
