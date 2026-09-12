package skip.ui

import skip.lib.*
import skip.lib.Array
import skip.lib.MutableCollection
import skip.lib.Set

// Copyright 2023–2026 Skip
// SPDX-License-Identifier: MPL-2.0
import skip.foundation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animate
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.gestures.snapTo
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.PullRefreshState
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.SwipeToDismissBoxDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path.Companion.combine
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.zIndex
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.ReorderableLazyListState
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

/// Corner radius for list sections.
internal val listSectionCornerRadius = 8.0

/// Discrete rest positions for a row's swipe gesture. Used as the value
/// type for the row's AnchoredDraggableState.
internal enum class SwipeAnchor {
    closed,
    leadingOpen,
    leadingFull,
    trailingOpen,
    trailingFull;
}

@Stable // Otherwise Compose recomposes all internal @Composable funcs because 'this' is unstable
@androidx.annotation.Keep
class List: View, Renderable, skip.lib.SwiftProjecting {
    internal val fixedContent: ComposeBuilder?
    internal val forEach: ForEach?
    internal val itemTransformer: ((Renderable) -> Renderable)?

    internal constructor(fixedContent: View? = null, identifier: ((Any) -> AnyHashable?)? = null, itemTransformer: ((Renderable) -> Renderable)? = null, indexRange: IntRange? = null, indexedContent: ((Int) -> View)? = null, objects: RandomAccessCollection<Any>? = null, objectContent: ((Any) -> View)? = null, objectsBinding: Binding<RandomAccessCollection<Any>>? = null, objectsBindingContent: ((Binding<RandomAccessCollection<Any>>, Int) -> View)? = null, editActions: EditActions = EditActions.of()) {
        if (fixedContent != null) {
            this.fixedContent = fixedContent as? ComposeBuilder ?: ComposeBuilder(view = fixedContent)
        } else {
            this.fixedContent = null
        }
        if (indexRange != null) {
            this.forEach = ForEach(identifier = identifier, indexRange = { -> indexRange }, indexedContent = indexedContent)
        } else if (objects != null) {
            this.forEach = ForEach(identifier = identifier, objects = objects, objectContent = objectContent)
        } else if (objectsBinding != null) {
            this.forEach = ForEach(identifier = identifier, objectsBinding = objectsBinding, objectsBindingContent = objectsBindingContent, editActions = editActions)
        } else {
            this.forEach = null
        }
        this.itemTransformer = itemTransformer
    }

    constructor(content: () -> View): this(fixedContent = content()) {
    }

    @Deprecated("This API is not yet available in Skip. Consider placing it within a #if !SKIP block. You can file an issue against the owning library at https://github.com/skiptools, or see the library README for information on adding support", level = DeprecationLevel.ERROR)
    constructor(selection: Binding<Any>, content: () -> View): this(fixedContent = content()) {
    }

