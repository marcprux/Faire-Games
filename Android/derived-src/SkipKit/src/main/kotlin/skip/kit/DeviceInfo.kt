package skip.kit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import skip.lib.*

// Copyright 2025–2026 Skip
// SPDX-License-Identifier: MPL-2.0
import skip.foundation.*
import skip.ui.*

import android.content.Context
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import skip.model.*

// MARK: - DeviceType

/// The general category of the device.
@androidx.annotation.Keep
enum class DeviceType(override val rawValue: String, @Suppress("UNUSED_PARAMETER") unusedp: Nothing? = null): RawRepresentable<String>, skip.lib.SwiftProjecting {
    phone("phone"),
    tablet("tablet"),
    desktop("desktop"),
    tv("tv"),
    watch("watch"),
    unknown("unknown");

    override fun Swift_projection(options: Int): () -> Any = Swift_projectionImpl(options)
    private external fun Swift_projectionImpl(options: Int): () -> Any

    @androidx.annotation.Keep
    companion object {
        fun init(rawValue: String): DeviceType? {
            return when (rawValue) {
                "phone" -> DeviceType.phone
                "tablet" -> DeviceType.tablet
                "desktop" -> DeviceType.desktop
                "tv" -> DeviceType.tv
                "watch" -> DeviceType.watch
                "unknown" -> DeviceType.unknown
                else -> null
            }
        }
    }
}

fun DeviceType(rawValue: String): DeviceType? = DeviceType.init(rawValue = rawValue)

// MARK: - BatteryState

/// The current charging state of the device battery.
@androidx.annotation.Keep
enum class BatteryState(override val rawValue: String, @Suppress("UNUSED_PARAMETER") unusedp: Nothing? = null): RawRepresentable<String>, skip.lib.SwiftProjecting {
    /// The device is not plugged in and running on battery.
    unplugged("unplugged"),
    /// The device is plugged in and charging.
    charging("charging"),
    /// The device is plugged in and the battery is full.
    full("full"),
    /// The battery state is unknown.
    unknown("unknown");

    override fun Swift_projection(options: Int): () -> Any = Swift_projectionImpl(options)
    private external fun Swift_projectionImpl(options: Int): () -> Any

    @androidx.annotation.Keep
    companion object {
        fun init(rawValue: String): BatteryState? {
            return when (rawValue) {
                "unplugged" -> BatteryState.unplugged
                "charging" -> BatteryState.charging
                "full" -> BatteryState.full
                "unknown" -> BatteryState.unknown
                else -> null
            }
        }
    }
}

fun BatteryState(rawValue: String): BatteryState? = BatteryState.init(rawValue = rawValue)

// MARK: - NetworkStatus

/// The current network connectivity status.
@androidx.annotation.Keep
enum class NetworkStatus(override val rawValue: String, @Suppress("UNUSED_PARAMETER") unusedp: Nothing? = null): RawRepresentable<String>, skip.lib.SwiftProjecting {
    /// The device has no network connectivity.
    offline("offline"),
    /// The device is connected via Wi-Fi.
    wifi("wifi"),
    /// The device is connected via cellular data.
    cellular("cellular"),
    /// The device is connected via Ethernet.
    ethernet("ethernet"),
    /// The device is connected via an unknown transport.
    other("other");

    override fun Swift_projection(options: Int): () -> Any = Swift_projectionImpl(options)
    private external fun Swift_projectionImpl(options: Int): () -> Any

    @androidx.annotation.Keep
    companion object {
        fun init(rawValue: String): NetworkStatus? {
            return when (rawValue) {
                "offline" -> NetworkStatus.offline
                "wifi" -> NetworkStatus.wifi
                "cellular" -> NetworkStatus.cellular
                "ethernet" -> NetworkStatus.ethernet
                "other" -> NetworkStatus.other
                else -> null
            }
        }
    }
}

fun NetworkStatus(rawValue: String): NetworkStatus? = NetworkStatus.init(rawValue = rawValue)

// MARK: - DeviceInfo

