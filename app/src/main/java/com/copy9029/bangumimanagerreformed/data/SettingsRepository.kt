package com.copy9029.bangumimanagerreformed.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
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

    private object Keys {
        val CALENDAR_INACTIVE_VISIBILITY = intPreferencesKey("calendar_inactive_visibility")
        val CALENDAR_FINISHED_EPISODE_VISIBLE = booleanPreferencesKey("calendar_finished_episode_visible")
        val CALENDAR_FINISHED_BANGUMI_VISIBLE = booleanPreferencesKey("calendar_finished_bangumi_visible")
        val CALENDAR_WEEKS_BEFORE_CURRENT = intPreferencesKey("calendar_weeks_before_current")
        val CALENDAR_WEEKS_AFTER_CURRENT = intPreferencesKey("calendar_weeks_after_current")
        val CALENDAR_WEEKS_PREFIX = intPreferencesKey("calendar_weeks_prefix")
    }

    companion object {
        const val DEFAULT_CALENDAR_INACTIVE_VISIBILITY = CalendarInactiveVisibilityDefaults.ACTIVE
        const val DEFAULT_CALENDAR_FINISHED_EPISODE_VISIBLE = true
        const val DEFAULT_CALENDAR_FINISHED_BANGUMI_VISIBLE = false
        const val DEFAULT_CALENDAR_WEEKS_BEFORE_CURRENT = 52 * 3
        const val DEFAULT_CALENDAR_WEEKS_AFTER_CURRENT = 52 * 3
        const val DEFAULT_CALENDAR_WEEKS_PREFIX = 1
    }
}


object CalendarInactiveVisibilityDefaults {
    const val ACTIVE = 1
    const val ALL = 2
    const val INACTIVE = 3
}
