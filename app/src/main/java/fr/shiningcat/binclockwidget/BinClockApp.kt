package fr.shiningcat.binclockwidget

import android.app.Application
import fr.shiningcat.binclockwidget.di.appModule
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
     * e.g. repainting the widget when the settings screen is dismissed.
     */
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@BinClockApp)
            modules(appModule)
        }
    }
}
