package skip.ui

import skip.lib.*
import skip.lib.Array
import skip.lib.Set

// Copyright 2023–2026 Skip
// SPDX-License-Identifier: MPL-2.0
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@androidx.annotation.Keep
class LazyVGrid: View, Renderable, skip.lib.SwiftProjecting {
    internal val columns: Array<GridItem>
    internal val alignment: HorizontalAlignment
    internal val spacing: Double?
    internal val content: ComposeBuilder

    constructor(columns: Array<GridItem>, alignment: HorizontalAlignment = HorizontalAlignment.center, spacing: Double? = null, pinnedViews: PinnedScrollableViews = PinnedScrollableViews.of(), content: () -> View) {
        this.columns = columns.sref()
        this.alignment = alignment
        this.spacing = spacing
        this.content = ComposeBuilder.from(content)
    }

    constructor(columns: Array<GridItem>, alignmentKey: String, spacing: Double?, bridgedPinnedViews: Int, bridgedContent: View) {
        this.columns = columns.sref()
        this.alignment = HorizontalAlignment(key = alignmentKey)
        this.spacing = spacing
        this.content = ComposeBuilder.from { -> bridgedContent }
        // Note: `bridgedPinnedViews` is ignored
    }

    @Composable
    override fun Render(context: ComposeContext) {
        // Let any parent scroll view know about our builtin scrolling. If there is a parent scroll
        // view that didn't already know, abort and wait for recompose to avoid fatal nested scroll error
        PreferenceValues.shared.contribute(context = context, key = BuiltinScrollAxisSetPreferenceKey::class, value = Axis.Set.vertical)
        if (EnvironmentValues.shared._scrollAxes.contains(Axis.Set.vertical)) {
            return
        }

        val (gridCells, cellAlignment, horizontalSpacing) = GridItem.asGridCells(items = columns)
        val boxAlignment = (cellAlignment?.asComposeAlignment() ?: androidx.compose.ui.Alignment.Center).sref()
        val horizontalArrangement = Arrangement.spacedBy((horizontalSpacing ?: 8.0).dp, alignment = alignment.asComposeAlignment())
        val verticalArrangement = Arrangement.spacedBy((spacing ?: 8.0).dp)
        val isScrollEnabled = EnvironmentValues.shared._scrollViewAxes.contains(Axis.Set.vertical)
        val scrollAxes: Axis.Set = (if (isScrollEnabled) Axis.Set.vertical else Axis.Set.of()).sref()
        val scrollTargetBehavior = EnvironmentValues.shared._scrollTargetBehavior.sref()

        val searchableState = EnvironmentValues.shared._searchableState
        val isSearchable = searchableState?.isOnNavigationStack == false

        val renderables = content.EvaluateLazyItems(level = 0, context = context)
        val itemCollector = remember { -> mutableStateOf(LazyItemCollector()) }
        ComposeContainer(axis = Axis.vertical, scrollAxes = scrollAxes, modifier = context.modifier, fillWidth = true) { modifier ->
            IgnoresSafeAreaLayout(expandInto = Edge.Set.of(), checkEdges = Edge.Set.of(Edge.Set.bottom), modifier = modifier, logTag = "LazyVGrid") { _, safeAreaEdges ->
                // Integrate with our scroll-to-top and ScrollViewReader
                val gridState = rememberLazyGridState(initialFirstVisibleItemIndex = if (isSearchable) 1 else 0)
                val flingBehavior = if (scrollTargetBehavior is ViewAlignedScrollTargetBehavior) rememberSnapFlingBehavior(gridState, SnapPosition.Start) else ScrollableDefaults.flingBehavior()
                val coroutineScope = rememberCoroutineScope()
                PreferenceValues.shared.contribute(context = context, key = ScrollToTopPreferenceKey::class, value = ScrollToTopAction(key = gridState) { ->
                    coroutineScope.launch { -> gridState.animateScrollToItem(0) }
                })
                val scrollToID = ScrollToIDAction(key = gridState) { id ->
                    itemCollector.value.index(for_ = id)?.let { itemIndex ->
                        coroutineScope.launch { ->
                            if (Animation.isInWithAnimation) {
                                gridState.animateScrollToItem(itemIndex)
                            } else {
                                gridState.scrollToItem(itemIndex)
                            }
                        }
                    }
                }
                PreferenceValues.shared.contribute(context = context, key = ScrollToIDPreferenceKey::class, value = scrollToID)
                if (safeAreaEdges.contains(Edge.Set.bottom)) {
                    PreferenceValues.shared.contribute(context = context, key = ToolbarPreferenceKey::class, value = ToolbarPreferences(scrollableState = gridState, for_ = arrayOf(ToolbarPlacement.bottomBar)))
                    PreferenceValues.shared.contribute(context = context, key = TabBarPreferenceKey::class, value = ToolbarBarPreferences(scrollableState = gridState))
                }

                // Observe scroll position changes and contribute them via preferences
                val scrollPositionState = remember { -> mutableStateOf<ScrollPositionState?>(null) }
                val currentItemCollector = rememberUpdatedState(itemCollector.value)
                LaunchedEffect(gridState) { ->
                    snapshotFlow { -> gridState.firstVisibleItemIndex }
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
                        return@l ComposeResult.ok
                    }, in_ = { ->
                        LazyVerticalGrid(state = gridState, modifier = Modifier.fillMaxWidth(), columns = gridCells, horizontalArrangement = horizontalArrangement, verticalArrangement = verticalArrangement, contentPadding = contentPadding, userScrollEnabled = isScrollEnabled, flingBehavior = flingBehavior) { ->
                            itemCollector.value.initialize(startItemIndex = if (isSearchable) 1 else 0, item = { renderable, _ ->
                                item { ->
                                    Box(contentAlignment = boxAlignment) { -> renderable.Render(context = context.content(scope = this)) }
                                }
                            }, indexedItems = { range, identifier, _, _, _, _, factory ->
                                val count = (range.endExclusive - range.start).sref()
                                val key: ((Int) -> String)? = if (identifier == null) null else { it -> composeBundleString(for_ = identifier!!(it + range.start)) }
                                items(count = count, key = key) { index ->
                                    Box(contentAlignment = boxAlignment) { ->
                                        val scopedContext = context.content(scope = this)
                                        factory(index + range.start, scopedContext).Render(context = scopedContext)
                                    }
                                }
                            }, objectItems = { objects, identifier, _, _, _, _, factory ->
                                val key: (Int) -> String = { it -> composeBundleString(for_ = identifier(objects[it])) }
                                items(count = objects.count, key = key) { index ->
                                    Box(contentAlignment = boxAlignment) { ->
                                        val scopedContext = context.content(scope = this)
                                        factory(objects[index], scopedContext).Render(context = scopedContext)
                                    }
                                }
                            }, objectBindingItems = { objectsBinding, identifier, _, _, _, _, _, factory ->
                                val key: (Int) -> String = { it -> composeBundleString(for_ = identifier(objectsBinding.wrappedValue[it])) }
                                items(count = objectsBinding.wrappedValue.count, key = key) { index ->
                                    Box(contentAlignment = boxAlignment) { ->
                                        val scopedContext = context.content(scope = this)
                                        factory(objectsBinding, index, scopedContext).Render(context = scopedContext)
                                    }
                                }
                            }, sectionHeader = { content ->
                                for (renderable in content.sref()) {
                                    item(span = { -> GridItemSpan(maxLineSpan) }) { ->
                                        Box(contentAlignment = androidx.compose.ui.Alignment.Center) { -> renderable.Render(context = context.content(scope = this)) }
                                    }
                                }
                            }, sectionFooter = { content ->
                                for (renderable in content.sref()) {
                                    item(span = { -> GridItemSpan(maxLineSpan) }) { ->
                                        Box(contentAlignment = androidx.compose.ui.Alignment.Center) { -> renderable.Render(context = context.content(scope = this)) }
                                    }
                                }
                            })
                            if (isSearchable) {
                                item(span = { -> GridItemSpan(maxLineSpan) }) { ->
                                    // We use the same logic in LazyVStack
                                    val modifier = Modifier
                                        .ignoreHorizontalContentPadding(start = contentPadding.asEdgeInsets().leading.dp, end = contentPadding.asEdgeInsets().trailing.dp)
                                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
                                        .fillMaxWidth()
                                    SearchField(state = searchableState!!, context = context.content(modifier = modifier, scope = this))
                                }
                            }
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
    }

    override fun Swift_projection(options: Int): () -> Any = Swift_projectionImpl(options)
    private external fun Swift_projectionImpl(options: Int): () -> Any

    @androidx.annotation.Keep
    companion object {
    }
}

