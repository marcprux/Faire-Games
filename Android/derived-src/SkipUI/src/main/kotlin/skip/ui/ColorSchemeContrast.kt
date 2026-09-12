package skip.ui

import skip.lib.*
import skip.lib.Array

// Copyright 2023–2026 Skip
// SPDX-License-Identifier: MPL-2.0
/// The contrast between the app's foreground and background colors.
///
/// You receive a contrast value when you read the
/// ``EnvironmentValues/colorSchemeContrast`` environment value. The value
/// tells you if a standard or increased contrast currently applies to the view.
/// SkipUI updates the value whenever the contrast changes, and redraws
/// views that depend on the value. For example, the following ``Text`` view
/// automatically updates when the user enables increased contrast:
///
///     @Environment(\.colorSchemeContrast) private var colorSchemeContrast
///
///     var body: some View {
///         Text(colorSchemeContrast == .standard ? "Standard" : "Increased")
///     }
///
/// The user sets the contrast by selecting the Increase Contrast option in
/// Accessibility > Display in System Preferences on macOS, or
/// Accessibility > Display & Text Size in the Settings app on iOS.
/// Your app can't override the user's choice.
@androidx.annotation.Keep
enum class ColorSchemeContrast(override val rawValue: Int, @Suppress("UNUSED_PARAMETER") unusedp: Nothing? = null): CaseIterable, RawRepresentable<Int> {
    standard(0), // For bridging
    increased(1); // For bridging


    @androidx.annotation.Keep
    companion object: CaseIterableCompanion<ColorSchemeContrast> {

        /// A collection of all values of this type.
        override val allCases: Array<ColorSchemeContrast>
            get() {
                fatalError()
            }

        fun init(rawValue: Int): ColorSchemeContrast? {
            return when (rawValue) {
                0 -> ColorSchemeContrast.standard
                1 -> ColorSchemeContrast.increased
                else -> null
            }
        }
    }
}

fun ColorSchemeContrast(rawValue: Int): ColorSchemeContrast? = ColorSchemeContrast.init(rawValue = rawValue)
