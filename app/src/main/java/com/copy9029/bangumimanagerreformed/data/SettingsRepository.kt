package com.copy9029.bangumimanagerreformed.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore by preferencesDataStore(
    name = "settings",
)

data class CalendarSettings(
    val calendarInactiveVisibility: Int = SettingsRepository.DEFAULT_CALENDAR_INACTIVE_VISIBILITY,
    val calendarFinishedEpisodeVisible: Boolean = SettingsRepository.DEFAULT_CALENDAR_FINISHED_EPISODE_VISIBLE,
    val calendarFinishedBangumiVisible: Boolean = SettingsRepository.DEFAULT_CALENDAR_FINISHED_BANGUMI_VISIBLE,
    val calendarWeeksBeforeCurrent: Int = SettingsRepository.DEFAULT_CALENDAR_WEEKS_BEFORE_CURRENT,
    val calendarWeeksAfterCurrent: Int = SettingsRepository.DEFAULT_CALENDAR_WEEKS_AFTER_CURRENT,
    val calendarWeeksPrefix: Int = SettingsRepository.DEFAULT_CALENDAR_WEEKS_PREFIX,
)

enum class AppThemeMode(val storedValue: String) {
    LIGHT("light"),
    DARK("dark"),
    FOLLOW_SYSTEM("follow_system");

    companion object {
        fun fromStoredValue(value: String): AppThemeMode? {
            return entries.firstOrNull { mode -> mode.storedValue == value }
        }
    }
}

