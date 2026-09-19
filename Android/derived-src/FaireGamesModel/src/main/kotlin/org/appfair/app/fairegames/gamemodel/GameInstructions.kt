// Licensed under the GNU General Public License v2.0 or later
// SPDX-License-Identifier: GPL-2.0-or-later

package org.appfair.app.fairegames.gamemodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import skip.lib.*
import skip.lib.Array

import skip.ui.*
import skip.foundation.*
import skip.model.*

/// Configuration for a game's "How to Play" instructions.
///
/// Games receive a `GameInstructionsConfig` from the app shell and use it to
/// (1) drive an "Instructions" entry in the pause menu and (2) auto-show
/// instructions the first time the user launches that specific game.
class GameInstructionsConfig {
    /// Localization key for the markdown body (e.g. "Drop7.instructions").
    val key: String
    /// Bundle the localization is stored in (typically the app shell module bundle).
    val bundle: Bundle
    /// UserDefaults key used to remember whether the user has already seen this game's instructions.
    val firstLaunchKey: String
    /// Localized title shown in the navigation bar of the instructions sheet.
    val title: String

    constructor(key: String, bundle: Bundle, firstLaunchKey: String, title: String) {
        this.key = key
        this.bundle = bundle
        this.firstLaunchKey = firstLaunchKey
        this.title = title
    }

    /// Whether the user has already seen this game's instructions at least once.
    fun hasShownToUser(): Boolean = UserDefaults.standard.bool(forKey = firstLaunchKey)

    /// Mark the instructions as shown so the auto-popup does not fire again.
    fun markShownToUser(): Unit = UserDefaults.standard.set(true, forKey = firstLaunchKey)

    override fun equals(other: Any?): Boolean {
        if (other !is GameInstructionsConfig) {
            return false
        }
        val lhs = this
        val rhs = other
        return lhs.key == rhs.key && lhs.firstLaunchKey == rhs.firstLaunchKey && lhs.title == rhs.title
    }

    override fun hashCode(): Int {
        var hasher = Hasher()
        hash(into = InOut<Hasher>({ hasher }, { hasher = it }))
        return hasher.finalize()
    }
    fun hash(into: InOut<Hasher>) {
        val hasher = into
        hasher.value.combine(key)
        hasher.value.combine(firstLaunchKey)
        hasher.value.combine(title)
    }

    @androidx.annotation.Keep
    companion object {
    }
}

/// A scrollable sheet that renders a localized markdown body for a game's
/// instructions, with a Done button.
class GameInstructionsView: View {
    internal val config: GameInstructionsConfig
    internal lateinit var dismiss: DismissAction

    constructor(config: GameInstructionsConfig) {
        this.config = config
    }

    override fun body(): View {
        return ComposeBuilder { composectx: ComposeContext ->
            NavigationStack { ->
                ComposeBuilder { composectx: ComposeContext ->
                    ScrollView { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            MarkdownBlocksView(text = localizedMarkdown)
                                .padding(Edge.Set.horizontal, 20.0)
                                .padding(Edge.Set.vertical, 16.0)
                                .frame(maxWidth = Double.infinity, alignment = Alignment.leading).Compose(composectx)
                            ComposeResult.ok
                        }
                    }
                    .navigationTitle(Text(verbatim = config.title))
                    .toolbar { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            ToolbarItem(placement = ToolbarItemPlacement.confirmationAction) { ->
                                ComposeBuilder { composectx: ComposeContext ->
                                    Button(action = { -> dismiss() }) { ->
                                        ComposeBuilder { composectx: ComposeContext ->
                                            Text(LocalizedStringKey(stringLiteral = "Done")).Compose(composectx)
                                            ComposeResult.ok
                                        }
                                    }.Compose(composectx)
                                    ComposeResult.ok
                                }
                            }.Compose(composectx)
                            ComposeResult.ok
                        }
                    }.Compose(composectx)
                    ComposeResult.ok
                }
            }.Compose(composectx)
        }
    }

    @Composable
    override fun Evaluate(context: ComposeContext, options: Int): kotlin.collections.List<Renderable> {
        this.dismiss = EnvironmentValues.shared.dismiss

        return super.Evaluate(context, options)
    }

    internal val localizedMarkdown: String
        get() {
            // Look up the markdown body in the configured bundle. NSLocalizedString
            // returns the key itself if the lookup fails, which is fine as a fallback.
            return NSLocalizedString(config.key, tableName = null, bundle = config.bundle, value = config.key, comment = "")
        }

    @androidx.annotation.Keep
    companion object {
    }
}

// MARK: - Minimal markdown block renderer

/// Parses a markdown string into a small set of block kinds and renders them.
/// Supports `# Heading`, `## Subheading`, `- Bullet`, blank-line paragraph
/// breaks, and inline `**bold**`. Anything else renders as a plain paragraph.
internal class MarkdownBlocksView: View {
    internal val blocks: Array<MarkdownBlock>

