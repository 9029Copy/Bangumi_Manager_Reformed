package com.copy9029.bangumimanagerreformed.data

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.YearMonth

class Converters {

    // ================= LocalDate 转换 =================

    // LocalDate -> Long (存储为 EpochDay)
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): Long? {
        return date?.toEpochDay()
    }

    // Long -> LocalDate
    @TypeConverter
    fun toLocalDate(epochDay: Long?): LocalDate? {
        return epochDay?.let { LocalDate.ofEpochDay(it) }
    }

//    // ================= YearMonth 转换 =================
//
//    // YearMonth -> String (存储为 "yyyy-MM" 格式)
//    @TypeConverter
//    fun fromYearMonth(yearMonth: YearMonth?): String? {
//        return yearMonth?.toString()
//    }
//
//    // String -> YearMonth
//    @TypeConverter
//    fun toYearMonth(value: String?): YearMonth? {
//        return value?.let { YearMonth.parse(it) }
//    }
}