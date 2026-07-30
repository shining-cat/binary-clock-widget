/*
 * SPDX-FileCopyrightText: 2026 shining-cat
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package fr.shiningcat.binclockwidget.config

import fr.shiningcat.binclockwidget.data.settings.SettingsStore
import fr.shiningcat.binclockwidget.domain.model.TapAction
import fr.shiningcat.binclockwidget.domain.model.TapZone
import fr.shiningcat.binclockwidget.domain.model.WidgetSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

private class FakeSettingsStore(
    initial: WidgetSettings = WidgetSettings(),
) : SettingsStore {
    val state = MutableStateFlow(initial)

    override fun settings(): Flow<WidgetSettings> = state

    override suspend fun update(transform: (WidgetSettings) -> WidgetSettings) {
        state.value = transform(state.value)
    }
}

private class FakeLocationChecker(
    var granted: Boolean,
) : () -> Boolean {
    override fun invoke(): Boolean = granted
}

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    private val store = FakeSettingsStore()
    private val checker = FakeLocationChecker(granted = false)

    private fun viewModel(materialYouAvailable: Boolean = true) = SettingsViewModel(store, checker, materialYouAvailable)

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun TestScope.collect(vm: SettingsViewModel) {
        backgroundScope.launch { vm.uiState.collect { } }
    }

    @Test
    fun `initial state is Loading then Ready reflecting store, permission and materialYou`() =
        runTest {
            checker.granted = true
            val vm = viewModel(materialYouAvailable = true)

            assertEquals(SettingsUiState.Loading, vm.uiState.value)

            collect(vm)
            advanceUntilIdle()

            val ready = vm.uiState.value as SettingsUiState.Ready
            assertEquals(store.state.value, ready.settings)
            assertTrue(ready.locationGranted)
            assertTrue(ready.materialYouAvailable)
        }

    @Test
    fun `onColorChanged writes through to store and state`() =
        runTest {
            val vm = viewModel()
            collect(vm)
            advanceUntilIdle()

            vm.onColorChanged(0xFF00FF00.toInt())
            advanceUntilIdle()

            assertEquals(0xFF00FF00.toInt(), store.state.value.colorArgb)
            val ready = vm.uiState.value as SettingsUiState.Ready
            assertEquals(0xFF00FF00.toInt(), ready.settings.colorArgb)
        }

    @Test
    fun `onIconColorChanged sets a value then null resets to inherit`() =
        runTest {
            val vm = viewModel()
            collect(vm)
            advanceUntilIdle()

            vm.onIconColorChanged(0xFF112233.toInt())
            advanceUntilIdle()
            assertEquals(0xFF112233.toInt(), store.state.value.iconColorArgb)

            vm.onIconColorChanged(null)
            advanceUntilIdle()
            assertEquals(null, store.state.value.iconColorArgb)
        }

    @Test
    fun `onHairlineToggled writes through`() =
        runTest {
            val vm = viewModel()
            collect(vm)
            advanceUntilIdle()

            vm.onHairlineToggled(true)
            advanceUntilIdle()

            assertTrue(store.state.value.hairline)
            assertTrue((vm.uiState.value as SettingsUiState.Ready).settings.hairline)
        }

    @Test
    fun `onTapActionChanged updates the zone action`() =
        runTest {
            val vm = viewModel()
            collect(vm)
            advanceUntilIdle()

            vm.onTapActionChanged(TapZone.WEATHER, TapAction.OPEN_APP)
            advanceUntilIdle()

            assertEquals(TapAction.OPEN_APP, store.state.value.tapActions[TapZone.WEATHER])
        }

    @Test
    fun `onMaterialYouToggled writes through`() =
        runTest {
            val vm = viewModel()
            collect(vm)
            advanceUntilIdle()

            vm.onMaterialYouToggled(true)
            advanceUntilIdle()

            assertTrue(store.state.value.useMaterialYou)
        }

    @Test
    fun `onTapAppPackageChanged updates the zone package`() =
        runTest {
            val vm = viewModel()
            collect(vm)
            advanceUntilIdle()

            vm.onTapAppPackageChanged(TapZone.WEATHER, "com.example")
            advanceUntilIdle()

            assertEquals("com.example", store.state.value.tapAppPackages[TapZone.WEATHER])
        }

    @Test
    fun `refreshPermission re-reads the injected checker`() =
        runTest {
            checker.granted = false
            val vm = viewModel()
            collect(vm)
            advanceUntilIdle()

            assertEquals(false, (vm.uiState.value as SettingsUiState.Ready).locationGranted)

            checker.granted = true
            vm.refreshPermission()
            advanceUntilIdle()

            assertTrue((vm.uiState.value as SettingsUiState.Ready).locationGranted)
        }
}
