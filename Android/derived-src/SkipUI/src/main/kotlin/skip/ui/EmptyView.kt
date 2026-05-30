package skip.ui

import skip.lib.*

// Copyright 2023–2026 Skip
// SPDX-License-Identifier: MPL-2.0
import androidx.compose.runtime.Composable

@androidx.annotation.Keep
class EmptyView: View, Renderable, skip.lib.SwiftProjecting {
    constructor() {
    }

    @Composable
    override fun Render(context: ComposeContext) = Unit

    override fun Swift_projection(options: Int): () -> Any = Swift_projectionImpl(options)
    private external fun Swift_projectionImpl(options: Int): () -> Any

    @androidx.annotation.Keep
    companion object {
    }
}

