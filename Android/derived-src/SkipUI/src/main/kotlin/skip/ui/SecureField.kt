package skip.ui

import skip.lib.*

// Copyright 2023–2026 Skip
// SPDX-License-Identifier: MPL-2.0
import skip.foundation.*
import androidx.compose.runtime.Composable

class SecureField: View, Renderable {
    internal val textField: TextField

    constructor(text: Binding<String>, prompt: Text? = null, label: () -> View) {
        textField = TextField(text = text, prompt = prompt, isSecure = true, label = label)
    }

    constructor(title: String, text: Binding<String>, prompt: Text? = null): this(text = text, prompt = prompt, label = { ->
        ComposeBuilder { composectx: ComposeContext ->
            Text(verbatim = title).Compose(composectx)
            ComposeResult.ok
        }
    }) {
    }

    constructor(titleKey: LocalizedStringKey, text: Binding<String>, prompt: Text? = null): this(text = text, prompt = prompt, label = { ->
        ComposeBuilder { composectx: ComposeContext ->
            Text(titleKey).Compose(composectx)
            ComposeResult.ok
        }
    }) {
    }

    constructor(titleResource: LocalizedStringResource, text: Binding<String>, prompt: Text? = null): this(text = text, prompt = prompt, label = { ->
        ComposeBuilder { composectx: ComposeContext ->
            Text(titleResource).Compose(composectx)
            ComposeResult.ok
        }
    }) {
    }

    @Composable
    override fun Render(context: ComposeContext) {
        textField.Compose(context = context)
    }

    @androidx.annotation.Keep
    companion object {
    }
}

