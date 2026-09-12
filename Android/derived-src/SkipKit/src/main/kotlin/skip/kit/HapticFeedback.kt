package skip.kit

import skip.lib.*
import skip.lib.Array

// Copyright 2024-2026 Skip
// SPDX-License-Identifier: MPL-2.0
import skip.foundation.*

/// A single haptic event within a pattern.
@androidx.annotation.Keep
class HapticEvent: skip.lib.SwiftProjecting {
    /// The type of haptic primitive to play.
    val type: HapticEventType
    /// Intensity from 0.0 to 1.0.
    val intensity: Double
    /// Delay in seconds before this event plays (relative to the previous event).
    val delay: Double

    constructor(type: HapticEventType, intensity: Double = 1.0, delay: Double = 0.0) {
        this.type = type
        this.intensity = min(max(intensity, 0.0), 1.0)
        this.delay = max(delay, 0.0)
    }

    override fun Swift_projection(options: Int): () -> Any = Swift_projectionImpl(options)
    private external fun Swift_projectionImpl(options: Int): () -> Any

    @androidx.annotation.Keep
    companion object {
    }
}

/// The type of haptic primitive.
@androidx.annotation.Keep
enum class HapticEventType: skip.lib.SwiftProjecting {
    /// A short, sharp tap. The most common haptic element.
    tap,
    /// A subtle, light tick. Good for selections and fine adjustments.
    tick,
    /// A heavy, deep vibration. Good for collisions and impacts.
    thud,
    /// A vibration that increases in intensity. Good for building tension or starting an action.
    rise,
    /// A vibration that decreases in intensity. Good for releasing tension or ending an action.
    fall,
    /// A deep, low-frequency tick. Good for warnings and errors.
    lowTick;

    override fun Swift_projection(options: Int): () -> Any = Swift_projectionImpl(options)
    private external fun Swift_projectionImpl(options: Int): () -> Any

    @androidx.annotation.Keep
    companion object {
    }
}

/// A sequence of haptic events that form a complete feedback pattern.
@androidx.annotation.Keep
class HapticPattern: skip.lib.SwiftProjecting {
    val events: Array<HapticEvent>

    constructor(events: Array<HapticEvent>) {
        this.events = events.sref()
    }

    override fun Swift_projection(options: Int): () -> Any = Swift_projectionImpl(options)
    private external fun Swift_projectionImpl(options: Int): () -> Any

