package skip.ui

import skip.lib.*

// Copyright 2023–2026 Skip
// SPDX-License-Identifier: MPL-2.0
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@androidx.annotation.Keep
class GroupBox: View, skip.lib.SwiftProjecting {
    internal val label: ComposeBuilder?
    internal val content: ComposeBuilder

    constructor(content: () -> View, label: () -> View) {
        this.label = ComposeBuilder.from(label)
        this.content = ComposeBuilder.from(content)
    }

    constructor(content: () -> View) {
        this.label = null
        this.content = ComposeBuilder.from(content)
    }

    constructor(titleKey: LocalizedStringKey, content: () -> View) {
        this.label = ComposeBuilder.from { -> Text(titleKey) }
        this.content = ComposeBuilder.from(content)
    }

    constructor(title: String, content: () -> View) {
        this.label = ComposeBuilder.from { -> Text(verbatim = title) }
        this.content = ComposeBuilder.from(content)
    }

    constructor(label: View, content: () -> View) {
        this.label = ComposeBuilder.from { -> label }
        this.content = ComposeBuilder.from(content)
    }

    constructor(bridgedLabel: View?, bridgedContent: View) {
        this.label = if (bridgedLabel == null) null else ComposeBuilder.from { -> bridgedLabel!! }
        this.content = ComposeBuilder.from { -> bridgedContent }
    }

    @Composable
    override fun ComposeContent(context: ComposeContext) {
        val contentContext = context.content()
        val isDark = EnvironmentValues.shared.colorScheme == ColorScheme.dark
        val backgroundColor = if (isDark) androidx.compose.ui.graphics.Color(0xFF2C2C2E.toLong()) else androidx.compose.ui.graphics.Color(0xFFF2F2F7.toLong())
        val shape = RoundedCornerShape(10.dp)

        ComposeContainer(axis = Axis.vertical, modifier = context.modifier, fillWidth = true) { modifier ->
            Column(modifier = modifier
                .fillMaxWidth()
                .clip(shape)
                .background(color = backgroundColor, shape = shape)
                .padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.Start) { ->
                if (label != null) {
                    Box(modifier = Modifier.fillMaxWidth()) { -> label.Compose(context = contentContext) }
                }
                content.Compose(context = contentContext)
            }
        }
    }

    override fun Swift_projection(options: Int): () -> Any = Swift_projectionImpl(options)
    private external fun Swift_projectionImpl(options: Int): () -> Any

    @androidx.annotation.Keep
    companion object {
    }
}

//extension View {
//    @available(*, unavailable)
//    public func groupBoxStyle(_ style: Any) -> some View {
//        return self
//    }
//}

