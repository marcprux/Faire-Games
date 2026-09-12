package skip.ui

import skip.lib.*

// Copyright 2025–2026 Skip
// SPDX-License-Identifier: MPL-2.0
import skip.foundation.*
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Activity
import android.app.Application
import android.app.UiModeManager
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

private val disableWindowBlursSetting = "disable_window_blurs"
private val highTextContrastEnabledSetting = "high_text_contrast_enabled"

/// Observes Android accessibility settings and exposes per-value Compose state.
internal class AccessibilityEnvironment: ContentObserver, Application.ActivityLifecycleCallbacks {

    private val enabled: MutableState<Boolean> = mutableStateOf(false)
    private val invertColors: MutableState<Boolean> = mutableStateOf(false)
    private val reduceMotion: MutableState<Boolean> = mutableStateOf(false)
    private val reduceTransparency: MutableState<Boolean> = mutableStateOf(false)
    private val switchControlEnabled: MutableState<Boolean> = mutableStateOf(false)
    private val voiceOverEnabled: MutableState<Boolean> = mutableStateOf(false)
    private val colorSchemeContrast: MutableState<ColorSchemeContrast> = mutableStateOf(ColorSchemeContrast.standard)

    private var observersInstalled = false

    private val appContext: android.content.Context
    private val accessibilityManager: AccessibilityManager
    private val uiModeManager: UiModeManager
    private val contentResolver: android.content.ContentResolver

    private constructor(): super(Handler(Looper.getMainLooper())) {
        val context = ProcessInfo.processInfo.androidContext.applicationContext.sref()
        appContext = context.sref()
        accessibilityManager = (context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager).sref()
        uiModeManager = (context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager).sref()
        contentResolver = context.contentResolver.sref()
    }

    @Composable
    internal fun accessibilityEnabled(): Boolean = enabled.value
    @Composable
    internal fun accessibilityInvertColors(): Boolean = invertColors.value
    @Composable
    internal fun accessibilityReduceMotion(): Boolean = reduceMotion.value
    @Composable
    internal fun accessibilityReduceTransparency(): Boolean = reduceTransparency.value
    @Composable
    internal fun accessibilitySwitchControlEnabled(): Boolean = switchControlEnabled.value
    @Composable
    internal fun accessibilityVoiceOverEnabled(): Boolean = voiceOverEnabled.value
    @Composable
    internal fun colorSchemeContrast(): ColorSchemeContrast = colorSchemeContrast.value

    private fun refreshEnabled() {
        enabled.value = accessibilityManager.isEnabled
        refreshVoiceOverEnabled()
        refreshSwitchControlEnabled()
    }

    private fun refreshInvertColors() {
        invertColors.value = Settings.Secure.getInt(contentResolver, Settings.Secure.ACCESSIBILITY_DISPLAY_INVERSION_ENABLED, 0) == 1
    }

    private fun refreshReduceMotion() {
        reduceMotion.value = Settings.Global.getFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1.0f) == 0.0f
    }

    private fun refreshReduceTransparency() {
        reduceTransparency.value = Settings.Global.getInt(contentResolver, disableWindowBlursSetting, 0) == 1
    }

    private fun refreshSwitchControlEnabled() {
        val am = accessibilityManager.sref()
        var switchEnabled = false
        if (am.isEnabled) {
            for (service in am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)) {
                service.settingsActivityName.sref()?.let { name ->
                    if (name.lowercased().contains("switchaccess")) {
                        switchEnabled = true
                        break
                    }
                }
            }
        }
        switchControlEnabled.value = switchEnabled
    }

    private fun refreshVoiceOverEnabled() {
        val am = accessibilityManager.sref()
        voiceOverEnabled.value = am.isEnabled && am.isTouchExplorationEnabled
    }

    private fun refreshColorSchemeContrast() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val contrast = uiModeManager.getContrast()
            colorSchemeContrast.value = if (contrast > 0.0f) ColorSchemeContrast.increased else ColorSchemeContrast.standard
        } else {
            val highContrastTextEnabled = Settings.Secure.getInt(contentResolver, highTextContrastEnabledSetting, 0) == 1
            colorSchemeContrast.value = if (highContrastTextEnabled) ColorSchemeContrast.increased else ColorSchemeContrast.standard
        }
    }

    private fun refreshAll() {
        refreshEnabled()
        refreshInvertColors()
        refreshReduceMotion()
        refreshReduceTransparency()
        refreshColorSchemeContrast()
    }

    private fun installObserversIfNeeded() {
        if (observersInstalled) {
            return
        }
        observersInstalled = true
        refreshAll()

        val resolver = contentResolver.sref()
        val invertUri = Settings.Secure.getUriFor(Settings.Secure.ACCESSIBILITY_DISPLAY_INVERSION_ENABLED)
        val motionUri = Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE)
        val blurUri = Settings.Global.getUriFor(disableWindowBlursSetting)
        val secureUri = Settings.Secure.getUriFor("secure")

        resolver.registerContentObserver(motionUri, false, this)
        resolver.registerContentObserver(invertUri, true, this)
        resolver.registerContentObserver(blurUri, false, this)
        resolver.registerContentObserver(secureUri, true, this)

        val am = accessibilityManager.sref()
        am.addAccessibilityStateChangeListener(AccessibilityManager.AccessibilityStateChangeListener { _ -> this.refreshEnabled() })
        am.addTouchExplorationStateChangeListener(AccessibilityManager.TouchExplorationStateChangeListener { _ -> this.refreshVoiceOverEnabled() })
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            am.addAccessibilityServicesStateChangeListener(AccessibilityManager.AccessibilityServicesStateChangeListener { _ -> this.refreshSwitchControlEnabled() })
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            uiModeManager.addContrastChangeListener(appContext.mainExecutor, UiModeManager.ContrastChangeListener { _ -> this.refreshColorSchemeContrast() })
        }

        (appContext as? Application).sref()?.let { application ->
            application.registerActivityLifecycleCallbacks(this)
        }
    }

    override fun onChange(selfChange: Boolean): Unit = refreshAll()

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        if (uri == null) {
            refreshAll()
            return
        }
        val motionUri = Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE)
        val invertUri = Settings.Secure.getUriFor(Settings.Secure.ACCESSIBILITY_DISPLAY_INVERSION_ENABLED)
        val blurUri = Settings.Global.getUriFor(disableWindowBlursSetting)
        val contrastUri = Settings.Secure.getUriFor(highTextContrastEnabledSetting)
        if (uri == motionUri) {
            refreshReduceMotion()
        } else if (uri == invertUri) {
            refreshInvertColors()
        } else if (uri == blurUri) {
            refreshReduceTransparency()
        } else if (uri == contrastUri) {
            refreshColorSchemeContrast()
        } else {
            refreshAll()
        }
    }

    override fun onActivityResumed(activity: Activity): Unit = refreshAll()

    override fun onActivityCreated(activity: Activity, savedInstanceState: android.os.Bundle?) = Unit
    override fun onActivityStarted(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: android.os.Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit

    @androidx.annotation.Keep
    companion object {
        private val _shared = AccessibilityEnvironment()

        internal val shared: AccessibilityEnvironment
            get() {
                _shared.installObserversIfNeeded()
                return _shared
            }
    }
}