    internal constructor(text: String) {
        this.blocks = MarkdownParser.parse(text)
    }

    override fun body(): View {
        return ComposeBuilder { composectx: ComposeContext ->
            VStack(alignment = HorizontalAlignment.leading, spacing = 10.0) { ->
                ComposeBuilder { composectx: ComposeContext ->
                    ForEach(0..<blocks.count, id = { it }) { i ->
                        ComposeBuilder { composectx: ComposeContext ->
                            renderBlock(blocks[i]).Compose(composectx)
                            ComposeResult.ok
                        }
                    }.Compose(composectx)
                    ComposeResult.ok
                }
            }.Compose(composectx)
        }
    }

    internal fun renderBlock(block: MarkdownBlock): View {
        return ComposeBuilder { composectx: ComposeContext ->
            when (block.kind) {
                MarkdownBlockKind.heading1 -> {
                    inlineText(block.text)
                        .font(Font.title)
                        .fontWeight(Font.Weight.bold)
                        .padding(Edge.Set.top, 6.0)
                        .padding(Edge.Set.bottom, 2.0).Compose(composectx)
                }
                MarkdownBlockKind.heading2 -> {
                    inlineText(block.text)
                        .font(Font.title3)
                        .fontWeight(Font.Weight.semibold)
                        .padding(Edge.Set.top, 4.0).Compose(composectx)
                }
                MarkdownBlockKind.paragraph -> {
                    inlineText(block.text)
                        .font(Font.body).Compose(composectx)
                }
                MarkdownBlockKind.bullet -> {
                    HStack(alignment = VerticalAlignment.top, spacing = 8.0) { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            Text(LocalizedStringKey(stringLiteral = "•"))
                                .font(Font.body)
                                .foregroundStyle(Color.secondary).Compose(composectx)
                            inlineText(block.text)
                                .font(Font.body).Compose(composectx)
                            ComposeResult.ok
                        }
                    }.Compose(composectx)
                }
                MarkdownBlockKind.spacer -> Color.clear.frame(height = 4.0).Compose(composectx)
            }
            ComposeResult.ok
        }
    }

    /// Render a line of body text. Inline `**bold**` markers are stripped — the
    /// visual hierarchy comes from headings and bullets, which is enough for
    /// instructions and avoids relying on `Text + Text` concatenation that
    /// Skip's Kotlin renderer does not support.
    internal fun inlineText(s: String): Text = Text(verbatim = s.replacingOccurrences(of = "**", with = ""))
}

internal enum class MarkdownBlockKind {
    heading1,
    heading2,
    paragraph,
    bullet,
    spacer;
}

@Suppress("MUST_BE_INITIALIZED")
internal class MarkdownBlock: MutableStruct {
    internal var kind: MarkdownBlockKind
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    internal var text: String
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }

    constructor(kind: MarkdownBlockKind, text: String) {
        this.kind = kind
        this.text = text
    }

    override var supdate: ((Any) -> Unit)? = null
    override var smutatingcount = 0
    override fun scopy(): MutableStruct = MarkdownBlock(kind, text)
}

internal enum class MarkdownParser {
    ;

    @androidx.annotation.Keep
    companion object {
        internal fun parse(text: String): Array<MarkdownBlock> {
            var blocks: Array<MarkdownBlock> = arrayOf()
            val lines = text.components(separatedBy = "\n")
            var paragraphLines: Array<String> = arrayOf()

            fun flushParagraph() {
                if (!paragraphLines.isEmpty) {
                    val joined = paragraphLines.joined(separator = " ")
                    blocks.append(MarkdownBlock(kind = MarkdownBlockKind.paragraph, text = joined))
                    paragraphLines = arrayOf()
                }
            }

            var i = 0
            while (i < lines.count) {
                val raw = lines[i]
                val trimmed = raw.trimmingCharacters(in_ = CharacterSet.whitespaces)
                i += 1

                if (trimmed.isEmpty) {
                    flushParagraph()
                    blocks.append(MarkdownBlock(kind = MarkdownBlockKind.spacer, text = ""))
                    continue
                }
                if (trimmed.hasPrefix("## ")) {
                    flushParagraph()
                    val body = String(trimmed.dropFirst(3))
                    blocks.append(MarkdownBlock(kind = MarkdownBlockKind.heading2, text = body))
                    continue
                }
                if (trimmed.hasPrefix("# ")) {
                    flushParagraph()
                    val body = String(trimmed.dropFirst(2))
                    blocks.append(MarkdownBlock(kind = MarkdownBlockKind.heading1, text = body))
                    continue
                }
                if (trimmed.hasPrefix("- ")) {
                    flushParagraph()
                    val body = String(trimmed.dropFirst(2))
                    blocks.append(MarkdownBlock(kind = MarkdownBlockKind.bullet, text = body))
                    continue
                }
                paragraphLines.append(trimmed)
            }
            flushParagraph()
            return blocks.sref()
        }
    }
}
