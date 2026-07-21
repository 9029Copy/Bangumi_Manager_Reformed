package com.copy9029.bangumimanagerreformed.util

import java.time.LocalDate
import java.time.YearMonth

fun calcNearestSeason(
    today: LocalDate,
): YearMonth {
    val year = today.year
    val month = today.monthValue

    return when (month) {
        12 -> YearMonth.of(year + 1, 1)
        1, 2 -> YearMonth.of(year, 1)
        3, 4, 5 -> YearMonth.of(year, 4)
        6, 7, 8 -> YearMonth.of(year, 7)
        else -> YearMonth.of(year, 10)
    }
}