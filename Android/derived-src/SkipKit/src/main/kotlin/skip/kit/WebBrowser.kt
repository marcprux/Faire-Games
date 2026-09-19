package skip.kit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import skip.lib.*
import skip.lib.Array

// Copyright 2025–2026 Skip
// SPDX-License-Identifier: MPL-2.0
import skip.foundation.*
import skip.ui.*


import android.content.Context
import android.content.Intent
import android.net.Uri
import android.app.PendingIntent
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import skip.model.*

// MARK: - Cross-Platform Types

/// A custom action that can be performed on a web page.
/// On iOS, this maps to a `UIActivity` in the Safari share sheet.
/// On Android, this maps to a menu item in Chrome Custom Tabs.
@androidx.annotation.Keep
class WebBrowserAction: skip.lib.SwiftProjecting {
    val label: String
    val handler: (URL) -> Unit

    constructor(label: String, handler: (URL) -> Unit) {
        this.label = label
        this.handler = handler
    }

    override fun Swift_projection(options: Int): () -> Any = Swift_projectionImpl(options)
    private external fun Swift_projectionImpl(options: Int): () -> Any

    @androidx.annotation.Keep
    companion object {
    }
}

/// Controls how the embedded browser is presented.
///
/// - `sheet`: The browser slides up vertically as a bottom sheet.
/// - `navigation`: The browser slides in horizontally like a navigation push.
@androidx.annotation.Keep
enum class WebBrowserPresentationMode: skip.lib.SwiftProjecting {
    /// Present as a vertically-sliding bottom sheet.
    /// On iOS, uses a full-screen cover that slides up.
    /// On Android, uses the Partial Custom Tabs API to display
    /// Chrome Custom Tabs as a resizable bottom sheet.
    /// If the browser does not support partial tabs, it falls back
    /// to a full-screen Custom Tab.
    sheet,
    /// Present as a horizontally-sliding navigation push.
    /// On iOS, the calling view must be inside a `NavigationStack` for this to take effect.
    /// On Android, launches Chrome Custom Tabs full-screen with the default transition.
    navigation;

    override fun Swift_projection(options: Int): () -> Any = Swift_projectionImpl(options)
    private external fun Swift_projectionImpl(options: Int): () -> Any

    @androidx.annotation.Keep
    companion object {
    }
}

/// Configuration parameters for the embedded browser.
@androidx.annotation.Keep
@Suppress("MUST_BE_INITIALIZED")
class EmbeddedParams: MutableStruct, skip.lib.SwiftProjecting {
    /// Custom actions available on the web page.
    /// On iOS, these appear in the Safari activity/share sheet.
    /// On Android, these appear as menu items in Chrome Custom Tabs (max 5).
    var customActions: Array<WebBrowserAction>
        get() = field.sref({ this.customActions = it })
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
            willmutate()
            field = newValue
            didmutate()
        }

    /// Controls how the embedded browser is presented.
    /// Defaults to `.sheet` (vertically-sliding modal).
    var presentationMode: WebBrowserPresentationMode
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }

    constructor(presentationMode: WebBrowserPresentationMode = WebBrowserPresentationMode.sheet, customActions: Array<WebBrowserAction> = arrayOf()) {
        this.customActions = customActions
        this.presentationMode = presentationMode
    }

    private constructor(copy: MutableStruct) {
        @Suppress("NAME_SHADOWING", "UNCHECKED_CAST") val copy = copy as EmbeddedParams
        this.customActions = copy.customActions
        this.presentationMode = copy.presentationMode
    }

    override var supdate: ((Any) -> Unit)? = null
    override var smutatingcount = 0
    override fun scopy(): MutableStruct = EmbeddedParams(this as MutableStruct)

    override fun Swift_projection(options: Int): () -> Any = Swift_projectionImpl(options)
    private external fun Swift_projectionImpl(options: Int): () -> Any

    @androidx.annotation.Keep
    companion object {
    }
}

/// The mode for opening a web page.
@androidx.annotation.Keep
sealed class WebBrowserMode: skip.lib.SwiftProjecting {
    /// Open the URL in the system's default browser application.
    class LaunchBrowserCase: WebBrowserMode() {
    }
    /// Open the URL in an embedded browser within the app.
    /// On iOS, uses `SFSafariViewController`. On Android, uses Chrome Custom Tabs.
    class EmbeddedBrowserCase(val associated0: EmbeddedParams?): WebBrowserMode() {
        val params = associated0
    }

    override fun Swift_projection(options: Int): () -> Any = Swift_projectionImpl(options)
    private external fun Swift_projectionImpl(options: Int): () -> Any

    @androidx.annotation.Keep
    companion object {
        val launchBrowser: WebBrowserMode = LaunchBrowserCase()
        fun embeddedBrowser(params: EmbeddedParams?): WebBrowserMode = EmbeddedBrowserCase(params)
    }
}

