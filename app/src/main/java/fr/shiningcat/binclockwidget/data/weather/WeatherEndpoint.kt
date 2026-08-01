/*
 * SPDX-FileCopyrightText: 2026 shining-cat
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package fr.shiningcat.binclockwidget.data.weather

import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

/**
 * Weather service endpoint rules, shared by the settings UI (validation) and the repository
 * (base-URL normalisation) so both sides agree on what a "valid endpoint" is.
 *
 * A blank endpoint means **weather is disabled** — the app makes no location or network calls.
 * Weather is opt-in: the user supplies an Open-Meteo-compatible base URL (the public default via
 * the "Use Open-Meteo" button, or their own self-hosted server) to turn it on.
 *
 * Parsing goes through OkHttp's [okhttp3.HttpUrl] — the very parser Retrofit uses for its base URL —
 * so a URL the UI accepts is guaranteed to build a Retrofit instance later.
 */
object WeatherEndpoint {
    const val DEFAULT_BASE_URL = "https://api.open-meteo.com/"

    /** Blank (disabled) is valid; otherwise the string must parse as an http(s) URL. */
    fun isValid(raw: String): Boolean {
        val trimmed = raw.trim()
        return trimmed.isEmpty() || trimmed.toHttpUrlOrNull() != null
    }

    /**
     * Normalises a non-blank endpoint to a Retrofit-safe base URL (guaranteed trailing "/").
     * Falls back to [DEFAULT_BASE_URL] if the input somehow doesn't parse — callers are expected to
     * have gated on [isValid] and to never pass a blank string (blank = disabled, no fetch).
     */
    fun normalize(raw: String): String {
        val url = raw.trim().toHttpUrlOrNull() ?: DEFAULT_BASE_URL.toHttpUrl()
        val asString = url.toString()
        return if (asString.endsWith("/")) asString else "$asString/"
    }
}
