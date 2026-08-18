package com.copy9029.bangumimanagerreformed.data.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Rebuild both tables so the Schedule foreign key points to the
        // new parent table after themeColorLong is removed.
        db.execSQL(
            "ALTER TABLE bangumi_schedule_items " +
                "RENAME TO bangumi_schedule_items_old",
        )
        db.execSQL("ALTER TABLE bangumi_items RENAME TO bangumi_items_old")
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
            """.trimIndent(),
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
            """.trimIndent(),
        )
        createScheduleTable(db)
        copyOldSchedules(db)
        dropOldTables(db)
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Rebuild both tables for compatibility with SQLite versions that do not
        // support ALTER TABLE RENAME COLUMN, while preserving Schedule foreign keys.
        db.execSQL(
            "ALTER TABLE bangumi_schedule_items " +
                "RENAME TO bangumi_schedule_items_old",
        )
        db.execSQL("ALTER TABLE bangumi_items RENAME TO bangumi_items_old")
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
            """.trimIndent(),
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
            """.trimIndent(),
        )
        createScheduleTable(db)
        copyOldSchedules(db)
        dropOldTables(db)
    }
}

private fun createScheduleTable(db: SupportSQLiteDatabase) {
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
        """.trimIndent(),
    )
}

private fun copyOldSchedules(db: SupportSQLiteDatabase) {
    db.execSQL(
        """
        INSERT INTO bangumi_schedule_items (
            bangumiId, episodeId, broadcastDate
        )
        SELECT bangumiId, episodeId, broadcastDate
        FROM bangumi_schedule_items_old
        """.trimIndent(),
    )
}

private fun dropOldTables(db: SupportSQLiteDatabase) {
    db.execSQL("DROP TABLE bangumi_schedule_items_old")
    db.execSQL("DROP TABLE bangumi_items_old")
}