/// Opens a web page when `isPresented` becomes `true`.
///
/// - Parameters:
///   - isPresented: Binding that controls when the web page is opened.
///   - url: The URL string of the web page to open.
///   - mode: How to open the web page — in the system browser or an embedded browser.
fun View.openWebBrowser(isPresented: Binding<Boolean>, url: URL, mode: WebBrowserMode): View {
    return ComposeBuilder l@{ composectx: ComposeContext ->
        when (mode) {
            is WebBrowserMode.LaunchBrowserCase -> {
                val context = LocalContext.current.sref()
                return@l onChange(of = isPresented.wrappedValue) { oldPresented, newPresented ->
                    if (newPresented == true) {
                        val uri = url.toAndroidUri()
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        context.startActivity(intent)
                        isPresented.wrappedValue = false
                    }
                }.Compose(composectx)
            }
            is WebBrowserMode.EmbeddedBrowserCase -> {
                val params = mode.associated0
                val context = LocalContext.current.sref()
                val useSheet = params?.presentationMode != WebBrowserPresentationMode.navigation
                return@l onChange(of = isPresented.wrappedValue) { oldPresented, newPresented ->
                    if (newPresented == true) {
                        val builder = CustomTabsIntent.Builder()

                        // In sheet mode, use the Partial Custom Tabs API to present
                        // Chrome Custom Tabs as a bottom sheet that slides up from
                        // the bottom. The initial height is set to 90% of the screen
                        // so the host app peeks behind the sheet. The user can drag
                        // the toolbar handle to resize or dismiss.
                        if (useSheet) {
                            val displayMetrics = context.resources.displayMetrics.sref()
                            val sheetHeight = (displayMetrics.heightPixels * 9) / 10
                            builder.setInitialActivityHeightPx(sheetHeight, CustomTabsIntent.ACTIVITY_HEIGHT_DEFAULT)
                        }

                        // Add custom menu items
                        if (params != null) {
                            if (params.customActions.count > 5) {
                                logger.warning("openWebBrowser: Chrome Custom Tabs supports at most 5 menu items; only the first 5 will be shown")
                            }
                            for (action in params.customActions.sref()) {
                                val actionId = java.util.UUID.randomUUID().toString()
                                WebBrowserActionRegistry.register(actionId = actionId, handler = action.handler)

                                val menuIntent = Intent(context, context.asActivity().javaClass)
                                menuIntent.setAction("skip.web.WEB_PAGE_ACTION")
                                menuIntent.putExtra("actionId", actionId)

                                val pendingFlags = PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                                val pendingIntent = PendingIntent.getActivity(context, actionId.hashCode(), menuIntent, pendingFlags)
                                builder.addMenuItem(action.label, pendingIntent)
                            }
                        }

                        val customTabsIntent = builder.build()
                        val uri = url.toAndroidUri()
                        customTabsIntent.launchUrl(context.asActivity(), uri)
                        isPresented.wrappedValue = false
                    }
                }.Compose(composectx)
            }
        }
        ComposeResult.ok
    }
}

// MARK: - iOS: SFSafariViewController Wrapper


// MARK: - Android: Custom Tabs Action Registry

/// Registry for custom action handlers used with Chrome Custom Tabs menu items.
/// When the user taps a menu item, Chrome fires a PendingIntent back to the activity.
/// The onNewIntent listener dispatches to the registered handler.
internal open class WebBrowserActionRegistry {

    @androidx.annotation.Keep
    companion object {
        private var handlers: Dictionary<String, (URL) -> Unit> = dictionaryOf()
            get() = field.sref({ this.handlers = it })
            set(newValue) {
                field = newValue.sref()
            }
        private var isListenerRegistered = false

        internal fun register(actionId: String, handler: (URL) -> Unit) {
            handlers[actionId] = handler
            ensureListenerRegistered()
        }

        internal fun handleIntent(intent: Intent): Boolean {
            if (intent.action != "skip.web.WEB_PAGE_ACTION") {
                return false
            }
            val actionId_0 = intent.getStringExtra("actionId")
            if (actionId_0 == null) {
                return false
            }
            val handler_0 = handlers.removeValue(forKey = actionId_0)
            if (handler_0 == null) {
                return false
            }

            intent.dataString.sref()?.let { dataString ->
                (try { URL(string = dataString) } catch (_: NullReturnException) { null })?.let { url ->
                    handler_0(url)
                }
            }
            return true
        }

        private fun ensureListenerRegistered() {
            if (isListenerRegistered) {
                return
            }
            val activity_0 = UIApplication.shared.androidActivity.sref()
            if (activity_0 == null) {
                return
            }
            isListenerRegistered = true
            activity_0.addOnNewIntentListener(WebBrowserActionIntentListener())
        }
    }
}

private class WebBrowserActionIntentListener: androidx.core.util.Consumer<Intent> {
    override fun accept(value: Intent) {
        WebBrowserActionRegistry.handleIntent(value)
    }
}
 // !SKIP_BRIDGE // !SKIP_BRIDGE
