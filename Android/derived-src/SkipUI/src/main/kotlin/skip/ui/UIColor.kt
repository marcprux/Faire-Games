package skip.ui

import skip.lib.*

// Copyright 2023–2026 Skip
// SPDX-License-Identifier: MPL-2.0

@androidx.annotation.Keep
class UIColor: skip.lib.SwiftProjecting {
    val red: Double
    val green: Double
    val blue: Double
    val alpha: Double

    constructor(red: Double, green: Double, blue: Double, alpha: Double) {
        this.red = red
        this.green = green
        this.blue = blue
        this.alpha = alpha
    }


    override fun Swift_projection(options: Int): () -> Any = Swift_projectionImpl(options)
    private external fun Swift_projectionImpl(options: Int): () -> Any

    @androidx.annotation.Keep
    companion object {
        // We can't resolve actual RGB values unless we're in a @Composable context.
        // Use with `Color(.systemBackground)` to get the adaptive semantic color.
        val systemBackground: UIColor = UIColor(red = 1.0, green = 1.0, blue = 1.0, alpha = 1.0)
    }
}

