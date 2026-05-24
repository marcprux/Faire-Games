package skip.ui

import skip.lib.*

// Copyright 2023–2026 Skip
// SPDX-License-Identifier: MPL-2.0

enum class SymbolRenderingMode(override val rawValue: Int, @Suppress("UNUSED_PARAMETER") unusedp: Nothing? = null): RawRepresentable<Int> {
    monochrome(0),
    multicolor(1),
    hierarchical(2),
    palette(3);

    @androidx.annotation.Keep
    companion object {
        fun init(rawValue: Int): SymbolRenderingMode? {
            return when (rawValue) {
                0 -> SymbolRenderingMode.monochrome
                1 -> SymbolRenderingMode.multicolor
                2 -> SymbolRenderingMode.hierarchical
                3 -> SymbolRenderingMode.palette
                else -> null
            }
        }
    }
}

fun SymbolRenderingMode(rawValue: Int): SymbolRenderingMode? = SymbolRenderingMode.init(rawValue = rawValue)

/// Symbol variants that can be applied to SF Symbols.
/// Uses a bit flag internally to combine variants.
class SymbolVariants: RawRepresentable<Int> {
    override val rawValue: Int

    constructor(rawValue: Int) {
        this.rawValue = rawValue
    }

    /// Combine with fill variant
    val fill: SymbolVariants
        get() = SymbolVariants(rawValue = rawValue or Companion.fillBit)

    /// Combine with circle variant
    val circle: SymbolVariants
        get() = SymbolVariants(rawValue = rawValue or Companion.circleBit)

    /// Combine with square variant
    val square: SymbolVariants
        get() = SymbolVariants(rawValue = rawValue or Companion.squareBit)

    /// Combine with rectangle variant
    val rectangle: SymbolVariants
        get() = SymbolVariants(rawValue = rawValue or Companion.rectangleBit)

    /// Combine with slash variant
    val slash: SymbolVariants
        get() = SymbolVariants(rawValue = rawValue or Companion.slashBit)

    /// Check if this variant contains another variant
    fun contains(other: SymbolVariants): Boolean = (rawValue and other.rawValue) == other.rawValue

    /// Apply the symbol variants to a symbol name, returning the modified name.
    fun applied(to: String): String {
        val symbolName = to
        var name = symbolName

        // Apply shape variants first (circle, square, rectangle)
        if (contains(SymbolVariants.circle) && !name.contains(".circle")) {
            name = name + ".circle"
        } else if (contains(SymbolVariants.square) && !name.contains(".square")) {
            name = name + ".square"
        } else if (contains(SymbolVariants.rectangle) && !name.contains(".rectangle")) {
            name = name + ".rectangle"
        }

        // Apply fill variant
        if (contains(SymbolVariants.fill) && !name.contains(".fill")) {
            name = name + ".fill"
        }

        // Apply slash variant
        if (contains(SymbolVariants.slash) && !name.contains(".slash")) {
            name = name + ".slash"
        }

        return name
    }

    override fun equals(other: Any?): Boolean {
        if (other !is SymbolVariants) return false
        return rawValue == other.rawValue
    }

    override fun hashCode(): Int {
        var result = 1
        result = Hasher.combine(result, rawValue)
        return result
    }

    @androidx.annotation.Keep
    companion object {

        // Bit flags for each variant type
        private val fillBit = 1 shl 0 // 1
        private val circleBit = 1 shl 1 // 2
        private val squareBit = 1 shl 2 // 4
        private val rectangleBit = 1 shl 3 // 8
        private val slashBit = 1 shl 4 // 16

        val none = SymbolVariants(rawValue = 0)
        val fill = SymbolVariants(rawValue = fillBit)
        val circle = SymbolVariants(rawValue = circleBit)
        val square = SymbolVariants(rawValue = squareBit)
        val rectangle = SymbolVariants(rawValue = rectangleBit)
        val slash = SymbolVariants(rawValue = slashBit)
    }
}

class SymbolVariableValueMode {

    override fun equals(other: Any?): Boolean = other is SymbolVariableValueMode

    @androidx.annotation.Keep
    companion object {
        val color = SymbolVariableValueMode()
        val draw = SymbolVariableValueMode()
    }
}

class SymbolColorRenderingMode {

