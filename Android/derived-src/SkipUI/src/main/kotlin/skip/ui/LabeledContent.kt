package skip.ui

import skip.lib.*

// Copyright 2023–2026 Skip
// SPDX-License-Identifier: MPL-2.0
import skip.foundation.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp

@androidx.annotation.Keep
class LabeledContent: View, Renderable, skip.lib.SwiftProjecting {
    internal val content: ComposeBuilder
    internal val label: ComposeBuilder

    constructor(content: () -> View, label: () -> View) {
        this.content = ComposeBuilder.from(content)
        this.label = ComposeBuilder.from(label)
    }

    constructor(bridgedContent: View, bridgedLabel: View) {
        this.content = ComposeBuilder.from { -> bridgedContent }
        this.label = ComposeBuilder.from { -> bridgedLabel }
    }

    constructor(titleKey: LocalizedStringKey, content: () -> View): this(content = content, label = { ->
        ComposeBuilder { composectx: ComposeContext ->
            Text(titleKey).Compose(composectx)
            ComposeResult.ok
        }
    }) {
    }
    constructor(title: String, content: () -> View): this(content = content, label = { ->
        ComposeBuilder { composectx: ComposeContext ->
            Text(verbatim = title).Compose(composectx)
            ComposeResult.ok
        }
    }) {
    }
    constructor(titleResource: LocalizedStringResource, content: () -> View): this(content = content, label = { ->
        ComposeBuilder { composectx: ComposeContext ->
            Text(titleResource).Compose(composectx)
            ComposeResult.ok
        }
    }) {
    }

    constructor(titleKey: LocalizedStringKey, value: String): this(content = { ->
        ComposeBuilder { composectx: ComposeContext ->
            Text(verbatim = value).Compose(composectx)
            ComposeResult.ok
        }
    }, label = { ->
        ComposeBuilder { composectx: ComposeContext ->
            Text(titleKey).Compose(composectx)
            ComposeResult.ok
        }
    }) {
    }
    constructor(title: String, value: String): this(content = { ->
        ComposeBuilder { composectx: ComposeContext ->
            Text(verbatim = value).Compose(composectx)
            ComposeResult.ok
        }
    }, label = { ->
        ComposeBuilder { composectx: ComposeContext ->
            Text(verbatim = title).Compose(composectx)
            ComposeResult.ok
        }
    }) {
    }

    @Composable
    override fun Render(context: ComposeContext) {
        var style = EnvironmentValues.shared._labeledContentStyle ?: LabeledContentStyle.automatic
        RenderLabeleledContent(context = context)
    }

    @Composable
    private fun RenderLabeleledContent(context: ComposeContext) {
        Row(modifier = context.modifier.fillWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) { ->
            RenderLabel(context = context.content())
            RenderContent(context = context.content())
        }
    }

    /// Render only the label of this labeled content.
    @Composable
    internal fun RenderLabel(context: ComposeContext, labelColor: Color? = null) {
        if (labelColor != null) {
            EnvironmentValues.shared.setValues(l@{ it ->
                it.set_foregroundStyle(labelColor)
                return@l ComposeResult.ok
            }, in_ = { -> label.Compose(context = context) })
        } else {
            label.Compose(context = context)
        }
    }

    /// Render only the content of this labeled content.
    @Composable
    internal fun RenderContent(context: ComposeContext) {
        content.Compose(context = context)
    }

    override fun Swift_projection(options: Int): () -> Any = Swift_projectionImpl(options)
    private external fun Swift_projectionImpl(options: Int): () -> Any

    @androidx.annotation.Keep
    companion object {
    }
}

class LabeledContentStyle: RawRepresentable<Int> {
    override val rawValue: Int

    constructor(rawValue: Int) {
        this.rawValue = rawValue
    }

    override fun equals(other: Any?): Boolean {
        if (other !is LabeledContentStyle) return false
        return rawValue == other.rawValue
    }

    @androidx.annotation.Keep
    companion object {

        val automatic = LabeledContentStyle(rawValue = 0) // For bridging
    }
}
