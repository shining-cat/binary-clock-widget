/*
 * SPDX-FileCopyrightText: 2026 shining-cat
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package fr.shiningcat.binclockwidget.data.battery

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import fr.shiningcat.binclockwidget.domain.model.BatteryStatus

fun interface BatteryDataSource {
    /** Current battery reading, or null if the sticky broadcast is unavailable / malformed. */
    fun read(): BatteryStatus?
}

class AndroidBatteryDataSource(
    private val context: Context,
) : BatteryDataSource {
    override fun read(): BatteryStatus? {
        // Sticky broadcast: registerReceiver(null, ...) returns the last ACTION_BATTERY_CHANGED
        // intent immediately, no receiver registered, no permission required.
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)) ?: run {
            android.util.Log.w("BatteryDataSource", "Battery intent is null")
            return null
        }
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        if (level < 0 || scale <= 0) {
            android.util.Log.w("BatteryDataSource", "Invalid battery level/scale: $level/$scale")
            return null
        }
        val percent = level * 100 / scale
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN)
        val isCharging =
            status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        val result = BatteryStatus(percent = percent, isCharging = isCharging)
        android.util.Log.d("BatteryDataSource", "Battery read: $percent%, charging=$isCharging, status=$status")
        return result
    }
}