    constructor(bridgedContent: View) {
        val matchtarget_0 = bridgedContent as? ForEach
        if (matchtarget_0 != null) {
            val forEach = matchtarget_0
            this.fixedContent = null
            this.forEach = forEach
        } else {
            this.fixedContent = ComposeBuilder.from { -> bridgedContent }
            this.forEach = null
        }
        this.itemTransformer = null
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    override fun Render(context: ComposeContext) {
        val style = EnvironmentValues.shared._listStyle ?: ListStyle.automatic
        val backgroundVisibility = EnvironmentValues.shared._scrollContentBackground ?: Visibility.visible
        val styling = ListStyling(style = style, backgroundVisibility = backgroundVisibility)
        val itemContext = context.content()

        // When we layout, extend into safe areas that are due to system bars, not into any app chrome
        val safeArea = EnvironmentValues.shared._safeArea
        var ignoresSafeAreaEdges: Edge.Set = Edge.Set.of(Edge.Set.top, Edge.Set.bottom)
        ignoresSafeAreaEdges.formIntersection(safeArea?.absoluteSystemBarEdges ?: Edge.Set.of())
        ComposeContainer(scrollAxes = Axis.Set.vertical, modifier = context.modifier, fillWidth = true, fillHeight = true, then = Modifier.background(BackgroundColor(styling = styling, isItem = false))) { modifier ->
            IgnoresSafeAreaLayout(expandInto = ignoresSafeAreaEdges, checkEdges = Edge.Set.of(Edge.Set.bottom), modifier = modifier, logTag = "List") { safeAreaExpansion, safeAreaEdges ->
                var containerModifier: Modifier
                val refreshing = remember { -> mutableStateOf(false) }
                val refreshAction = EnvironmentValues.shared.refresh
                val refreshState: PullRefreshState?
                if (refreshAction != null) {
                    val refreshScope = rememberCoroutineScope()
                    val updatedAction = rememberUpdatedState(refreshAction)
                    refreshState = rememberPullRefreshState(refreshing.value, { ->
                        refreshScope.launch { ->
                            refreshing.value = true
                            updatedAction.value()
                            refreshing.value = false
                        }
                    })
                    containerModifier = modifier.pullRefresh(refreshState!!)
                } else {
                    refreshState = null
                    containerModifier = modifier
                }
                containerModifier = containerModifier.scrollDismissesKeyboardMode(EnvironmentValues.shared.scrollDismissesKeyboardMode)

                Box(modifier = containerModifier) { ->
                    val density = LocalDensity.current.sref()
                    val headerSafeAreaHeight = with(density) { -> safeAreaExpansion.top.toDp() }
                    val footerSafeAreaHeight = with(density) { -> safeAreaExpansion.bottom.toDp() }
                    RenderList(context = itemContext, styling = styling, arguments = ListArguments(headerSafeAreaHeight = headerSafeAreaHeight, footerSafeAreaHeight = footerSafeAreaHeight, safeAreaEdges = safeAreaEdges))
                    if (refreshState != null) {
                        PullRefreshIndicator(refreshing.value, refreshState, Modifier.align(androidx.compose.ui.Alignment.TopCenter))
                    }
                }
            }
        }
    }

    @Composable
    private fun RenderList(context: ComposeContext, styling: ListStyling, arguments: ListArguments) {
        val renderables: kotlin.collections.List<Renderable>
        if (forEach != null) {
            renderables = forEach.EvaluateLazyItems(level = 0, context = context)
        } else if (fixedContent != null) {
            renderables = fixedContent.EvaluateLazyItems(level = 0, context = context)
        } else {
            renderables = listOf()
        }

        var modifier = context.modifier
        if (styling.style != ListStyle.plain) {
            modifier = modifier.padding(start = Companion.horizontalInset.dp, end = Companion.horizontalInset.dp)
        }
        modifier = modifier.fillMaxSize()

        val searchableState = EnvironmentValues.shared._searchableState
        val isSearchable = searchableState?.isOnNavigationStack == false

        val hasHeader = styling.style != ListStyle.plain || (!isSearchable && arguments.headerSafeAreaHeight.value > 0)
        val hasFooter = styling.style != ListStyle.plain || arguments.footerSafeAreaHeight.value > 0

        // Remember the factory because we use it in the remembered reorderable state
        val itemCollector = remember { -> mutableStateOf(LazyItemCollector()) }
        val moveTrigger = remember { -> mutableStateOf(0) }
        val listState = rememberLazyListState(initialFirstVisibleItemIndex = if (isSearchable && arguments.headerSafeAreaHeight.value <= 0) 1 else 0)
        val reorderableState = rememberReorderableLazyListState(listState = listState, onMove = { from, to ->
            // Trigger recompose on move, but don't read the trigger state until we're inside the list content to limit its scope
            itemCollector.value.move(from = from.index, to = to.index, trigger = { it -> moveTrigger.value = it })
        }, onDragEnd = { _, _ -> itemCollector.value.commitMove() }, canDragOver = { candidate, dragging -> itemCollector.value.canMove(from = dragging.index, to = candidate.index) })
        modifier = modifier.reorderable(reorderableState)

        // Integrate with our scroll-to-top and ScrollViewReader
        val coroutineScope = rememberCoroutineScope()
        PreferenceValues.shared.contribute(context = context, key = ScrollToTopPreferenceKey::class, value = ScrollToTopAction(key = reorderableState.listState) { ->
            coroutineScope.launch { -> reorderableState.listState.animateScrollToItem(0) }
        })
        val scrollToID = ScrollToIDAction(key = reorderableState.listState) { id ->
            itemCollector.value.index(for_ = id)?.let { itemIndex ->
                coroutineScope.launch { ->
                    if (Animation.isInWithAnimation) {
                        reorderableState.listState.animateScrollToItem(itemIndex)
                    } else {
                        reorderableState.listState.scrollToItem(itemIndex)
                    }
                }
            }
        }
        PreferenceValues.shared.contribute(context = context, key = ScrollToIDPreferenceKey::class, value = scrollToID)
        val isSystemBackground = styling.style != ListStyle.plain && styling.backgroundVisibility != Visibility.hidden
        // We contribute top bar preferences even without knowing we're safe area-adjacent for multiple reasons:
        // - When there is a search bar we may not be adjacent to the top safe area, but we should act like it
        // - An expanding nav bar can causes issues detecting safe area adjacency
        // - It is unlikely that anyone will use a grouped-style list that is not top-bar adjacent, so the top
        //   bar should always have the grouped-style system color
        PreferenceValues.shared.contribute(context = context, key = ToolbarPreferenceKey::class, value = ToolbarPreferences(isSystemBackground = isSystemBackground, scrollableState = listState, for_ = arrayOf(ToolbarPlacement.navigationBar)))
        if (arguments.safeAreaEdges.contains(Edge.Set.bottom)) {
            PreferenceValues.shared.contribute(context = context, key = ToolbarPreferenceKey::class, value = ToolbarPreferences(isSystemBackground = isSystemBackground, scrollableState = listState, for_ = arrayOf(ToolbarPlacement.bottomBar)))
            PreferenceValues.shared.contribute(context = context, key = TabBarPreferenceKey::class, value = ToolbarBarPreferences(isSystemBackground = isSystemBackground, scrollableState = listState))
        }

        // List item animations in Compose work by setting the `animateItemPlacement` modifier on the items. Critically,
        // this must be done when the items are composed *prior* to any animated change. So by default we compose all items
        // with `animateItemPlacement`. If the entire List is recomposed without an animation in progress (e.g. an unanimated
        // data change), we recompose without animation, then after some time to complete the recompose we flip back to the
        // animated state in anticipation of the next, potentially animated, update
        val forceUnanimatedItems = remember { -> mutableStateOf(false) }
        if (Animation.current(isAnimating = false) == null) {
            forceUnanimatedItems.value = true
            LaunchedEffect(System.currentTimeMillis()) { ->
                delay(300)
                forceUnanimatedItems.value = false
            }
        } else {
            forceUnanimatedItems.value = false
        }

        val itemContext = context.content()
        /* Tracks which row currently has its swipe actions revealed. When one
        opens, all others observe this state and animate closed. Matches
        iOS list behavior of "only one row's swipe actions visible at once". */
        val activeSwipeKey = remember { -> mutableStateOf<String?>(null) }
        // Combine contentPadding with contentMargins additively
        var contentPadding = EnvironmentValues.shared._contentPadding.asPaddingValues()
        EnvironmentValues.shared._contentMargins?.asComposePaddingValues(for_ = ContentMarginPlacement.automatic)?.let { contentMargins ->
            contentPadding = contentPadding.adding(contentMargins)
        }
        val listRowSpacing = EnvironmentValues.shared._listRowSpacing
        val listVerticalArrangement = (if (listRowSpacing != null) Arrangement.spacedBy(listRowSpacing!!.dp) else Arrangement.Top).sref()
        LazyColumn(state = reorderableState.listState, modifier = modifier, contentPadding = contentPadding, verticalArrangement = listVerticalArrangement) { ->
            // Read move trigger here so that a move will recompose list content
            moveTrigger.value.sref()
            val shouldAnimateItems: @Composable () -> Boolean = l@{ ->
                // We disable animation to prevent filtered items from animating when they return
                val animate = !forceUnanimatedItems.value && EnvironmentValues.shared._searchableState?.isSearching?.value != true
                return@l animate
            }

            // Initialize the factory context with closures that use the LazyListScope to generate items
            var startItemIndex = if (hasHeader) 1 else 0 // Header inset
            if (isSearchable) {
                startItemIndex += 1 // Search field
            }
            itemCollector.value.initialize(startItemIndex = startItemIndex, item = { renderable, level ->
                item { ->
                    val itemModifier: Modifier = if (shouldAnimateItems()) Modifier.animateItem() else Modifier
                    RenderItem(content = renderable, level = level, context = itemContext, modifier = itemModifier, styling = styling)
                }
            }, indexedItems = { range, identifier, offset, onDelete, onMove, level, factory ->
                val count = (range.endExclusive - range.start).sref()
                val key: ((Int) -> String)? = if (identifier == null) null else { it -> composeBundleString(for_ = identifier!!(range.start + itemCollector.value.remapIndex(it, from = offset))) }
                items(count = count, key = key) { index ->
                    val keyValue = key?.invoke(index) // Key closure already remaps index
                    val index = itemCollector.value.remapIndex(index, from = offset)
                    val itemModifier: Modifier = if (shouldAnimateItems()) Modifier.animateItem() else Modifier
                    val renderable = factory(index + range.start, itemContext)
                    RenderEditableItem(content = renderable, level = level, context = itemContext, modifier = itemModifier, styling = styling, key = keyValue, index = index, onDelete = onDelete, onMove = onMove, reorderableState = reorderableState, activeSwipeKey = activeSwipeKey)
                }
            }, objectItems = { objects, identifier, offset, onDelete, onMove, level, factory ->
                val key: (Int) -> String = { it -> composeBundleString(for_ = identifier(objects[itemCollector.value.remapIndex(it, from = offset)])) }
                items(count = objects.count, key = key) { index ->
                    val keyValue = key(index) // Key closure already remaps index
                    val index = itemCollector.value.remapIndex(index, from = offset)
                    val itemModifier: Modifier = if (shouldAnimateItems()) Modifier.animateItem() else Modifier
                    val renderable = factory(objects[index], itemContext)
                    RenderEditableItem(content = renderable, level = level, context = itemContext, modifier = itemModifier, styling = styling, key = keyValue, index = index, onDelete = onDelete, onMove = onMove, reorderableState = reorderableState, activeSwipeKey = activeSwipeKey)
                }
            }, objectBindingItems = { objectsBinding, identifier, offset, editActions, onDelete, onMove, level, factory ->
                val key: (Int) -> String = { it -> composeBundleString(for_ = identifier(objectsBinding.wrappedValue[itemCollector.value.remapIndex(it, from = offset)])) }
                items(count = objectsBinding.wrappedValue.count, key = key) { index ->
                    val keyValue = key(index) // Key closure already remaps index
                    val index = itemCollector.value.remapIndex(index, from = offset)
                    val itemModifier: Modifier = if (shouldAnimateItems()) Modifier.animateItem() else Modifier
                    val renderable = factory(objectsBinding, index, itemContext)
                    RenderEditableItem(content = renderable, level = level, context = itemContext, modifier = itemModifier, styling = styling, objectsBinding = objectsBinding, key = keyValue, index = index, editActions = editActions, onDelete = onDelete, onMove = onMove, reorderableState = reorderableState, activeSwipeKey = activeSwipeKey)
                }
            }, sectionHeader = { content ->
                val headerRenderables = (if (content.size == 0) listOf(EmptyView()) else content).sref()
                val firstRenderable = (renderables.firstOrNull() as? LazySectionHeader)?.content?.firstOrNull()
                val isTop = firstRenderable === headerRenderables.firstOrNull()
                for (renderable in headerRenderables.sref()) {
                    if (styling.style == ListStyle.plain) {
                        stickyHeader { _ -> RenderSectionHeader(content = renderable, context = itemContext, styling = styling, isTop = isTop) }
                    } else {
                        item { -> RenderSectionHeader(content = renderable, context = itemContext, styling = styling, isTop = isTop) }
                    }
                }
            }, sectionFooter = { content ->
                val footerRenderables = (if (content.size == 0) listOf(EmptyView()) else content).sref()
                for (renderable in footerRenderables.sref()) {
                    item { -> RenderSectionFooter(content = renderable, context = itemContext, styling = styling) }
                }
            })

            if (isSearchable) {
                item { -> RenderSearchField(state = searchableState!!, context = context, styling = styling, safeAreaHeight = arguments.headerSafeAreaHeight) }
            }
            if (hasHeader) {
                val hasTopSection = renderables.firstOrNull() is LazySectionHeader
                item { -> RenderHeader(styling = styling, safeAreaHeight = if (isSearchable) 0.dp else arguments.headerSafeAreaHeight, hasTopSection = hasTopSection) }
            }
            for (renderable in renderables.sref()) {
                val matchtarget_1 = renderable as? LazyItemFactory
                if (matchtarget_1 != null) {
                    val factory = matchtarget_1
                    if (factory.shouldProduceLazyItems()) {
                        factory.produceLazyItems(collector = itemCollector.value, modifiers = listOf(), level = 0)
                    } else {
                        itemCollector.value.item(renderable, 0)
                    }
                } else {
                    itemCollector.value.item(renderable, 0)
                }
            }
            if (hasFooter) {
                val hasBottomSection = renderables.lastOrNull() is LazySectionFooter
                item { -> RenderFooter(styling = styling, safeAreaHeight = arguments.footerSafeAreaHeight, hasBottomSection = hasBottomSection) }
            }
        }
    }

    @Composable
    private fun RenderItem(content: Renderable, level: Int, context: ComposeContext, modifier: Modifier = Modifier, styling: ListStyling, isItem: Boolean = true) {
        if (content.isSwiftUIEmptyView) {
            return
        }

        val itemRenderable = (itemTransformer?.invoke(content) ?: content).sref()
        val listItemModifier = ListItemModifier.combined(for_ = itemRenderable)
        var itemModifier: Modifier = Modifier
        if (listItemModifier?.background == null) {
            itemModifier = itemModifier.background(BackgroundColor(styling = styling.withStyle(ListStyle.plain), isItem = isItem))
        }

        // The given modifiers include elevation shadow for dragging, etc that need to go before the others
        val containerContext = context.content(modifier = modifier.then(itemModifier).then(context.modifier))
        val contentContext = context.content()
        val contentModifier = Companion.contentModifier(level = level)
        val renderContainer: @Composable (ComposeContext) -> Unit = { context ->
            Column(modifier = context.modifier) { ->
                val placement = EnvironmentValues.shared._placement.sref()
                EnvironmentValues.shared.setValues(l@{ it ->
                    it.set_placement(placement.union(ViewPlacement.listItem))
                    return@l ComposeResult.ok
                }, in_ = { -> Companion.RenderItemContent(item = itemRenderable, context = contentContext, modifier = contentModifier) })
                if (listItemModifier?.separator != Visibility.hidden) {
                    Companion.RenderSeparator(level = level)
                }
            }
        }

        val matchtarget_2 = listItemModifier?.background
        if (matchtarget_2 != null) {
            val background = matchtarget_2
            TargetViewLayout(context = containerContext, isOverlay = false, alignment = Alignment.center, target = renderContainer, dependent = { it -> background.Compose(context = it) })
        } else {
            renderContainer(containerContext)
        }
    }

    @Composable
    private fun RenderEditableItem(content: Renderable, level: Int, context: ComposeContext, modifier: Modifier, styling: ListStyling, objectsBinding: Binding<RandomAccessCollection<Any>>? = null, key: String?, index: Int, editActions: EditActions = EditActions.of(), onDelete: ((IntSet) -> Unit)?, onMove: ((IntSet, Int) -> Unit)?, reorderableState: ReorderableLazyListState, activeSwipeKey: MutableState<String?>) {
        if (content.isSwiftUIEmptyView) {
            return
        }
        if (key == null) {
            RenderItem(content = content, level = level, context = context, modifier = modifier, styling = styling)
            return
        }
        val editActionsModifier = EditActionsModifier.combined(for_ = content)
        val swipeConfigs = SwipeActionsModifier.combined(for_ = content)
        val leadingSwipe = swipeConfigs.leading
        val trailingSwipe = swipeConfigs.trailing
        val hasUserSwipe = leadingSwipe != null || trailingSwipe != null
        val isDeleteEnabled = (editActions.contains(EditActions.delete) || onDelete != null) && editActionsModifier.isDeleteDisabled != true
        val isMoveEnabled = (editActions.contains(EditActions.move) || onMove != null) && editActionsModifier.isMoveDisabled != true
        if (!isDeleteEnabled && !isMoveEnabled && !hasUserSwipe) {
            RenderItem(content = content, level = level, context = context, modifier = modifier, styling = styling)
            return
        }

        /* Build the inner swipe + content composable. User-provided .swipeActions
        wins over the implicit onDelete trash. If neither swipe path applies
        we just render the row (caller still handles reorder wrapping). */
        val itemContent: @Composable (Modifier) -> Unit
        if (hasUserSwipe) {
            /* Mirror iOS: a destructive button's full-swipe both fires the
            user's action AND removes the row from the underlying data, so
            the row visibly disappears via LazyColumn's animateItem. We pass
            an `onDestructiveDelete` closure to RenderSwipeableItem; it is
            only invoked when (a) the destructive full-swipe fires AND
            (b) the List has either an onDelete handler or a deletable
            objectsBinding to remove from. */
            val canAutoDelete = isDeleteEnabled
            val onDestructiveDelete: (() -> Unit)? = if (canAutoDelete) { ->
                if (onDelete != null) {
                    withAnimation { -> onDelete(IntSet(integer = index)) }
                } else if ((objectsBinding != null) && (objectsBinding.wrappedValue.count > index)) {
                    withAnimation { ->
                        (objectsBinding.wrappedValue as? RangeReplaceableCollection<Any>)?.remove(at = index)
                    }
                }
            } else null
            itemContent = { rowModifier -> RenderSwipeableItem(content = content, level = level, context = context, modifier = rowModifier, styling = styling, leadingConfig = leadingSwipe, trailingConfig = trailingSwipe, rowKey = key, activeSwipeKey = activeSwipeKey, onDestructiveDelete = onDestructiveDelete) }
        } else if (isDeleteEnabled) {
            val rememberedOnDelete = rememberUpdatedState({ ->
                if (onDelete != null) {
                    withAnimation { -> onDelete(IntSet(integer = index)) }
                } else if ((objectsBinding != null) && (objectsBinding.wrappedValue.count > index)) {
                    withAnimation { ->
                        (objectsBinding.wrappedValue as? RangeReplaceableCollection<Any>)?.remove(at = index)
                    }
                }
            })
            val coroutineScope = rememberCoroutineScope()
            val positionalThreshold = with(LocalDensity.current) { -> 164.dp.toPx() }
            val dismissState = rememberSwipeToDismissBoxState(confirmValueChange = l@{ it ->
                if (it == SwipeToDismissBoxValue.EndToStart) {
                    coroutineScope.launch { -> rememberedOnDelete.value() }
                }
                return@l false
            }, positionalThreshold = SwipeToDismissBoxDefaults.positionalThreshold.sref())

            itemContent = { it ->
                SwipeToDismissBox(state = dismissState, enableDismissFromEndToStart = true, enableDismissFromStartToEnd = false, modifier = it, backgroundContent = { ->
                    /* Red background unconditional (destructive cue); icon only
                    if the trash vector resolves — force-unwrapping inside a LazyColumn item would crash measurement. */
                    Box(modifier = Modifier.background(androidx.compose.ui.graphics.Color.Red).fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.CenterEnd) { ->
                        Image.composeImageVector(named = "trash")?.let { trashVector ->
                            Icon(imageVector = trashVector, contentDescription = "Delete", modifier = Modifier.padding(end = 24.dp), tint = androidx.compose.ui.graphics.Color.White)
                        }
                    }
                }, content = { -> RenderItem(content = content, level = level, context = context, styling = styling) })
            }
        } else {
            itemContent = { rowModifier -> RenderItem(content = content, level = level, context = context, modifier = rowModifier, styling = styling) }
        }

        if (isMoveEnabled) {
            RenderReorderableItem(reorderableState = reorderableState, key = key, modifier = modifier, content = itemContent)
        } else {
            itemContent(modifier)
        }
    }

    /// Render a row wrapped in a horizontal-drag swipe container that reveals
    /// user-provided action Buttons on the leading and/or trailing edge.
    /// The foreground row determines the cell's height; reveal buttons match it
    /// via `Modifier.matchParentSize()` so we never propagate unbounded height
    /// constraints up into the surrounding LazyColumn.
    @Composable
    private fun RenderSwipeableItem(content: Renderable, level: Int, context: ComposeContext, modifier: Modifier, styling: ListStyling, leadingConfig: SwipeActionsConfig?, trailingConfig: SwipeActionsConfig?, rowKey: String, activeSwipeKey: MutableState<String?>, onDestructiveDelete: (() -> Unit)? = null) {
        val coroutineScope = rememberCoroutineScope()

        /* Extract Buttons and per-button .tint(_:) values from each edge's
        rendered content. .tint() is implemented as an env modifier with
        affectsEvaluate=false, so it wraps the Button via ModifiedContent
        but doesn't appear in the EnvironmentValues during Evaluate. We
        walk each renderable's modifier chain, run any EnvironmentModifier
        actions in a scoped env, and capture the resulting _tint. The
        innermost matching modifier wins (matches SwiftUI). */
        val trailingRenderables = (trailingConfig?.content?.Evaluate(context = context, options = 0) ?: listOf()).sref()
        val leadingRenderables = (leadingConfig?.content?.Evaluate(context = context, options = 0) ?: listOf()).sref()
        val trailingTintMap: kotlin.collections.MutableMap<Button, Color> = mutableMapOf()
        val leadingTintMap: kotlin.collections.MutableMap<Button, Color> = mutableMapOf()
        val trailingButtonsRawMutable: kotlin.collections.MutableList<Button> = mutableListOf()
        for (renderable in trailingRenderables.sref()) {
            (renderable.strip() as? Button)?.let { button ->
                trailingButtonsRawMutable.add(button)
                ExtractEnvironmentTint(from = renderable)?.let { tint ->
                    trailingTintMap[button] = tint.sref()
                }
            }
        }
        val leadingButtonsRawMutable: kotlin.collections.MutableList<Button> = mutableListOf()
        for (renderable in leadingRenderables.sref()) {
            (renderable.strip() as? Button)?.let { button ->
                leadingButtonsRawMutable.add(button)
                ExtractEnvironmentTint(from = renderable)?.let { tint ->
                    leadingTintMap[button] = tint.sref()
                }
            }
        }
        val trailingButtonsRaw: kotlin.collections.List<Button> = trailingButtonsRawMutable.sref()
        val leadingButtonsRaw: kotlin.collections.List<Button> = leadingButtonsRawMutable.sref()
        /* iOS displays trailing swipe actions in the opposite visual order from
        their declaration so the first declared action sits on the swipe edge.
        Leading actions keep declaration order. iOS also reorders .destructive
        Buttons to the swipe-from edge regardless of declaration order. For
        trailing swipes that means pinned to the right (last in the Row laid
        out with Arrangement.End); for leading, pinned to the left (first with
        Arrangement.Start). The destructive button also becomes the full-swipe
        target. */
        val trailingDestructive = trailingButtonsRaw.firstOrNull { it -> it.role == ButtonRole.destructive }
        val trailingNonDestructive = trailingButtonsRaw.filter { it -> it.role != ButtonRole.destructive }
        val trailingButtons: kotlin.collections.List<Button>
        if (trailingDestructive != null) {
            trailingButtons = (trailingNonDestructive.reversed() + listOf(trailingDestructive)).sref()
        } else {
            trailingButtons = trailingButtonsRaw.reversed()
        }
        val leadingDestructive = leadingButtonsRaw.firstOrNull { it -> it.role == ButtonRole.destructive }
        val leadingNonDestructive = leadingButtonsRaw.filter { it -> it.role != ButtonRole.destructive }
        val leadingButtons: kotlin.collections.List<Button>
        if (leadingDestructive != null) {
            leadingButtons = (listOf(leadingDestructive) + leadingNonDestructive).sref()
        } else {
            leadingButtons = leadingButtonsRaw.sref()
        }
        /* The full-swipe gesture should fire the destructive action when one
        exists, otherwise the edge-most action. */
        val trailingFullSwipeTarget = (trailingDestructive ?: trailingButtons.lastOrNull()).sref()
        val leadingFullSwipeTarget = (leadingDestructive ?: leadingButtons.firstOrNull()).sref()
        val allowsTrailingFullSwipe = trailingConfig?.allowsFullSwipe == true && trailingButtons.size > 0
        val allowsLeadingFullSwipe = leadingConfig?.allowsFullSwipe == true && leadingButtons.size > 0

        /* Measured buttons-row width per edge drives the open-anchor distance.
        swipeButtonMinWidth is the floor. */
        val minButtonWidthDp = swipeButtonMinWidth.sref()
        val density = LocalDensity.current.sref()
        val minButtonWidthPx = with(density) { -> minButtonWidthDp.toPx() }

        /* AnchoredDraggableState owns the horizontal offset and runs both the
        live drag and the snap-back animation. Anchors are populated below
        via updateAnchors once the row width has been measured; until then
        the state has only the .closed anchor at 0f so it behaves as a
        no-op draggable. */
        val velocityThresholdPx = with(density) { -> 125.dp.toPx() }
        val anchoredState = remember { ->
            AnchoredDraggableState<SwipeAnchor>(initialValue = SwipeAnchor.closed, positionalThreshold = { distance -> distance * 0.5f }, velocityThreshold = { -> velocityThresholdPx }, snapAnimationSpec = tween(durationMillis = 300), decayAnimationSpec = exponentialDecay<Float>())
        }
        val rowWidthPxState = remember { -> mutableFloatStateOf(0f) }
        /* Natural (content-sized) buttons-row width per edge, captured via
        onSizeChanged when the reveal is in natural-layout mode. Defaults
        to count × minButtonWidth until the first measurement lands. */
        val trailingNaturalState = remember { -> mutableFloatStateOf(0f) }
        val leadingNaturalState = remember { -> mutableFloatStateOf(0f) }

        /* When a *different* row's swipe opens, animate this row closed.
        Matches iOS list behavior of one open swipe at a time. */
        val currentlyOpen = activeSwipeKey.value.sref()
        LaunchedEffect(currentlyOpen) { ->
            if (currentlyOpen != rowKey && anchoredState.currentValue != SwipeAnchor.closed) {
                anchoredState.animateTo(SwipeAnchor.closed)
            }
        }

        /* Sync activeSwipeKey from this row's currentValue: when the user
        commits to an open anchor we register ourselves so siblings close;
        when we settle back to closed we clear the key. */
        LaunchedEffect(anchoredState.currentValue) { ->
            val cur = anchoredState.currentValue.sref()
            if (cur == SwipeAnchor.closed) {
                if (activeSwipeKey.value == rowKey) {
                    activeSwipeKey.value = null
                }
            } else if (cur != SwipeAnchor.trailingFull && cur != SwipeAnchor.leadingFull) {
                activeSwipeKey.value = rowKey
            }
        }

        /* When the row actually settles on a full-swipe anchor, fire the
        primary action (and optional auto-delete for destructive), then
        snap back to closed. */
        LaunchedEffect(anchoredState.settledValue) { ->
            val settled = anchoredState.settledValue.sref()
            if (settled == SwipeAnchor.trailingFull) {
                trailingFullSwipeTarget?.action.sref()?.let { action ->
                    action()
                }
                if ((trailingFullSwipeTarget?.role == ButtonRole.destructive) && (onDestructiveDelete != null)) {
                    onDestructiveDelete()
                }
                anchoredState.snapTo(SwipeAnchor.closed)
                if (activeSwipeKey.value == rowKey) {
                    activeSwipeKey.value = null
                }
            } else if (settled == SwipeAnchor.leadingFull) {
                leadingFullSwipeTarget?.action.sref()?.let { action ->
                    action()
                }
                if ((leadingFullSwipeTarget?.role == ButtonRole.destructive) && (onDestructiveDelete != null)) {
                    onDestructiveDelete()
                }
                anchoredState.snapTo(SwipeAnchor.closed)
                if (activeSwipeKey.value == rowKey) {
                    activeSwipeKey.value = null
                }
            }
        }

        Box(modifier = modifier.onSizeChanged { it -> rowWidthPxState.value = Float(it.width) }.clipToBounds()) { ->
            val rowWidthPx = rowWidthPxState.value.sref()
            /* Natural buttons-row width is the measured content width (each
            button sized to its label), captured below in natural-layout
            mode. Before the first measurement lands the fallback is
            count × minButtonWidth so the open anchor is never zero on
            frame zero. */
            val trailingNaturalFallback = (Float(trailingButtons.size) * minButtonWidthPx).sref()
            val leadingNaturalFallback = (Float(leadingButtons.size) * minButtonWidthPx).sref()
            val trailingNaturalPx = (if (trailingNaturalState.value > 0f) trailingNaturalState.value else trailingNaturalFallback).sref()
            val leadingNaturalPx = (if (leadingNaturalState.value > 0f) leadingNaturalState.value else leadingNaturalFallback).sref()
            val trailingOpenPx = (-trailingNaturalPx).sref()
            val leadingOpenPx = leadingNaturalPx.sref()

            /* Current revealed width per edge (positive). When the user drags
            further than the natural buttons-row width, the reveal area
            stretches: the row's width follows the foreground so the
            buttons grow to track the row edge instead of leaving a gap. */
            val rawOffset = anchoredState.offset.sref()
            val curOffset: Float = if (rawOffset.isNaN()) 0f else rawOffset
            val revealedTrailingPx = if (curOffset < 0f) -curOffset else 0f
            val revealedLeadingPx = if (curOffset > 0f) curOffset else 0f

            /* Past the full-swipe trigger, the destructive (or otherwise
            edge-most) action takes over and expands to fill the entire
            revealed width while the other actions shrink to zero. The
            transition is animated to smooth out the moment of takeover. */
            val trailingFullSwipeActive = allowsTrailingFullSwipe && revealedTrailingPx > rowWidthPx * swipeFullSwipeFraction
            val leadingFullSwipeActive = allowsLeadingFullSwipe && revealedLeadingPx > rowWidthPx * swipeFullSwipeFraction
            val trailingFullSwipeAnim = animateFloatAsState(targetValue = if (trailingFullSwipeActive) 1f else 0f).value.sref()
            val leadingFullSwipeAnim = animateFloatAsState(targetValue = if (leadingFullSwipeActive) 1f else 0f).value.sref()

            /* Reveal area sits beneath the row content. Buttons are sized to
            share the row's current revealed (stretched) width. The primary
            action (destructive if present, else the edge-most) absorbs all
            additional width during full-swipe takeover; other buttons
            proportionally shrink to zero. The direction guards ensure only
            one edge's reveal renders at any non-zero offset. */
            val leadingUseStretch = leadingButtons.size > 0 && revealedLeadingPx > leadingNaturalPx + 1f
            val trailingUseStretch = trailingButtons.size > 0 && revealedTrailingPx > trailingNaturalPx + 1f
            val leadingCount = Float(leadingButtons.size)
            val trailingCount = Float(trailingButtons.size)

            if (leadingButtons.size > 0 && curOffset >= 0f) {
                Box(modifier = Modifier.matchParentSize(), contentAlignment = androidx.compose.ui.Alignment.CenterStart) { ->
                    if (leadingUseStretch) {
                        val rowWidthDp = with(density) { -> revealedLeadingPx.toDp() }
                        Row(modifier = Modifier.fillMaxHeight().width(rowWidthDp)) { ->
                            for (button in leadingButtons.sref()) {
                                val isPrimary = button === leadingFullSwipeTarget
                                val primaryWeight = 1f + (leadingCount - 1f) * leadingFullSwipeAnim
                                val otherWeight = 1f - leadingFullSwipeAnim
                                val weight = if (isPrimary) primaryWeight else otherWeight
                                if (weight <= 0.01f) {
                                    continue
                                }
                                Box(modifier = Modifier.weight(weight).fillMaxHeight()) { ->
                                    RenderSwipeRevealButton(button = button, sizeModifier = Modifier.fillMaxSize(), context = context, tintOverride = leadingTintMap[button], onTap = { ->
                                        button.action()
                                        if ((button.role == ButtonRole.destructive) && (onDestructiveDelete != null)) {
                                            onDestructiveDelete()
                                        }
                                        coroutineScope.launch { -> anchoredState.animateTo(SwipeAnchor.closed) }
                                    })
                                }
                            }
                        }
                    } else {
                        Row(modifier = Modifier.fillMaxHeight().wrapContentWidth().onSizeChanged { it -> leadingNaturalState.value = Float(it.width) }) { ->
                            for (button in leadingButtons.sref()) {
                                RenderSwipeRevealButton(button = button, sizeModifier = Modifier.fillMaxHeight().widthIn(min = minButtonWidthDp), context = context, tintOverride = leadingTintMap[button], onTap = { ->
                                    button.action()
                                    if ((button.role == ButtonRole.destructive) && (onDestructiveDelete != null)) {
                                        onDestructiveDelete()
                                    }
                                    coroutineScope.launch { -> anchoredState.animateTo(SwipeAnchor.closed) }
                                })
                            }
                        }
                    }
                }
            }
            if (trailingButtons.size > 0 && curOffset <= 0f) {
                Box(modifier = Modifier.matchParentSize(), contentAlignment = androidx.compose.ui.Alignment.CenterEnd) { ->
                    if (trailingUseStretch) {
                        val rowWidthDp = with(density) { -> revealedTrailingPx.toDp() }
                        Row(modifier = Modifier.fillMaxHeight().width(rowWidthDp)) { ->
                            for (button in trailingButtons.sref()) {
                                val isPrimary = button === trailingFullSwipeTarget
                                val primaryWeight = 1f + (trailingCount - 1f) * trailingFullSwipeAnim
                                val otherWeight = 1f - trailingFullSwipeAnim
                                val weight = if (isPrimary) primaryWeight else otherWeight
                                if (weight <= 0.01f) {
                                    continue
                                }
                                Box(modifier = Modifier.weight(weight).fillMaxHeight()) { ->
                                    RenderSwipeRevealButton(button = button, sizeModifier = Modifier.fillMaxSize(), context = context, tintOverride = trailingTintMap[button], onTap = { ->
                                        button.action()
                                        if ((button.role == ButtonRole.destructive) && (onDestructiveDelete != null)) {
                                            onDestructiveDelete()
                                        }
                                        coroutineScope.launch { -> anchoredState.animateTo(SwipeAnchor.closed) }
                                    })
                                }
                            }
                        }
                    } else {
                        Row(modifier = Modifier.fillMaxHeight().wrapContentWidth().onSizeChanged { it -> trailingNaturalState.value = Float(it.width) }) { ->
                            for (button in trailingButtons.sref()) {
                                RenderSwipeRevealButton(button = button, sizeModifier = Modifier.fillMaxHeight().widthIn(min = minButtonWidthDp), context = context, tintOverride = trailingTintMap[button], onTap = { ->
                                    button.action()
                                    if ((button.role == ButtonRole.destructive) && (onDestructiveDelete != null)) {
                                        onDestructiveDelete()
                                    }
                                    coroutineScope.launch { -> anchoredState.animateTo(SwipeAnchor.closed) }
                                })
                            }
                        }
                    }
                }
            }

            /*
            Foreground row content sizes itself naturally (no fillMaxSize)
            so the parent Box adopts its height and the LazyColumn item is
            measurable.
            On release, project the current position forward by the gesture's
            velocity and snap to whichever anchor (closed / leading-open /
            trailing-open / full-swipe) the projected position is closest to.
            This guarantees the row always lands on a defined state — never
            stops mid-track — and naturally handles reversing direction:
            dragging back from open toward closed projects past closed and
            snaps shut, even if the finger lifted while still partially open.
            Full-swipe checks ACTUAL drag distance, not the projected position,
            so a fast flick alone can't trigger the destructive action.
            */

            /* Anchor refresh: rebuilt whenever the measured sizes or
            allowsXxxFullSwipe flags change. Anchors are defined via
            Compose's DraggableAnchors DSL inside a SKIP INSERT block
            because Skip's transpiler doesn't model Kotlin lambdas with
            receiver-type extensions like `T.at(Float)`. */
            LaunchedEffect(rowWidthPx, trailingNaturalPx, leadingNaturalPx, allowsTrailingFullSwipe, allowsLeadingFullSwipe, trailingButtons.size, leadingButtons.size) { ->
                val hasTrailing = trailingButtons.size > 0
                val hasLeading = leadingButtons.size > 0
                val trailingOpenPos = (-trailingNaturalPx).sref()
                val leadingOpenPos = leadingNaturalPx.sref()
                val trailingFullPos = (-rowWidthPx).sref()
                val leadingFullPos = rowWidthPx.sref()
                val newAnchors = androidx.compose.foundation.gestures.DraggableAnchors<skip.ui.SwipeAnchor> {
                skip.ui.SwipeAnchor.closed at 0f
                if (hasTrailing) skip.ui.SwipeAnchor.trailingOpen at trailingOpenPos
                if (allowsTrailingFullSwipe) skip.ui.SwipeAnchor.trailingFull at trailingFullPos
                if (hasLeading) skip.ui.SwipeAnchor.leadingOpen at leadingOpenPos
                if (allowsLeadingFullSwipe) skip.ui.SwipeAnchor.leadingFull at leadingFullPos
                }
                anchoredState.updateAnchors(newAnchors)
            }
            Box(modifier = Modifier
                .fillMaxWidth()
                .offset { ->
                    val o = anchoredState.offset.sref()
                    IntOffset(if (o.isNaN()) 0 else o.toInt(), 0)
                }
                .anchoredDraggable(state = anchoredState, orientation = Orientation.Horizontal)) { ->
                RenderItem(content = content, level = level, context = context, styling = styling)
                /* Subtle scrim that intensifies with swipe progress so the row
                visually recedes as actions appear.*/
                val rawScrimOffset = anchoredState.offset.sref()
                val scrimOffset: Float = if (rawScrimOffset.isNaN()) 0f else rawScrimOffset
                val curAbs = if (scrimOffset < 0f) -scrimOffset else scrimOffset
                val openMag: Float
                if (scrimOffset < 0f) {
                    openMag = -trailingOpenPx
                } else if (scrimOffset > 0f) {
                    openMag = leadingOpenPx
                } else {
                    openMag = 1f // unused; progress stays 0
                }
                val progressRaw = if (openMag > 0f) curAbs / openMag else 0f
                val progress: Float = if (progressRaw > 1f) 1f else progressRaw
                if (progress > 0f) {
                    val scrimAlpha = progress * swipeScrimMaxAlpha
                    Box(modifier = Modifier.matchParentSize().background(androidx.compose.ui.graphics.Color.LightGray.copy(alpha = scrimAlpha)))
                }
            }
        }
    }

    /// Walk the renderable's ModifiedContent chain, run any
    /// EnvironmentModifier action in a scoped EnvironmentValues, and
    /// return whatever ._tint it sets. We walk manually (instead of
    /// forEachModifier) because the action is @Composable and so is
    /// setValuesWithReturn — both must be invoked from a @Composable
    /// scope, which forEachModifier's plain callback isn't. Outermost
    /// modifier processed first so the innermost wins (matches SwiftUI).
    @Composable
    private fun ExtractEnvironmentTint(from: Renderable): Color? {
        val renderable = from
        var captured: Color? = null
        var current: Renderable? = renderable.sref()
        while (true) {
            val mod_0 = current as? ModifiedContent
            if (mod_0 == null) {
                break
            }
            (mod_0.modifier as? EnvironmentModifier)?.let { envMod ->
                envMod.action?.let { action ->
                    val scoped: Color? = EnvironmentValues.shared.setValuesWithReturn(action) l@{ -> return@l EnvironmentValues.shared._tint }
                    if (scoped != null) {
                        captured = scoped.sref()
                    }
                }
            }
            current = mod_0.renderable.sref()
        }
        return captured.sref()
    }

    /// Render a single action button inside the swipe reveal area.
    /// Sizing is supplied by the caller: in natural mode this is
    /// widthIn(min:).fillMaxHeight() so the button hugs its label; in
    /// stretch mode it's fillMaxSize() inside a weighted Box so the button
    /// expands with the row. Padding around the label keeps icon/text from
    /// touching the cell edges.
    @Composable
    private fun RenderSwipeRevealButton(button: Button, sizeModifier: Modifier, context: ComposeContext, tintOverride: Color? = null, onTap: () -> Unit) {
        val backgroundColor: androidx.compose.ui.graphics.Color
        val contentColor: androidx.compose.ui.graphics.Color
        if (button.role == ButtonRole.destructive) {
            /* Match the red used by the implicit onDelete SwipeToDismissBox so
            destructive actions look the same whether triggered via .onDelete
            or an explicit .swipeActions { Button(role: .destructive) }. */
            backgroundColor = androidx.compose.ui.graphics.Color.Red
            contentColor = androidx.compose.ui.graphics.Color.White
        } else {
            /* Per-button .tint(_:) wins over the surrounding env tint. */
            val tint = (tintOverride ?: EnvironmentValues.shared._tint)?.colorImpl?.invoke()
            if (tint != null) {
                backgroundColor = tint
                contentColor = androidx.compose.ui.graphics.Color.White
            } else {
                backgroundColor = MaterialTheme.colorScheme.secondaryContainer
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            }
        }
        /* Render the user's label view (Text, Image, Label, or any custom
        composition). clipToBounds on the button means the label can
        safely use Modifier.width(IntrinsicSize.Max) — Text stays on one
        line at its intrinsic width and any overflow is clipped instead
        of wrapping mid-animation when full-swipe takeover squeezes a
        non-primary button's width below the natural content width. */
        Row(modifier = sizeModifier
            .clipToBounds()
            .background(backgroundColor)
            .clickable(onClick = onTap)
            .padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) { ->
            val contentSwiftColor = Color(colorImpl = { -> contentColor })
            EnvironmentValues.shared.setValues(l@{ it ->
                it.set_foregroundStyle(contentSwiftColor)
                return@l ComposeResult.ok
            }) { ->
                /* Apply the font via the .font() View modifier which routes
                through environment(\.font, ...). Setting the font property
                on EnvironmentValues directly isn't exposed as a Kotlin
                setter by Skip's transpilation. */
                /* requiredWidth(IntrinsicSize.Max) overrides parent's max
                width constraint so Text always lays out at its one-line
                intrinsic width. An extra 8dp horizontal pad gives a tiny
                measurement headroom so subpixel rounding can't trigger
                a one-line→two-line→one-line flicker during the
                takeover animation. The button Row's clipToBounds
                clips the resulting overflow visually. */
                button.label.font(Font.footnote).Compose(context = context.content(modifier = Modifier.requiredWidth(IntrinsicSize.Max).padding(horizontal = 4.dp)))
            }
        }
    }

    @Composable
    private fun RenderReorderableItem(reorderableState: ReorderableLazyListState, key: String, modifier: Modifier, content: @Composable (Modifier) -> Unit) {
        ReorderableItem(state = reorderableState, key = key, defaultDraggingModifier = modifier) { dragging ->
            var itemModifier = Modifier.detectReorderAfterLongPress(reorderableState)
            if (dragging) {
                val elevation = animateDpAsState(8.dp)
                itemModifier = itemModifier.shadow(elevation.value)
            }
            content(itemModifier)
        }
    }

    @Composable
    private fun RenderSectionHeader(content: Renderable, context: ComposeContext, styling: ListStyling, isTop: Boolean) {
        if (!isTop && styling.style != ListStyle.plain) {
            // Vertical padding
            RenderFooter(styling = styling, safeAreaHeight = 0.dp, hasBottomSection = true)
        }
        val backgroundColor = BackgroundColor(styling = styling, isItem = false)
        val modifier = Modifier
            .zIndex(0.5f)
            .background(backgroundColor)
            .then(context.modifier)
        var contentModifier = Modifier.fillMaxWidth()
        if (isTop && styling.style != ListStyle.plain) {
            contentModifier = contentModifier.padding(start = Companion.horizontalItemInset.dp, top = 0.dp, end = Companion.horizontalItemInset.dp, bottom = Companion.verticalItemInset.dp)
        } else {
            contentModifier = contentModifier.padding(horizontal = Companion.horizontalItemInset.dp, vertical = Companion.verticalItemInset.dp)
        }
        Box(modifier = modifier, contentAlignment = androidx.compose.ui.Alignment.BottomCenter) { ->
            Column(modifier = Modifier.fillMaxWidth()) { ->
                EnvironmentValues.shared.setValues(l@{ it ->
                    it.set_listSectionHeaderStyle(styling.style)
                    return@l ComposeResult.ok
                }, in_ = { -> content.Render(context = context.content(modifier = contentModifier)) })
            }
            if (styling.style != ListStyle.plain) {
                RenderRoundedCorners(isTop = true, fill = backgroundColor)
            }
        }
    }

    @Composable
    private fun RenderSectionFooter(content: Renderable, context: ComposeContext, styling: ListStyling) {
        if (styling.style == ListStyle.plain) {
            val footerContent: Renderable
            val matchtarget_3 = content as? LazySectionFooter
            if (matchtarget_3 != null) {
                val lazySectionFooter = matchtarget_3
                if (!lazySectionFooter.content.any({ it -> !it.isSwiftUIEmptyView })) {
                    // Replace an empty footer with an empty view for RenderItem handling
                    footerContent = EmptyView()
                } else {
                    footerContent = content.sref()
                }
            } else {
                footerContent = content.sref()
            }
            RenderItem(content = footerContent, level = 0, context = context, styling = styling, isItem = false)
        } else {
            val backgroundColor = BackgroundColor(styling = styling, isItem = false)
            val modifier = Modifier.offset(y = -1.dp)
                .zIndex(0.5f)
                .background(backgroundColor)
                .then(context.modifier)
            val contentModifier = Modifier.fillMaxWidth().padding(horizontal = Companion.horizontalItemInset.dp, vertical = Companion.verticalItemInset.dp)
            Box(modifier = modifier, contentAlignment = androidx.compose.ui.Alignment.TopCenter) { ->
                Column(modifier = Modifier.fillMaxWidth().heightIn(min = 1.dp)) { ->
                    EnvironmentValues.shared.setValues(l@{ it ->
                        it.set_listSectionFooterStyle(styling.style)
                        return@l ComposeResult.ok
                    }, in_ = { -> content.Render(context = context.content(modifier = contentModifier)) })
                }
                RenderRoundedCorners(isTop = false, fill = backgroundColor)
            }
        }
    }

    /// - Warning: Only call for non-.plain styles or with a positive safe area height. This is distinct from having this function detect
    /// .plain and zero-height and return without rendering. That causes .plain style lists to have a weird rubber banding effect on overscroll.
    @Composable
    private fun RenderHeader(styling: ListStyling, safeAreaHeight: Dp, hasTopSection: Boolean) {
        var height = safeAreaHeight.sref()
        if (styling.style != ListStyle.plain) {
            height += Companion.verticalInset.dp.sref()
        }
        val backgroundColor = BackgroundColor(styling = styling, isItem = false)
        val modifier = Modifier.fillMaxWidth()
            .height(height)
            .zIndex(0.5f)
            .background(backgroundColor)
        Box(modifier = modifier, contentAlignment = androidx.compose.ui.Alignment.BottomCenter) { ->
            if (!hasTopSection && styling.style != ListStyle.plain) {
                RenderRoundedCorners(isTop = true, fill = backgroundColor)
            }
        }
    }

    /// - Warning: Only call for non-.plain styles or with a positive safe area height. This is distinct from having this function detect
    /// .plain and zero-height and return without rendering. That causes .plain style lists to have a weird rubber banding effect on overscroll.
    @Composable
    private fun RenderFooter(styling: ListStyling, safeAreaHeight: Dp, hasBottomSection: Boolean) {
        var height = safeAreaHeight.sref()
        var offset = 0.dp.sref()
        if (styling.style != ListStyle.plain) {
            height += Companion.verticalInset.dp.sref()
            offset = (-1.dp).sref() // Cover last row's divider
        }
        val backgroundColor = BackgroundColor(styling = styling, isItem = false)
        val modifier = Modifier.fillMaxWidth()
            .height(height)
            .offset(y = offset)
            .zIndex(0.5f)
            .background(backgroundColor)
        Box(modifier = modifier, contentAlignment = androidx.compose.ui.Alignment.TopCenter) { ->
            if (!hasBottomSection && styling.style != ListStyle.plain) {
                RenderRoundedCorners(isTop = false, fill = backgroundColor)
            }
        }
    }

    @Composable
    private fun RenderRoundedCorners(isTop: Boolean, fill: androidx.compose.ui.graphics.Color) {
        val shape = GenericShape { size, _ ->
            val rect = Rect(left = 0.0f, top = 0.0f, right = size.width, bottom = size.height)
            val rectPath = androidx.compose.ui.graphics.Path()
            rectPath.addRect(rect)
            val roundRect: RoundRect
            if (isTop) {
                roundRect = RoundRect(rect, topLeft = CornerRadius(size.height), topRight = CornerRadius(size.height))
            } else {
                roundRect = RoundRect(rect, bottomLeft = CornerRadius(size.height), bottomRight = CornerRadius(size.height))
            }
            val roundedRectPath = androidx.compose.ui.graphics.Path()
            roundedRectPath.addRoundRect(roundRect)
            addPath(combine(PathOperation.Difference, rectPath, roundedRectPath))
        }
        val offset = (if (isTop) listSectionCornerRadius.dp else -listSectionCornerRadius.dp).sref()
        val modifier = Modifier
            .fillMaxWidth()
            .height(listSectionCornerRadius.dp)
            .offset(y = offset)
            .clip(shape)
            .background(fill)
        Box(modifier = modifier)
    }

    @Composable
    private fun RenderSearchField(state: SearchableState, context: ComposeContext, styling: ListStyling, safeAreaHeight: Dp) {
        var modifier = Modifier.background(BackgroundColor(styling = styling, isItem = false))
        if (styling.style == ListStyle.plain) {
            modifier = modifier.padding(top = Companion.verticalInset.dp + safeAreaHeight, start = Companion.horizontalInset.dp, end = Companion.horizontalInset.dp, bottom = Companion.verticalInset.dp)
        } else {
            modifier = modifier.padding(top = Companion.verticalInset.dp + safeAreaHeight)
        }
        modifier = modifier.fillMaxWidth()
        SearchField(state = state, context = context.content(modifier = modifier))
    }

    @Composable
    private fun BackgroundColor(styling: ListStyling, isItem: Boolean): androidx.compose.ui.graphics.Color {
        if (!isItem && styling.backgroundVisibility == Visibility.hidden) {
            return Color.clear.colorImpl()
        } else if (styling.style == ListStyle.plain) {
            return Color.background.colorImpl()
        } else {
            return Color.systemBarBackground.colorImpl()
        }
    }

    override fun Swift_projection(options: Int): () -> Any = Swift_projectionImpl(options)
    private external fun Swift_projectionImpl(options: Int): () -> Any

    @androidx.annotation.Keep
    companion object {

        private val horizontalInset = 16.0
        private val verticalInset = 16.0
        private val minimumItemHeight = 32.0
        private val horizontalItemInset = 16.0
        private val verticalItemInset = 8.0
        private val levelInset = 24.0

        /// Seconds of velocity-based projection used when picking the snap anchor.
        private val swipeVelocityProjectionSeconds: Float = 0.15f
        /// Fraction of the row width past which a full swipe fires the destructive action.
        private val swipeFullSwipeFraction: Float = 0.65f
        /// Cap on the gray scrim alpha applied to the foreground during a swipe.
        private val swipeScrimMaxAlpha: Float = 0.18f
        /// Minimum width for a single reveal button; grows to fit longer labels.
        private val swipeButtonMinWidth: Dp = 80.dp.sref()

        internal fun contentModifier(level: Int): Modifier = Modifier.padding(start = (horizontalItemInset + level * levelInset).dp, end = horizontalItemInset.dp, top = verticalItemInset.dp, bottom = verticalItemInset.dp).fillMaxWidth().requiredHeightIn(min = minimumItemHeight.dp)

        @Composable
        internal fun RenderSeparator(level: Int): Unit = androidx.compose.material3.Divider(modifier = Modifier.padding(start = (horizontalItemInset + level * levelInset).dp).fillMaxWidth(), color = Color.separator.colorImpl())

        @Composable
        internal fun RenderItemContent(item: Renderable, context: ComposeContext, modifier: Modifier) {
            val badgeModifier = BadgeModifier.combined(for_ = item)
            val badge = badgeModifier.badge
            val (isListItem, listItemAction) = item.shouldRenderListItem(context = context)
            if (isListItem) {
                val actionModifier: Modifier
                if (listItemAction != null) {
                    val isDisabled = !EnvironmentValues.shared.isEnabled || item.forEachModifier { it ->
                        (it as? DisabledModifier)?.disabled
                    } == true
                    actionModifier = Modifier.clickable(onClick = listItemAction, enabled = !isDisabled)
                } else {
                    actionModifier = Modifier
                }
                if (badge != null) {
                    Row(modifier = actionModifier.then(modifier), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) { ->
                        Box(modifier = Modifier.weight(1.0f), contentAlignment = androidx.compose.ui.Alignment.CenterStart) { -> item.RenderListItem(context = context, modifiers = listOf()) }
                        RenderBadge(badge = badge, prominence = badgeModifier.prominence ?: BadgeProminence.standard, context = context)
                    }
                } else {
                    Box(modifier = actionModifier.then(modifier), contentAlignment = androidx.compose.ui.Alignment.CenterStart) { -> item.RenderListItem(context = context, modifiers = listOf()) }
                }
            } else if (badge != null) {
                Row(modifier = modifier, horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) { ->
                    Box(modifier = Modifier.weight(1.0f), contentAlignment = androidx.compose.ui.Alignment.CenterStart) { -> item.Render(context = context) }
                    RenderBadge(badge = badge, prominence = badgeModifier.prominence ?: BadgeProminence.standard, context = context)
                }
            } else {
                Box(modifier = modifier, contentAlignment = androidx.compose.ui.Alignment.CenterStart) { -> item.Render(context = context) }
            }
        }

        @Composable
        private fun RenderBadge(badge: Text, prominence: BadgeProminence, context: ComposeContext) {
            val badgeColor: androidx.compose.ui.graphics.Color
            when (prominence) {
                BadgeProminence.increased -> badgeColor = Color.red.colorImpl()
                BadgeProminence.decreased -> badgeColor = Color.secondary.colorImpl()
                else -> badgeColor = Color.secondary.colorImpl()
            }
            EnvironmentValues.shared.setValues(l@{ it ->
                it.set_foregroundStyle(Color(colorImpl = { -> badgeColor }))
                return@l ComposeResult.ok
            }, in_ = { -> badge.Compose(context = context) })
        }
    }
}

// Kotlin does not support generic constructor parameters, so we have to model many List constructors as functions

//extension List {
//    public init<Data, RowContent>(_ data: Data, @ViewBuilder rowContent: @escaping (Data.Element) -> RowContent) where Content == ForEach<Data, Data.Element.ID, RowContent>, Data : RandomAccessCollection, RowContent : View, Data.Element : Identifiable
//}
fun <ObjectType> List(data: RandomAccessCollection<ObjectType>, rowContent: (ObjectType) -> View): List {
    return List(identifier = { it -> (it as Identifiable<Hashable>).id }, objects = data as RandomAccessCollection<Any>, objectContent = { it -> rowContent(it as ObjectType) })
}

//extension List {
//    public init<Data, ID, RowContent>(_ data: Data, id: KeyPath<Data.Element, ID>, @ViewBuilder rowContent: @escaping (Data.Element) -> RowContent) where Content == ForEach<Data, ID, RowContent>, Data : RandomAccessCollection, ID : Hashable, RowContent : View
//}
fun <ObjectType> List(data: RandomAccessCollection<ObjectType>, id: (ObjectType) -> AnyHashable?, rowContent: (ObjectType) -> View): List where ObjectType: Any {
    return List(identifier = { it -> id(it as ObjectType) }, objects = data as RandomAccessCollection<Any>, objectContent = { it -> rowContent(it as ObjectType) })
}
fun List(data: IntRange, id: ((Int) -> AnyHashable?)? = null, rowContent: (Int) -> View): List {
    return List(identifier = if (id == null) null else { it -> id!!(it as Int) }, indexRange = data, indexedContent = rowContent)
}

//extension List {
//  public init<Data, RowContent>(_ data: Binding<Data>, editActions: EditActions /* <Data> */, @ViewBuilder rowContent: @escaping (Binding<Data.Element>) -> RowContent) where Content == ForEach<IndexedIdentifierCollection<Data, Data.Element.ID>, Data.Element.ID, EditableCollectionContent<RowContent, Data>>, Data : MutableCollection, Data : RandomAccessCollection, RowContent : View, Data.Element : Identifiable, Data.Index : Hashable
//}
fun <Data, ObjectType> List(data: Binding<Data>, editActions: EditActions = EditActions.of(), rowContent: (Binding<ObjectType>) -> View): List where Data: RandomAccessCollection<ObjectType> {
    return List(identifier = { it -> (it as Identifiable<Hashable>).id }, objectsBinding = data as Binding<RandomAccessCollection<Any>>, objectsBindingContent = l@{ data, index ->
        val binding = Binding<ObjectType>(get = { -> data.wrappedValue[index] as ObjectType }, set = { it -> (data.wrappedValue as skip.lib.MutableCollection<ObjectType>)[index] = it.sref() })
        return@l rowContent(binding)
    }, editActions = editActions)
}

//extension List {
//  public init<Data, ID, RowContent>(_ data: Binding<Data>, id: KeyPath<Data.Element, ID>, editActions: EditActions /* <Data> */, @ViewBuilder rowContent: @escaping (Binding<Data.Element>) -> RowContent) where Content == ForEach<IndexedIdentifierCollection<Data, ID>, ID, EditableCollectionContent<RowContent, Data>>, Data : MutableCollection, Data : RandomAccessCollection, ID : Hashable, RowContent : View, Data.Index : Hashable
//}
fun <Data, ObjectType> List(data: Binding<Data>, id: (ObjectType) -> AnyHashable?, editActions: EditActions = EditActions.of(), rowContent: (Binding<ObjectType>) -> View): List where Data: RandomAccessCollection<ObjectType> {
    return List(identifier = { it -> id(it as ObjectType) }, objectsBinding = data as Binding<RandomAccessCollection<Any>>, objectsBindingContent = l@{ data, index ->
        val binding = Binding<ObjectType>(get = { -> data.wrappedValue[index] as ObjectType }, set = { it -> (data.wrappedValue as skip.lib.MutableCollection<ObjectType>)[index] = it.sref() })
        return@l rowContent(binding)
    }, editActions = editActions)
}

internal class ListStyling {
    internal val style: ListStyle
    internal val backgroundVisibility: Visibility