    @androidx.annotation.Keep
    companion object {

        /// A light tap for picking up or selecting an element.
        val pick = HapticPattern(arrayOf(HapticEvent(HapticEventType.tick, intensity = 0.4)))

        /// A subtle tick for snapping to a grid or alignment point.
        val snap = HapticPattern(arrayOf(
            HapticEvent(HapticEventType.tick, intensity = 0.5),
            HapticEvent(HapticEventType.tick, intensity = 0.3, delay = 0.04)
        ))

        /// A solid tap for placing or confirming an action.
        val place = HapticPattern(arrayOf(HapticEvent(HapticEventType.tap, intensity = 0.7)))

        /// A satisfying confirmation pattern.
        val success = HapticPattern(arrayOf(
            HapticEvent(HapticEventType.tap, intensity = 0.8),
            HapticEvent(HapticEventType.tick, intensity = 0.5, delay = 0.1)
        ))

        /// An attention-getting warning pattern.
        val warning = HapticPattern(arrayOf(
            HapticEvent(HapticEventType.rise, intensity = 0.8),
            HapticEvent(HapticEventType.fall, intensity = 0.9, delay = 0.1)
        ))

        /// A rejection or failure pattern with three descending taps.
        val error = HapticPattern(arrayOf(
            HapticEvent(HapticEventType.lowTick, intensity = 1.0),
            HapticEvent(HapticEventType.lowTick, intensity = 0.7, delay = 0.1),
            HapticEvent(HapticEventType.lowTick, intensity = 0.4, delay = 0.1)
        ))

        /// A heavy single impact.
        val impact = HapticPattern(arrayOf(HapticEvent(HapticEventType.thud, intensity = 1.0)))

        /// A celebratory double-tap pattern.
        val celebrate = HapticPattern(arrayOf(
            HapticEvent(HapticEventType.tap, intensity = 1.0),
            HapticEvent(HapticEventType.rise, intensity = 0.6, delay = 0.08),
            HapticEvent(HapticEventType.tap, intensity = 0.9, delay = 0.08),
            HapticEvent(HapticEventType.tick, intensity = 0.4, delay = 0.1)
        ))

        /// A big celebration with escalating intensity for combos and achievements.
        val bigCelebrate = HapticPattern(arrayOf(
            HapticEvent(HapticEventType.thud, intensity = 0.8),
            HapticEvent(HapticEventType.rise, intensity = 1.0, delay = 0.1),
            HapticEvent(HapticEventType.tap, intensity = 1.0, delay = 0.08),
            HapticEvent(HapticEventType.tick, intensity = 0.8, delay = 0.06),
            HapticEvent(HapticEventType.tap, intensity = 0.6, delay = 0.06),
            HapticEvent(HapticEventType.tick, intensity = 0.4, delay = 0.1)
        ))

        /// Creates a repeating bounce pattern with decreasing intensity, like a ball bouncing to rest.
        fun bounce(count: Int = 3, startIntensity: Double = 0.9): HapticPattern {
            var events: Array<HapticEvent> = arrayOf()
            for (i in 0..<count) {
                val fraction = 1.0 - (Double(i) / Double(count))
                val intensity = startIntensity * fraction
                val delay = if (i == 0) 0.0 else 0.06 + Double(i) * 0.03
                events.append(HapticEvent(HapticEventType.tap, intensity = intensity, delay = delay))
            }
            return HapticPattern(events)
        }

        /// Creates an escalating pattern for combo streaks. Higher streaks feel more dramatic.
        fun combo(streak: Int): HapticPattern {
            val clamped = min(max(streak, 1), 8)
            var events: Array<HapticEvent> = arrayOf()
            // Quick escalating taps
            for (i in 0..<clamped) {
                val intensity = 0.4 + (0.6 * Double(i) / Double(clamped))
                val delay = if (i == 0) 0.0 else 0.06
                events.append(HapticEvent(HapticEventType.tap, intensity = intensity, delay = delay))
            }
            // Finish with a satisfying thud
            events.append(HapticEvent(HapticEventType.thud, intensity = min(0.5 + Double(clamped) * 0.1, 1.0), delay = 0.08))
            return HapticPattern(events)
        }
    }
}

// MARK: - Playback

/// Plays custom haptic patterns on both iOS and Android.
@androidx.annotation.Keep
class HapticFeedback: skip.lib.SwiftProjecting {

    // MARK: - iOS Implementation


    // MARK: - Android Implementation


    override fun Swift_projection(options: Int): () -> Any = Swift_projectionImpl(options)
    private external fun Swift_projectionImpl(options: Int): () -> Any

    @androidx.annotation.Keep
    companion object {

        /// Play a haptic pattern. Call from any thread.
        fun play(pattern: HapticPattern) {
            if (pattern.events.isEmpty) {
                return
            }

            playAndroid(pattern)
        }
        private val vibrator: android.os.Vibrator? = linvoke l@{ ->
            val context = ProcessInfo.processInfo.androidContext.sref()
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S) {
                return@l null
            }
            val mgr_0 = (context.getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE) as? android.os.VibratorManager).sref()
            if (mgr_0 == null) {
                return@l null
            }
            return@l mgr_0.getDefaultVibrator()
        }

        private fun playAndroid(pattern: HapticPattern) {
            if (vibrator == null) {
                return
            }

            val composition = android.os.VibrationEffect.startComposition()
            var delayMs = 0

            for (event in pattern.events.sref()) {
                delayMs += Int(event.delay * 1000.0)

                val primitive: Int
                when (event.type) {
                    HapticEventType.tap -> primitive = android.os.VibrationEffect.Composition.PRIMITIVE_CLICK
                    HapticEventType.tick -> primitive = android.os.VibrationEffect.Composition.PRIMITIVE_TICK
                    HapticEventType.thud -> primitive = android.os.VibrationEffect.Composition.PRIMITIVE_THUD
                    HapticEventType.rise -> primitive = android.os.VibrationEffect.Composition.PRIMITIVE_QUICK_RISE
                    HapticEventType.fall -> primitive = android.os.VibrationEffect.Composition.PRIMITIVE_QUICK_FALL
                    HapticEventType.lowTick -> primitive = android.os.VibrationEffect.Composition.PRIMITIVE_LOW_TICK
                }

                composition.addPrimitive(primitive, Float(event.intensity), delayMs)
                delayMs = 0 // reset after adding; next event's delay is relative
            }

            vibrator.vibrate(composition.compose())
        }
    }
}

