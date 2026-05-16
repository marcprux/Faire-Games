package skip.ui

import skip.lib.*

// Copyright 2023–2026 Skip
// SPDX-License-Identifier: MPL-2.0
import androidx.compose.runtime.Composable

@androidx.annotation.Keep
class Group: View, skip.lib.SwiftProjecting {
    internal val content: ComposeBuilder

    constructor(content: () -> View) {
        this.content = ComposeBuilder.from(content)
    }

    @Deprecated("This API is not yet available in Skip. Consider placing it within a #if !SKIP block. You can file an issue against the owning library at https://github.com/skiptools, or see the library README for information on adding support", level = DeprecationLevel.ERROR)
    constructor(subviews: View, transform: (Any) -> View) {
        val view = subviews
        this.content = ComposeBuilder(view = EmptyView())
    }

    @Deprecated("This API is not yet available in Skip. Consider placing it within a #if !SKIP block. You can file an issue against the owning library at https://github.com/skiptools, or see the library README for information on adding support", level = DeprecationLevel.ERROR)
    constructor(sections: View, transform: (Any) -> View, @Suppress("UNUSED_PARAMETER") unusedp_0: Nothing? = null) {
        val view = sections
        this.content = ComposeBuilder(view = EmptyView())
    }

    constructor(bridgedContent: View) {
        this.content = ComposeBuilder.from { -> bridgedContent }
    }

    @Composable
    override fun Evaluate(context: ComposeContext, options: Int): kotlin.collections.List<Renderable> = content.Evaluate(context = context, options = options)

    override fun Swift_projection(options: Int): () -> Any = Swift_projectionImpl(options)
    private external fun Swift_projectionImpl(options: Int): () -> Any

    @androidx.annotation.Keep
    companion object {
    }
}

