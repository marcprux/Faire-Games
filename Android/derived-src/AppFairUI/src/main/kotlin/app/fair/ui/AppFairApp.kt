package app.fair.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import skip.lib.*

// SPDX-License-Identifier: GPL-2.0-or-later
import skip.foundation.*
import skip.ui.*
import skip.kit.*
import skip.model.*

/// The name of the current app
internal val appName = (Bundle.main.infoDictionary?.get("CFBundleDisplayName") as? String) ?: (Bundle.main.infoDictionary?.get("CFBundleName") as? String) ?: "Unknown"

/// The current version of the app, using the main bundle's infoDictionary `CFBundleShortVersionString` property on iOS and `android.content.pm.PackageManager` `versionName` on Android.
internal val appVersion = (Bundle.main.infoDictionary?.get("CFBundleShortVersionString") as? String) ?: "0.0.0"

/// The bundle identifier of the current app
internal val appIdentifier = (Bundle.main.infoDictionary?.get("CFBundleIdentifier") as? String) ?: "app.unknown"

internal val appToken = (appIdentifier.split(separator = ".").last?.description ?: "Unknown").replacingOccurrences(of = "_", with = "-")

// FIXME: this only works in cases where the Bundle ID matches the
internal val appRepository = URL(string = "https://github.com/${appToken}/${appToken}")

/// A top-level settings view that presents a Form with app settings, along with information about the App Fair Project.
///
/// If a `bundle` is provided that contains an SPDX SBOM resource for the current platform
/// (`sbom-darwin-ios.spdx.json` on Apple, `sbom-linux-android.spdx.json` on Android), the
/// settings view also exposes a "Bill of Materials" navigation entry that opens an
/// `SBOMView` for the bundled dependencies and their licenses.
class AppFairSettings<Content>: View where Content: View {
    internal val content: Content
    internal val bundle: Bundle?

    constructor(content: () -> Content) {
        this.content = content()
        this.bundle = null
    }

    constructor(bundle: Bundle?, content: () -> Content) {
        this.content = content()
        this.bundle = bundle
    }

    override fun body(): View {
        return ComposeBuilder { composectx: ComposeContext ->
            NavigationStack { ->
                ComposeBuilder { composectx: ComposeContext ->
                    Form { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            content.Compose(composectx)

                            Section { ->
                                ComposeBuilder { composectx: ComposeContext ->
                                    //NavigationLink("About \(appName)") {
                                    //    Text("App info about \(appName)")
                                    //}
                                    //NavigationLink("About the App Fair Project") {
                                    //    Text("App info")
                                    //}
                                    //NavigationLink("Help and Support") {
                                    //    Text("Support this app…")
                                    //}
                                    //NavigationLink("Translations") {
                                    //    Text("Translate this app…")
                                    //}

                                    if ((bundle != null) && SBOMView.bundleContainsSBOM(bundle)) {
                                        NavigationLink(LocalizedStringKey(stringLiteral = "Bill of Materials")) { ->
                                            ComposeBuilder { composectx: ComposeContext ->
                                                SBOMView(bundle = bundle).Compose(composectx)
                                                ComposeResult.ok
                                            }
                                        }.Compose(composectx)
                                    }

                                    NavigationURLLink(title = LocalizedStringKey("Project Home"), destination = appRepository).Compose(composectx)
                                    ComposeResult.ok
                                }
                            }.Compose(composectx)
                            ComposeResult.ok
                        }
                    }
                    .navigationTitle({
                        val str = LocalizedStringKey.StringInterpolation(literalCapacity = 0, interpolationCount = 0)
                        str.appendInterpolation(appName)
                        str.appendLiteral(" ")
                        str.appendInterpolation(appVersion)
                        LocalizedStringKey(stringInterpolation = str)
                    }()).Compose(composectx)
                    ComposeResult.ok
                }
            }.Compose(composectx)
        }
    }

    @androidx.annotation.Keep
    companion object {
    }
}

/// A button that opens the given URL in an embedded browser (SFSafariViewController on iOS, Chrome Custom Tabs on Android).
internal class NavigationURLLink: View {
    internal val title: LocalizedStringKey
    internal val destination: URL
    private var isPresented: Boolean
        get() = _isPresented.wrappedValue
        set(newValue) {
            _isPresented.wrappedValue = newValue
        }
    private var _isPresented: skip.ui.State<Boolean>

    override fun body(): View {
        return ComposeBuilder { composectx: ComposeContext ->
            Link(destination = destination) { ->
                ComposeBuilder { composectx: ComposeContext ->
                    NavigationLink(title) { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            NavigationURLLinkDestinationView(title = title, destination = destination).Compose(composectx)
                            ComposeResult.ok
                        }
                    }.Compose(composectx)
                    ComposeResult.ok
                }
            }.Compose(composectx)
        }
    }

    @Composable
    @Suppress("UNCHECKED_CAST")
    override fun Evaluate(context: ComposeContext, options: Int): kotlin.collections.List<Renderable> {
        val rememberedisPresented by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Boolean>, Any>) { mutableStateOf(_isPresented) }
        _isPresented = rememberedisPresented

        return super.Evaluate(context, options)
    }

    private class NavigationURLLinkDestinationView: View {
        internal lateinit var openURL: OpenURLAction
        internal lateinit var dismiss: DismissAction
        internal val title: LocalizedStringKey
        internal val destination: URL

        override fun body(): View {
            return ComposeBuilder { composectx: ComposeContext ->
                Link(title, destination = destination)
                    .onAppear { -> openURL(destination) }.task { -> MainActor.run {
                    // pop back up the nav stack when we launch the
                    dismiss()
                } }.Compose(composectx)
            }
        }

        @Composable
        override fun Evaluate(context: ComposeContext, options: Int): kotlin.collections.List<Renderable> {
            this.openURL = EnvironmentValues.shared.openURL
            this.dismiss = EnvironmentValues.shared.dismiss

            return super.Evaluate(context, options)
        }

        constructor(title: LocalizedStringKey, destination: URL) {
            this.title = title
            this.destination = destination.sref()
        }
    }

    private constructor(title: LocalizedStringKey, destination: URL, isPresented: Boolean = false, privatep: Nothing? = null) {
        this.title = title
        this.destination = destination.sref()
        this._isPresented = skip.ui.State(isPresented)
    }

    constructor(title: LocalizedStringKey, destination: URL): this(title = title, destination = destination, privatep = null) {
    }
}