data class GlobalSettings(
    val appThemeMode: AppThemeMode = AppThemeMode.FOLLOW_SYSTEM,
    val default01ColorLong: Long = SettingsRepository.DEFAULT_01_COLOR_LONG,
    val default04ColorLong: Long = SettingsRepository.DEFAULT_04_COLOR_LONG,
    val default07ColorLong: Long = SettingsRepository.DEFAULT_07_COLOR_LONG,
    val default10ColorLong: Long = SettingsRepository.DEFAULT_10_COLOR_LONG,
) {
    val defaultColorBySeasonMonth: Map<Int, Long>
        get() = mapOf(
            1  to default01ColorLong,
            4  to default04ColorLong,
            7  to default07ColorLong,
            10 to default10ColorLong,
        )
}

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val dataStore = context.settingsDataStore

    val calendarSettings: Flow<CalendarSettings> = dataStore.data.map { preferences ->
        CalendarSettings(
            calendarInactiveVisibility = preferences[Keys.CALENDAR_INACTIVE_VISIBILITY]
                ?: DEFAULT_CALENDAR_INACTIVE_VISIBILITY,
            calendarFinishedEpisodeVisible = preferences[Keys.CALENDAR_FINISHED_EPISODE_VISIBLE]
                ?: DEFAULT_CALENDAR_FINISHED_EPISODE_VISIBLE,
            calendarFinishedBangumiVisible = preferences[Keys.CALENDAR_FINISHED_BANGUMI_VISIBLE]
                ?: DEFAULT_CALENDAR_FINISHED_BANGUMI_VISIBLE,
            calendarWeeksBeforeCurrent = preferences[Keys.CALENDAR_WEEKS_BEFORE_CURRENT]
                ?: DEFAULT_CALENDAR_WEEKS_BEFORE_CURRENT,
            calendarWeeksAfterCurrent = preferences[Keys.CALENDAR_WEEKS_AFTER_CURRENT]
                ?: DEFAULT_CALENDAR_WEEKS_AFTER_CURRENT,
            calendarWeeksPrefix = preferences[Keys.CALENDAR_WEEKS_PREFIX]
                ?: DEFAULT_CALENDAR_WEEKS_PREFIX,
        )
    }

    val globalSettings: Flow<GlobalSettings> = dataStore.data.map { preferences ->
        GlobalSettings(
            appThemeMode = preferences[Keys.APP_THEME_MODE]
                ?.let { storedValue -> AppThemeMode.fromStoredValue(storedValue) }
                ?: AppThemeMode.FOLLOW_SYSTEM,
            default01ColorLong = preferences[Keys.DEFAULT_01_COLOR_LONG]
                ?: DEFAULT_01_COLOR_LONG,
            default04ColorLong = preferences[Keys.DEFAULT_04_COLOR_LONG]
                ?: DEFAULT_04_COLOR_LONG,
            default07ColorLong = preferences[Keys.DEFAULT_07_COLOR_LONG]
                ?: DEFAULT_07_COLOR_LONG,
            default10ColorLong = preferences[Keys.DEFAULT_10_COLOR_LONG]
                ?: DEFAULT_10_COLOR_LONG,
        )
    }

    suspend fun setCalendarInactiveVisibility(value: Int) {
        dataStore.edit { preferences ->
            preferences[Keys.CALENDAR_INACTIVE_VISIBILITY] = value
        }
    }

    suspend fun setCalendarFinishedEpisodeVisible(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.CALENDAR_FINISHED_EPISODE_VISIBLE] = value
        }
    }

    suspend fun setCalendarFinishedBangumiVisible(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.CALENDAR_FINISHED_BANGUMI_VISIBLE] = value
        }
    }

    suspend fun setCalendarWeeksBeforeCurrent(value: Int) {
        dataStore.edit { preferences ->
            preferences[Keys.CALENDAR_WEEKS_BEFORE_CURRENT] = value
        }
    }

    suspend fun setCalendarWeeksAfterCurrent(value: Int) {
        dataStore.edit { preferences ->
            preferences[Keys.CALENDAR_WEEKS_AFTER_CURRENT] = value
        }
    }

    suspend fun setCalendarWeeksPrefix(value: Int) {
        dataStore.edit { preferences ->
            preferences[Keys.CALENDAR_WEEKS_PREFIX] = value
        }
    }

    suspend fun resetCalendarSettings() {
        dataStore.edit { preferences ->
            preferences[Keys.CALENDAR_INACTIVE_VISIBILITY] =
                DEFAULT_CALENDAR_INACTIVE_VISIBILITY
            preferences[Keys.CALENDAR_FINISHED_EPISODE_VISIBLE] =
                DEFAULT_CALENDAR_FINISHED_EPISODE_VISIBLE
            preferences[Keys.CALENDAR_FINISHED_BANGUMI_VISIBLE] =
                DEFAULT_CALENDAR_FINISHED_BANGUMI_VISIBLE
            preferences[Keys.CALENDAR_WEEKS_BEFORE_CURRENT] =
                DEFAULT_CALENDAR_WEEKS_BEFORE_CURRENT
            preferences[Keys.CALENDAR_WEEKS_AFTER_CURRENT] =
                DEFAULT_CALENDAR_WEEKS_AFTER_CURRENT
            preferences[Keys.CALENDAR_WEEKS_PREFIX] =
                DEFAULT_CALENDAR_WEEKS_PREFIX
        }
    }

    suspend fun setAppThemeMode(value: AppThemeMode) {
        dataStore.edit { preferences ->
            preferences[Keys.APP_THEME_MODE] = value.storedValue
        }
    }

    suspend fun setDefault01ColorLong(value: Long) {
        dataStore.edit { preferences ->
            preferences[Keys.DEFAULT_01_COLOR_LONG] = value
        }
    }

    suspend fun setDefault04ColorLong(value: Long) {
        dataStore.edit { preferences ->
            preferences[Keys.DEFAULT_04_COLOR_LONG] = value
        }
    }

    suspend fun setDefault07ColorLong(value: Long) {
        dataStore.edit { preferences ->
            preferences[Keys.DEFAULT_07_COLOR_LONG] = value
        }
    }

    suspend fun setDefault10ColorLong(value: Long) {
        dataStore.edit { preferences ->
            preferences[Keys.DEFAULT_10_COLOR_LONG] = value
        }
    }

    suspend fun setGlobalSettings(value: GlobalSettings) {
        dataStore.edit { preferences ->
            preferences[Keys.APP_THEME_MODE] = value.appThemeMode.storedValue
            preferences[Keys.DEFAULT_01_COLOR_LONG] = value.default01ColorLong
            preferences[Keys.DEFAULT_04_COLOR_LONG] = value.default04ColorLong
            preferences[Keys.DEFAULT_07_COLOR_LONG] = value.default07ColorLong
            preferences[Keys.DEFAULT_10_COLOR_LONG] = value.default10ColorLong
        }
    }

    private object Keys {
        val CALENDAR_INACTIVE_VISIBILITY = intPreferencesKey("calendar_inactive_visibility")
        val CALENDAR_FINISHED_EPISODE_VISIBLE = booleanPreferencesKey("calendar_finished_episode_visible")
        val CALENDAR_FINISHED_BANGUMI_VISIBLE = booleanPreferencesKey("calendar_finished_bangumi_visible")
        val CALENDAR_WEEKS_BEFORE_CURRENT = intPreferencesKey("calendar_weeks_before_current")
        val CALENDAR_WEEKS_AFTER_CURRENT = intPreferencesKey("calendar_weeks_after_current")
        val CALENDAR_WEEKS_PREFIX = intPreferencesKey("calendar_weeks_prefix")
        val APP_THEME_MODE = stringPreferencesKey("app_theme_mode")
        val DEFAULT_01_COLOR_LONG = longPreferencesKey("default_01_color_long")
        val DEFAULT_04_COLOR_LONG = longPreferencesKey("default_04_color_long")
        val DEFAULT_07_COLOR_LONG = longPreferencesKey("default_07_color_long")
        val DEFAULT_10_COLOR_LONG = longPreferencesKey("default_10_color_long")
    }

    companion object {
        const val DEFAULT_CALENDAR_INACTIVE_VISIBILITY = CalendarInactiveVisibilityDefaults.ACTIVE
        const val DEFAULT_CALENDAR_FINISHED_EPISODE_VISIBLE = true
        const val DEFAULT_CALENDAR_FINISHED_BANGUMI_VISIBLE = false
        const val DEFAULT_CALENDAR_WEEKS_BEFORE_CURRENT = 52 * 3
        const val DEFAULT_CALENDAR_WEEKS_AFTER_CURRENT = 52 * 3
        const val DEFAULT_CALENDAR_WEEKS_PREFIX = 1

        const val DEFAULT_01_COLOR_LONG = 0xFF598CD6L
        const val DEFAULT_04_COLOR_LONG = 0xFFA188D8L
        const val DEFAULT_07_COLOR_LONG = 0xFF82C956L
        const val DEFAULT_10_COLOR_LONG = 0xFFF39252L
    }
}


object CalendarInactiveVisibilityDefaults {
    const val ACTIVE = 1
    const val ALL = 2
    const val INACTIVE = 3
}
