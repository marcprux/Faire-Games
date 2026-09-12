package skip.ui

import skip.lib.*

// Copyright 2023–2026 Skip
// SPDX-License-Identifier: MPL-2.0
import skip.foundation.*

/// Holds a single edge's swipe action configuration.
internal class SwipeActionsConfig {
    internal val allowsFullSwipe: Boolean
    internal val content: ComposeBuilder

    constructor(allowsFullSwipe: Boolean, content: ComposeBuilder) {
        this.allowsFullSwipe = allowsFullSwipe
        this.content = content
    }
}

internal class SwipeActionsModifier: RenderModifier {
    internal val edge: HorizontalEdge
    internal val allowsFullSwipe: Boolean
    internal val content: ComposeBuilder

    internal constructor(edge: HorizontalEdge, allowsFullSwipe: Boolean, content: ComposeBuilder): super() {
        this.edge = edge
        this.allowsFullSwipe = allowsFullSwipe
        this.content = content
    }

    @androidx.annotation.Keep
    companion object {

        /// Walk the modifier chain and collect leading + trailing swipe configs.
        /// Innermost modifier on a given edge wins (matches SwiftUI semantics).
        internal fun combined(for_: Renderable): Tuple2<SwipeActionsConfig?, SwipeActionsConfig?> {
            val renderable = for_
            var leading: SwipeActionsConfig? = null
            var trailing: SwipeActionsConfig? = null
            renderable.forEachModifier l@{ it ->
                (it as? SwipeActionsModifier)?.let { mod ->
                    val config = SwipeActionsConfig(allowsFullSwipe = mod.allowsFullSwipe, content = mod.content)
                    if (mod.edge == HorizontalEdge.leading) {
                        leading = leading ?: config
                    } else {
                        trailing = trailing ?: config
                    }
                }
                return@l null
            }
            return Tuple2(leading, trailing)
        }
    }
}
