package skip.ui

import skip.lib.*

// Copyright 2023–2026 Skip
// SPDX-License-Identifier: MPL-2.0

enum class BadgeProminence(override val rawValue: Int, @Suppress("UNUSED_PARAMETER") unusedp: Nothing? = null): RawRepresentable<Int> {
    decreased(0),
    standard(1),
    increased(2);

    @androidx.annotation.Keep
    companion object {
        fun init(rawValue: Int): BadgeProminence? {
            return when (rawValue) {
                0 -> BadgeProminence.decreased
                1 -> BadgeProminence.standard
                2 -> BadgeProminence.increased
                else -> null
            }
        }
    }
}

fun BadgeProminence(rawValue: Int): BadgeProminence? = BadgeProminence.init(rawValue = rawValue)