    override fun equals(other: Any?): Boolean = other is SymbolColorRenderingMode

    @androidx.annotation.Keep
    companion object {
        val flat = SymbolColorRenderingMode()
        val gradient = SymbolColorRenderingMode()
    }
}

/*
import protocol Symbols.SymbolEffect
import struct Symbols.SymbolEffectOptions
import protocol Symbols.TransitionSymbolEffect
import protocol Symbols.ContentTransitionSymbolEffect
import protocol Symbols.IndefiniteSymbolEffect
import protocol Symbols.DiscreteSymbolEffect


@available(iOS 17.0, macOS 14.0, tvOS 17.0, watchOS 10.0, *)
extension Transition where Self == SymbolEffectTransition {

/// Creates a transition that applies the provided effect to symbol
/// images within the inserted or removed view hierarchy. Other
/// views are unaffected by this transition.
///
/// - Parameter effect: the symbol effect value.
///
/// - Returns: a new transition.
public static func symbolEffect<T>(_ effect: T, options: SymbolEffectOptions = .default) -> SymbolEffectTransition where T : SymbolEffect, T : TransitionSymbolEffect { fatalError() }

/// A transition that applies the default symbol effect transition
/// to symbol images within the inserted or removed view hierarchy.
/// Other views are unaffected by this transition.
public static var symbolEffect: SymbolEffectTransition { get { fatalError() } }
}

/// Creates a transition that applies the Appear or Disappear
/// symbol animation to symbol images within the inserted or
/// removed view hierarchy.
///
/// Other views are unaffected by this transition.
@available(iOS 17.0, macOS 14.0, tvOS 17.0, watchOS 10.0, *)
@frozen public struct SymbolEffectTransition : Transition {

public init<T>(effect: T, options: SymbolEffectOptions) where T : SymbolEffect, T : TransitionSymbolEffect { fatalError() }

/// Gets the current body of the caller.
///
/// `content` is a proxy for the view that will have the modifier
/// represented by `Self` applied to it.
public func body(content: SymbolEffectTransition.Content, phase: TransitionPhase) -> some View { return stubView() }


/// Returns the properties this transition type has.
///
/// Defaults to `TransitionProperties()`.
public static let properties: TransitionProperties = { fatalError() }()

/// The type of view representing the body.
//    public typealias Body = some View
}

@available(iOS 17.0, macOS 14.0, tvOS 17.0, watchOS 10.0, *)
extension ContentTransition {

/// Creates a content transition that applies the symbol Replace
/// animation to symbol images that it is applied to.
///
/// - Parameter config: the animation configuration value.
///
/// - Returns: a new content transition.
public static func symbolEffect<T>(_ effect: T, options: SymbolEffectOptions = .default) -> ContentTransition where T : ContentTransitionSymbolEffect, T : SymbolEffect { fatalError() }

/// A content transition that applies the default symbol effect
/// transition to symbol images within the inserted or removed view
/// hierarchy. Other views are unaffected by this transition.
public static var symbolEffect: ContentTransition { get { fatalError() } }
}

@available(iOS 15.0, macOS 12.0, tvOS 15.0, watchOS 8.0, *)
extension EnvironmentValues {

/// The current symbol rendering mode, or `nil` denoting that the
/// mode is picked automatically using the current image and
/// foreground style as parameters.
public var symbolRenderingMode: SymbolRenderingMode? { get { fatalError() } }
}

@available(iOS 15.0, macOS 12.0, tvOS 15.0, watchOS 8.0, *)
extension EnvironmentValues {

/// The symbol variant to use in this environment.
///
/// You set this environment value indirectly by using the
/// ``View/symbolVariant(_:)`` view modifier. However, you access the
/// environment variable directly using the ``View/environment(_:_:)``
/// modifier. Do this when you want to use the ``SymbolVariants/none``
/// variant to ignore the value that's already in the environment:
///
///     HStack {
///         Image(systemName: "heart")
///         Image(systemName: "heart")
///             .environment(\.symbolVariants, .none)
///     }
///     .symbolVariant(.fill)
///
/// ![A screenshot of two heart symbols. The first is filled while the
/// second is outlined.](SymbolVariants-none-1)
public var symbolVariants: SymbolVariants { get { fatalError() } }
}
*/
