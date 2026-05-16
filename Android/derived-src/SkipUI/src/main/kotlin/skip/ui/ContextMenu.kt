package skip.ui

import skip.lib.*

// Copyright 2023–2026 Skip
// SPDX-License-Identifier: MPL-2.0
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.abs

/// Modifier that wraps content in a long-press-triggered dropdown menu.
internal open class ContextMenuModifier: RenderModifier {
    internal val menuItems: ComposeBuilder

    internal constructor(menuItems: ComposeBuilder): super() {
        this.menuItems = menuItems
        this.action = { content, context -> RenderContextMenu(content = content, context = context, menuItems = this.menuItems) }
    }
}

@Composable
internal fun RenderContextMenu(content: Renderable, context: ComposeContext, menuItems: ComposeBuilder) {
    val haptic = LocalHapticFeedback.current.sref()
    val isMenuExpanded = remember { -> mutableStateOf(false) }
    val nestedMenu = remember { -> mutableStateOf<Menu?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val contentContext = context.content()
    val isContextMenuEnabled = EnvironmentValues.shared.isEnabled && EnvironmentValues.shared._isHitTestingEnabled
    val replaceMenu: (Menu?) -> Unit = { menu ->
        coroutineScope.launch { ->
            delay(200)
            isMenuExpanded.value = false
            delay(100)
            nestedMenu.value = null
            if (menu != null) {
                nestedMenu.value = menu
                isMenuExpanded.value = true
            }
        }
    }
    ComposeContainer(eraseAxis = true, modifier = context.modifier) { modifier ->
        val interactionModifier: Modifier
        if (isContextMenuEnabled) {
            // Use pointerInput on the Initial pass to detect long press without consuming
            // events, so that child clickable handlers (e.g. Buttons) still receive taps.
            // Standard APIs like combinedClickable consume the down event on the Main pass,
            // which prevents nested clickables from firing.
            interactionModifier = modifier.pointerInput(true) { ->
                val slop = viewConfiguration.touchSlop.sref()
                awaitEachGesture { ->
                    val down = awaitPointerEvent(pass = PointerEventPass.Initial)
                    down.changes.firstOrNull({ it -> it.pressed })?.position.sref()?.let { start ->
                        val longPressed = withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) { ->
                            var active = true
                            while (active) {
                                val matchtarget_0 = awaitPointerEvent(pass = PointerEventPass.Initial).changes.firstOrNull()
                                if (matchtarget_0 != null) {
                                    val c = matchtarget_0
                                    active = c.pressed && abs(c.position.x - start.x) <= slop && abs(c.position.y - start.y) <= slop
                                } else {
                                    active = false
                                }
                            }
                        } == null
                        if (longPressed) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isMenuExpanded.value = true
                            // Consume remaining pointer events so the child's tap handler
                            // does not fire when the finger lifts
                            var pressed = true
                            while (pressed) {
                                val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                                event.changes.forEach { it -> it.consume() }
                                pressed = event.changes.any({ it -> it.pressed })
                            }
                        }
                    }
                }
            }
        } else {
            interactionModifier = modifier
        }
        Box(modifier = interactionModifier) { ->
            content.Render(context = contentContext)
            DropdownMenu(expanded = isMenuExpanded.value, onDismissRequest = { ->
                isMenuExpanded.value = false
                coroutineScope.launch { ->
                    delay(100)
                    nestedMenu.value = null
                }
            }) { ->
                var placement = EnvironmentValues.shared._placement.sref()
                EnvironmentValues.shared.setValues(l@{ it ->
                    placement.remove(ViewPlacement.toolbar)
                    it.set_placement(placement)
                    return@l ComposeResult.ok
                }, in_ = { ->
                    val renderables = (nestedMenu.value?.content ?: menuItems).Evaluate(context = contentContext, options = 0)
                    Menu.RenderDropdownMenuItems(for_ = renderables, context = contentContext, replaceMenu = replaceMenu)
                })
            }
        }
    }
}
