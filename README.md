# BinClockWidget

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

An AMOLED-black, monochrome **binary-clock** Android home-screen widget.

It renders the current time and date as a 6×4 grid of dots. Cells that would
otherwise go unused carry a couple of small extras: the device **alarm state**
and a **weather** glyph. The aesthetic is deliberately austere — solid dots on
true black, no chrome, no colour — so it disappears into an AMOLED wallpaper and
sips battery.

## How to read it

The face is a grid of **rows** (values) and **columns** (place values):

- **Rows** encode, top to bottom: **hours**, **minutes**, **day** (of month),
  **month**.
- **Columns** are binary place values: **32 · 16 · 8 · 4 · 2 · 1**.
- A **lit** (solid) dot means that place value is *on*; an unlit dot is *off*.
  Add the lit columns in a row to read that row's number.

For example, a minutes row with the `32`, `8`, and `2` dots lit reads
`32 + 8 + 2 = 42`.

The cells not needed by the low-value ends of the shorter rows are reused to show
the **alarm state** and a **weather** condition glyph.

## Tech stack

- **Kotlin**
- **Jetpack Glance** (`glance-appwidget`) for the widget UI
- **Koin** for dependency injection
- **DataStore** for settings + weather caching
- **Retrofit / OkHttp / kotlinx.serialization** for networking
- **WorkManager** for periodic weather refresh
- **Open-Meteo** as the weather data source (keyless, FOSS-friendly)

## Distribution

BinClockWidget targets **F-Droid**: it is 100% FOSS and contains **no Google
Play Services, no Firebase, and no proprietary SDKs**. Weather uses the keyless
[Open-Meteo](https://open-meteo.com) API; location uses the platform
`LocationManager` (no fused/Play location).

## Build

```bash
./gradlew :app:assembleDebug
```

Requirements: JDK 21, Android SDK with platform 37 installed.

- **minSdk** 26
- **compileSdk / targetSdk** 37

## License

BinClockWidget is free and open-source software licensed under the
[GNU General Public License v3.0](LICENSE).

```
SPDX-License-Identifier: GPL-3.0-or-later
```

You're free to use, modify, and distribute this software, but any derivative
works must also be open source under GPL-3.0-or-later.
