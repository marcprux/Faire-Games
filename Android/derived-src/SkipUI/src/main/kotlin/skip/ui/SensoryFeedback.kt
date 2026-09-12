package skip.ui

import skip.lib.*

// Copyright 2023–2026 Skip
// SPDX-License-Identifier: MPL-2.0
import android.os.VibrationEffect

class SensoryFeedback: RawRepresentable<Int> {
    override val rawValue: Int

    constructor(rawValue: Int) {
        this.rawValue = rawValue
    }

    enum class Weight {
        light,
        medium,
        heavy;

        @androidx.annotation.Keep
        companion object {
        }
    }

    enum class Flexibility {
        rigid,
        solid,
        soft;

        @androidx.annotation.Keep
        companion object {
        }
    }

    fun activate() {
        if (systemVibratorService == null) {
            return
        }

        // Custom haptic feedback compositions designed to approximate iOS UIFeedbackGenerator behavior.
        // Uses VibrationEffect.Composition primitives (API 30+) rather than HapticFeedbackConstants
        // (which require API 34+ for many constants).
        // See: https://developer.android.com/develop/ui/views/haptics/custom-haptic-effects
        val composition = VibrationEffect.startComposition()

        when (this) {
            SensoryFeedback.success -> {
                // iOS: UINotificationFeedbackGenerator.success - a strong tap then a lighter confirmation tap
                composition.addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 1.0f, 0)
                composition.addPrimitive(VibrationEffect.Composition.PRIMITIVE_TICK, 0.5f, 100)
            }
            SensoryFeedback.warning -> {
                // iOS: UINotificationFeedbackGenerator.warning - two strong, heavy beats
                composition.addPrimitive(VibrationEffect.Composition.PRIMITIVE_SLOW_RISE, 1.0f, 0)
                composition.addPrimitive(VibrationEffect.Composition.PRIMITIVE_QUICK_FALL, 1.0f, 100)
            }
            SensoryFeedback.error -> {
                // iOS: UINotificationFeedbackGenerator.error - three rapid taps with decreasing intensity
                composition.addPrimitive(VibrationEffect.Composition.PRIMITIVE_LOW_TICK, 1.0f, 0)
                composition.addPrimitive(VibrationEffect.Composition.PRIMITIVE_LOW_TICK, 0.75f, 100)
                composition.addPrimitive(VibrationEffect.Composition.PRIMITIVE_LOW_TICK, 0.5f, 200)
            }
            SensoryFeedback.selection -> {
                // iOS: UISelectionFeedbackGenerator - very light, subtle single tick
                composition.addPrimitive(VibrationEffect.Composition.PRIMITIVE_TICK, 0.3f, 0)
            }
            SensoryFeedback.increase -> {
                // iOS: a quick upward-feeling tap for incrementing a value
                composition.addPrimitive(VibrationEffect.Composition.PRIMITIVE_QUICK_RISE, 0.7f, 0)
            }
            SensoryFeedback.decrease -> {
                // iOS: a quick downward-feeling tap for decrementing a value
                composition.addPrimitive(VibrationEffect.Composition.PRIMITIVE_QUICK_FALL, 0.7f, 0)
            }
            SensoryFeedback.start -> {
                // iOS: rising intensity to indicate an activity beginning
                composition.addPrimitive(VibrationEffect.Composition.PRIMITIVE_QUICK_RISE, 1.0f, 0)
            }
            SensoryFeedback.stop -> {
                // iOS: falling intensity to indicate an activity ending
                composition.addPrimitive(VibrationEffect.Composition.PRIMITIVE_QUICK_FALL, 1.0f, 0)
            }
            SensoryFeedback.alignment -> {
                // iOS: two quick, light taps for snapping to a guide or grid
                composition.addPrimitive(VibrationEffect.Composition.PRIMITIVE_TICK, 0.5f, 0)
                composition.addPrimitive(VibrationEffect.Composition.PRIMITIVE_TICK, 0.5f, 50)
            }
            SensoryFeedback.levelChange -> {
                // iOS: two distinct taps for notching into a discrete position
                composition.addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.8f, 0)
                composition.addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.8f, 100)
            }
            SensoryFeedback.impact -> {
                // iOS: UIImpactFeedbackGenerator.medium - a single strong thud
                composition.addPrimitive(VibrationEffect.Composition.PRIMITIVE_THUD, 1.0f, 0)
            }
        }

        systemVibratorService.vibrate(composition.compose())
    }

    override fun equals(other: Any?): Boolean {
        if (other !is SensoryFeedback) return false
        return rawValue == other.rawValue
    }

    @androidx.annotation.Keep
    companion object {

        val success = SensoryFeedback(rawValue = 1) // For bridging
        val warning = SensoryFeedback(rawValue = 2) // For bridging
        val error = SensoryFeedback(rawValue = 3) // For bridging
        val selection = SensoryFeedback(rawValue = 4) // For bridging
        val increase = SensoryFeedback(rawValue = 5) // For bridging
        val decrease = SensoryFeedback(rawValue = 6) // For bridging
        val start = SensoryFeedback(rawValue = 7) // For bridging
        val stop = SensoryFeedback(rawValue = 8) // For bridging
        val alignment = SensoryFeedback(rawValue = 9) // For bridging
        val levelChange = SensoryFeedback(rawValue = 10) // For bridging
        val impact = SensoryFeedback(rawValue = 11) // For bridging

        fun impact(weight: SensoryFeedback.Weight = SensoryFeedback.Weight.medium, intensity: Double = 1.0): SensoryFeedback = SensoryFeedback.impact

        fun impact(flexibility: SensoryFeedback.Flexibility, intensity: Double = 1.0): SensoryFeedback = SensoryFeedback.impact
    }
}

