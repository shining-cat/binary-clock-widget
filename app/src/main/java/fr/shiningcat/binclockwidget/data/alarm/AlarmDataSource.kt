/*
 * SPDX-FileCopyrightText: 2026 shining-cat
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package fr.shiningcat.binclockwidget.data.alarm

import android.app.AlarmManager
import android.content.Context

fun interface AlarmDataSource {
    fun isAlarmSet(): Boolean
}

class AndroidAlarmDataSource(
    private val context: Context,
) : AlarmDataSource {
    override fun isAlarmSet(): Boolean {
        val manager = context.getSystemService(AlarmManager::class.java) ?: return false
        return manager.nextAlarmClock != null
    }
}