/// Provides information about the current device, including screen size, device type,
/// battery status, and network connectivity.
///
/// Access via the `DeviceInfo.current` singleton.
///
/// On iOS, this reads from `UIDevice`, `UIScreen`, `NWPathMonitor`, and `ProcessInfo`.
/// On Android, this reads from `DisplayMetrics`, `BatteryManager`, `ConnectivityManager`, and `Build`.
@androidx.annotation.Keep
class DeviceInfo: skip.lib.SwiftProjecting {

    private constructor() {
    }

    // MARK: - Screen

    /// The screen width in points.
    val screenWidth: Double
        get() {
            val context = ProcessInfo.processInfo.androidContext.sref()
            val dm = context.getResources().getDisplayMetrics()
            return Double(dm.widthPixels) / Double(dm.density)
        }

    /// The screen height in points.
    val screenHeight: Double
        get() {
            val context = ProcessInfo.processInfo.androidContext.sref()
            val dm = context.getResources().getDisplayMetrics()
            return Double(dm.heightPixels) / Double(dm.density)
        }

    /// The screen scale factor (pixels per point).
    val screenScale: Double
        get() {
            val context = ProcessInfo.processInfo.androidContext.sref()
            return Double(context.getResources().getDisplayMetrics().density)
        }

    // MARK: - Device Type

    /// The general category of the current device.
    ///
    /// On iOS: uses `UIDevice.current.userInterfaceIdiom`.
    /// On Android: uses screen size configuration (smallest width >= 600dp = tablet).
    val deviceType: DeviceType
        get() {
            val context = ProcessInfo.processInfo.androidContext.sref()
            val config = context.getResources().getConfiguration()
            val screenLayout = (config.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK).sref()
            if (screenLayout >= Configuration.SCREENLAYOUT_SIZE_XLARGE) {
                return DeviceType.tablet
            } else if (screenLayout >= Configuration.SCREENLAYOUT_SIZE_LARGE) {
                return DeviceType.tablet
            } else {
                return DeviceType.phone
            }
        }

    /// Whether the device is likely a tablet (iPad or large-screen Android device).
    val isTablet: Boolean
        get() = deviceType == DeviceType.tablet

    /// Whether the device is likely a phone.
    val isPhone: Boolean
        get() = deviceType == DeviceType.phone

    // MARK: - Device Model

    /// The manufacturer of the device.
    ///
    /// On iOS: always `"Apple"`.
    /// On Android: `Build.MANUFACTURER` (e.g. `"Google"`, `"Samsung"`).
    val manufacturer: String
        get() = Build.MANUFACTURER

    /// The model name of the device.
    ///
    /// On iOS: the machine identifier (e.g. `"iPhone15,2"`).
    /// On Android: `Build.MODEL` (e.g. `"Pixel 7"`).
    val modelName: String
        get() = Build.MODEL

    // MARK: - Battery

    /// The current battery level as a value from 0.0 to 1.0, or `nil` if unavailable.
    ///
    /// On iOS: uses `UIDevice.current.batteryLevel` (must enable monitoring).
    /// On Android: uses `BatteryManager.EXTRA_LEVEL`.
    val batteryLevel: Double?
        get() {
            val context = ProcessInfo.processInfo.androidContext.sref()
            val bm = (context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager).sref()
            if (bm == null) {
                return null
            }
            val level = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            if (level < 0) {
                return null
            }
            return Double(level) / 100.0
        }

    /// The current battery charging state.
    ///
    /// On iOS: uses `UIDevice.current.batteryState`.
    /// On Android: uses `BatteryManager.isCharging()` and battery property.
    val batteryState: BatteryState
        get() {
            val context = ProcessInfo.processInfo.androidContext.sref()
            val bm = (context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager).sref()
            if (bm == null) {
                return BatteryState.unknown
            }
            if (bm.isCharging()) {
                val level = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                return if (level >= 100) BatteryState.full else BatteryState.charging
            }
            return BatteryState.unplugged
        }

