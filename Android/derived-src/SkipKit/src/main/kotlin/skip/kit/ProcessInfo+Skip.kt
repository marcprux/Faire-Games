package skip.kit

import skip.lib.*

// Copyright 2025–2026 Skip
// SPDX-License-Identifier: MPL-2.0
import skip.foundation.*

/// Returns the version of the app.
///
/// On iOS, uses the `CFBundleShortVersionString` of the main `Bundle's Info.plist`
///
/// On Android, uses the `versionName` property of the `android.content.pm.PackageManager`
val ProcessInfo.appVersionString: String?
    get() = _appVersionString

/// Returns the version of the app.
///
/// On iOS, uses the `CFBundleVersion` of the main `Bundle's Info.plist`
///
/// On Android, uses the `versionCode` property of the `android.content.pm.PackageManager`
val ProcessInfo.appVersionNumber: Int?
    get() = _appVersionNumber

/// Returns the identifier of the app.
///
/// On iOS, uses the `CFBundleIdentifier` of the main `Bundle's Info.plist`
///
/// On Android, uses the `packageName` property of the `android.content.Context`
val ProcessInfo.appIdentifier: String?
    get() = _appIdentifier

private val packageInfo: android.content.pm.PackageInfo = linvoke l@{ ->
    val context = ProcessInfo.processInfo.androidContext.sref()
    val packageManager = context.getPackageManager()
    return@l packageManager.getPackageInfo(context.getPackageName(), android.content.pm.PackageManager.GET_META_DATA)
}

private val _appVersionString: String? = { -> packageInfo.versionName }()

private val _appVersionNumber: Int? = { -> packageInfo.versionCode }()

private val _appIdentifier: String? = { -> ProcessInfo.processInfo.androidContext.getPackageName() }()


