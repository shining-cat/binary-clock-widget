/*
 * SPDX-FileCopyrightText: 2026 shining-cat
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package fr.shiningcat.binclockwidget.domain

import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Derives day/night from the *current* clock against today's sunrise/sunset, instead of trusting a
 * boolean baked at fetch time. The fetch (WeatherRefreshWorker) runs at most every 30 min and is
 * Doze-deferrable, so a cached `is_day` goes stale — the moon crescent would otherwise persist into
 * the day. Comparing time-of-day self-corrects on every minute-tick redraw.
 */
object DayNightResolver {
    /**
     * @param sunriseIso / [sunsetIso] Open-Meteo ISO-local timestamps (e.g. "2026-08-04T05:42").
     * @param fallback the fetch-time `is_day` value, used when sunrise/sunset are absent or unparseable.
     * @return true if [now] falls in [sunrise, sunset) by time-of-day; else [fallback].
     */
    fun isDay(
        now: LocalDateTime,
        sunriseIso: String?,
        sunsetIso: String?,
        fallback: Boolean,
    ): Boolean {
        val sunrise = sunriseIso?.let { parseLocalTime(it) } ?: return fallback
        val sunset = sunsetIso?.let { parseLocalTime(it) } ?: return fallback
        val nowTime = now.toLocalTime()
        return nowTime >= sunrise && nowTime < sunset
    }

    private fun parseLocalTime(iso: String): LocalTime? = runCatching { LocalDateTime.parse(iso).toLocalTime() }.getOrNull()
}
