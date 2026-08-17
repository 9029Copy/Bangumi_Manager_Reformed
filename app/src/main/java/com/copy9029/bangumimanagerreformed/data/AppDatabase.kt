package com.copy9029.bangumimanagerreformed.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// entities 数组里放入所有的 Entity（表）
// version 是数据库版本号，以后修改表结构时需要升级这个版本号
@TypeConverters(Converters::class)
@Database(
    entities = [
        Bangumi::class,
        BangumiSchedule::class,
    ],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    // 抽象方法，返回我们的 DAO。Room 会在编译时自动帮我们实现这个接口。
    abstract fun bangumiDao(): BangumiDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Rebuild both tables so the Schedule foreign key points to the
                // new parent table after themeColorLong is removed.
                db.execSQL(
                    "ALTER TABLE bangumi_schedule_items " +
                        "RENAME TO bangumi_schedule_items_old"
                )
                db.execSQL(
                    "ALTER TABLE bangumi_items RENAME TO bangumi_items_old"
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS bangumi_items (
                        bangumiId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        seasonYear INTEGER NOT NULL,
                        seasonMonth INTEGER NOT NULL,
                        myScore INTEGER,
                        firstBroadcastDate INTEGER NOT NULL,
                        totalEpisodes INTEGER,
                        latestWatchedEpisode INTEGER NOT NULL,
                        isActive INTEGER NOT NULL,
                        lastBasicInfoModifiedAtMillis INTEGER NOT NULL,
                        expectedEndDate INTEGER
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO bangumi_items (
                        bangumiId, title, seasonYear, seasonMonth, myScore,
                        firstBroadcastDate, totalEpisodes, latestWatchedEpisode,
                        isActive, lastBasicInfoModifiedAtMillis, expectedEndDate
                    )
                    SELECT
                        bangumiId, title, seasonYear, seasonMonth, myScore,
                        firstBroadcastDate, totalEpisodes, latestWatchedEpisode,
                        isActive, lastBasicInfoModifiedAtMillis, expectedEndDate
                    FROM bangumi_items_old
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS bangumi_schedule_items (
                        bangumiId INTEGER NOT NULL,
                        episodeId INTEGER NOT NULL,
                        broadcastDate INTEGER NOT NULL,
                        PRIMARY KEY(bangumiId, episodeId),
                        FOREIGN KEY(bangumiId) REFERENCES bangumi_items(bangumiId)
                            ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO bangumi_schedule_items (
                        bangumiId, episodeId, broadcastDate
                    )
                    SELECT bangumiId, episodeId, broadcastDate
                    FROM bangumi_schedule_items_old
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE bangumi_schedule_items_old")
                db.execSQL("DROP TABLE bangumi_items_old")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Rebuild both tables for compatibility with SQLite versions that do not
                // support ALTER TABLE RENAME COLUMN, while preserving Schedule foreign keys.
                db.execSQL(
                    "ALTER TABLE bangumi_schedule_items " +
                        "RENAME TO bangumi_schedule_items_old"
                )
                db.execSQL(
                    "ALTER TABLE bangumi_items RENAME TO bangumi_items_old"
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS bangumi_items (
                        bangumiId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        seasonYear INTEGER NOT NULL,
                        seasonMonth INTEGER NOT NULL,
                        myScore INTEGER,
                        firstBroadcastDate INTEGER NOT NULL,
                        totalEpisodes INTEGER,
                        latestWatchedEpisode INTEGER NOT NULL,
                        isActive INTEGER NOT NULL,
                        lastModifiedAtMillis INTEGER NOT NULL,
                        expectedEndDate INTEGER
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO bangumi_items (
                        bangumiId, title, seasonYear, seasonMonth, myScore,
                        firstBroadcastDate, totalEpisodes, latestWatchedEpisode,
                        isActive, lastModifiedAtMillis, expectedEndDate
                    )
                    SELECT
                        bangumiId, title, seasonYear, seasonMonth, myScore,
                        firstBroadcastDate, totalEpisodes, latestWatchedEpisode,
                        isActive, lastBasicInfoModifiedAtMillis, expectedEndDate
                    FROM bangumi_items_old
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS bangumi_schedule_items (
                        bangumiId INTEGER NOT NULL,
                        episodeId INTEGER NOT NULL,
                        broadcastDate INTEGER NOT NULL,
                        PRIMARY KEY(bangumiId, episodeId),
                        FOREIGN KEY(bangumiId) REFERENCES bangumi_items(bangumiId)
                            ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO bangumi_schedule_items (
                        bangumiId, episodeId, broadcastDate
                    )
                    SELECT bangumiId, episodeId, broadcastDate
                    FROM bangumi_schedule_items_old
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE bangumi_schedule_items_old")
                db.execSQL("DROP TABLE bangumi_items_old")
            }
        }
    }

//    // --- 下面是单例模式 (Singleton) ---
//    companion object {
//        @Volatile
//        private var INSTANCE: AppDatabase? = null
//
//        fun getDatabase(context: Context): AppDatabase {
//            return INSTANCE ?: synchronized(this) {
//                val instance = Room.databaseBuilder(
//                    context.applicationContext,
//                    AppDatabase::class.java,
//                    "app_database" // 数据库文件的名字
//                ).build()
//                INSTANCE = instance
//                instance
//            }
//        }
//    }
}
