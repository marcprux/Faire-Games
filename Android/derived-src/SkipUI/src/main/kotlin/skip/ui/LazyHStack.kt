package skip.ui

import skip.lib.*
import skip.lib.Set

// Copyright 2023–2026 Skip
// SPDX-License-Identifier: MPL-2.0
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@androidx.annotation.Keep
class LazyHStack: View, Renderable, skip.lib.SwiftProjecting {
    internal val alignment: VerticalAlignment
    internal val spacing: Double?
    internal val content: ComposeBuilder

    constructor(alignment: VerticalAlignment = VerticalAlignment.center, spacing: Double? = null, pinnedViews: PinnedScrollableViews = PinnedScrollableViews.of(), content: () -> View) {
        this.alignment = alignment
        this.spacing = spacing
        this.content = ComposeBuilder.from(content)
    }

    constructor(alignmentKey: String, spacing: Double?, bridgedPinnedViews: Int, bridgedContent: View) {
        this.alignment = VerticalAlignment(key = alignmentKey)
        this.spacing = spacing
        this.content = ComposeBuilder.from { -> bridgedContent }
    }

    @Composable
    override fun Render(context: ComposeContext) {
        // Let any parent scroll view know about our builtin scrolling. If there is a parent scroll
        // view that didn't already know, abort and wait for recompose to avoid fatal nested scroll error
        PreferenceValues.shared.contribute(context = context, key = BuiltinScrollAxisSetPreferenceKey::class, value = Axis.Set.horizontal)
        if (EnvironmentValues.shared._scrollAxes.contains(Axis.Set.horizontal)) {
            return
        }

        val rowAlignment = alignment.asComposeAlignment()
        val rowArrangement = Arrangement.spacedBy((spacing ?: 8.0).dp, alignment = androidx.compose.ui.Alignment.CenterHorizontally)
        val isScrollEnabled = EnvironmentValues.shared._scrollViewAxes.contains(Axis.Set.horizontal)
        val scrollAxes: Axis.Set = (if (isScrollEnabled) Axis.Set.horizontal else Axis.Set.of()).sref()
        val scrollTargetBehavior = EnvironmentValues.shared._scrollTargetBehavior.sref()

        val renderables = content.EvaluateLazyItems(level = 0, context = context)
        val itemCollector = remember { -> mutableStateOf(LazyItemCollector()) }
        // If we're in a vertical scrolling layout, attempting to fill height uses the intrinsic size
        // instead. But Compose crashes if you attempt to get the intrinsic height for components like
        // lists, so turn off fill behavior if vertically scrolling
        val fillHeight = !EnvironmentValues.shared._layoutScrollAxes.contains(Axis.Set.vertical)
        ComposeContainer(axis = Axis.horizontal, scrollAxes = scrollAxes, modifier = context.modifier, fillHeight = fillHeight) { modifier ->
            // Integrate with ScrollViewReader
            val listState = rememberLazyListState()
            val flingBehavior = if (scrollTargetBehavior is ViewAlignedScrollTargetBehavior) rememberSnapFlingBehavior(listState, SnapPosition.Start) else ScrollableDefaults.flingBehavior()
            val coroutineScope = rememberCoroutineScope()
            val scrollToID = ScrollToIDAction(key = listState) { id ->
                itemCollector.value.index(for_ = id)?.let { itemIndex ->
                    coroutineScope.launch { ->
                        if (Animation.isInWithAnimation) {
                            listState.animateScrollToItem(itemIndex)
                        } else {
                            listState.scrollToItem(itemIndex)
                        }
                    }
                }
            }
            PreferenceValues.shared.contribute(context = context, key = ScrollToIDPreferenceKey::class, value = scrollToID)

            // Observe scroll position changes and contribute them via preferences
            val scrollPositionState = remember { -> mutableStateOf<ScrollPositionState?>(null) }
            val currentItemCollector = rememberUpdatedState(itemCollector.value)
            LaunchedEffect(listState) { ->
                snapshotFlow { -> listState.firstVisibleItemIndex }
                    .distinctUntilChanged()
                    .collect { index ->
                        val id = currentItemCollector.value.id(for_ = index)
                        scrollPositionState.value = ScrollPositionState(id = id)
                    }
            }
            scrollPositionState.value.sref()?.let { scrollPosition ->
                PreferenceValues.shared.contribute(context = context, key = ScrollPositionPreferenceKey::class, value = scrollPosition)
            }

            EnvironmentValues.shared.setValues(l@{ it ->
                it.set_scrollTargetBehavior(null)
                return@l ComposeResult.ok
            }, in_ = { ->
                // Combine contentPadding with contentMargins additively
                var contentPadding = EnvironmentValues.shared._contentPadding.asPaddingValues()
                EnvironmentValues.shared._contentMargins?.asComposePaddingValues(for_ = ContentMarginPlacement.automatic)?.let { contentMargins ->
                    contentPadding = contentPadding.adding(contentMargins)
                }
                EnvironmentValues.shared.setValues(l@{ it ->
                    it.set_contentPadding(EdgeInsets())
                    it.set_horizontalStackVerticalAlignmentKey(alignment.key)
                    return@l ComposeResult.ok
                }, in_ = { ->
                    LazyRow(state = listState, modifier = modifier, horizontalArrangement = rowArrangement, verticalAlignment = rowAlignment, contentPadding = contentPadding, userScrollEnabled = isScrollEnabled, flingBehavior = flingBehavior) { ->
                        itemCollector.value.initialize(startItemIndex = 0, item = { renderable, _ ->
                            item { -> renderable.Render(context = context.content(scope = this)) }
                        }, indexedItems = { range, identifier, _, _, _, _, factory ->
                            val count = (range.endExclusive - range.start).sref()
                            val key: ((Int) -> String)? = if (identifier == null) null else { it -> composeBundleString(for_ = identifier!!(it + range.start)) }
                            items(count = count, key = key) { index -> factory(index + range.start, context.content(scope = this)).Render(context = context.content(scope = this)) }
                        }, objectItems = { objects, identifier, _, _, _, _, factory ->
                            val key: (Int) -> String = { it -> composeBundleString(for_ = identifier(objects[it])) }
                            items(count = objects.count, key = key) { index -> factory(objects[index], context.content(scope = this)).Render(context = context.content(scope = this)) }
                        }, objectBindingItems = { objectsBinding, identifier, _, _, _, _, _, factory ->
                            val key: (Int) -> String = { it -> composeBundleString(for_ = identifier(objectsBinding.wrappedValue[it])) }
                            items(count = objectsBinding.wrappedValue.count, key = key) { index -> factory(objectsBinding, index, context.content(scope = this)).Render(context = context.content(scope = this)) }
                        }, sectionHeader = { content ->
                            for (renderable in content.sref()) {
                                item { -> renderable.Render(context = context.content(scope = this)) }
                            }
                        }, sectionFooter = { content ->
                            for (renderable in content.sref()) {
                                item { -> renderable.Render(context = context.content(scope = this)) }
                            }
                        })
                        for (renderable in renderables.sref()) {
                            val matchtarget_0 = renderable as? LazyItemFactory
                            if (matchtarget_0 != null) {
                                val factory = matchtarget_0
                                if (factory.shouldProduceLazyItems()) {
                                    factory.produceLazyItems(collector = itemCollector.value, modifiers = listOf(), level = 0)
                                } else {
                                    itemCollector.value.item(renderable, 0)
                                }
                            } else {
                                itemCollector.value.item(renderable, 0)
                            }
                        }
                    }
                })
            })
        }
    }

    override fun Swift_projection(options: Int): () -> Any = Swift_projectionImpl(options)
    private external fun Swift_projectionImpl(options: Int): () -> Any

    @androidx.annotation.Keep
    companion object {
    }
}

