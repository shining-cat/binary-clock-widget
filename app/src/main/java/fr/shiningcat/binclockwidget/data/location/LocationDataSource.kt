/*
 * SPDX-FileCopyrightText: 2026 shining-cat
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package fr.shiningcat.binclockwidget.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Looper
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

interface LocationDataSource {
    /**
     * A coarse location as (latitude, longitude), or null if unavailable / no permission.
     * Suspends: may await a single fresh fix when no cached location exists.
     */
    suspend fun currentLocation(): Pair<Double, Double>?
}

class AndroidLocationDataSource(
    private val context: Context,
) : LocationDataSource {
    // Permission is checked in hasLocationPermission() below, which lint's dataflow can't follow;
    // calls are additionally guarded by runCatching against a revoked-at-runtime SecurityException.
    @SuppressLint("MissingPermission")
    override suspend fun currentLocation(): Pair<Double, Double>? {
        if (!hasLocationPermission()) return null
        val manager = context.getSystemService(LocationManager::class.java) ?: return null

        // Only providers usable with ACCESS_COARSE_LOCATION. GPS_PROVIDER and PASSIVE_PROVIDER both
        // require ACCESS_FINE_LOCATION, which the app deliberately does not request — querying them
        // throws SecurityException (this was the bug behind "weather never appears": PASSIVE always
        // threw and was swallowed). FUSED_PROVIDER is the AOSP platform fused provider (API 31+),
        // NOT Google Play Services, so it stays FOSS-clean.
        val providers =
            buildList {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) add(LocationManager.FUSED_PROVIDER)
                add(LocationManager.NETWORK_PROVIDER)
            }

        // 1) A cached last-known fix is instant and enough for a weather widget.
        providers
            .firstNotNullOfOrNull { provider ->
                runCatching { manager.getLastKnownLocation(provider) }.getOrNull()
            }?.let { return it.latitude to it.longitude }

        // 2) Cold device with no cached fix (the reviewer's emulator case) — ask for a single fresh
        //    one, bounded so a background refresh never hangs.
        return requestSingleFix(manager, providers)
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestSingleFix(
        manager: LocationManager,
        providers: List<String>,
    ): Pair<Double, Double>? =
        withTimeoutOrNull(FIX_TIMEOUT_MS) {
            for (provider in providers) {
                val enabled = runCatching { manager.isProviderEnabled(provider) }.getOrDefault(false)
                if (!enabled) continue
                val fix = awaitFix(manager, provider) ?: continue
                return@withTimeoutOrNull fix.latitude to fix.longitude
            }
            null
        }

    @SuppressLint("MissingPermission")
    private suspend fun awaitFix(
        manager: LocationManager,
        provider: String,
    ): Location? =
        suspendCancellableCoroutine { cont ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val signal = CancellationSignal()
                cont.invokeOnCancellation { signal.cancel() }
                runCatching {
                    manager.getCurrentLocation(provider, signal, context.mainExecutor) { location ->
                        if (cont.isActive) cont.resume(location)
                    }
                }.onFailure { if (cont.isActive) cont.resume(null) }
            } else {
                val listener =
                    object : LocationListener {
                        override fun onLocationChanged(location: Location) {
                            manager.removeUpdates(this)
                            if (cont.isActive) cont.resume(location)
                        }

                        @Deprecated("Required by the pre-API-30 LocationListener contract")
                        override fun onStatusChanged(
                            provider: String?,
                            status: Int,
                            extras: Bundle?,
                        ) = Unit

                        override fun onProviderEnabled(provider: String) = Unit

                        override fun onProviderDisabled(provider: String) = Unit
                    }
                cont.invokeOnCancellation { manager.removeUpdates(listener) }
                runCatching {
                    manager.requestLocationUpdates(provider, 0L, 0f, listener, Looper.getMainLooper())
                }.onFailure { if (cont.isActive) cont.resume(null) }
            }
        }

    private fun hasLocationPermission(): Boolean =
        context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private companion object {
        const val FIX_TIMEOUT_MS = 10_000L
    }
}
