/*
 * SPDX-FileCopyrightText: 2026 shining-cat
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package fr.shiningcat.binclockwidget.data.weather

import fr.shiningcat.binclockwidget.domain.model.WeatherSnapshot

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
    private val api: OpenMeteoApi,
    private val cache: WeatherCache,
    private val now: () -> Long = { System.currentTimeMillis() },
) : WeatherRepository {
    override suspend fun cached(): WeatherSnapshot? = cache.read()

    override suspend fun refresh(
        lat: Double,
        lon: Double,
    ): WeatherSnapshot {
        val dto = api.forecast(lat, lon)
        return WeatherMapper.toSnapshot(dto, now()).also { cache.write(it) }
    }
}
