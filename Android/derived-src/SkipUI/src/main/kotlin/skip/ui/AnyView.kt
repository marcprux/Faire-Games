package skip.ui

import skip.lib.*

// Copyright 2023–2026 Skip
// SPDX-License-Identifier: MPL-2.0
import androidx.compose.runtime.Composable

class AnyView: View {
    private val view: View

    constructor(view: View) {
        this.view = view.sref()
    }

    constructor(erasing: View, @Suppress("UNUSED_PARAMETER") unusedp_0: Nothing? = null) {
        val view = erasing
        this.view = view.sref()
    }

    @Composable
    override fun Evaluate(context: ComposeContext, options: Int): kotlin.collections.List<Renderable> = view.Evaluate(context = context, options = options)

    @androidx.annotation.Keep
    companion object {
    }
}