    internal fun withStyle(style: ListStyle): ListStyling = ListStyling(style = style, backgroundVisibility = backgroundVisibility)

    constructor(style: ListStyle, backgroundVisibility: Visibility) {
        this.style = style
        this.backgroundVisibility = backgroundVisibility
    }

    override fun equals(other: Any?): Boolean {
        if (other !is ListStyling) return false
        return style == other.style && backgroundVisibility == other.backgroundVisibility
    }
}

@Stable
internal class ListArguments {
    internal val headerSafeAreaHeight: Dp
    internal val footerSafeAreaHeight: Dp
    internal val safeAreaEdges: Edge.Set

    constructor(headerSafeAreaHeight: Dp, footerSafeAreaHeight: Dp, safeAreaEdges: Edge.Set) {
        this.headerSafeAreaHeight = headerSafeAreaHeight.sref()
        this.footerSafeAreaHeight = footerSafeAreaHeight.sref()
        this.safeAreaEdges = safeAreaEdges.sref()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is ListArguments) return false
        return headerSafeAreaHeight == other.headerSafeAreaHeight && footerSafeAreaHeight == other.footerSafeAreaHeight && safeAreaEdges == other.safeAreaEdges
    }
}

class ListStyle: RawRepresentable<Int> {
    override val rawValue: Int

