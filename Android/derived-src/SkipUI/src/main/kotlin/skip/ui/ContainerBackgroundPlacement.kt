package skip.ui

import skip.lib.*

// Copyright 2023–2026 Skip
// SPDX-License-Identifier: MPL-2.0

class ContainerBackgroundPlacement {
    override fun equals(other: Any?): Boolean = other is ContainerBackgroundPlacement

    override fun hashCode(): Int = "ContainerBackgroundPlacement".hashCode()

    @androidx.annotation.Keep
    companion object {
    }
}

