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
import android.Manifest
import android.os.Build
import android.content.Context
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.registerForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import skip.model.*

/// Provides an interface for requesting permissions
@androidx.annotation.Keep
class PermissionManager: skip.lib.SwiftProjecting {
    private constructor() {
    }



    override fun Swift_projection(options: Int): () -> Any = Swift_projectionImpl(options)
    private external fun Swift_projectionImpl(options: Int): () -> Any

    @androidx.annotation.Keep
    companion object {

        fun queryPermission(permission: PermissionType): PermissionAuthorization {
            val activity_0 = UIApplication.shared.androidActivity.sref()
            if (activity_0 == null) {
                return PermissionAuthorization.unknown
            }
            val granted = ContextCompat.checkSelfPermission(activity_0, permission.androidPermissionName)
            when (granted) {
                PackageManager.PERMISSION_GRANTED -> return PermissionAuthorization.authorized
                PackageManager.PERMISSION_DENIED -> return PermissionAuthorization.unknown // "DENIED" is a misnomer: if may also mean that permission has not yet been requested
                else -> return PermissionAuthorization.unknown
            }
        }

        /// Requests the given permission.
        /// - Parameters:
        ///   - permission: the permission, such as `PermissionType.CAMERA`
        ///   - showRationale: an optional async callback to invoke when the system determies that a rationale should be displayed for the permission check
        /// - Returns: true if the permission was granted, false if denied or there was an error making the request
        suspend fun requestPermission(permission: PermissionType, showRationale: (suspend () -> Boolean)?): PermissionAuthorization = Async.run l@{
            // e.g.: android.permission.ACCESS_FINE_LOCATION
            // Android does not have limited options, so we always return `authorized` or `denied`
            if (UIApplication.shared.requestPermission(permission.androidPermissionName, showRationale = showRationale) == true) {
                return@l PermissionAuthorization.authorized
            } else {
                return@l PermissionAuthorization.denied
            }
        }
        fun callback_requestPermission(permission: PermissionType, showRationale: (suspend () -> Boolean)?, f_return_callback: (skip.kit.PermissionAuthorization) -> Unit) {
            Task {
                f_return_callback(requestPermission(permission = permission, showRationale = showRationale))
            }
        }

        /// Requests the given permission.
        /// - Parameters:
        ///   - permission: the permission, such as `PermissionType.CAMERA`
        /// - Returns: true if the permission was granted, false if denied or there was an error making the request
        suspend fun requestPermission(permission: PermissionType): PermissionAuthorization {
            return requestPermission(permission, showRationale = null)
        }
        fun callback_requestPermission(permission: PermissionType, f_return_callback: (skip.kit.PermissionAuthorization) -> Unit) {
            Task {
                f_return_callback(requestPermission(permission = permission))
            }
        }

        /// Queries whether push notifications have been permitted
        suspend fun queryPostNotificationPermission(): PermissionAuthorization = Async.run l@{
            return@l queryPermission(PermissionType.POST_NOTIFICATIONS)
        }
        fun callback_queryPostNotificationPermission(f_return_callback: (skip.kit.PermissionAuthorization) -> Unit) {
            Task {
                f_return_callback(queryPostNotificationPermission())
            }
        }

        /// Requests permission to send push notifications
        ///
        /// - seeAlso: https://developer.apple.com/documentation/usernotifications/asking-permission-to-use-notifications
        suspend fun requestPostNotificationPermission(alert: Boolean = true, sound: Boolean = true, badge: Boolean = true): PermissionAuthorization = Async.run l@{
            return@l requestPermission(PermissionType.POST_NOTIFICATIONS)
        }
        fun callback_requestPostNotificationPermission(alert: Boolean, sound: Boolean, badge: Boolean, f_return_callback: (skip.kit.PermissionAuthorization?, Throwable?) -> Unit) {
            Task {
                try {
                    f_return_callback(requestPostNotificationPermission(alert = alert, sound = sound, badge = badge), null)
                } catch(t: Throwable) {
                    f_return_callback(null, t)
                }
            }
        }

        /// Queries camera access
        fun queryCameraPermission(): PermissionAuthorization = queryPermission(PermissionType.CAMERA)

        /// Request permission to use the device camera
        suspend fun requestCameraPermission(): PermissionAuthorization = Async.run l@{
            return@l requestPermission(PermissionType.CAMERA)
        }
        fun callback_requestCameraPermission(f_return_callback: (skip.kit.PermissionAuthorization) -> Unit) {
            Task {
                f_return_callback(requestCameraPermission())
            }
        }

        /// Queries microphone access
        fun queryRecordAudioPermission(): PermissionAuthorization = queryPermission(PermissionType.RECORD_AUDIO)

        /// Requests microphone access
        suspend fun requestRecordAudioPermission(): PermissionAuthorization = Async.run l@{
            return@l requestPermission(PermissionType.RECORD_AUDIO)
        }
        fun callback_requestRecordAudioPermission(f_return_callback: (skip.kit.PermissionAuthorization) -> Unit) {
            Task {
                f_return_callback(requestRecordAudioPermission())
            }
        }


        fun queryPhotoLibraryPermission(readWrite: Boolean = true): PermissionAuthorization = queryPermission(if (readWrite) PermissionType.WRITE_EXTERNAL_STORAGE else PermissionType.READ_EXTERNAL_STORAGE)

        /// Requests the media library permission
        suspend fun requestPhotoLibraryPermission(readWrite: Boolean = true): PermissionAuthorization = Async.run l@{
            return@l requestPermission(if (readWrite) PermissionType.WRITE_EXTERNAL_STORAGE else PermissionType.READ_EXTERNAL_STORAGE)
        }
        fun callback_requestPhotoLibraryPermission(readWrite: Boolean, f_return_callback: (skip.kit.PermissionAuthorization) -> Unit) {
            Task {
                f_return_callback(requestPhotoLibraryPermission(readWrite = readWrite))
            }
        }

        fun queryLocationPermission(precise: Boolean, always: Boolean): PermissionAuthorization {
            val coarseLocationPermission = queryPermission(PermissionType.ACCESS_COARSE_LOCATION)
            val fineLocationPermission = queryPermission(PermissionType.ACCESS_FINE_LOCATION)
            val backgroundLocationPermission = queryPermission(PermissionType.ACCESS_BACKGROUND_LOCATION)

            if (always == true) {
                if (backgroundLocationPermission == PermissionAuthorization.authorized) {
                    if (precise == true) {
                        if (fineLocationPermission == PermissionAuthorization.authorized) {
                            return PermissionAuthorization.authorized
                        } else {
                            return PermissionAuthorization.limited
                        }
                    } else if (coarseLocationPermission == PermissionAuthorization.authorized) {
                        return PermissionAuthorization.authorized
                    }
                }
            } else if (precise == true) {
                if (fineLocationPermission == PermissionAuthorization.authorized) {
                    return PermissionAuthorization.authorized
                }
            } else if (coarseLocationPermission == PermissionAuthorization.authorized) {
                return PermissionAuthorization.authorized
            }
            return PermissionAuthorization.unknown

        }

        /// Requests location permission
        suspend fun requestLocationPermission(precise: Boolean, always: Boolean): PermissionAuthorization = Async.run l@{
            val status = queryLocationPermission(precise = precise, always = always)
            if (status == PermissionAuthorization.unknown) {
                return@l requestPermission(if (precise) PermissionType.ACCESS_FINE_LOCATION else PermissionType.ACCESS_COARSE_LOCATION)
            } else if (status == PermissionAuthorization.limited && always == true) {
                // NOTE: for API 30+, this redirects directly to the app's settings Location permission. There is no UI popup in Android for this
                return@l PermissionManager.requestPermission(PermissionType.ACCESS_BACKGROUND_LOCATION)
            } else {
                return@l status
            }
        }
        fun callback_requestLocationPermission(precise: Boolean, always: Boolean, f_return_callback: (skip.kit.PermissionAuthorization) -> Unit) {
            Task {
                f_return_callback(requestLocationPermission(precise = precise, always = always))
            }
        }
    }
}


