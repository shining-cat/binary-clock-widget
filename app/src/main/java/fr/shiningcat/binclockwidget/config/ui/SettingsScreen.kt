package fr.shiningcat.binclockwidget.config.ui

import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.shiningcat.binclockwidget.config.SettingsUiState
import fr.shiningcat.binclockwidget.config.SettingsViewModel
import fr.shiningcat.binclockwidget.domain.model.TapAction
import fr.shiningcat.binclockwidget.domain.model.TapZone
import fr.shiningcat.binclockwidget.domain.model.WidgetSettings

/** An installed, launchable app the user can bind a tap zone to. */
data class InstalledApp(val packageName: String, val label: String)

/**
 * Selectable actions per zone, per design §7. The zone default is always included so the
 * dropdown can render the current value; irrelevant actions (e.g. calendar on the alarm
 * zone) are deliberately omitted for a sensible UX.
 */
fun tapActionOptions(zone: TapZone): List<TapAction> = when (zone) {
    TapZone.ALARM -> listOf(TapAction.NONE, TapAction.OPEN_ALARMS, TapAction.OPEN_APP)
    TapZone.TIME -> listOf(TapAction.NONE, TapAction.OPEN_CLOCK, TapAction.OPEN_ALARMS, TapAction.OPEN_APP)
    TapZone.DATE -> listOf(TapAction.NONE, TapAction.OPEN_CALENDAR, TapAction.OPEN_APP)
    TapZone.WEATHER -> listOf(TapAction.NONE, TapAction.OPEN_APP)
}

fun sortAppsByLabel(apps: List<InstalledApp>): List<InstalledApp> =
    apps.distinctBy { it.packageName }.sortedBy { it.label.lowercase() }

private fun loadLaunchableApps(pm: PackageManager): List<InstalledApp> {
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    return sortAppsByLabel(
        pm.queryIntentActivities(intent, 0).map {
            InstalledApp(it.activityInfo.packageName, it.loadLabel(pm).toString())
        },
    )
}

private val presetColors: List<Int> = listOf(
    0xFFFFFFFF, 0xFFB0BEC5, 0xFFFF5252, 0xFFFFB300, 0xFF69F0AE, 0xFF40C4FF, 0xFFE040FB,
).map { it.toInt() }

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onRequestLocation: () -> Unit,
    onConfirm: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsScreen(
        state = uiState,
        onColorChanged = viewModel::onColorChanged,
        onMaterialYouToggled = viewModel::onMaterialYouToggled,
        onHairlineToggled = viewModel::onHairlineToggled,
        onTapActionChanged = viewModel::onTapActionChanged,
        onTapAppPackageChanged = viewModel::onTapAppPackageChanged,
        onRequestLocation = onRequestLocation,
        onConfirm = onConfirm,
    )
}

@Composable
private fun SettingsScreen(
    state: SettingsUiState,
    onColorChanged: (Int) -> Unit,
    onMaterialYouToggled: (Boolean) -> Unit,
    onHairlineToggled: (Boolean) -> Unit,
    onTapActionChanged: (TapZone, TapAction) -> Unit,
    onTapAppPackageChanged: (TapZone, String?) -> Unit,
    onRequestLocation: () -> Unit,
    onConfirm: () -> Unit,
) {
    when (state) {
        SettingsUiState.Loading -> Unit
        is SettingsUiState.Ready -> ReadySettings(
            state = state,
            onColorChanged = onColorChanged,
            onMaterialYouToggled = onMaterialYouToggled,
            onHairlineToggled = onHairlineToggled,
            onTapActionChanged = onTapActionChanged,
            onTapAppPackageChanged = onTapAppPackageChanged,
            onRequestLocation = onRequestLocation,
            onConfirm = onConfirm,
        )
    }
}

