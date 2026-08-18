package com.copy9029.bangumimanagerreformed.data

import android.content.Context
import androidx.room.Room
import com.copy9029.bangumimanagerreformed.data.migration.MIGRATION_1_2
import com.copy9029.bangumimanagerreformed.data.migration.MIGRATION_2_3
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "bangumi_database"
        ).addMigrations(
            MIGRATION_1_2,
            MIGRATION_2_3,
        )
            .build()
    }

    @Provides
    fun provideBangumiDao(
        database: AppDatabase,
    ): BangumiDao {
        return database.bangumiDao()
    }
}