    // MARK: - Network

    /// A one-shot check of the current network connectivity status.
    ///
    /// For live updates, use `monitorNetwork()` instead.
    ///
    /// On iOS: uses `NWPathMonitor` for a single snapshot.
    /// On Android: uses `ConnectivityManager` with `NetworkCapabilities`.
    val networkStatus: NetworkStatus
        get() = Companion.queryAndroidNetworkStatus()

    /// Whether the device currently has network connectivity (one-shot check).
    val isOnline: Boolean
        get() = networkStatus != NetworkStatus.offline

    /// Whether the device is connected via Wi-Fi (one-shot check).
    val isOnWifi: Boolean
        get() = networkStatus == NetworkStatus.wifi

    /// Whether the device is connected via cellular data (one-shot check).
    val isOnCellular: Boolean
        get() = networkStatus == NetworkStatus.cellular

    /// Returns an `AsyncStream` that emits `NetworkStatus` values whenever connectivity changes.
    ///
    /// The stream emits an initial value immediately, then a new value each time the
    /// network status changes (e.g. Wi-Fi connected, cellular lost, etc.).
    ///
    /// Cancel the `for await` loop or the enclosing `Task` to stop monitoring.
    ///
    /// On iOS: uses `NWPathMonitor` for live path updates.
    /// On Android: uses `ConnectivityManager.registerDefaultNetworkCallback`.
    ///
    /// ```swift
    /// for await status in DeviceInfo.current.monitorNetwork() {
    ///     print("Network: \(status)")
    /// }
    /// ```
    fun monitorNetwork(): AsyncStream<NetworkStatus> {
        return AsyncStream l@{ continuation ->
            val context = ProcessInfo.processInfo.androidContext.sref()
            val cm = (context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager).sref()

            // Emit initial status
            continuation.yield(Companion.queryAndroidNetworkStatus())
            if (cm == null) {
                continuation.finish()
                return@l
            }

            val callback = object : ConnectivityManager.NetworkCallback() {
    override fun onAvailable(network: android.net.Network) {
        continuation.yield(DeviceInfo.queryAndroidNetworkStatus())
    }
    override fun onLost(network: android.net.Network) {
        continuation.yield(NetworkStatus.offline)
    }
    override fun onCapabilitiesChanged(network: android.net.Network, caps: NetworkCapabilities) {
        continuation.yield(DeviceInfo.queryAndroidNetworkStatus())
    }
}


            cm.registerDefaultNetworkCallback(callback)

            continuation.onTermination = { _ ->
                cm.unregisterNetworkCallback(callback)
            }

        }
    }

    // MARK: - Network Helpers



    // MARK: - Locale

    /// The user's current locale identifier (e.g. `"en_US"`).
    val localeIdentifier: String
        get() = Locale.current.identifier

    /// The user's preferred language code (e.g. `"en"`).
    val languageCode: String?
        get() {
            return Locale.current.language.languageCode?.identifier
        }

    /// The user's current time zone identifier (e.g. `"America/New_York"`).
    val timeZoneIdentifier: String
        get() = TimeZone.current.identifier

    override fun Swift_projection(options: Int): () -> Any = Swift_projectionImpl(options)
    private external fun Swift_projectionImpl(options: Int): () -> Any

    @androidx.annotation.Keep
    companion object {
        val current = DeviceInfo()
        private fun queryAndroidNetworkStatus(): NetworkStatus {
            val context = ProcessInfo.processInfo.androidContext.sref()
            val cm = (context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager).sref()
            if (cm == null) {
                return NetworkStatus.offline
            }
            val network = cm.getActiveNetwork()
            if (network == null) {
                return NetworkStatus.offline
            }
            val caps = cm.getNetworkCapabilities(network)
            if (caps == null) {
                return NetworkStatus.offline
            }
            if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                return NetworkStatus.wifi
            }
            if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                return NetworkStatus.cellular
            }
            if (caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                return NetworkStatus.ethernet
            }
            return NetworkStatus.other
        }
    }
}