    constructor(rawValue: Int) {
        this.rawValue = rawValue
    }

    override fun equals(other: Any?): Boolean {
        if (other !is ListStyle) return false
        return rawValue == other.rawValue
    }

    @androidx.annotation.Keep
    companion object {

        val automatic = ListStyle(rawValue = 0) // For bridging

        @Deprecated("This API is not yet available in Skip. Consider placing it within a #if !SKIP block. You can file an issue against the owning library at https://github.com/skiptools, or see the library README for information on adding support", level = DeprecationLevel.ERROR)
        val sidebar = ListStyle(rawValue = 1) // For bridging

        @Deprecated("This API is not yet available in Skip. Consider placing it within a #if !SKIP block. You can file an issue against the owning library at https://github.com/skiptools, or see the library README for information on adding support", level = DeprecationLevel.ERROR)
        val insetGrouped = ListStyle(rawValue = 2) // For bridging

        @Deprecated("This API is not yet available in Skip. Consider placing it within a #if !SKIP block. You can file an issue against the owning library at https://github.com/skiptools, or see the library README for information on adding support", level = DeprecationLevel.ERROR)
        val grouped = ListStyle(rawValue = 3) // For bridging

        @Deprecated("This API is not yet available in Skip. Consider placing it within a #if !SKIP block. You can file an issue against the owning library at https://github.com/skiptools, or see the library README for information on adding support", level = DeprecationLevel.ERROR)
        val inset = ListStyle(rawValue = 4) // For bridging

        val plain = ListStyle(rawValue = 5) // For bridging
    }
}

