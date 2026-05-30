package skip.ui

import skip.lib.*

// Copyright 2023–2026 Skip
// SPDX-License-Identifier: MPL-2.0
import skip.foundation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat


/// Log layout constraints for debugging purposes.
///
/// - Parameter tag: The log tag to use (default: "LogLayout").
/// - Returns: A modifier that logs layout constraints and bounds.
///
fun Modifier.logLayout(tag: String = "LogLayout"): Modifier = this.logLayoutModifier(tag = tag)

internal class AspectRatioModifier: RenderModifier {
    internal val ratio: Double?
    internal val contentMode: ContentMode

    internal constructor(ratio: Double?, contentMode: ContentMode): super() {
        this.ratio = ratio
        this.contentMode = contentMode
        this.action = { renderable, context ->
            val stripped = renderable.strip()
            if (stripped is Image || stripped is AsyncImage || ratio == null) {
                // Image has its own support for aspect ratios, and we allow the loaded Image in AsyncImage
                // to consume the modifier too
                EnvironmentValues.shared.setValues(l@{ it ->
                    it.set_aspectRatio(Tuple2(ratio, contentMode))
                    return@l ComposeResult.ok
                }, in_ = { -> renderable.Render(context = context) })
            } else {
                var context = context.sref()
                context.modifier = context.modifier.aspectRatio(Float(ratio))
                renderable.Render(context = context)
            }
        }
    }
}

internal class DisabledModifier: EnvironmentModifier {
    internal val disabled: Boolean

    internal constructor(disabled: Boolean): super() {
        this.disabled = disabled
        this.action = l@{ it ->
            it.setisEnabled(!disabled)
            return@l ComposeResult.ok
        }
    }
}

internal class PaddingModifier: RenderModifier {
    internal val insets: EdgeInsets

    internal constructor(insets: EdgeInsets): super(role = ModifierRole.spacing) {
        this.insets = insets.sref()
        this.action = { renderable, context ->
            val topAnim = Float(insets.top).asAnimatable(context = context)
            val leadingAnim = Float(insets.leading).asAnimatable(context = context)
            val bottomAnim = Float(insets.bottom).asAnimatable(context = context)
            val trailingAnim = Float(insets.trailing).asAnimatable(context = context)
            val animatedInsets = EdgeInsets(top = Double(topAnim.value), leading = Double(leadingAnim.value), bottom = Double(bottomAnim.value), trailing = Double(trailingAnim.value))
            val stripped = renderable.strip()
            if ((stripped is LazyVGrid || stripped is LazyHGrid || stripped is LazyVStack || stripped is LazyHStack) && renderable.forEachModifier(perform = { it -> if (it.role == ModifierRole.spacing) true else null }) == null) {
                // Certain views apply their padding themselves
                EnvironmentValues.shared.setValues(l@{ it ->
                    it.set_contentPadding(animatedInsets)
                    return@l ComposeResult.ok
                }, in_ = { -> renderable.Render(context = context) })
            } else {
                PaddingLayout(content = renderable, padding = animatedInsets, context = context)
            }
        }
    }
}

/// Used to mark views with a tag or ID.
internal class TagModifier: RenderModifier {

    internal val value: Any?
    internal var stateSaver: ComposeStateSaver? = null

    internal constructor(value: Any?, role: ModifierRole): super(role = role) {
        this.value = value.sref()
    }

    @Composable
    override fun Evaluate(content: View, context: ComposeContext, options: Int): kotlin.collections.List<Renderable>? {
        val matchtarget_0 = Companion.IdStateSaver(for_ = context, role = role, value = value)
        if (matchtarget_0 != null) {
            val stateSaver = matchtarget_0
            this.stateSaver = stateSaver
            var context = context.sref()
            context.stateSaver = stateSaver
            // Use key() to reset remembered values that do not use the state saver
            return androidx.compose.runtime.key(value ?: Companion.defaultIdValue) l@{ -> return@l super.Evaluate(content = content, context = context, options = options) }
        } else {
            this.stateSaver = null
            return super.Evaluate(content = content, context = context, options = options)
        }
    }