@Composable
private fun ReadySettings(
    state: SettingsUiState.Ready,
    onColorChanged: (Int) -> Unit,
    onMaterialYouToggled: (Boolean) -> Unit,
    onHairlineToggled: (Boolean) -> Unit,
    onTapActionChanged: (TapZone, TapAction) -> Unit,
    onTapAppPackageChanged: (TapZone, String?) -> Unit,
    onRequestLocation: () -> Unit,
    onConfirm: () -> Unit,
) {
    val settings = state.settings
    val pm = LocalContext.current.packageManager
    val apps = remember { loadLaunchableApps(pm) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Cheatsheet()

        Section("Color") {
            ColorSwatches(selected = settings.colorArgb, onSelected = onColorChanged)
            if (state.materialYouAvailable) {
                ToggleRow(
                    label = "Use Material You",
                    checked = settings.useMaterialYou,
                    onCheckedChange = onMaterialYouToggled,
                )
            }
        }

        Section("Hairline separator") {
            ToggleRow(
                label = "Show separator",
                checked = settings.hairline,
                onCheckedChange = onHairlineToggled,
            )
        }

        Section("Tap actions") {
            TapZone.entries.forEach { zone ->
                TapZoneSetting(
                    zone = zone,
                    settings = settings,
                    apps = apps,
                    onTapActionChanged = onTapActionChanged,
                    onTapAppPackageChanged = onTapAppPackageChanged,
                )
            }
        }

        Section("Location") {
            Text(
                text = if (state.locationGranted) {
                    "Location permission granted — weather can update."
                } else {
                    "Location permission is required to show local weather."
                },
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(
                onClick = onRequestLocation,
                enabled = !state.locationGranted,
            ) {
                Text("Grant location")
            }
        }

        Button(
            onClick = onConfirm,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Done")
        }
    }
}

@Composable
private fun Cheatsheet() {
    Section("How to read the clock") {
        Text(
            text = "Each row is a number in binary. A filled dot counts, a hollow ring " +
                "does not — add the place values of the filled dots to read the number. " +
                "Rows, top to bottom: hours, minutes, day of month, month.",
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = "Glyphs: the icon on the hours row shows the alarm (a bell when one is " +
                "set, struck through when none). The icons on the last two rows show the " +
                "weather now, today and tomorrow.",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun TapZoneSetting(
    zone: TapZone,
    settings: WidgetSettings,
    apps: List<InstalledApp>,
    onTapActionChanged: (TapZone, TapAction) -> Unit,
    onTapAppPackageChanged: (TapZone, String?) -> Unit,
) {
    val action = settings.tapActions[zone] ?: TapAction.NONE
    LabeledDropdown(
        label = zoneLabel(zone),
        selectedLabel = actionLabel(action),
        options = tapActionOptions(zone),
        optionLabel = ::actionLabel,
        onSelected = { onTapActionChanged(zone, it) },
    )
    if (action == TapAction.OPEN_APP) {
        val selectedPackage = settings.tapAppPackages[zone]
        LabeledDropdown(
            label = "App",
            selectedLabel = apps.firstOrNull { it.packageName == selectedPackage }?.label
                ?: "Choose an app",
            options = apps,
            optionLabel = { it.label },
            onSelected = { onTapAppPackageChanged(zone, it.packageName) },
        )
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        HorizontalDivider()
        content()
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun ColorSwatches(selected: Int, onSelected: (Int) -> Unit) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        presetColors.forEach { argb ->
            val isSelected = argb == selected
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(argb))
                    .border(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline
                        },
                        shape = CircleShape,
                    )
                    .clickable { onSelected(argb) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> LabeledDropdown(
    label: String,
    selectedLabel: String,
    options: List<T>,
    optionLabel: (T) -> String,
    onSelected: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        TextField(
            readOnly = true,
            value = selectedLabel,
            onValueChange = {},
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

private fun zoneLabel(zone: TapZone): String = when (zone) {
    TapZone.ALARM -> "Alarm"
    TapZone.TIME -> "Time"
    TapZone.DATE -> "Date"
    TapZone.WEATHER -> "Weather"
}

private fun actionLabel(action: TapAction): String = when (action) {
    TapAction.NONE -> "Do nothing"
    TapAction.OPEN_ALARMS -> "Open alarms"
    TapAction.OPEN_CLOCK -> "Open clock"
    TapAction.OPEN_CALENDAR -> "Open calendar"
    TapAction.OPEN_APP -> "Open an app"
}
