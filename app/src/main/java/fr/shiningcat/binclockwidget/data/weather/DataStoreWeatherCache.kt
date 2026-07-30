/*
 * SPDX-FileCopyrightText: 2026 shining-cat
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package fr.shiningcat.binclockwidget.data.weather

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import fr.shiningcat.binclockwidget.domain.model.WeatherSnapshot
import kotlinx.coroutines.flow.first

class DataStoreWeatherCache(
    private val dataStore: DataStore<Preferences>,
) : WeatherCache {
    override suspend fun read(): WeatherSnapshot? {
        val prefs = dataStore.data.first()
        val fetchedAt = prefs[FETCHED_AT] ?: return null
        return WeatherSnapshot(
            nowCode = prefs[NOW_CODE] ?: 0,
            nowIsDay = prefs[NOW_IS_DAY] ?: true,
            todayCode = prefs[TODAY_CODE] ?: 0,
            tomorrowCode = prefs[TOMORROW_CODE] ?: 0,
            fetchedAtEpochMs = fetchedAt,
        )
    }

    override suspend fun write(snapshot: WeatherSnapshot) {
        dataStore.edit { prefs ->
            prefs[NOW_CODE] = snapshot.nowCode
            prefs[NOW_IS_DAY] = snapshot.nowIsDay
            prefs[TODAY_CODE] = snapshot.todayCode
            prefs[TOMORROW_CODE] = snapshot.tomorrowCode
            prefs[FETCHED_AT] = snapshot.fetchedAtEpochMs
        }
    }

    private companion object {
        val NOW_CODE = intPreferencesKey("now_code")
        val NOW_IS_DAY = booleanPreferencesKey("now_is_day")
        val TODAY_CODE = intPreferencesKey("today_code")
        val TOMORROW_CODE = intPreferencesKey("tomorrow_code")
        val FETCHED_AT = longPreferencesKey("fetched_at")
    }
}