    @Composable
    override fun Render(content: Renderable, context: ComposeContext) {
        val matchtarget_1 = stateSaver
        if (matchtarget_1 != null) {
            val stateSaver = matchtarget_1
            var context = context.sref()
            context.stateSaver = stateSaver
            androidx.compose.runtime.key(value ?: Companion.defaultIdValue) { -> super.Render(content = content, context = context) }
        } else {
            super.Render(content = content, context = context)
        }
    }

    @androidx.annotation.Keep
    companion object {
        internal val defaultIdValue = "<TagModifier.defaultIdValue>"

        @Composable
        private fun IdStateSaver(for_: ComposeContext, role: ModifierRole, value: Any?): ComposeStateSaver? {
            val context = for_
            if (role != ModifierRole.id) {
                return null
            }
            // Reset the state saver when the id value changes
            val idValue = value ?: Companion.defaultIdValue
            val rememberedId = rememberSaveable(stateSaver = context.stateSaver as Saver<Any, Any>) { -> mutableStateOf(idValue) }
            if (rememberedId.value == idValue) {
                return context.stateSaver as? ComposeStateSaver
            }
            rememberedId.value = idValue
            return ComposeStateSaver()
        }

        /// Extract the existing tag modifier view from the given view's modifiers.
        internal fun on(content: Renderable, role: ModifierRole): TagModifier? {
            return content.forEachModifier l@{ it ->
                if (it.role == role) {
                    return@l it as? TagModifier
                } else {
                    return@l null
                }
            }
        }
    }
}

/// Use a special modifier for `zIndex` so that the artificial parent container created by `.frame` can
/// pull the `zIndex` value into its own modifiers.
///
/// Otherwise the extra frame container hides the `zIndex` value from this view's logical parent container.
///
/// - Seealso: `FrameLayout`
internal class ZIndexModifier: RenderModifier {
    private val zIndex: Double
    private var isConsumed = false

    internal constructor(zIndex: Double): super() {
        this.zIndex = zIndex
        this.action = { renderable, context ->
            var context = context.sref()
            if (!isConsumed) {
                context.modifier = context.modifier.zIndex(Float(zIndex))
            }
            renderable.Render(context = context)
        }
    }

    @androidx.annotation.Keep
    companion object {

        /// Move the application of the `zIndex` to the given modifier, erasing it from this view.
        internal fun consume(for_: Renderable, with: Modifier): Modifier {
            val renderable = for_
            val modifier = with
            val matchtarget_2 = renderable.forEachModifier(perform = l@{ it ->
                val matchtarget_3 = it as? ZIndexModifier
                if (matchtarget_3 != null) {
                    val zIndexModifier = matchtarget_3
                    zIndexModifier.isConsumed = true
                    return@l zIndexModifier.zIndex
                } else {
                    return@l null
                }
            })
            if (matchtarget_2 != null) {
                val zIndex = matchtarget_2
                return modifier.zIndex(Float(zIndex))
            } else {
                return modifier
            }
        }
    }
}

/// Animated border modifier that animates both the shape style and border width.
internal class AnimatedBorderModifier: RenderModifier {
    internal val style: ShapeStyle
    internal val width: Double

    internal constructor(style: ShapeStyle, width: Double): super() {
        this.style = style.sref()
        this.width = width
    }

    @Composable
    override fun Render(content: Renderable, context: ComposeContext) {
        // Animate the border width
        val animatedWidth = Float(width).asAnimatable(context = context).value.sref()

        // Apply the border with animated width
        // ShapeStyle animation is handled by asBrush/asColor via animationContext
        var context = context.sref()
        val matchtarget_4 = style.asColor(opacity = 1.0, animationContext = context)
        if (matchtarget_4 != null) {
            val color = matchtarget_4
            context.modifier = context.modifier.border(width = animatedWidth.dp, color = color)
        } else {
            style.asBrush(opacity = 1.0, animationContext = context)?.let { brush ->
                context.modifier = context.modifier.border(BorderStroke(width = animatedWidth.dp, brush = brush))
            }
        }
        content.Render(context = context)
    }
}

