/*
 * SPDX-FileCopyrightText: 2026 shining-cat
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package fr.shiningcat.binclockwidget.data.weather

import fr.shiningcat.binclockwidget.data.settings.SettingsStore
import fr.shiningcat.binclockwidget.domain.model.WeatherSnapshot
import kotlinx.coroutines.flow.first

interface WeatherRepository {
    /** Returns cached snapshot immediately if present; null if never fetched. */
    suspend fun cached(): WeatherSnapshot?

    /** Fetches fresh, writes cache, returns it. Throws on network/parse error. */
    suspend fun refresh(
        lat: Double,
        lon: Double,
    ): WeatherSnapshot
}

interface WeatherCache {
    suspend fun read(): WeatherSnapshot?

    suspend fun write(snapshot: WeatherSnapshot)
}

class WeatherRepositoryImpl(
    // Builds an API bound to a given base URL. The URL is user-configurable (settings), so the API
    // can't be a fixed singleton — it's rebuilt per refresh from the current endpoint. Cheap: refresh
    // runs at most every 30 min and the factory reuses the shared OkHttp/JSON stack.
    private val apiFactory: (baseUrl: String) -> OpenMeteoApi,
    private val settings: SettingsStore,
    private val cache: WeatherCache,
    private val now: () -> Long = { System.currentTimeMillis() },
) : WeatherRepository {
    override suspend fun cached(): WeatherSnapshot? = cache.read()

    override suspend fun refresh(
        lat: Double,
        lon: Double,
    ): WeatherSnapshot {
        val endpoint = settings.settings().first().weatherEndpoint
        // Blank = weather disabled. Callers (the worker) guard on this before requesting location;
        // require() here is a defensive backstop so a misuse fails loudly rather than silently
        // hitting the default server the user never opted into.
        require(endpoint.isNotBlank()) { "weather endpoint not configured (weather is disabled)" }
        val api = apiFactory(WeatherEndpoint.normalize(endpoint))
        val dto = api.forecast(lat, lon)
        return WeatherMapper.toSnapshot(dto, now()).also { cache.write(it) }
    }
}
