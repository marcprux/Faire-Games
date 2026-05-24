package skip.ui

import skip.lib.*

// Copyright 2023–2026 Skip
// SPDX-License-Identifier: MPL-2.0

class Namespace {
    constructor() {
    }

    val wrappedValue: Namespace.ID
        get() {
            fatalError()
        }

    class ID {
        override fun equals(other: Any?): Boolean = other is Namespace.ID

        override fun hashCode(): Int = "Namespace.ID".hashCode()

        @androidx.annotation.Keep
        companion object {
        }
    }

    @androidx.annotation.Keep
    companion object {
    }
}

