package fr.shiningcat.binclockwidget.config

import android.Manifest
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.lifecycleScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import fr.shiningcat.binclockwidget.config.ui.SettingsScreen
import fr.shiningcat.binclockwidget.data.weather.WeatherRefreshWorker
import fr.shiningcat.binclockwidget.widget.BinClockWidget
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Widget configuration activity. Serves both the first-drop configuration flow (launched with an
 * [AppWidgetManager.EXTRA_APPWIDGET_ID]) and reconfiguration via the launcher (no widget id extra).
 */
class SettingsActivity : ComponentActivity() {
    private val viewModel: SettingsViewModel by viewModel()

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
            MaterialTheme(colorScheme = colorSchemeForDevice()) {
                // Surface paints the scheme background and sets content colour to onSurface, so text
                // is legible regardless of the DayNight window background (black-on-dark otherwise).
                // It stays fillMaxSize (edge-to-edge, painting behind the system bars on Android 15+
                // where edge-to-edge is enforced); safeDrawingPadding then insets the content so text
                // clears the status and navigation bars.
                Surface(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.safeDrawingPadding()) {
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
        }
    }

    private fun confirm(appWidgetId: Int) {
        lifecycleScope.launch {
            BinClockWidget().updateAll(applicationContext)
            setResult(RESULT_OK, resultIntent(appWidgetId))
            finish()
        }
    }

    // Settings follows the system light/dark preference (the widget itself stays AMOLED-black).
    // Material You applies the wallpaper palette on S+; otherwise the static Material schemes.
    @Composable
    private fun colorSchemeForDevice() = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (isSystemInDarkTheme()) dynamicDarkColorScheme(this) else dynamicLightColorScheme(this)
        isSystemInDarkTheme() -> darkColorScheme()
        else -> lightColorScheme()
    }

    private fun resultIntent(appWidgetId: Int): Intent =
        Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
}
