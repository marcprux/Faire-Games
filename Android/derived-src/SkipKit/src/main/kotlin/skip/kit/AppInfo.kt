package skip.kit

import skip.lib.*
import skip.lib.Array

// Copyright 2025–2026 Skip
// SPDX-License-Identifier: MPL-2.0
import skip.foundation.*

import android.content.pm.PackageManager
import android.content.pm.ApplicationInfo
import android.os.Build

/// Provides information about the currently running application.
///
/// Access via the `AppInfo.current` singleton. All properties are computed lazily
/// and cached for the lifetime of the process.
///
/// On iOS, this reads from `Bundle.main.infoDictionary` and system APIs.
/// On Android, this reads from `PackageManager`, `ApplicationInfo`, and `Build`.
@androidx.annotation.Keep
class AppInfo: skip.lib.SwiftProjecting {

    private constructor() {
    }

    // MARK: - Identity

    /// The bundle identifier (iOS) or package name (Android).
    ///
    /// Example: `"com.example.myapp"`
    val appIdentifier: String?
        get() = _appIdentifier

    /// The user-visible display name of the app.
    ///
    /// On iOS: `CFBundleDisplayName` or `CFBundleName`.
    /// On Android: The application label from `PackageManager`.
    val displayName: String?
        get() = _displayName

    // MARK: - Version

    /// The user-facing version string (e.g. `"1.2.3"`).
    ///
    /// On iOS: `CFBundleShortVersionString`.
    /// On Android: `versionName` from `PackageInfo`.
    val version: String?
        get() = _version

    /// The internal build number as a string (e.g. `"42"` or `"2024.03.15"`).
    ///
    /// On iOS: `CFBundleVersion`.
    /// On Android: `versionCode` from `PackageInfo` (as a string).
    val buildNumber: String?
        get() = _buildNumber

    /// The build number as an integer, if parseable.
    ///
    /// On iOS: `CFBundleVersion` parsed as Int.
    /// On Android: `versionCode`.
    val buildNumberInt: Int?
        get() = _buildNumberInt

    /// A combined "version (build)" string, e.g. `"1.2.3 (42)"`.
    val versionWithBuild: String
        get() {
            val v = version ?: "0.0.0"
            buildNumber?.let { b ->
                return "${v} (${b})"
            }
            return v
        }

    // MARK: - Build Configuration

    /// Whether the app is running in a debug build.
    ///
    /// On iOS: Checks for the `DEBUG` preprocessor flag.
    /// On Android: Reads `ApplicationInfo.FLAG_DEBUGGABLE`.
    val isDebug: Boolean
        get() = _isDebug

    /// Whether the app is running in a release build (the inverse of `isDebug`).
    val isRelease: Boolean
        get() = !isDebug

    /// Whether the app was installed from TestFlight (iOS only).
    /// Returns `false` on Android.
    val isTestFlight: Boolean
        get() = _isTestFlight

    // MARK: - Platform Info

    /// The operating system name (e.g. `"iOS"`, `"Android"`).
    val osName: String
        get() = "Android"

    /// The operating system version string.
    ///
    /// On iOS/macOS: e.g. `"17.4.1"`.
    /// On Android: The SDK version string, e.g. `"14"` (API level 34).
    val osVersion: String
        get() = _osVersion

    /// The device model identifier.
    ///
    /// On iOS: e.g. `"iPhone15,2"`.
    /// On Android: e.g. `"Pixel 7"`.
    val deviceModel: String
        get() = _deviceModel

    // MARK: - App Bundle Info (iOS-specific, safe no-ops on Android)

    /// The minimum OS version required by the app.
    ///
    /// On iOS: `MinimumOSVersion` from Info.plist.
    /// On Android: `minSdkVersion` from `ApplicationInfo`.
    val minimumOSVersion: String?
        get() = _minimumOSVersion

    /// The app's URL scheme types (iOS only). Returns an empty array on Android.
    val urlSchemes: Array<String>
        get() = _urlSchemes

    /// All keys available in the iOS Info.plist. Returns an empty array on Android.
    val infoDictionaryKeys: Array<String>
        get() = arrayOf()

    /// Access a raw Info.plist value by key (iOS) or returns `nil` on Android.
    fun infoDictionaryValue(forKey: String): Any? {
        val key = forKey
        return null
    }

    override fun Swift_projection(options: Int): () -> Any = Swift_projectionImpl(options)
    private external fun Swift_projectionImpl(options: Int): () -> Any

    @androidx.annotation.Keep
    companion object {
        /// The shared instance for the currently running app.
        val current = AppInfo()
    }
}

// MARK: - Private Cached Values

private val _pkgInfo: android.content.pm.PackageInfo = linvoke l@{ ->
    val context = ProcessInfo.processInfo.androidContext.sref()
    return@l context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA)
}

private val _appInfo: ApplicationInfo = linvoke l@{ ->
    val context = ProcessInfo.processInfo.androidContext.sref()
    return@l context.getApplicationInfo()
}

private val _appIdentifier: String? = { -> ProcessInfo.processInfo.androidContext.getPackageName() }()

private val _displayName: String? = linvoke l@{ ->
    val context = ProcessInfo.processInfo.androidContext.sref()
    val pm = context.getPackageManager()
    val label = _appInfo.loadLabel(pm)
    return@l "${label}"
}

private val _version: String? = { -> _pkgInfo.versionName }()

private val _buildNumber: String? = { -> "${_pkgInfo.versionCode}" }()

private val _buildNumberInt: Int? = { -> Int(_pkgInfo.versionCode) }()

private val _isDebug: Boolean = linvoke l@{ -> return@l (_appInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0 }

private val _isTestFlight: Boolean = linvoke l@{ -> return@l false }

private val _osVersion: String = linvoke l@{ -> return@l "${Build.VERSION.SDK_INT}" }

private val _deviceModel: String = linvoke l@{ -> return@l Build.MODEL }

private val _minimumOSVersion: String? = linvoke l@{ -> return@l "${_appInfo.minSdkVersion}" }

private val _urlSchemes: Array<String> = linvoke l@{ -> return@l arrayOf() }

