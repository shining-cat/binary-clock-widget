package fr.shiningcat.binclockwidget

import android.app.Application
import fr.shiningcat.binclockwidget.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

/**
 * Application entry point. Starts Koin so the widget, worker and settings screen can resolve
 * their dependencies from [appModule].
 */
class BinClockApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@BinClockApp)
            modules(appModule)
        }
    }
}
