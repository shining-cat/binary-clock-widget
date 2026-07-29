package fr.shiningcat.binclockwidget.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager

interface LocationDataSource {
    /** Coarse last-known location as (latitude, longitude), or null if unavailable / no permission. */
    fun lastKnown(): Pair<Double, Double>?
}

class AndroidLocationDataSource(
    private val context: Context,
) : LocationDataSource {
    // Permission is checked in hasLocationPermission() below, which lint's dataflow can't follow;
    // the call is additionally guarded by runCatching against a revoked-at-runtime SecurityException.
    @SuppressLint("MissingPermission")
    override fun lastKnown(): Pair<Double, Double>? {
        if (!hasLocationPermission()) return null
        val manager = context.getSystemService(LocationManager::class.java) ?: return null
        val providers = listOf(LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER)
        return providers.firstNotNullOfOrNull { provider ->
            runCatching { manager.getLastKnownLocation(provider) }.getOrNull()
                ?.let { it.latitude to it.longitude }
        }
    }

    private fun hasLocationPermission(): Boolean =
        context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
}
