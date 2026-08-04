package com.copy9029.bangumimanagerreformed.ui

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import java.util.Locale

@Composable
fun MondayFirstCalendarLocale(
    content: @Composable () -> Unit,
) {
    val configuration = LocalConfiguration.current
    val mondayFirstConfiguration = remember(configuration) {
        Configuration(configuration).apply {
            val currentLocale = locales[0]
            // The Unicode "fw" extension changes only the first day of the week.
            val mondayFirstLocale = Locale.Builder()
                .setLocale(currentLocale)
                .setUnicodeLocaleKeyword("fw", "mon")
                .build()
            setLocale(mondayFirstLocale)
        }
    }

    CompositionLocalProvider(
        LocalConfiguration provides mondayFirstConfiguration,
        content = content,
    )
}
