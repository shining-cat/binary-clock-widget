/*
 * SPDX-FileCopyrightText: 2026 shining-cat
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
import java.util.Locale

plugins {
    // Gradle core plugin that provides standard lifecycle tasks (check, build) to the root project.
    base
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ktlint.gradle) apply false
    alias(libs.plugins.spotless)
    alias(libs.plugins.kover)
    alias(libs.plugins.dependencyupdate)
}

// Configure the root clean task to also delete Kover reports.
tasks.named<Delete>("clean") {
    delete("build/reports/kover")
}

// Dependency-update plugin: only surface stable releases (not RC/beta/alpha/snapshot).
fun String.isNonStable(): Boolean {
    val stableKeyword =
        listOf("RELEASE", "FINAL", "GA").any { uppercase(Locale.getDefault()).contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(this)
    return isStable.not()
}

tasks.withType<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask> {
    rejectVersionIf { candidate.version.isNonStable() }
    // NOTE: this plugin is incompatible with the configuration cache / parallel execution on
    // Gradle 9+. Run with: ./gradlew dependencyUpdates --no-configuration-cache --no-parallel
    // See: https://github.com/ben-manes/gradle-versions-plugin/issues/839
}

// Kover coverage: shared exclusions for framework/generated/UI-only code that carries no logic.
fun kotlinx.kover.gradle.plugin.dsl.KoverReportFiltersConfig.applyCommonExclusions() {
    excludes {
        annotatedBy("*Generated")
        annotatedBy("javax.annotation.processing.Generated")

        // Generated / framework classes with no business logic.
        classes("*.BuildConfig", "*.R", "*.R$*")
        classes("*ComposableSingletons*")
        classes("*PreviewParameterProvider")

        // DI wiring and plain data holders.
        packages("*.di")
        packages("*.domain.model")

        // Pure Compose/Glance UI — verified via interaction, not unit coverage.
        packages("*.config.ui")
        packages("*.widget")
    }
}

kover {
    reports {
        filters {
            applyCommonExclusions()
        }
    }
}

dependencies {
    kover(projects.app)
}

// ktlint is applied per-project (it lints each project's own source set). Applying and configuring
// it across all projects covers both the root Gradle scripts and the :app module uniformly.
allprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        android.set(true)
        outputColorName.set("RED")

        filter {
            exclude { element -> element.file.path.contains("generated/") }
            exclude { element -> element.file.path.contains("/build/") }
        }
    }
}

// Spotless: license headers + trailing-whitespace/newline hygiene.
spotless {
    format("markdown") {
        target("**/*.md")
        targetExclude("**/build/**", "**/.gradle/**")
        // License headers for markdown are managed manually: README.md has badges/images that
        // automated header insertion would disrupt, and delimiter detection is unreliable for
        // varied markdown. New docs should add the header from license-header-markdown.txt.
        trimTrailingWhitespace()
        endWithNewline()
    }

    format("misc") {
        target(".gitignore")
        trimTrailingWhitespace()
        endWithNewline()
    }

    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**", "**/.gradle/**", "**/generated/**")
        licenseHeaderFile(
            rootProject.file("license-header.txt"),
            "(@file|package|import)",
        )
    }

    kotlinGradle {
        target("**/*.gradle.kts")
        targetExclude("**/build/**", "**/.gradle/**")
        licenseHeaderFile(
            rootProject.file("license-header.txt"),
            "(//|@file|import|plugins|buildscript|pluginManagement|dependencyResolutionManagement|rootProject)",
        )
    }
}

// Emit test events for IDE integration across all modules.
subprojects {
    tasks.withType<Test> {
        testLogging {
            events("passed", "skipped", "failed")
            showStandardStreams = false
            showExceptions = true
            showCauses = true
            showStackTraces = true
        }
    }
}
