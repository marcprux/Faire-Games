package skip.kit

import skip.lib.*

// Copyright 2025–2026 Skip
// SPDX-License-Identifier: MPL-2.0
import skip.foundation.*

internal val logger: SkipLogger = SkipLogger(subsystem = "skip.kit", category = "SkipKit") // adb logcat '*:S' 'skip.kit.SkipKit:V'

@androidx.annotation.Keep
open class SkipKitModule: skip.lib.SwiftProjecting {

    override fun Swift_projection(options: Int): () -> Any = Swift_projectionImpl(options)
    private external fun Swift_projectionImpl(options: Int): () -> Any

    @androidx.annotation.Keep
    companion object: CompanionClass() {
    }
    open class CompanionClass {
    }
}

