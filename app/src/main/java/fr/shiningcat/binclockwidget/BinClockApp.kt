/*
 * SPDX-FileCopyrightText: 2026 shining-cat
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package fr.shiningcat.binclockwidget

import android.app.Application
import android.util.Log
import fr.shiningcat.binclockwidget.di.appModule
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

/**
 * Application entry point. Starts Koin so the widget, worker and settings screen can resolve
 * their dependencies from [appModule].
 */
class BinClockApp : Application() {
    /**
     * Process-lifetime scope for fire-and-forget work that must outlive a finishing Activity —
     * e.g. repainting the widget when the settings screen is dismissed. The handler is the single
     * place background failures on this scope are logged: without it an uncaught throw from any
     * launch would reach the JVM default handler and crash the process with no domain context.
     */
    val applicationScope =
        CoroutineScope(
            SupervisorJob() + Dispatchers.Default +
                CoroutineExceptionHandler { _, e -> Log.e("BinClockApp", "Background task failed", e) },
        )

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@BinClockApp)
            modules(appModule)
        }
    }
}
