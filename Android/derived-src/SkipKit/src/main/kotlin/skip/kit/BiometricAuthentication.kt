package skip.kit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import skip.lib.*

// Copyright 2025-2026 Skip
// SPDX-License-Identifier: MPL-2.0
import skip.foundation.*
import skip.ui.*

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import skip.model.*

@androidx.annotation.Keep
enum class BiometricAuthenticationType(override val rawValue: String, @Suppress("UNUSED_PARAMETER") unusedp: Nothing? = null): RawRepresentable<String>, skip.lib.SwiftProjecting {
    none("none"),
    fingerprint("fingerprint"),
    facialRecognition("facialRecognition"),
    unspecified("unspecified");

    override fun Swift_projection(options: Int): () -> Any = Swift_projectionImpl(options)
    private external fun Swift_projectionImpl(options: Int): () -> Any

    @androidx.annotation.Keep
    companion object {
        fun init(rawValue: String): BiometricAuthenticationType? {
            return when (rawValue) {
                "none" -> BiometricAuthenticationType.none
                "fingerprint" -> BiometricAuthenticationType.fingerprint
                "facialRecognition" -> BiometricAuthenticationType.facialRecognition
                "unspecified" -> BiometricAuthenticationType.unspecified
                else -> null
            }
        }
    }
}

fun BiometricAuthenticationType(rawValue: String): BiometricAuthenticationType? = BiometricAuthenticationType.init(rawValue = rawValue)

@androidx.annotation.Keep
enum class BiometricAuthenticationResult: skip.lib.SwiftProjecting {
    success,
    cancelled,
    failed,
    unavailable;

    override fun Swift_projection(options: Int): () -> Any = Swift_projectionImpl(options)
    private external fun Swift_projectionImpl(options: Int): () -> Any

    @androidx.annotation.Keep
    companion object {
    }
}

enum class BiometricAuthentication {
    ;

    @androidx.annotation.Keep
    companion object {

        // MARK: - Properties
        val authenticationType: BiometricAuthenticationType
            get() {
                val activity_0 = UIApplication.shared.androidActivity.sref()
                if (activity_0 == null) {
                    return BiometricAuthenticationType.none
                }

                val manager = BiometricManager.from(activity_0)
                val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG.sref()

                if (manager.canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS) {
                    return BiometricAuthenticationType.unspecified
                } else {
                    return BiometricAuthenticationType.none
                }
            }

        val canAuthenticate: Boolean
            get() = this.authenticationType != BiometricAuthenticationType.none

        // MARK: - Functions

        /// Authenticates the user with the device's biometric authentication method.
        ///
        /// - Parameters:
        ///   - localizedReason: The authentication reason.
        ///   - allowsDeviceCredentialFallback: A Boolean value indicating whether the system device credential can be used as a fallback.
        ///   - completion: The authentication completion handler.
        fun authenticate(localizedReason: String, allowsDeviceCredentialFallback: Boolean = false, completion: (BiometricAuthenticationResult) -> Unit) {
            val activity_1 = (UIApplication.shared.androidActivity as? FragmentActivity).sref()
            if (activity_1 == null) {
                completion(BiometricAuthenticationResult.unavailable)
                return
            }

            val manager = BiometricManager.from(activity_1)
            val authenticators = (if (allowsDeviceCredentialFallback) BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL else BiometricManager.Authenticators.BIOMETRIC_STRONG).sref()
            if (manager.canAuthenticate(authenticators) != BiometricManager.BIOMETRIC_SUCCESS) {
                completion(BiometricAuthenticationResult.unavailable)
                return
            }

            val executor = ContextCompat.getMainExecutor(activity_1)
            val callback = AndroidBiometricAuthenticationCallback(completion = completion)
            val prompt = BiometricPrompt(activity_1, executor, callback)
            val builder = BiometricPrompt.PromptInfo.Builder()
                .setTitle(localizedReason)
                .setAllowedAuthenticators(authenticators)

            if (!allowsDeviceCredentialFallback) {
                builder.setNegativeButtonText(activity_1.getString(android.R.string.cancel))
            }

            prompt.authenticate(builder.build())
        }
    }
}

private class AndroidBiometricAuthenticationCallback: BiometricPrompt.AuthenticationCallback {

    // MARK: - Properties
    internal val completion: (BiometricAuthenticationResult) -> Unit

    // MARK: - Initialization

    /// Initializes a new instance of the `AndroidBiometricAuthenticationCallback` class.
    ///
    /// - Parameter completion: The authentication completion handler.
    internal constructor(completion: (BiometricAuthenticationResult) -> Unit) {
        this.completion = completion
    }

    // MARK: - Functions

    /// Handles successful biometric authentication.
    ///
    /// - Parameter result: The biometric prompt authentication result.
    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult): Unit = this.completion(BiometricAuthenticationResult.success)

    /// Handles biometric authentication errors.
    ///
    /// - Parameters:
    ///   - errorCode: The Android biometric prompt error code.
    ///   - errString: The Android biometric prompt error message.
    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
        when (errorCode) {
            BiometricPrompt.ERROR_NEGATIVE_BUTTON, BiometricPrompt.ERROR_USER_CANCELED, BiometricPrompt.ERROR_CANCELED -> this.completion(BiometricAuthenticationResult.cancelled)
            else -> this.completion(BiometricAuthenticationResult.failed)
        }
    }

    /// Handles a failed biometric match while the prompt remains active.
    override fun onAuthenticationFailed() {
        // Nothing to do here. Android keeps the biometric prompt active.
    }
}

