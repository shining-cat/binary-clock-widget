/*
 * SPDX-FileCopyrightText: 2026 shining-cat
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "BinClockWidget"

// Type-safe project accessors (e.g. `projects.app`) — lets the root build script reference
// modules as proper dependency notation instead of a Project object, avoiding the deprecated
// "Project object as dependency notation" path in `dependencies { kover(...) }`.
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":app")