sealed class ListItemTint {
    class FixedCase(val associated0: Color): ListItemTint() {
    }
    class PreferredCase(val associated0: Color): ListItemTint() {
    }
    class MonochromeCase: ListItemTint() {
    }

    @androidx.annotation.Keep
    companion object {
        fun fixed(associated0: Color): ListItemTint = FixedCase(associated0)
        fun preferred(associated0: Color): ListItemTint = PreferredCase(associated0)
        val monochrome: ListItemTint = MonochromeCase()
    }
}

sealed class ListSectionSpacing {
    class DefaultCase: ListSectionSpacing() {
    }
    class CompactCase: ListSectionSpacing() {
    }
    class CustomCase(val associated0: Double): ListSectionSpacing() {
    }

    @androidx.annotation.Keep
    companion object {
        val default: ListSectionSpacing = DefaultCase()
        val compact: ListSectionSpacing = CompactCase()
        fun custom(associated0: Double): ListSectionSpacing = CustomCase(associated0)
    }
}

internal class ListItemModifier: RenderModifier {
    internal val background: View?
    internal val separator: Visibility?

    internal constructor(background: View? = null, separator: Visibility? = null): super() {
        this.background = background.sref()
        this.separator = separator
    }

    @androidx.annotation.Keep
    companion object {

        internal fun combined(for_: Renderable): ListItemModifier {
            val renderable = for_
            var background: View? = null
            var separator: Visibility? = null
            renderable.forEachModifier l@{ it ->
                (it as? ListItemModifier)?.let { listItemModifier ->
                    background = (background ?: listItemModifier.background).sref()
                    separator = separator ?: listItemModifier.separator
                }
                return@l null
            }
            return ListItemModifier(background = background, separator = separator)
        }
    }
}