/// The status of a permission authorization
@androidx.annotation.Keep
enum class PermissionAuthorization(override val rawValue: String, @Suppress("UNUSED_PARAMETER") unusedp: Nothing? = null): RawRepresentable<String>, skip.lib.SwiftProjecting {
    /// Authorization status is unknown
    unknown("unknown"),
    /// The app isn’t authorized to access the permission, and the user can’t grant such permission.
    restricted("restricted"),
    /// The user explicitly denied this app the permission.
    denied("denied"),
    /// The user explicitly granted this app the permission.
    authorized("authorized"),
    /// The user authorized this app for limited access to the permission.
    limited("limited");

    /// Returns true if the permission definitely has some authorization, false if is definitely does not, or nil if it is unknown
    val isAuthorized: Boolean?
        get() {
            when (this) {
                PermissionAuthorization.unknown -> return null
                PermissionAuthorization.restricted -> return false
                PermissionAuthorization.denied -> return false
                PermissionAuthorization.authorized -> return true
                PermissionAuthorization.limited -> return true
            }
        }

    override fun Swift_projection(options: Int): () -> Any = Swift_projectionImpl(options)
    private external fun Swift_projectionImpl(options: Int): () -> Any

    @androidx.annotation.Keep
    companion object {
        fun init(rawValue: String): PermissionAuthorization? {
            return when (rawValue) {
                "unknown" -> PermissionAuthorization.unknown
                "restricted" -> PermissionAuthorization.restricted
                "denied" -> PermissionAuthorization.denied
                "authorized" -> PermissionAuthorization.authorized
                "limited" -> PermissionAuthorization.limited
                else -> null
            }
        }
    }
}

