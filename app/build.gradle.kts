/*
 * SPDX-FileCopyrightText: 2026 shining-cat
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
import java.io.File

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kover)
}

android {
    namespace = "fr.shiningcat.binclockwidget"
    compileSdk = 37

    defaultConfig {
        applicationId = "fr.shiningcat.binclockwidget"
        minSdk = 26
        targetSdk = 37
        versionCode = 2
        versionName = "0.1.1"
    }

    // Strip AGP's "Dependency metadata" blob from the APK signing block. F-Droid's scanner rejects
    // it as an opaque proprietary block; it also isn't reproducible. We don't need it.
    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    // Release signing is driven entirely by environment variables so the keystore never lives in
    // the repo. When they are absent (local builds, and F-Droid — which signs with its own key) the
    // release build stays unsigned; CI injects them from repository secrets for GitHub releases.
    val signingKeystorePath = System.getenv("SIGNING_KEYSTORE_PATH")
    val signingKeystorePassword = System.getenv("SIGNING_KEYSTORE_PASSWORD")
    val signingKeyAlias = System.getenv("SIGNING_KEY_ALIAS")
    val signingKeyPassword = System.getenv("SIGNING_KEY_PASSWORD")

    if (signingKeystorePath != null && signingKeystorePassword != null) {
        signingConfigs {
            create("release") {
                storeFile = File(signingKeystorePath)
                storePassword = signingKeystorePassword
                keyAlias = signingKeyAlias
                keyPassword = signingKeyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            if (signingKeystorePath != null && signingKeystorePassword != null) {
                signingConfig = signingConfigs.getByName("release")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        // We ship no native code of our own; the only .so is the prebuilt one bundled with
        // DataStore. Keep it unstripped so the release APK is byte-for-byte reproducible — the
        // NDK strip step is non-deterministic across build environments (needed for F-Droid).
        jniLibs {
            keepDebugSymbols += "**/*.so"
        }
    }
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.activity.compose)
    implementation(libs.google.material)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Glance (home-screen widget)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)

    // DI
    implementation(libs.koin.android)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Networking / serialization
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.kotlinx.serialization.json)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Testing
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
