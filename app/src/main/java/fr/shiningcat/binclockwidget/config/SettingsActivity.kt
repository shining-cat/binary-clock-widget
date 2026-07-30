package fr.shiningcat.binclockwidget.config

import android.Manifest
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.core.content.ContextCompat
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import fr.shiningcat.binclockwidget.config.ui.SettingsScreen
import fr.shiningcat.binclockwidget.data.weather.WeatherRefreshWorker
import fr.shiningcat.binclockwidget.widget.BinClockWidget
import fr.shiningcat.binclockwidget.widget.WidgetGraph
import kotlinx.coroutines.launch

/**
 * Widget configuration activity. Serves both the first-drop configuration flow (launched with an
 * [AppWidgetManager.EXTRA_APPWIDGET_ID]) and reconfiguration via the launcher (no widget id extra).
 */
class SettingsActivity : ComponentActivity() {
    private val viewModel: SettingsViewModel by viewModels {
        viewModelFactory {
            initializer {
                SettingsViewModel(
                    store = WidgetGraph.settingsStore(applicationContext),
                    locationGranted = {
                        ContextCompat.checkSelfPermission(
                            this@SettingsActivity,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                        ) == PackageManager.PERMISSION_GRANTED
                    },
                    materialYouAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
                )
            }
        }
    }

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            viewModel.refreshPermission()
            if (granted) {
                WorkManager.getInstance(applicationContext)
                    .enqueue(OneTimeWorkRequestBuilder<WeatherRefreshWorker>().build())
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            INVALID_APPWIDGET_ID,
        ) ?: INVALID_APPWIDGET_ID

        // Default to cancelled so backing out of first-drop configuration cancels the placement.
        setResult(RESULT_CANCELED, resultIntent(appWidgetId))

        setContent {
            MaterialTheme(colorScheme = darkColorSchemeForDevice()) {
                SettingsScreen(
                    viewModel = viewModel,
                    onRequestLocation = {
                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                    },
                    onConfirm = { confirm(appWidgetId) },
                )
            }
        }
    }

    private fun confirm(appWidgetId: Int) {
        lifecycleScope.launch {
            BinClockWidget().updateAll(applicationContext)
            setResult(RESULT_OK, resultIntent(appWidgetId))
            finish()
        }
    }

    // AMOLED-black monochrome app: always dark. Material You applies the wallpaper palette on S+.
    private fun darkColorSchemeForDevice() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            dynamicDarkColorScheme(this)
        } else {
            darkColorScheme()
        }

    private fun resultIntent(appWidgetId: Int): Intent =
        Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
}
