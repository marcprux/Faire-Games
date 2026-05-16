package skip.ui

import skip.lib.*

// Copyright 2023–2026 Skip
// SPDX-License-Identifier: MPL-2.0

sealed class HoverPhase {
    class ActiveCase(val associated0: CGPoint): HoverPhase() {
        override fun equals(other: Any?): Boolean {
            if (other !is ActiveCase) return false
            return associated0 == other.associated0
        }
    }
    class EndedCase: HoverPhase() {
    }

    @androidx.annotation.Keep
    companion object {
        fun active(associated0: CGPoint): HoverPhase = ActiveCase(associated0)
        val ended: HoverPhase = EndedCase()
    }
}