fun PermissionAuthorization(rawValue: String): PermissionAuthorization? = PermissionAuthorization.init(rawValue = rawValue)

/// The encapsulation of a permission name
@androidx.annotation.Keep
class PermissionType: skip.lib.SwiftProjecting {
    val androidPermissionName: String

    constructor(androidPermissionName: String) {
        this.androidPermissionName = androidPermissionName
    }

    override fun equals(other: Any?): Boolean {
        if (other !is PermissionType) {
            return false
        }
        val lhs = this
        val rhs = other
        return lhs.androidPermissionName == rhs.androidPermissionName
    }

    override fun Swift_projection(options: Int): () -> Any = Swift_projectionImpl(options)
    private external fun Swift_projectionImpl(options: Int): () -> Any

    @androidx.annotation.Keep
    companion object {

        val CAMERA = PermissionType(androidPermissionName = "android.permission.CAMERA")
        val RECORD_AUDIO = PermissionType(androidPermissionName = "android.permission.RECORD_AUDIO")

        val READ_CONTACTS = PermissionType(androidPermissionName = "android.permission.READ_CONTACTS")
        val WRITE_CONTACTS = PermissionType(androidPermissionName = "android.permission.WRITE_CONTACTS")

        val READ_CALENDAR = PermissionType(androidPermissionName = "android.permission.READ_CALENDAR")
        val WRITE_CALENDAR = PermissionType(androidPermissionName = "android.permission.WRITE_CALENDAR")

        // API 11+ breaks this up into: READ_MEDIA_IMAGES, READ_MEDIA_VIDEO
        val READ_EXTERNAL_STORAGE = PermissionType(androidPermissionName = "android.permission.READ_EXTERNAL_STORAGE")
        val WRITE_EXTERNAL_STORAGE = PermissionType(androidPermissionName = "android.permission.WRITE_EXTERNAL_STORAGE")

        val POST_NOTIFICATIONS = PermissionType(androidPermissionName = "android.permission.POST_NOTIFICATIONS")
        val ACCESS_FINE_LOCATION = PermissionType(androidPermissionName = "android.permission.ACCESS_FINE_LOCATION")
        val ACCESS_COARSE_LOCATION = PermissionType(androidPermissionName = "android.permission.ACCESS_COARSE_LOCATION")
        val ACCESS_BACKGROUND_LOCATION = PermissionType(androidPermissionName = "android.permission.ACCESS_BACKGROUND_LOCATION")
    }
}

