package com.copy9029.bangumimanagerreformed.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

// entities 数组里放入所有的 Entity（表）
// version 是数据库版本号，以后修改表结构时需要升级这个版本号
@TypeConverters(Converters::class)
@Database(entities = [Bangumi::class, BangumiSchedule::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {

    // 抽象方法，返回我们的 DAO。Room 会在编译时自动帮我们实现这个接口。
    abstract fun bangumiDao(): BangumiDao

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