package com.copy9029.bangumimanagerreformed.util

import java.time.LocalDate

data class ParsedBangumiText(
    val title: String,
    val firstBroadcastDate: LocalDate,
)

private val dateRegex = Regex("""^(\d{4})年(\d{1,2})月(\d{1,2})日""")

fun parseSingleFormattedText(text: String): ParsedBangumiText? {
    val lines = text.trim().split('\n')
    if (lines.size != 2) return null

    val title = lines[0].trim()
    val dateText = lines[1].trim()

    val match = dateRegex.find(dateText) ?: return null

    val (yearStr, monthStr, dayStr) = match.destructured

    val firstBroadcastDate = try {
        LocalDate.of(
            yearStr.toInt(),
            monthStr.toInt(),
            dayStr.toInt()
        )
    } catch (_: Exception) {
        return null
    }

    return ParsedBangumiText(
        title = title,
        firstBroadcastDate = firstBroadcastDate
    )
}

fun parseMultipleFormattedText(text: String): List<ParsedBangumiText?> {
    val lines = text
        .lineSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .toList()

    return lines
        .chunked(2)
        .map { group ->
            if (group.size != 2) {
                null
            } else {
                parseSingleFormattedText(
                    group.joinToString("\n")
                )
            }
        }
}