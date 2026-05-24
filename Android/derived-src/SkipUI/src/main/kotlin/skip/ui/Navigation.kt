package skip.ui

import kotlin.reflect.KClass
import skip.lib.*
import skip.lib.Array
import skip.lib.Sequence
import skip.lib.Set

// Copyright 2023–2026 Skip
// SPDX-License-Identifier: MPL-2.0
import skip.foundation.*
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.KeyboardArrowLeft
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import kotlin.reflect.full.superclasses
import kotlinx.serialization.Serializable
import androidx.compose.runtime.key
import kotlinx.coroutines.delay

@androidx.annotation.Keep
class NavigationStack: View, Renderable, skip.lib.SwiftProjecting {
    internal val root: ComposeBuilder
    internal val path: Binding<Array<Any>>?
    internal val navigationPath: Binding<NavigationPath>?
    internal val destinationKeyTransformer: ((Any) -> String)?

    constructor(root: () -> View) {
        this.root = ComposeBuilder.from(root)
        this.path = null
        this.navigationPath = null
        this.destinationKeyTransformer = null
    }

    constructor(path: Binding<NavigationPath>, root: () -> View) {
        this.root = ComposeBuilder.from(root)
        this.path = null
        this.navigationPath = path.sref()
        this.destinationKeyTransformer = null
    }

    constructor(path: Any, root: () -> View) {
        this.root = ComposeBuilder.from(root)
        this.path = (path as Binding<Array<Any>>?).sref()
        this.navigationPath = null
        this.destinationKeyTransformer = null
    }

    constructor(getData: (() -> Array<Any>)?, setData: ((Array<Any>) -> Unit)?, bridgedRoot: View, destinationKeyTransformer: (Any) -> String) {
        this.root = ComposeBuilder.from { -> bridgedRoot }
        this.navigationPath = null
        if ((getData != null) && (setData != null)) {
            this.path = Binding(get = getData, set = setData)
        } else {
            this.path = null
        }
        this.destinationKeyTransformer = destinationKeyTransformer
    }

    @Composable
    override fun Render(context: ComposeContext) {
        // Have to use rememberSaveable for e.g. a nav stack in each tab. Make the collectors non-erasable so that
        // destinations defined at e.g. the root nav stack layer don't disappear when you push.
        val (destinations, destinationsCollector) = rememberSaveablePreferenceCollector(key = NavigationDestinationsPreferenceKey::class, stateSaver = context.stateSaver as Saver<Preference<Dictionary<AnyHashable, NavigationDestination>>, Any>, isErasable = false)
        val (destinationLayoutHints, destinationLayoutHintsCollector) = rememberSaveablePreferenceCollector(key = NavigationDestinationLayoutHintsPreferenceKey::class, stateSaver = context.stateSaver as Saver<Preference<Dictionary<AnyHashable, NavigationStackLayoutHints>>, Any>, isErasable = false)
        val reducedDestinations = destinations.value.reduced.sref()
        val reducedDestinationLayoutHints = destinationLayoutHints.value.reduced.sref()
        val mergedDestinations = mergeNavigationDestinationsWithLayoutHints(reducedDestinations, layoutHints = reducedDestinationLayoutHints)
        val navBackStack = rememberNavBackStack(SkipNavigationStackRootKey.root)
        val navigator = rememberSaveable(stateSaver = context.stateSaver as Saver<Navigator, Any>) { -> mutableStateOf(Navigator(navBackStack = navBackStack, destinations = mergedDestinations, destinationKeyTransformer = destinationKeyTransformer)) }
        navigator.value.didCompose(navBackStack = navBackStack, destinations = mergedDestinations, path = path, navigationPath = navigationPath, keyboardController = LocalSoftwareKeyboardController.current)

        val providedNavigator = LocalNavigator provides navigator.value
        CompositionLocalProvider(providedNavigator) { ->
            val safeArea = EnvironmentValues.shared._safeArea
            // We have to ignore the safe area around the entire NavDisplay to prevent push/pop animation issues with the system bars.
            // When we layout, only extend into safe areas that are due to system bars, not into any app chrome
            var ignoresSafeAreaEdges: Edge.Set = Edge.Set.of(Edge.Set.top, Edge.Set.bottom)
            ignoresSafeAreaEdges.formIntersection(safeArea?.absoluteSystemBarEdges ?: Edge.Set.of())
            IgnoresSafeAreaLayout(expandInto = ignoresSafeAreaEdges, checkEdges = ignoresSafeAreaEdges, logTag = "NavigationStack") { _, _ ->
                ComposeContainer(modifier = context.modifier, fillWidth = true, fillHeight = true) { modifier ->
                    val decoratorList = listOf(rememberSaveableStateHolderNavEntryDecorator<NavKey>())
                    val entryProvider = entryProvider { ->
                        entry<SkipNavigationStackRootKey> l@{ _ ->
                            val state_0 = navigator.value.stateForRoot()
                            if (state_0 == null) {
                                return@l
                            }
                            // These preferences are per-entry, but if we put them in RenderEntry then their initial values don't show
                            // during the navigation animation. We have to collect them here
                            val (title, titleCollector) = rememberSaveablePreferenceCollector(key = NavigationTitlePreferenceKey::class, stateSaver = state_0.stateSaver as Saver<Preference<Text>, Any>)
                            val (toolbarPreferences, toolbarPreferencesCollector) = rememberSaveablePreferenceCollector(key = ToolbarPreferenceKey::class, stateSaver = state_0.stateSaver as Saver<Preference<ToolbarPreferences>, Any>)
                            val (toolbarContentPreferences, toolbarContentPreferencesCollector) = rememberSaveablePreferenceCollector(key = ToolbarContentPreferenceKey::class, stateSaver = state_0.stateSaver as Saver<Preference<ToolbarContentPreferences>, Any>)
                            val arguments = NavigationEntryArguments(isRoot = true, state = state_0, safeArea = safeArea, ignoresSafeAreaEdges = ignoresSafeAreaEdges, title = title.value.reduced, toolbarPreferences = toolbarPreferences.value.reduced)
                            PreferenceValues.shared.collectPreferences(arrayOf(titleCollector, toolbarPreferencesCollector, toolbarContentPreferencesCollector, destinationsCollector, destinationLayoutHintsCollector)) { ->
                                RenderEntry(navigator = navigator, toolbarContent = toolbarContentPreferences, arguments = arguments, context = context) { context -> root.Compose(context = context) }
                            }
                        }
                        entry<SkipNavigationStackPushKey> l@{ navKey ->
                            val state_1 = navigator.value.state(forPushKey = navKey)
                            if (state_1 == null) {
                                return@l
                            }
                            val targetValue_0 = state_1.targetValue.sref()
                            if (targetValue_0 == null) {
                                return@l
                            }
                            // These preferences are per-entry, but if we put them in RenderEntry then their initial values don't show
                            // during the navigation animation. We have to collect them here
                            val (title, titleCollector) = rememberSaveablePreferenceCollector(key = NavigationTitlePreferenceKey::class, stateSaver = state_1.stateSaver as Saver<Preference<Text>, Any>)
                            val (toolbarPreferences, toolbarPreferencesCollector) = rememberSaveablePreferenceCollector(key = ToolbarPreferenceKey::class, stateSaver = state_1.stateSaver as Saver<Preference<ToolbarPreferences>, Any>)
                            val (toolbarContentPreferences, toolbarContentPreferencesCollector) = rememberSaveablePreferenceCollector(key = ToolbarContentPreferenceKey::class, stateSaver = state_1.stateSaver as Saver<Preference<ToolbarContentPreferences>, Any>)
                            EnvironmentValues.shared.setValues(l@{ it ->
                                it.setdismiss(DismissAction(action = { -> navigator.value.navigateBack() }))
                                return@l ComposeResult.ok
                            }, in_ = { ->
                                val arguments = NavigationEntryArguments(isRoot = false, state = state_1, safeArea = safeArea, ignoresSafeAreaEdges = ignoresSafeAreaEdges, title = title.value.reduced, toolbarPreferences = toolbarPreferences.value.reduced)
                                PreferenceValues.shared.collectPreferences(arrayOf(titleCollector, toolbarPreferencesCollector, toolbarContentPreferencesCollector, destinationsCollector, destinationLayoutHintsCollector)) { ->
                                    RenderEntry(navigator = navigator, toolbarContent = toolbarContentPreferences, arguments = arguments, context = context) { context ->
                                        val destinationArguments = NavigationDestinationArguments(targetValue = targetValue_0)
                                        RenderDestination(state_1.destination, arguments = destinationArguments, context = context)
                                    }
                                }
                            })
                        }
                    }
                    NavDisplay(backStack = navBackStack, modifier = modifier, onBack = { -> navigator.value.navigateBack() }, transitionSpec = { ->
                        slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                    }, popTransitionSpec = { ->
                        slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                    }, predictivePopTransitionSpec = { _ ->
                        slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                    }, entryDecorators = decoratorList, entryProvider = entryProvider)
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
    @Composable
    private fun RenderEntry(navigator: MutableState<Navigator>, toolbarContent: MutableState<Preference<ToolbarContentPreferences>>, arguments: NavigationEntryArguments, context: ComposeContext, content: @Composable (ComposeContext) -> Unit) {
        val state = arguments.state
        val context = context.content(stateSaver = state.stateSaver)

        val entryLayoutHints = state.layoutHints ?: (if (arguments.isRoot) EnvironmentValues.shared._navigationStackLayoutHints else null)
        val hasTitleFromPreferences = arguments.title != NavigationTitlePreferenceKey.defaultValue
        val hasTitle: Boolean
        val title: Text
        val titleDisplayPreference: ToolbarTitleDisplayMode?
        if (entryLayoutHints != null) {
            val layoutHints = entryLayoutHints
            val hasTitleFromHints = layoutHints.expectedTitle != NavigationTitlePreferenceKey.defaultValue
            hasTitle = hasTitleFromPreferences || hasTitleFromHints
            title = if (hasTitleFromPreferences) arguments.title else (if (hasTitleFromHints) layoutHints.expectedTitle else arguments.title)
            titleDisplayPreference = arguments.toolbarPreferences.titleDisplayMode ?: layoutHints.expectedTitleDisplayMode
        } else {
            hasTitle = hasTitleFromPreferences
            title = arguments.title
            titleDisplayPreference = arguments.toolbarPreferences.titleDisplayMode
        }

        val topBarPreferences = arguments.toolbarPreferences.navigationBar
        val bottomBarPreferences = arguments.toolbarPreferences.bottomBar
        val effectiveTitleDisplayMode = navigator.value.titleDisplayMode(for_ = state, hasTitle = hasTitle, preference = titleDisplayPreference)
        val isInlineTitleDisplayMode = useInlineTitleDisplayMode(for_ = effectiveTitleDisplayMode, safeArea = arguments.safeArea)

        // We would like to only process toolbar content in our topBar/bottomBar Composables, but composing
        // custom ToolbarContent multiple times (in order to process the placement of the items in its body
        // content for each bar) prevents it from updating properly on recompose
        val toolbarContentReduced = toolbarContent.value.reduced.sref()
        val toolbarItems = ToolbarItems(content = toolbarContentReduced.content ?: arrayOf())
        val (titleMenu, topLeadingItems, topTrailingItems, bottomItems) = toolbarItems.Evaluate(context = context)

        val showTopBar: Boolean
        when (topBarPreferences?.visibility ?: Visibility.automatic) {
            Visibility.hidden -> showTopBar = false
            Visibility.visible -> showTopBar = true
            Visibility.automatic -> showTopBar = !arguments.isRoot || hasTitle || topLeadingItems.size > 0 || topTrailingItems.size > 0
        }
        val searchFieldPadding = 16.dp.sref()
        val density = LocalDensity.current.sref()
        val searchFieldHeightPx = with(density) { -> searchFieldHeight.dp.toPx() + searchFieldPadding.toPx() }
        val searchFieldOffsetPx = rememberSaveable(stateSaver = context.stateSaver as Saver<Float, Any>) { -> mutableStateOf(0.0f) }
        val searchFieldScrollConnection = remember { -> SearchFieldScrollConnection(heightPx = searchFieldHeightPx, offsetPx = searchFieldOffsetPx) }

        val (searchableStatePreference, searchableStateCollector) = rememberSaveablePreferenceCollector(key = SearchableStatePreferenceKey::class, stateSaver = context.stateSaver as Saver<Preference<SearchableState?>, Any>)

        val (scrollToTop, scrollToTopCollector) = rememberSaveablePreferenceCollector(key = ScrollToTopPreferenceKey::class, stateSaver = context.stateSaver as Saver<Preference<ScrollToTopAction>, Any>)

        val initialScrollBehavior = if (isInlineTitleDisplayMode) TopAppBarDefaults.pinnedScrollBehavior() else TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        // Determine the final scrollBehavior early by checking if the environment value would modify it
        // We need to do this before we create the nestedScroll modifier so we attach the correct nestedScrollConnection
        val scrollBehavior: TopAppBarScrollBehavior
        val navigationIconButtonStyle: Material3TopAppBarNavigationIconButtonStyle
        val navigationIconButtonColors: IconButtonColors?
        val matchtarget_0 = EnvironmentValues.shared._material3TopAppBar
        if (matchtarget_0 != null) {
            val updateOptions = matchtarget_0
            val tempOptions = Material3TopAppBarOptions(title = { ->  }, modifier = Modifier, navigationIcon = { ->  }, colors = TopAppBarDefaults.topAppBarColors(), scrollBehavior = initialScrollBehavior)
            val updatedOptions = updateOptions(tempOptions)
            scrollBehavior = (updatedOptions.scrollBehavior ?: initialScrollBehavior).sref()
            navigationIconButtonStyle = updatedOptions.navigationIconButtonStyle
            navigationIconButtonColors = updatedOptions.navigationIconButtonColors.sref()
        } else {
            scrollBehavior = initialScrollBehavior.sref()
            navigationIconButtonStyle = Material3TopAppBarNavigationIconButtonStyle.iconButton
            navigationIconButtonColors = null
        }
        var modifier = Modifier.nestedScroll(searchFieldScrollConnection)
        if (showTopBar) {
            modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
        }
        modifier = modifier.then(context.modifier)

        val defaultTopBarHeight = 112.dp.sref()
        val topBarBottomPx = remember { ->
            // Default our initial value to the expected value, which helps avoid visual artifacts as we measure actual values and
            // recompose with adjusted layouts
            val safeAreaTopPx = arguments.safeArea?.safeBoundsPx?.top ?: 0.0f
            mutableStateOf(with(density) { -> safeAreaTopPx + defaultTopBarHeight.toPx() })
        }
        val topBarHeightPx = remember { ->
            mutableStateOf(with(density) { -> defaultTopBarHeight.toPx() })
        }

        val isSystemBackground = topBarPreferences?.isSystemBackground == true
        val topBar: @Composable () -> Unit = { ->
            val animation: Animation = (Animation.current(isAnimating = false) ?: topBarPreferences?.visibilityAnimation ?: Animation.linear(duration = 0.0)).sref()
            val animationSpec: AnimationSpec<Any> = animation.asAnimationSpec()
            val moveEdgeTop = MoveTransition(edge = Edge.top)
            val topBarEnter = moveEdgeTop.asEnterTransition(spec = animationSpec)
            val topBarExit = moveEdgeTop.asExitTransition(spec = animationSpec)
            AnimatedVisibility(visible = showTopBar, modifier = Modifier.fillMaxWidth(), enter = topBarEnter, exit = topBarExit, label = "NavigationTopBar") { ->
                DisposableEffect(true) { ->
                    onDispose { ->
                        topBarBottomPx.value = 0.0f
                        topBarHeightPx.value = 0.0f
                    }
                }
                val isOverlapped = scrollBehavior.state.overlappedFraction > 0
                val materialColorScheme: androidx.compose.material3.ColorScheme
                if (isOverlapped) {
                    val matchtarget_1 = topBarPreferences?.colorScheme?.asMaterialTheme()
                    if (matchtarget_1 != null) {
                        val customColorScheme = matchtarget_1
                        materialColorScheme = customColorScheme.sref()
                    } else {
                        materialColorScheme = MaterialTheme.colorScheme.sref()
                    }
                } else {
                    materialColorScheme = MaterialTheme.colorScheme.sref()
                }
                MaterialTheme(colorScheme = materialColorScheme) { ->
                    val topBarBackgroundColor: androidx.compose.ui.graphics.Color
                    val unscrolledTopBarBackgroundColor: androidx.compose.ui.graphics.Color
                    val topBarBackgroundForBrush: ShapeStyle?
                    // If there is a custom color scheme, we also always show any custom background even when unscrolled, because we can't
                    // properly interpolate between the title text colors
                    val topBarHasColorScheme = topBarPreferences?.colorScheme != null
                    val isSystemBackground = topBarPreferences?.isSystemBackground == true
                    if (topBarPreferences?.backgroundVisibility == Visibility.hidden) {
                        topBarBackgroundColor = androidx.compose.ui.graphics.Color.Transparent
                        unscrolledTopBarBackgroundColor = androidx.compose.ui.graphics.Color.Transparent
                        topBarBackgroundForBrush = null
                    } else {
                        val matchtarget_2 = topBarPreferences?.background
                        if (matchtarget_2 != null) {
                            val background = matchtarget_2
                            val matchtarget_3 = background.asColor(opacity = 1.0, animationContext = null)
                            if (matchtarget_3 != null) {
                                val color = matchtarget_3
                                topBarBackgroundColor = color
                                unscrolledTopBarBackgroundColor = if (isSystemBackground) Color.systemBarBackground.colorImpl() else color.copy(alpha = 0.0f)
                                topBarBackgroundForBrush = null
                            } else {
                                unscrolledTopBarBackgroundColor = if (isSystemBackground) Color.systemBarBackground.colorImpl() else androidx.compose.ui.graphics.Color.Transparent
                                topBarBackgroundColor = if (!topBarHasColorScheme || isOverlapped) unscrolledTopBarBackgroundColor.copy(alpha = 0.0f) else unscrolledTopBarBackgroundColor
                                topBarBackgroundForBrush = background.sref()
                            }
                        } else {
                            topBarBackgroundColor = Color.systemBarBackground.colorImpl()
                            unscrolledTopBarBackgroundColor = if (isSystemBackground) topBarBackgroundColor else topBarBackgroundColor.copy(alpha = 0.0f)
                            topBarBackgroundForBrush = null
                        }
                    }

                    val tint = (EnvironmentValues.shared._tint ?: Color(colorImpl = { -> MaterialTheme.colorScheme.onSurface })).sref()
                    val placement = EnvironmentValues.shared._placement.sref()
                    EnvironmentValues.shared.setValues(l@{ it ->
                        it.set_placement(placement.union(ViewPlacement.toolbar))
                        it.set_tint(tint)
                        return@l ComposeResult.ok
                    }, in_ = { ->
                        val interactionSource = remember { -> MutableInteractionSource() }
                        var topBarModifier = Modifier.zIndex(1.1f)
                            .clickable(interactionSource = interactionSource, indication = null, onClick = { -> scrollToTop.value.reduced.action() })
                            .onGloballyPositionedInWindow { bounds ->
                                topBarBottomPx.value = bounds.bottom
                                topBarHeightPx.value = bounds.bottom - bounds.top
                            }
                        if ((!topBarHasColorScheme || isOverlapped) && (topBarBackgroundForBrush != null)) {
                            val opacity = if (topBarHasColorScheme) 1.0 else if (isInlineTitleDisplayMode) min(1.0, Double(scrollBehavior.state.overlappedFraction * 5)) else Double(scrollBehavior.state.collapsedFraction)
                            topBarBackgroundForBrush.asBrush(opacity = opacity, animationContext = null)?.let { topBarBackgroundBrush ->
                                topBarModifier = topBarModifier.background(topBarBackgroundBrush)
                            }
                        }
                        val alwaysShowScrolledBackground = topBarPreferences?.backgroundVisibility == Visibility.visible
                        val topBarColors = TopAppBarDefaults.topAppBarColors(containerColor = if (alwaysShowScrolledBackground) topBarBackgroundColor else unscrolledTopBarBackgroundColor, scrolledContainerColor = topBarBackgroundColor, titleContentColor = MaterialTheme.colorScheme.onSurface)
                        val topBarTitle: @Composable () -> Unit = { ->
                            if (titleMenu != null) {
                                val menuModifier = Modifier.clickable(interactionSource = interactionSource, indication = null, onClick = { -> titleMenu.toggleMenu() })
                                val arrangement = Arrangement.spacedBy(2.dp, alignment = androidx.compose.ui.Alignment.CenterHorizontally)
                                Row(modifier = menuModifier, horizontalArrangement = arrangement, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) { ->
                                    androidx.compose.material3.Text(title.localizedTextString(), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Image(systemName = "chevron.down").accessibilityHidden(true).Compose(context = context)
                                }
                                titleMenu.Render(context = context)
                            } else {
                                androidx.compose.material3.Text(title.localizedTextString(), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                        val topBarNavigationIcon: @Composable () -> Unit = { ->
                            val hasBackButton = !arguments.isRoot && arguments.toolbarPreferences.backButtonHidden != true
                            if (hasBackButton || topLeadingItems.size > 0) {
                                val toolbarItemContext = context.content(modifier = Modifier.padding(start = 12.dp, end = 12.dp))
                                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) { ->
                                    if (hasBackButton) {
                                        val isRTL = EnvironmentValues.shared.layoutDirection == LayoutDirection.rightToLeft
                                        val backIcon: @Composable () -> Unit = { -> Icon(imageVector = (if (isRTL) Icons.Filled.ArrowForward else Icons.Filled.ArrowBack), contentDescription = "Back", tint = tint.colorImpl()) }
                                        when (navigationIconButtonStyle) {
                                            Material3TopAppBarNavigationIconButtonStyle.filledIconButton -> {
                                                FilledIconButton(onClick = { -> navigator.value.navigateBack() }, colors = navigationIconButtonColors ?: IconButtonDefaults.filledIconButtonColors()) { -> backIcon() }
                                            }
                                            Material3TopAppBarNavigationIconButtonStyle.iconButton -> {
                                                IconButton(onClick = { -> navigator.value.navigateBack() }, colors = navigationIconButtonColors ?: IconButtonDefaults.iconButtonColors()) { -> backIcon() }
                                            }
                                        }
                                    }
                                    for (renderable in topLeadingItems.sref()) {
                                        renderable.Render(context = toolbarItemContext)
                                    }
                                }
                            }
                        }
                        val topBarActions: @Composable () -> Unit = { ->
                            val toolbarItemContext = context.content(modifier = Modifier.padding(start = 12.dp, end = 12.dp))
                            for (renderable in topTrailingItems.sref()) {
                                renderable.Render(context = toolbarItemContext)
                            }
                        }
                        var options = Material3TopAppBarOptions(title = topBarTitle, modifier = topBarModifier, navigationIcon = topBarNavigationIcon, colors = topBarColors, scrollBehavior = scrollBehavior)
                        EnvironmentValues.shared._material3TopAppBar?.let { updateOptions ->
                            options = updateOptions(options)
                        }
                        // Use scrollBehavior (from the early call) for the TopAppBar to ensure it matches the nestedScrollConnection
                        options = options.copy(scrollBehavior = scrollBehavior)
                        if (isInlineTitleDisplayMode) {
                            if (options.preferCenterAlignedStyle) {
                                CenterAlignedTopAppBar(title = options.title, modifier = options.modifier, navigationIcon = options.navigationIcon, actions = { -> topBarActions() }, colors = options.colors, scrollBehavior = options.scrollBehavior)
                            } else {
                                TopAppBar(title = options.title, modifier = options.modifier, navigationIcon = options.navigationIcon, actions = { -> topBarActions() }, colors = options.colors, scrollBehavior = options.scrollBehavior)
                            }
                        } else {
                            // Force a larger, bold title style in the uncollapsed state by replacing the headlineSmall style the bar uses
                            val typography = MaterialTheme.typography.sref()
                            val appBarTitleStyle = typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
                            val appBarTypography = typography.copy(headlineSmall = appBarTitleStyle)
                            MaterialTheme(colorScheme = MaterialTheme.colorScheme, typography = appBarTypography, shapes = MaterialTheme.shapes) { ->
                                if (options.preferLargeStyle) {
                                    LargeTopAppBar(title = options.title, modifier = options.modifier, navigationIcon = options.navigationIcon, actions = { -> topBarActions() }, colors = options.colors, scrollBehavior = options.scrollBehavior)
                                } else {
                                    MediumTopAppBar(title = options.title, modifier = options.modifier, navigationIcon = options.navigationIcon, actions = { -> topBarActions() }, colors = options.colors, scrollBehavior = options.scrollBehavior)
                                }
                            }
                        }
                    })
                }
            }
        }

        val bottomBarTopPx = remember { -> mutableStateOf(0.0f) }
        val bottomBarHeightPx = remember { -> mutableStateOf(0.0f) }
        val bottomBar: @Composable () -> Unit = l@{ ->
            if (bottomBarPreferences?.visibility == Visibility.hidden) {
                SideEffect { ->
                    bottomBarTopPx.value = 0.0f
                    bottomBarHeightPx.value = 0.0f
                }
                return@l
            }
            if (bottomItems.size <= 0 && bottomBarPreferences?.visibility != Visibility.visible) {
                SideEffect { ->
                    bottomBarTopPx.value = 0.0f
                    bottomBarHeightPx.value = 0.0f
                }
                return@l
            }

            val showScrolledBackground = bottomBarPreferences?.backgroundVisibility == Visibility.visible || bottomBarPreferences?.scrollableState?.canScrollForward == true
            val materialColorScheme: androidx.compose.material3.ColorScheme
            if (showScrolledBackground) {
                val matchtarget_4 = bottomBarPreferences?.colorScheme?.asMaterialTheme()
                if (matchtarget_4 != null) {
                    val customColorScheme = matchtarget_4
                    materialColorScheme = customColorScheme.sref()
                } else {
                    materialColorScheme = MaterialTheme.colorScheme.sref()
                }
            } else {
                materialColorScheme = MaterialTheme.colorScheme.sref()
            }
            MaterialTheme(colorScheme = materialColorScheme) { ->
                val bottomBarBackgroundColor: androidx.compose.ui.graphics.Color
                val unscrolledBottomBarBackgroundColor: androidx.compose.ui.graphics.Color
                val bottomBarBackgroundForBrush: ShapeStyle?
                val bottomBarHasColorScheme = bottomBarPreferences?.colorScheme != null
                if (bottomBarPreferences?.backgroundVisibility == Visibility.hidden) {
                    bottomBarBackgroundColor = androidx.compose.ui.graphics.Color.Transparent
                    unscrolledBottomBarBackgroundColor = androidx.compose.ui.graphics.Color.Transparent
                    bottomBarBackgroundForBrush = null
                } else {
                    val matchtarget_5 = bottomBarPreferences?.background
                    if (matchtarget_5 != null) {
                        val background = matchtarget_5
                        val matchtarget_6 = background.asColor(opacity = 1.0, animationContext = null)
                        if (matchtarget_6 != null) {
                            val color = matchtarget_6
                            bottomBarBackgroundColor = color
                            unscrolledBottomBarBackgroundColor = if (isSystemBackground) Color.systemBarBackground.colorImpl() else color.copy(alpha = 0.0f)
                            bottomBarBackgroundForBrush = null
                        } else {
                            unscrolledBottomBarBackgroundColor = if (isSystemBackground) Color.systemBarBackground.colorImpl() else androidx.compose.ui.graphics.Color.Transparent
                            bottomBarBackgroundColor = unscrolledBottomBarBackgroundColor.copy(alpha = 0.0f)
                            bottomBarBackgroundForBrush = background.sref()
                        }
                    } else {
                        bottomBarBackgroundColor = Color.systemBarBackground.colorImpl()
                        unscrolledBottomBarBackgroundColor = if (isSystemBackground) bottomBarBackgroundColor else bottomBarBackgroundColor.copy(alpha = 0.0f)
                        bottomBarBackgroundForBrush = null
                    }
                }

                val tint = (EnvironmentValues.shared._tint ?: Color(colorImpl = { -> MaterialTheme.colorScheme.onSurface })).sref()
                val placement = EnvironmentValues.shared._placement.sref()
                EnvironmentValues.shared.setValues(l@{ it ->
                    it.set_tint(tint)
                    it.set_placement(placement.union(ViewPlacement.toolbar))
                    return@l ComposeResult.ok
                }, in_ = { ->
                    var bottomBarModifier = Modifier.zIndex(1.1f)
                        .onGloballyPositionedInWindow { bounds ->
                            bottomBarTopPx.value = bounds.top
                            bottomBarHeightPx.value = bounds.bottom - bounds.top
                        }
                    if (showScrolledBackground && (bottomBarBackgroundForBrush != null)) {
                        bottomBarBackgroundForBrush.asBrush(opacity = 1.0, animationContext = null)?.let { bottomBarBackgroundBrush ->
                            bottomBarModifier = bottomBarModifier.background(bottomBarBackgroundBrush)
                        }
                    }
                    // Pull the bottom bar below the keyboard
                    val bottomPadding = with(density) { -> min(bottomBarHeightPx.value, Float(WindowInsets.ime.getBottom(density))).toDp() }
                    PaddingLayout(padding = EdgeInsets(top = 0.0, leading = 0.0, bottom = Double(-bottomPadding.value), trailing = 0.0), context = context.content()) { context ->
                        val containerColor = if (showScrolledBackground) bottomBarBackgroundColor else unscrolledBottomBarBackgroundColor
                        val windowInsets = (if (EnvironmentValues.shared._isEdgeToEdge == true) BottomAppBarDefaults.windowInsets else WindowInsets(bottom = 0.dp)).sref()
                        var options = Material3BottomAppBarOptions(modifier = context.modifier.then(bottomBarModifier), containerColor = containerColor, contentColor = MaterialTheme.colorScheme.contentColorFor(containerColor), contentPadding = PaddingValues.Absolute(left = 16.dp, right = 16.dp))
                        EnvironmentValues.shared._material3BottomAppBar?.let { updateOptions ->
                            options = updateOptions(options)
                        }
                        BottomAppBar(modifier = options.modifier, containerColor = options.containerColor, contentColor = options.contentColor, tonalElevation = options.tonalElevation, contentPadding = options.contentPadding, windowInsets = windowInsets) { ->
                            // Use an HStack so that it sets up the environment for bottom toolbar Spacers
                            HStack(spacing = 24.0) { ->
                                ComposeBuilder { composectx: ComposeContext ->
                                    ComposeView { context ->
                                        for (renderable in bottomItems.sref()) {
                                            renderable.Render(context = context)
                                        }
                                    }.Compose(composectx)
                                    ComposeResult.ok
                                }
                            }.Compose(context)
                        }
                    }
                })
            }
        }

        // We place nav bars within each entry rather than at the navigation controller level so toolbar preferences apply per entry.

        val layoutImplementationVersion = EnvironmentValues.shared._layoutImplementationVersion
        if (layoutImplementationVersion < 2) {
            // Old Column layout (version < 2)
            Column(modifier = modifier.background(Color.background.colorImpl())) { ->
                // Calculate safe area for content
                val contentSafeArea = arguments.safeArea?.insetting(Edge.top, to = topBarBottomPx.value)?.insetting(Edge.bottom, to = bottomBarTopPx.value)
                // Inset manually for any edge where our container ignored the safe area, but we aren't showing a bar
                val topPadding = (if (topBarBottomPx.value <= 0.0f && arguments.ignoresSafeAreaEdges.contains(Edge.Set.top)) WindowInsets.safeDrawing.asPaddingValues().calculateTopPadding() else 0.dp).sref()
                var bottomPadding = 0.dp.sref()
                if (bottomBarTopPx.value <= 0.0f && arguments.ignoresSafeAreaEdges.contains(Edge.Set.bottom)) {
                    bottomPadding = max(0.dp, WindowInsets.safeDrawing.asPaddingValues().calculateBottomPadding() - WindowInsets.ime.asPaddingValues().calculateBottomPadding())
                }
                val contentModifier = Modifier.fillMaxWidth().weight(1.0f).padding(top = topPadding, bottom = bottomPadding)

                topBar()
                Box(modifier = contentModifier, contentAlignment = androidx.compose.ui.Alignment.Center) { ->
                    var topPadding = 0.dp.sref()
                    val searchableState: SearchableState? = if (arguments.isRoot) (EnvironmentValues.shared._searchableState ?: searchableStatePreference.value.reduced) else null
                    if (searchableState != null) {
                        val searchFieldBackground = if (isSystemBackground) Color.systemBarBackground.colorImpl() else androidx.compose.ui.graphics.Color.Transparent
                        val searchFieldFadeOffset = searchFieldHeightPx / 3
                        val searchFieldModifier = Modifier.height(searchFieldHeight.dp + searchFieldPadding)
                            .align(androidx.compose.ui.Alignment.TopCenter)
                            .offset({ -> IntOffset(0, Int(searchFieldOffsetPx.value)) })
                            .background(searchFieldBackground)
                            .padding(start = searchFieldPadding, bottom = searchFieldPadding, end = searchFieldPadding)
                            .graphicsLayer { -> alpha = max(0.0f, (searchFieldFadeOffset + searchFieldOffsetPx.value) / searchFieldFadeOffset) }
                            .fillMaxWidth()
                        SearchField(state = searchableState, context = context.content(modifier = searchFieldModifier))
                        val searchFieldPlaceholderPadding = (searchFieldHeight.dp + searchFieldPadding + (with(LocalDensity.current) { -> searchFieldOffsetPx.value.toDp() })).sref()
                        topPadding = searchFieldPlaceholderPadding.sref()
                    }
                    EnvironmentValues.shared.setValues(l@{ it ->
                        if (contentSafeArea != null) {
                            it.set_safeArea(contentSafeArea)
                        }
                        it.set_searchableState(searchableState)
                        it.set_isNavigationRoot(arguments.isRoot)
                        it.set_nestedScrollConnection(scrollBehavior.nestedScrollConnection)
                        return@l ComposeResult.ok
                    }, in_ = { ->
                        // Elevate the top padding modifier so that content always has the same context, allowing it to avoid recomposition
                        Box(modifier = Modifier.padding(top = topPadding)) { ->
                            PreferenceValues.shared.collectPreferences(arrayOf(searchableStateCollector, scrollToTopCollector)) { -> content(context.content()) }
                        }
                    })
                }
                bottomBar()
            }
        } else {
            // New Box layout (version >= 2)
            Box(modifier = modifier.background(Color.background.colorImpl()).fillMaxSize()) { ->
                // Calculate safe area for content by insetting by topBar and bottomBar heights
                var contentSafeArea: SafeArea? = null
                arguments.safeArea?.let { safeArea ->
                    val clampedTopBarBottomPxValue: Float = max(topBarBottomPx.value, safeArea.safeBoundsPx.top)
                    contentSafeArea = safeArea
                        .insetting(Edge.top, to = clampedTopBarBottomPxValue)
                        .insetting(Edge.bottom, to = bottomBarTopPx.value)
                }

                // Top bar aligned to top
                Box(modifier = Modifier.zIndex(1.1f).align(androidx.compose.ui.Alignment.TopCenter)) { -> topBar() }

                // Bottom bar aligned to bottom
                Box(modifier = Modifier.zIndex(1.1f).align(androidx.compose.ui.Alignment.BottomCenter)) { -> bottomBar() }

                // Constrain the content to the area between the top bar and bottom bar. In the Box layout we use
                // fillMaxSize(), so we must add top/bottom padding to reserve space for our nav bars. Use the
                // measured topBarBottomPx and bottomBarHeightPx when the bars are visible. When a bar is hidden,
                // inset by the system safe area (WindowInsets.safeDrawing) for that edge when
                // arguments.ignoresSafeAreaEdges contains it, so content does not overlap the status bar or home
                // indicator.
                var contentModifier = Modifier.fillMaxSize()
                val safeTopDp = WindowInsets.safeDrawing.asPaddingValues().calculateTopPadding()
                val topBarHeightDp = with(density) { -> topBarHeightPx.value.toDp() }
                val topPadding = (if (arguments.ignoresSafeAreaEdges.contains(Edge.Set.top)) max(topBarHeightDp, safeTopDp) else topBarHeightDp).sref()
                val bottomPadding = if (bottomBarHeightPx.value <= 0.0f && arguments.ignoresSafeAreaEdges.contains(Edge.Set.bottom)) max(0.dp, WindowInsets.safeDrawing.asPaddingValues().calculateBottomPadding() - WindowInsets.ime.asPaddingValues().calculateBottomPadding()) else with(density) { -> bottomBarHeightPx.value.toDp() }
                contentModifier = contentModifier.padding(top = topPadding, bottom = bottomPadding)
                Box(modifier = contentModifier, contentAlignment = androidx.compose.ui.Alignment.Center) { ->
                    var topPadding = 0.dp.sref()
                    val searchableState: SearchableState? = if (arguments.isRoot) (EnvironmentValues.shared._searchableState ?: searchableStatePreference.value.reduced) else null
                    if (searchableState != null) {
                        val searchFieldBackground = if (isSystemBackground) Color.systemBarBackground.colorImpl() else androidx.compose.ui.graphics.Color.Transparent
                        val searchFieldFadeOffset = searchFieldHeightPx / 3
                        val searchFieldModifier = Modifier.height(searchFieldHeight.dp + searchFieldPadding)
                            .align(androidx.compose.ui.Alignment.TopCenter)
                            .offset({ -> IntOffset(0, Int(searchFieldOffsetPx.value)) })
                            .background(searchFieldBackground)
                            .padding(start = searchFieldPadding, bottom = searchFieldPadding, end = searchFieldPadding)
                            .graphicsLayer { -> alpha = max(0.0f, (searchFieldFadeOffset + searchFieldOffsetPx.value) / searchFieldFadeOffset) }
                            .fillMaxWidth()
                        SearchField(state = searchableState, context = context.content(modifier = searchFieldModifier))
                        val searchFieldPlaceholderPadding = (searchFieldHeight.dp + searchFieldPadding + (with(LocalDensity.current) { -> searchFieldOffsetPx.value.toDp() })).sref()
                        topPadding = searchFieldPlaceholderPadding.sref()
                    }
                    EnvironmentValues.shared.setValues(l@{ it ->
                        if (contentSafeArea != null) {
                            it.set_safeArea(contentSafeArea)
                        }
                        it.set_searchableState(searchableState)
                        it.set_isNavigationRoot(arguments.isRoot)
                        it.set_nestedScrollConnection(scrollBehavior.nestedScrollConnection)
                        return@l ComposeResult.ok
                    }, in_ = { ->
                        // Elevate the top padding modifier so that content always has the same context, allowing it to avoid recomposition
                        Box(modifier = Modifier.padding(top = topPadding)) { ->
                            PreferenceValues.shared.collectPreferences(arrayOf(searchableStateCollector, scrollToTopCollector)) { -> content(context.content()) }
                        }
                    })
                }
            }
        }
    }

    @Composable
    private fun RenderDestination(destination: ((Any) -> View)?, arguments: NavigationDestinationArguments, context: ComposeContext) {
        // Break out this function to give it stable arguments and avoid recomosition on push/pop
        destination?.invoke(arguments.targetValue)?.Compose(context = context)
    }

    @Composable
    private fun useInlineTitleDisplayMode(for_: ToolbarTitleDisplayMode, safeArea: SafeArea?): Boolean {
        val titleDisplayMode = for_
        if (titleDisplayMode != ToolbarTitleDisplayMode.automatic) {
            return titleDisplayMode == ToolbarTitleDisplayMode.inline_
        }
        // Default to inline if in landscape or a sheet
        if ((safeArea != null) && (safeArea.presentationBoundsPx.width > safeArea.presentationBoundsPx.height)) {
            return true
        }
        return EnvironmentValues.shared._sheetDepth > 0
    }

    override fun Swift_projection(options: Int): () -> Any = Swift_projectionImpl(options)
    private external fun Swift_projectionImpl(options: Int): () -> Any

    @androidx.annotation.Keep
    companion object {
    }
}


@Serializable
enum class SkipNavigationStackRootKey: NavKey {
    root;

    @androidx.annotation.Keep
    companion object {
    }
}

@Serializable
class SkipNavigationStackPushKey: NavKey {
    val destinationIndex: Int
    val identifier: String

    constructor(destinationIndex: Int, identifier: String) {
        this.destinationIndex = destinationIndex
        this.identifier = identifier
    }

    override fun equals(other: Any?): Boolean {
        if (other !is SkipNavigationStackPushKey) return false
        return destinationIndex == other.destinationIndex && identifier == other.identifier
    }

    override fun hashCode(): Int {
        var result = 1
        result = Hasher.combine(result, destinationIndex)
        result = Hasher.combine(result, identifier)
        return result
    }

    @androidx.annotation.Keep
    companion object {
    }
}

@Stable
internal class NavigationEntryArguments {
    internal val isRoot: Boolean
    internal val state: Navigator.BackStackState
    internal val safeArea: SafeArea?
    internal val ignoresSafeAreaEdges: Edge.Set
    internal val title: Text
    internal val toolbarPreferences: ToolbarPreferences

    constructor(isRoot: Boolean, state: Navigator.BackStackState, safeArea: SafeArea? = null, ignoresSafeAreaEdges: Edge.Set, title: Text, toolbarPreferences: ToolbarPreferences) {
        this.isRoot = isRoot
        this.state = state
        this.safeArea = safeArea
        this.ignoresSafeAreaEdges = ignoresSafeAreaEdges.sref()
        this.title = title
        this.toolbarPreferences = toolbarPreferences
    }

    override fun equals(other: Any?): Boolean {
        if (other !is NavigationEntryArguments) return false
        return isRoot == other.isRoot && state == other.state && safeArea == other.safeArea && ignoresSafeAreaEdges == other.ignoresSafeAreaEdges && title == other.title && toolbarPreferences == other.toolbarPreferences
    }
}

@Stable
internal class NavigationDestinationArguments {
    internal val targetValue: Any

    constructor(targetValue: Any) {
        this.targetValue = targetValue.sref()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is NavigationDestinationArguments) return false
        return targetValue == other.targetValue
    }
}

internal typealias NavigationDestinations = Dictionary<AnyHashable, NavigationDestination>
internal typealias NavigationDestinationLayoutHintsMap = Dictionary<AnyHashable, NavigationStackLayoutHints>

internal class NavigationDestination {
    internal val destination: (Any) -> View
    internal val layoutHints: NavigationStackLayoutHints?

    internal constructor(destination: (Any) -> View, layoutHints: NavigationStackLayoutHints? = null) {
        this.destination = destination
        this.layoutHints = layoutHints
    }

    // No way to compare closures. Assume equal so we don't think our destinations are constantly updating
    override fun equals(other: Any?): Boolean = true
}

private fun mergeNavigationDestinationsWithLayoutHints(destinations: Dictionary<AnyHashable, NavigationDestination>, layoutHints: Dictionary<AnyHashable, NavigationStackLayoutHints>): Dictionary<AnyHashable, NavigationDestination> {
    if (layoutHints.isEmpty) {
        return destinations.sref()
    }
    var merged = destinations.sref()
    for ((key, hint) in layoutHints.sref()) {
        merged[key]?.let { existing ->
            merged[key] = NavigationDestination(destination = existing.destination, layoutHints = hint)
        }
    }
    return merged.sref()
}

@Stable
@Suppress("MUST_BE_INITIALIZED")
internal class Navigator {

    private var navBackStack: NavBackStack<NavKey>
        get() = field.sref({ this.navBackStack = it })
        set(newValue) {
            field = newValue.sref()
        }
    private var keyboardController: SoftwareKeyboardController? = null
        get() = field.sref({ this.keyboardController = it })
        set(newValue) {
            field = newValue.sref()
        }
    private var destinations: Dictionary<AnyHashable, NavigationDestination>
        get() = field.sref({ this.destinations = it })
        set(newValue) {
            field = newValue.sref()
        }
    private var destinationIndexes: Dictionary<AnyHashable, Int> = dictionaryOf()
        get() = field.sref({ this.destinationIndexes = it })
        set(newValue) {
            field = newValue.sref()
        }
    private var destinationKeyTransformer: ((Any) -> String)? = null

    // We reserve the last destination index for static destinations. Every time we navigate to a static destination view, we increment the
    // destination value to give it a unique navigation path of e.g. 99/0, 99/1, 99/2, etc
    private val viewDestinationIndex = Companion.destinationCount - 1
    private var viewDestinationValue = 0

    private var path: Binding<Array<Any>>? = null
        get() = field.sref({ this.path = it })
        set(newValue) {
            field = newValue.sref()
        }
    private var navigationPath: Binding<NavigationPath>? = null
        get() = field.sref({ this.navigationPath = it })
        set(newValue) {
            field = newValue.sref()
        }

    private var backStackState: Dictionary<String, Navigator.BackStackState> = dictionaryOf()
        get() = field.sref({ this.backStackState = it })
        set(newValue) {
            field = newValue.sref()
        }
    internal class BackStackState {
        internal val id: String
        internal val route: String
        internal val destination: ((Any) -> View)?
        internal val targetValue: Any?
        internal val layoutHints: NavigationStackLayoutHints?
        internal val stateSaver: ComposeStateSaver
        internal var titleDisplayMode: ToolbarTitleDisplayMode? = null
        internal var binding: Binding<Boolean>? = null
            get() = field.sref({ this.binding = it })
            set(newValue) {
                field = newValue.sref()
            }

        internal constructor(id: String, route: String, destination: ((Any) -> View)? = null, targetValue: Any? = null, layoutHints: NavigationStackLayoutHints? = null, stateSaver: ComposeStateSaver = ComposeStateSaver()) {
            this.id = id
            this.route = route
            this.destination = destination
            this.targetValue = targetValue.sref()
            this.layoutHints = layoutHints
            this.stateSaver = stateSaver
        }
    }

    internal constructor(navBackStack: NavBackStack<NavKey>, destinations: Dictionary<AnyHashable, NavigationDestination>, destinationKeyTransformer: ((Any) -> String)?) {
        this.navBackStack = navBackStack
        this.destinations = destinations
        this.destinationKeyTransformer = destinationKeyTransformer
        updateDestinationIndexes()
    }

    /// Call with updated state on recompose.
    @Composable
    internal fun didCompose(navBackStack: NavBackStack<NavKey>, destinations: Dictionary<AnyHashable, NavigationDestination>, path: Binding<Array<Any>>?, navigationPath: Binding<NavigationPath>?, keyboardController: SoftwareKeyboardController?) {
        this.navBackStack = navBackStack
        this.destinations = destinations
        this.path = path
        this.navigationPath = navigationPath
        this.keyboardController = keyboardController
        updateDestinationIndexes()
        syncState()
        navigateToPath()
    }

    /// Whether we're at the root of the navigation stack.
    internal val isRoot: Boolean
        get() = navBackStack.size <= 1

    /// Navigate to a target value specified in a `NavigationLink`.
    internal fun navigate(to: Any, @Suppress("UNUSED_PARAMETER") unusedp_0: Nothing? = null) {
        val targetValue = to
        val matchtarget_7 = path
        if (matchtarget_7 != null) {
            val path = matchtarget_7
            path.wrappedValue.append(targetValue)
        } else {
            val matchtarget_8 = navigationPath
            if (matchtarget_8 != null) {
                val navigationPath = matchtarget_8
                navigationPath.wrappedValue.append(targetValue)
            } else {
                navigate(toKeyed = targetValue)
            }
        }
    }

    private fun navigate(toKeyed: Any) {
        val targetValue = toKeyed
        val key: AnyHashable
        val matchtarget_9 = destinationKeyTransformer
        if (matchtarget_9 != null) {
            val destinationKeyTransformer = matchtarget_9
            key = destinationKeyTransformer(targetValue)
        } else {
            key = type(of = targetValue)
        }
        navigate(toKeyed = targetValue, key = key)
    }

    /// Navigate to a destination view.
    ///
    /// - Parameter binding: Optional binding to toggle to `false` when the view is popped.
    /// - Returns: The navigation stack entry ID of the pushed view.
    internal fun navigateToView(view: View, binding: Binding<Boolean>? = null): String? {
        val targetValue = viewDestinationValue
        viewDestinationValue += 1

        val route = Companion.route(for_ = viewDestinationIndex, valueString = String(describing = targetValue))
        return navigate(route = route, destination = { _ -> view }, layoutHints = null, targetValue = targetValue, binding = binding)
    }

    /// Pop the back stack.
    internal fun navigateBack() {
        // Check for a view destination before we pop our path bindings, because the user could push arbitrary views
        // that are not represented in the bound path
        val matchtarget_10 = navBackStack.lastOrNull() as? SkipNavigationStackPushKey
        if (matchtarget_10 != null) {
            val lastKey = matchtarget_10
            if (lastKey.destinationIndex == viewDestinationIndex) {
                navBackStack.removeLastOrNull()
            } else {
                val matchtarget_11 = path
                if (matchtarget_11 != null) {
                    val path = matchtarget_11
                    path.wrappedValue.popLast()
                } else {
                    val matchtarget_12 = navigationPath
                    if (matchtarget_12 != null) {
                        val navigationPath = matchtarget_12
                        navigationPath.wrappedValue.removeLast()
                    } else if (!isRoot) {
                        navBackStack.removeLastOrNull()
                    }
                }
            }
        } else {
            val matchtarget_11 = path
            if (matchtarget_11 != null) {
                val path = matchtarget_11
                path.wrappedValue.popLast()
            } else {
                val matchtarget_12 = navigationPath
                if (matchtarget_12 != null) {
                    val navigationPath = matchtarget_12
                    navigationPath.wrappedValue.removeLast()
                } else if (!isRoot) {
                    navBackStack.removeLastOrNull()
                }
            }
        }
    }

    /// Whether the given view entry ID is presented.
    internal fun isViewPresented(id: String, asTop: Boolean = false): Boolean {
        if (navBackStack.isEmpty()) {
            return false
        }
        if (asTop) {
            return stableEntryId(forKey = navBackStack[navBackStack.size - 1]) == id
        }
        for (key in navBackStack.sref()) {
            if (stableEntryId(forKey = key) == id) {
                return true
            }
        }
        return false
    }

    internal fun stateForRoot(): Navigator.BackStackState? {
        val rootId = stableEntryId(forKey = SkipNavigationStackRootKey.root)
        backStackState[rootId]?.let { state ->
            return state
        }
        if ((navBackStack.size < 1) || (navBackStack.firstOrNull() !is SkipNavigationStackRootKey)) {
            return null
        }
        val rootState = BackStackState(id = rootId, route = Companion.rootRoute)
        backStackState[rootId] = rootState
        return rootState
    }

    /// The entry being navigated to.
    internal fun state(forPushKey: SkipNavigationStackPushKey): Navigator.BackStackState? {
        val pushKey = forPushKey
        val id = stableEntryId(forKey = pushKey)
        return backStackState[id]
    }

    /// The effective title display mode for the given preference value.
    internal fun titleDisplayMode(for_: Navigator.BackStackState, hasTitle: Boolean, preference: ToolbarTitleDisplayMode?): ToolbarTitleDisplayMode {
        val state = for_
        if (preference != null) {
            state.titleDisplayMode = preference
            return preference
        }
        if (!hasTitle) {
            return ToolbarTitleDisplayMode.inline_
        }

        // Base the display mode on the back stack
        var titleDisplayMode: ToolbarTitleDisplayMode? = null
        for (key in navBackStack.sref()) {
            val entryId = stableEntryId(forKey = key)
            if (entryId == state.id) {
                break
            } else {
                backStackState[entryId]?.titleDisplayMode?.let { entryTitleDisplayMode ->
                    titleDisplayMode = entryTitleDisplayMode
                }
            }
        }
        return titleDisplayMode ?: ToolbarTitleDisplayMode.automatic
    }

    private fun stableEntryId(forKey: NavKey): String {
        val key = forKey
        if (key is SkipNavigationStackRootKey) {
            return Companion.rootRoute
        } else {
            val matchtarget_13 = key as? SkipNavigationStackPushKey
            if (matchtarget_13 != null) {
                val pushKey = matchtarget_13
                return Companion.route(for_ = pushKey.destinationIndex, valueString = pushKey.identifier)
            } else {
                return String(describing = key)
            }
        }
    }

    /// Sync our back stack state with the navigation back stack.
    @Composable
    private fun syncState() {
        val stackKeys = navBackStack.toList()
        // re-present views that were removed from the stack
        val entryIDs = Set(stackKeys.map { it -> stableEntryId(forKey = it) })
        // Toggle any presented bindings for popped states back to false. Do this immediately so that we don't
        // continue to present popped values while waiting for our delayed state sync below.
        for ((id, state) in backStackState.sref()) {
            if (!entryIDs.contains(id)) {
                state.binding?.wrappedValue = false
            }
        }

        // Sync the back stack with remaining states. We delay this to allow views that receive compose calls while
        // animating away to find their state
        val stackEffectKey = stackKeys.map { it -> stableEntryId(forKey = it) }.joinToString(separator = "|")
        LaunchedEffect(stackEffectKey) { ->
            delay(1000) // 1 second
            var syncedBackStackState: Dictionary<String, Navigator.BackStackState> = dictionaryOf()
            for (key in stackKeys.sref()) {
                val entryId = stableEntryId(forKey = key)
                backStackState[entryId]?.let { state ->
                    syncedBackStackState[entryId] = state
                }
            }
            backStackState = syncedBackStackState
        }
    }

    private fun navigateToPath() {
        val path_0 = (this.path?.wrappedValue ?: navigationPath?.wrappedValue?.path).sref()
        if (path_0 == null) {
            return
        }
        val keys = navBackStack.toList()
        if (keys.isEmpty()) {
            return
        }

        // Figure out where the path and back stack first differ
        var pathIndex = 0
        var backStackIndex = 1 // root key at 0
        while (pathIndex < path_0.count) {
            if (backStackIndex >= keys.size) {
                break
            }
            val kid = stableEntryId(forKey = keys[backStackIndex])
            val state = backStackState[kid]
            if (state?.targetValue != path_0[pathIndex]) {
                break
            }
            pathIndex += 1
            backStackIndex += 1
        }

        // If we exhausted the path and the back stack contains only post-path views, keep them in place. This allows
        // users to have a path binding but then append arbitrary views as leaves
        var hasOnlyTrailingViews = false
        if (pathIndex == path_0.count) {
            hasOnlyTrailingViews = true
            val viewDestinationPrefix = Companion.route(for_ = viewDestinationIndex, valueString = "")
            for (i in 0..<(keys.size - backStackIndex)) {
                val kid = stableEntryId(forKey = keys[backStackIndex + i])
                if (!kid.hasPrefix(viewDestinationPrefix)) {
                    hasOnlyTrailingViews = false
                    break
                }
            }
        }
        if (hasOnlyTrailingViews) {
            return
        }

        // Pop back to last common value
        for (unusedbinding in 0..<(keys.size - backStackIndex)) {
            navBackStack.removeLastOrNull()
        }
        // Navigate to any new path values
        for (i in pathIndex..<path_0.count) {
            navigate(toKeyed = path_0[i])
        }
    }

    private fun navigate(toKeyed: Any, key: AnyHashable?): Boolean {
        val targetValue = toKeyed
        if (key == null) {
            return false
        }
        val destination_0 = destinations[key]
        if (destination_0 == null) {
            (key as? KClass<*>)?.let { type ->
                for (supertype in type.superclasses.sref()) {
                    if (navigate(toKeyed = targetValue, key = supertype)) {
                        return true
                    }
                }
            }
            return false
        }

        val route = route(for_ = key, value = targetValue)
        navigate(route = route, destination = destination_0.destination, layoutHints = destination_0.layoutHints, targetValue = targetValue)
        return true
    }

    private fun navigate(route: String, destination: ((Any) -> View)?, layoutHints: NavigationStackLayoutHints?, targetValue: Any, binding: Binding<Boolean>? = null): String? {
        val slash = route.indexOf("/")
        if (slash < 0) {
            return null
        }
        val indexStr = route.substring(0, slash)
        val identifier = route.substring(slash + 1)
        val destIndex_0 = Int(string = indexStr)
        if (destIndex_0 == null) {
            return null
        }
        val pushKey = SkipNavigationStackPushKey(destinationIndex = destIndex_0, identifier = identifier)
        return navigate(pushKey = pushKey, route = route, destination = destination, layoutHints = layoutHints, targetValue = targetValue, binding = binding)
    }

    private fun navigate(pushKey: SkipNavigationStackPushKey, route: String, destination: ((Any) -> View)?, layoutHints: NavigationStackLayoutHints?, targetValue: Any, binding: Binding<Boolean>? = null): String? {
        // We see a top app bar glitch when the keyboard animates away after push, so manually dismiss it first
        keyboardController?.hide()
        navBackStack.add(pushKey)
        val entryId = stableEntryId(forKey = pushKey)
        var state = backStackState[entryId]
        if (state == null) {
            state = BackStackState(id = entryId, route = route, destination = destination, targetValue = targetValue, layoutHints = layoutHints)
            backStackState[entryId] = state
        }
        if (binding != null) {
            state?.binding = binding
        }
        return entryId
    }

    private fun route(for_: AnyHashable, value: Any): String {
        val key = for_
        val index_0 = destinationIndexes[key]
        if (index_0 == null) {
            return String(describing = key) + "?"
        }
        // Escape '/' because it is meaningful in navigation routes
        val valueString = composeBundleString(for_ = value).replacingOccurrences(of = "/", with = "%2F")
        return route(for_ = index_0, valueString = valueString)
    }

    private fun updateDestinationIndexes() {
        for (key in destinations.keys.sref()) {
            if (destinationIndexes[key] == null) {
                destinationIndexes[key] = destinationIndexes.count
            }
        }
    }

    @androidx.annotation.Keep
    companion object {
        /// Route for the root of the navigation stack.
        internal val rootRoute = "navigationroot"

        /// Number of possible destiation routes.
        ///
        /// We route to destinations by static index rather than a dynamic system based on the provided destination
        /// keys because changing the destinations of a `NavHost` wipes out its back stack. By using a fixed set of
        /// indexes, we can maintain the back stack even as we add destination mappings.
        internal val destinationCount = 100

        /// Route for the given destination index and value string.
        internal fun route(for_: Int, valueString: String): String {
            val destinationIndex = for_
            return String(describing = destinationIndex) + "/" + valueString
        }
    }
}

internal class NavigationDestinationItemWrapper<D>: View {
    internal lateinit var dismiss: DismissAction
    internal val item: Binding<D?>
    internal val isBeingDismissedByNavigator: MutableState<Boolean>
    internal val navigationId: MutableState<String?>
    internal val destination: (D) -> View

    override fun body(): View {
        return ComposeBuilder { composectx: ComposeContext ->
            Group { ->
                ComposeBuilder { composectx: ComposeContext ->
                    linvokeComposable l@{
                        val matchtarget_14 = item.wrappedValue
                        if (matchtarget_14 != null) {
                            val itemValue = matchtarget_14
                            return@l destination(itemValue).Compose(composectx)
                        } else {
                            return@l EmptyView().Compose(composectx)
                        }
                    }
                    ComposeResult.ok
                }
            }
            .onChange(of = item.wrappedValue, initial = true) { oldValue, newValue ->
                if (newValue == null) {
                    if (!isBeingDismissedByNavigator.value) {
                        dismiss()
                    }
                    navigationId.value = null
                }
                isBeingDismissedByNavigator.value = false
            }.Compose(composectx)
        }
    }

    @Composable
    override fun Evaluate(context: ComposeContext, options: Int): kotlin.collections.List<Renderable> {
        this.dismiss = EnvironmentValues.shared.dismiss

        return super.Evaluate(context, options)
    }

    constructor(item: Binding<D?>, isBeingDismissedByNavigator: MutableState<Boolean>, navigationId: MutableState<String?>, destination: (D) -> View) {
        this.item = item.sref()
        this.isBeingDismissedByNavigator = isBeingDismissedByNavigator.sref()
        this.navigationId = navigationId.sref()
        this.destination = destination
    }
}

internal val LocalNavigator: ProvidableCompositionLocal<Navigator?> = compositionLocalOf { -> null as Navigator? }

class NavigationSplitViewStyle: RawRepresentable<Int> {
    override val rawValue: Int

    constructor(rawValue: Int) {
        this.rawValue = rawValue
    }

    override fun equals(other: Any?): Boolean {
        if (other !is NavigationSplitViewStyle) return false
        return rawValue == other.rawValue
    }

    @androidx.annotation.Keep
    companion object {

        var automatic = NavigationSplitViewStyle(rawValue = 0)
            get() = field.sref({ this.automatic = it })
            set(newValue) {
                field = newValue.sref()
            }
        var balanced = NavigationSplitViewStyle(rawValue = 1)
            get() = field.sref({ this.balanced = it })
            set(newValue) {
                field = newValue.sref()
            }
        var prominentDetail = NavigationSplitViewStyle(rawValue = 2)
            get() = field.sref({ this.prominentDetail = it })
            set(newValue) {
                field = newValue.sref()
            }
    }
}

class NavigationBarItem {
    enum class TitleDisplayMode(override val rawValue: Int, @Suppress("UNUSED_PARAMETER") unusedp: Nothing? = null): RawRepresentable<Int> {
        automatic(0), // For bridging
        inline_(1), // For bridging
        large(2); // For bridging

        @androidx.annotation.Keep
        companion object {
            fun init(rawValue: Int): NavigationBarItem.TitleDisplayMode? {
                return when (rawValue) {
                    0 -> TitleDisplayMode.automatic
                    1 -> TitleDisplayMode.inline_
                    2 -> TitleDisplayMode.large
                    else -> null
                }
            }
        }
    }

    override fun equals(other: Any?): Boolean = other is NavigationBarItem

    override fun hashCode(): Int = "NavigationBarItem".hashCode()

    @androidx.annotation.Keep
    companion object {

        fun TitleDisplayMode(rawValue: Int): NavigationBarItem.TitleDisplayMode? = TitleDisplayMode.init(rawValue = rawValue)
    }
}


enum class Material3TopAppBarNavigationIconButtonStyle {
    iconButton,
    filledIconButton;

    @androidx.annotation.Keep
    companion object {
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("MUST_BE_INITIALIZED")
class Material3TopAppBarOptions: MutableStruct {
    var title: @Composable () -> Unit
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    var modifier: Modifier
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    var navigationIcon: @Composable () -> Unit
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    var navigationIconButtonStyle: Material3TopAppBarNavigationIconButtonStyle
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    var navigationIconButtonColors: IconButtonColors? = null
        get() = field.sref({ this.navigationIconButtonColors = it })
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
            willmutate()
            field = newValue
            didmutate()
        }
    var colors: TopAppBarColors
        get() = field.sref({ this.colors = it })
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
            willmutate()
            field = newValue
            didmutate()
        }
    var scrollBehavior: TopAppBarScrollBehavior? = null
        get() = field.sref({ this.scrollBehavior = it })
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
            willmutate()
            field = newValue
            didmutate()
        }
    var preferCenterAlignedStyle: Boolean
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    var preferLargeStyle: Boolean
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }

    fun copy(title: @Composable () -> Unit = this.title, modifier: Modifier = this.modifier, navigationIcon: @Composable () -> Unit = this.navigationIcon, navigationIconButtonStyle: Material3TopAppBarNavigationIconButtonStyle = this.navigationIconButtonStyle, navigationIconButtonColors: IconButtonColors? = this.navigationIconButtonColors, colors: TopAppBarColors = this.colors, scrollBehavior: TopAppBarScrollBehavior? = this.scrollBehavior, preferCenterAlignedStyle: Boolean = this.preferCenterAlignedStyle, preferLargeStyle: Boolean = this.preferLargeStyle): Material3TopAppBarOptions = Material3TopAppBarOptions(title = title, modifier = modifier, navigationIcon = navigationIcon, navigationIconButtonStyle = navigationIconButtonStyle, navigationIconButtonColors = navigationIconButtonColors, colors = colors, scrollBehavior = scrollBehavior, preferCenterAlignedStyle = preferCenterAlignedStyle, preferLargeStyle = preferLargeStyle)

    constructor(title: @Composable () -> Unit, modifier: Modifier = Modifier, navigationIcon: @Composable () -> Unit = { ->  }, navigationIconButtonStyle: Material3TopAppBarNavigationIconButtonStyle = Material3TopAppBarNavigationIconButtonStyle.iconButton, navigationIconButtonColors: IconButtonColors? = null, colors: TopAppBarColors, scrollBehavior: TopAppBarScrollBehavior? = null, preferCenterAlignedStyle: Boolean = false, preferLargeStyle: Boolean = false) {
        this.title = title
        this.modifier = modifier
        this.navigationIcon = navigationIcon
        this.navigationIconButtonStyle = navigationIconButtonStyle
        this.navigationIconButtonColors = navigationIconButtonColors
        this.colors = colors
        this.scrollBehavior = scrollBehavior
        this.preferCenterAlignedStyle = preferCenterAlignedStyle
        this.preferLargeStyle = preferLargeStyle
    }

    override var supdate: ((Any) -> Unit)? = null
    override var smutatingcount = 0
    override fun scopy(): MutableStruct = Material3TopAppBarOptions(title, modifier, navigationIcon, navigationIconButtonStyle, navigationIconButtonColors, colors, scrollBehavior, preferCenterAlignedStyle, preferLargeStyle)

    @androidx.annotation.Keep
    companion object {
    }
}

@Suppress("MUST_BE_INITIALIZED")
class Material3BottomAppBarOptions: MutableStruct {
    var modifier: Modifier
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    var containerColor: androidx.compose.ui.graphics.Color
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    var contentColor: androidx.compose.ui.graphics.Color
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    var tonalElevation: Dp
        get() = field.sref({ this.tonalElevation = it })
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
            willmutate()
            field = newValue
            didmutate()
        }
    var contentPadding: PaddingValues
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }

    fun copy(modifier: Modifier = this.modifier, containerColor: androidx.compose.ui.graphics.Color = this.containerColor, contentColor: androidx.compose.ui.graphics.Color = this.contentColor, tonalElevation: Dp = this.tonalElevation, contentPadding: PaddingValues = this.contentPadding): Material3BottomAppBarOptions = Material3BottomAppBarOptions(modifier = modifier, containerColor = containerColor, contentColor = contentColor, tonalElevation = tonalElevation, contentPadding = contentPadding)

    constructor(modifier: Modifier = Modifier, containerColor: androidx.compose.ui.graphics.Color, contentColor: androidx.compose.ui.graphics.Color, tonalElevation: Dp = BottomAppBarDefaults.ContainerElevation.sref(), contentPadding: PaddingValues = BottomAppBarDefaults.ContentPadding) {
        this.modifier = modifier
        this.containerColor = containerColor
        this.contentColor = contentColor
        this.tonalElevation = tonalElevation
        this.contentPadding = contentPadding
    }

    override var supdate: ((Any) -> Unit)? = null
    override var smutatingcount = 0
    override fun scopy(): MutableStruct = Material3BottomAppBarOptions(modifier, containerColor, contentColor, tonalElevation, contentPadding)

    @androidx.annotation.Keep
    companion object {
    }
}

@androidx.annotation.Keep
internal class NavigationDestinationsPreferenceKey: PreferenceKey<Dictionary<AnyHashable, NavigationDestination>> {

    @androidx.annotation.Keep
    companion object: PreferenceKeyCompanion<Dictionary<AnyHashable, NavigationDestination>> {
        override val defaultValue: Dictionary<AnyHashable, NavigationDestination> = dictionaryOf()

        override fun reduce(value: InOut<Dictionary<AnyHashable, NavigationDestination>>, nextValue: () -> Dictionary<AnyHashable, NavigationDestination>) {
            for ((type, destination) in nextValue()) {
                value.value[type] = destination
            }
        }
    }
}

@androidx.annotation.Keep
internal class NavigationTitlePreferenceKey: PreferenceKey<Text> {

    @androidx.annotation.Keep
    companion object: PreferenceKeyCompanion<Text> {
        override val defaultValue = Text(LocalizedStringKey(stringLiteral = ""))

        override fun reduce(value: InOut<Text>, nextValue: () -> Text) {
            value.value = nextValue()
        }
    }
}

@androidx.annotation.Keep
internal class NavigationDestinationLayoutHintsPreferenceKey: PreferenceKey<Dictionary<AnyHashable, NavigationStackLayoutHints>> {

    @androidx.annotation.Keep
    companion object: PreferenceKeyCompanion<Dictionary<AnyHashable, NavigationStackLayoutHints>> {
        override val defaultValue: Dictionary<AnyHashable, NavigationStackLayoutHints> = dictionaryOf()

        override fun reduce(value: InOut<Dictionary<AnyHashable, NavigationStackLayoutHints>>, nextValue: () -> Dictionary<AnyHashable, NavigationStackLayoutHints>) {
            for ((key, hints) in nextValue()) {
                value.value[key] = hints
            }
        }
    }
}

/// Values supplied before `navigationTitle` / `navigationBarTitleDisplayMode` preferences propagate, so the first frames use the correct top bar eligibility, scroll behavior, and title text, preventing layout shift.
class NavigationStackLayoutHints {
    val expectedTitle: Text
    val expectedTitleDisplayMode: ToolbarTitleDisplayMode?

    constructor(expectedTitle: Text = NavigationTitlePreferenceKey.defaultValue, expectedTitleDisplayMode: ToolbarTitleDisplayMode? = null) {
        this.expectedTitle = expectedTitle
        this.expectedTitleDisplayMode = expectedTitleDisplayMode
    }

    @androidx.annotation.Keep
    companion object {
    }
}

@androidx.annotation.Keep
class NavigationLink: View, Renderable, skip.lib.SwiftProjecting {
    internal val value: Any?
    internal val destination: ComposeBuilder?
    internal val label: ComposeBuilder

    constructor(value: Any?, label: () -> View) {
        this.value = value.sref()
        this.destination = null
        this.label = ComposeBuilder.from(label)
    }

    constructor(title: String, value: Any?): this(value = value, label = { ->
        ComposeBuilder { composectx: ComposeContext ->
            Text(verbatim = title).Compose(composectx)
            ComposeResult.ok
        }
    }) {
    }

    constructor(titleKey: LocalizedStringKey, value: Any?): this(value = value, label = { ->
        ComposeBuilder { composectx: ComposeContext ->
            Text(titleKey).Compose(composectx)
            ComposeResult.ok
        }
    }) {
    }

    constructor(titleResource: LocalizedStringResource, value: Any?): this(value = value, label = { ->
        ComposeBuilder { composectx: ComposeContext ->
            Text(titleResource).Compose(composectx)
            ComposeResult.ok
        }
    }) {
    }

    constructor(destination: () -> View, label: () -> View) {
        this.value = null
        this.destination = ComposeBuilder.from(destination)
        this.label = ComposeBuilder.from(label)
    }

    constructor(destination: View, label: () -> View): this(destination = { ->
        ComposeBuilder { composectx: ComposeContext ->
            destination.Compose(composectx)
            ComposeResult.ok
        }
    }, label = label) {
    }

    constructor(titleKey: LocalizedStringKey, destination: () -> View): this(destination = destination, label = { ->
        ComposeBuilder { composectx: ComposeContext ->
            Text(titleKey).Compose(composectx)
            ComposeResult.ok
        }
    }) {
    }

    constructor(titleResource: LocalizedStringResource, destination: () -> View): this(destination = destination, label = { ->
        ComposeBuilder { composectx: ComposeContext ->
            Text(titleResource).Compose(composectx)
            ComposeResult.ok
        }
    }) {
    }

    constructor(bridgedDestination: View?, value: Any?, bridgedLabel: View) {
        this.destination = if (bridgedDestination == null) null else ComposeBuilder.from { -> bridgedDestination!! }
        this.value = value.sref()
        this.label = ComposeBuilder.from { -> bridgedLabel }
    }

    @Composable
    override fun Render(context: ComposeContext) {
        val isEnabled = (value != null || destination != null) && EnvironmentValues.shared.isEnabled
        Button.RenderButton(label = label, context = context, isEnabled = isEnabled, action = navigationAction())
    }

    @Composable
    override fun shouldRenderListItem(context: ComposeContext): Tuple2<Boolean, (() -> Unit)?> {
        val buttonStyle = EnvironmentValues.shared._buttonStyle
        if (buttonStyle != null && buttonStyle != ButtonStyle.automatic && buttonStyle != ButtonStyle.plain) {
            return Tuple2(false, null)
        }
        val action: (() -> Unit)? = if (value != null || destination != null) navigationAction() else null
        return Tuple2(true, action)
    }

    @Composable
    override fun RenderListItem(context: ComposeContext, modifiers: kotlin.collections.List<ModifierProtocol>) {
        ModifiedContent.RenderWithModifiers(modifiers, context = context) { context ->
            val renderables = label.Evaluate(context = context, options = 0)
            Row(modifier = context.modifier, horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) { ->
                Box(modifier = Modifier.weight(1.0f)) { ->
                    val labelContext = context.content()
                    // Continue to specialize for list rendering within the content (e.g. Label)
                    if ((renderables.size == 1) && renderables[0].shouldRenderListItem(context = context).element0) {
                        renderables[0].RenderListItem(context = labelContext, modifiers = listOf())
                    } else {
                        for (renderable in renderables.sref()) {
                            renderable.Render(context = labelContext)
                        }
                    }
                }
                Companion.RenderChevron()
            }
        }
    }

    @Composable
    internal fun navigationAction(): () -> Unit {
        val navigator = LocalNavigator.current.sref()
        return l@{ ->
            // Hack to prevent multiple quick taps from pushing duplicate entries
            val now = CFAbsoluteTimeGetCurrent()
            if (NavigationLink.lastNavigationTime + NavigationLink.minimumNavigationInterval > now) {
                logger.debug("navigation throttled; diff: ${now - lastNavigationTime} minimumNavigationInterval: ${NavigationLink.minimumNavigationInterval}")
                return@l
            }
            NavigationLink.lastNavigationTime = now

            if (value != null) {
                navigator?.navigate(to = value)
            } else if (destination != null) {
                navigator?.navigateToView(destination)
            }
        }
    }

    override fun Swift_projection(options: Int): () -> Any = Swift_projectionImpl(options)
    private external fun Swift_projectionImpl(options: Int): () -> Any

    @androidx.annotation.Keep
    companion object {

        private val minimumNavigationInterval = 0.35
        private var lastNavigationTime = 0.0

        @Composable
        internal fun RenderChevron() {
            val isRTL = EnvironmentValues.shared.layoutDirection == LayoutDirection.rightToLeft
            Icon(imageVector = if (isRTL) Icons.Outlined.KeyboardArrowLeft else Icons.Outlined.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

class NavigationPath: MutableStruct {
    internal var path: Array<Any> = arrayOf()
        get() = field.sref({ this.path = it })
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
            willmutate()
            field = newValue
            didmutate()
        }

    constructor() {
    }

    constructor(elements: Sequence<*>) {
        path.append(contentsOf = elements as Sequence<Any>)
    }

    @Deprecated("This API is not yet available in Skip. Consider placing it within a #if !SKIP block. You can file an issue against the owning library at https://github.com/skiptools, or see the library README for information on adding support", level = DeprecationLevel.ERROR)
    constructor(codable: NavigationPath.CodableRepresentation) {
    }

    val count: Int
        get() = path.count

    val isEmpty: Boolean
        get() = path.isEmpty

    @Deprecated("This API is not yet available in Skip. Consider placing it within a #if !SKIP block. You can file an issue against the owning library at https://github.com/skiptools, or see the library README for information on adding support", level = DeprecationLevel.ERROR)
    val codable: NavigationPath.CodableRepresentation?
        get() {
            fatalError()
        }

    fun append(value: Any) {
        willmutate()
        try {
            path.append(value)
        } finally {
            didmutate()
        }
    }

    fun removeLast(k: Int = 1) {
        willmutate()
        try {
            path.removeLast(k)
        } finally {
            didmutate()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other !is NavigationPath) {
            return false
        }
        val lhs = this
        val rhs = other
        return lhs.path == rhs.path
    }

    @androidx.annotation.Keep
    class CodableRepresentation: Codable {
        constructor(from: Decoder) {
        }

        override fun encode(to: Encoder) = Unit

        @androidx.annotation.Keep
        companion object: DecodableCompanion<NavigationPath.CodableRepresentation> {
            override fun init(from: Decoder): NavigationPath.CodableRepresentation = CodableRepresentation(from = from)
        }
    }

    private constructor(copy: MutableStruct) {
        @Suppress("NAME_SHADOWING", "UNCHECKED_CAST") val copy = copy as NavigationPath
        this.path = copy.path
    }

    override var supdate: ((Any) -> Unit)? = null
    override var smutatingcount = 0
    override fun scopy(): MutableStruct = NavigationPath(this as MutableStruct)

    @androidx.annotation.Keep
    companion object {
    }
}

/*
import struct CoreGraphics.CGFloat
import struct Foundation.URL

@available(iOS 13.0, *)
@available(macOS, unavailable)
@available(tvOS, unavailable)
@available(watchOS, unavailable)
extension NavigationLink {

/// Sets the navigation link to present its destination as the detail
/// component of the containing navigation view.
///
/// This method sets the behavior when the navigation link is used in a
/// ``NavigationSplitView``, or a
/// multi-column navigation view, such as one using
/// ``ColumnNavigationViewStyle``.
///
/// For example, in a two-column navigation split view, if `isDetailLink` is
/// `true`, triggering the link in the sidebar column sets the contents of
/// the detail column to be the link's destination view. If `isDetailLink`
/// is `false`, the link navigates to the destination view within the
/// primary column.
///
/// If you do not set the detail link behavior with this method, the
/// behavior defaults to `true`.
///
/// The `isDetailLink` modifier only affects view-destination links. Links
/// that present data values always search for a matching navigation
/// destination beginning in the column that contains the link.
///
/// - Parameter isDetailLink: A Boolean value that specifies whether this
/// link presents its destination as the detail component when used in a
/// multi-column navigation view.
/// - Returns: A view that applies the specified detail link behavior.
@available(macOS, unavailable)
@available(tvOS, unavailable)
@available(watchOS, unavailable)
public func isDetailLink(_ isDetailLink: Bool) -> some View { return stubView() }

}

/// A view that presents views in two or three columns, where selections in
/// leading columns control presentations in subsequent columns.
///
/// You create a navigation split view with two or three columns, and typically
/// use it as the root view in a ``Scene``. People choose one or more
/// items in a leading column to display details about those items in
/// subsequent columns.
///
/// To create a two-column navigation split view, use the
/// ``init(sidebar:detail:)`` initializer:
///
///     @State private var employeeIds: Set<Employee.ID> = []
///
///     var body: some View {
///         NavigationSplitView {
///             List(model.employees, selection: $employeeIds) { employee in
///                 Text(employee.name)
///             }
///         } detail: {
///             EmployeeDetails(for: employeeIds)
///         }
///     }
///
/// In the above example, the navigation split view coordinates with the
/// ``List`` in its first column, so that when people make a selection, the
/// detail view updates accordingly. Programmatic changes that you make to the
/// selection property also affect both the list appearance and the presented
/// detail view.
///
/// To create a three-column view, use the ``init(sidebar:content:detail:)``
/// initializer. The selection in the first column affects the second, and the
/// selection in the second column affects the third. For example, you can show
/// a list of departments, the list of employees in the selected department,
/// and the details about all of the selected employees:
///
///     @State private var departmentId: Department.ID? // Single selection.
///     @State private var employeeIds: Set<Employee.ID> = [] // Multiple selection.
///
///     var body: some View {
///         NavigationSplitView {
///             List(model.departments, selection: $departmentId) { department in
///                 Text(department.name)
///             }
///         } content: {
///             if let department = model.department(id: departmentId) {
///                 List(department.employees, selection: $employeeIds) { employee in
///                     Text(employee.name)
///                 }
///             } else {
///                 Text("Select a department")
///             }
///         } detail: {
///             EmployeeDetails(for: employeeIds)
///         }
///     }
///
/// You can also embed a ``NavigationStack`` in a column. Tapping or clicking a
/// ``NavigationLink`` that appears in an earlier column sets the view that the
/// stack displays over its root view. Activating a link in the same column
/// adds a view to the stack. Either way, the link must present a data type
/// for which the stack has a corresponding
/// ``View/navigationDestination(for:destination:)`` modifier.
///
/// On watchOS and tvOS, and with narrow sizes like on iPhone or on iPad in
/// Slide Over, the navigation split view collapses all of its columns
/// into a stack, and shows the last column that displays useful information.
/// For example, the three-column example above shows the list of departments to
/// start, the employees in the department after someone selects a department,
/// and the employee details when someone selects an employee. For rows in a
/// list that have ``NavigationLink`` instances, the list draws disclosure
/// chevrons while in the collapsed state.
///
/// ### Control column visibility
///
/// You can programmatically control the visibility of navigation split view
/// columns by creating a ``State`` value of type
/// ``NavigationSplitViewVisibility``. Then pass a ``Binding`` to that state to
/// the appropriate initializer --- such as
/// ``init(columnVisibility:sidebar:detail:)`` for two columns, or
/// the ``init(columnVisibility:sidebar:content:detail:)`` for three columns.
///
/// The following code updates the first example above to always hide the
/// first column when the view appears:
///
///     @State private var employeeIds: Set<Employee.ID> = []
///     @State private var columnVisibility =
///         NavigationSplitViewVisibility.detailOnly
///
///     var body: some View {
///         NavigationSplitView(columnVisibility: $columnVisibility) {
///             List(model.employees, selection: $employeeIds) { employee in
///                 Text(employee.name)
///             }
///         } detail: {
///             EmployeeDetails(for: employeeIds)
///         }
///     }
///
/// The split view ignores the visibility control when it collapses its columns
/// into a stack.
///
/// ### Collapsed split views
///
/// At narrow size classes, such as on iPhone or Apple Watch, a navigation split
/// view collapses into a single stack. Typically SkipUI automatically chooses
/// the view to show on top of this single stack, based on the content of the
/// split view's columns.
///
/// For custom navigation experiences, you can provide more information to help
/// SkipUI choose the right column. Create a `State` value of type
/// ``NavigationSplitViewColumn``. Then pass a `Binding` to that state to the
/// appropriate initializer, such as
/// ``init(preferredCompactColumn:sidebar:detail:)`` or
/// ``init(preferredCompactColumn:sidebar:content:detail:)``.
///
/// The following code shows the blue detail view when run on iPhone. When the
/// person using the app taps the back button, they'll see the yellow view. The
/// value of `preferredPreferredCompactColumn` will change from `.detail` to
/// `.sidebar`:
///
///     @State private var preferredColumn =
///         NavigationSplitViewColumn.detail
///
///     var body: some View {
///         NavigationSplitView(preferredCompactColumn: $preferredColumn) {
///             Color.yellow
///         } detail: {
///             Color.blue
///         }
///     }
///
/// ### Customize a split view
///
/// To specify a preferred column width in a navigation split view, use the
/// ``View/navigationSplitViewColumnWidth(_:)`` modifier. To set minimum,
/// maximum, and ideal sizes for a column, use
/// ``View/navigationSplitViewColumnWidth(min:ideal:max:)``. You can specify a
/// different modifier in each column. The navigation split view does its
/// best to accommodate the preferences that you specify, but might make
/// adjustments based on other constraints.
///
/// To specify how columns in a navigation split view interact, use the
/// ``View/navigationSplitViewStyle(_:)`` modifier with a
/// ``NavigationSplitViewStyle`` value. For example, you can specify
/// whether to emphasize the detail column or to give all of the columns equal
/// prominence.
@available(iOS 16.0, macOS 13.0, tvOS 16.0, watchOS 9.0, *)
public struct NavigationSplitView<Sidebar, Content, Detail> : View where Sidebar : View, Content : View, Detail : View {

/// Creates a three-column navigation split view.
///
/// - Parameters:
///   - sidebar: The view to show in the leading column.
///   - content: The view to show in the middle column.
///   - detail: The view to show in the detail area.
public init(@ViewBuilder sidebar: () -> Sidebar, @ViewBuilder content: () -> Content, @ViewBuilder detail: () -> Detail) { fatalError() }

/// Creates a three-column navigation split view that enables programmatic
/// control of leading columns' visibility.
///
/// - Parameters:
///   - columnVisibility: A ``Binding`` to state that controls the
///     visibility of the leading columns.
///   - sidebar: The view to show in the leading column.
///   - content: The view to show in the middle column.
///   - detail: The view to show in the detail area.
public init(columnVisibility: Binding<NavigationSplitViewVisibility>, @ViewBuilder sidebar: () -> Sidebar, @ViewBuilder content: () -> Content, @ViewBuilder detail: () -> Detail) { fatalError() }

/// Creates a two-column navigation split view.
///
/// - Parameters:
///   - sidebar: The view to show in the leading column.
///   - detail: The view to show in the detail area.
public init(@ViewBuilder sidebar: () -> Sidebar, @ViewBuilder detail: () -> Detail) where Content == EmptyView { fatalError() }

/// Creates a two-column navigation split view that enables programmatic
/// control of the sidebar's visibility.
///
/// - Parameters:
///   - columnVisibility: A ``Binding`` to state that controls the
///     visibility of the leading column.
///   - sidebar: The view to show in the leading column.
///   - detail: The view to show in the detail area.
public init(columnVisibility: Binding<NavigationSplitViewVisibility>, @ViewBuilder sidebar: () -> Sidebar, @ViewBuilder detail: () -> Detail) where Content == EmptyView { fatalError() }

@MainActor public var body: some View { get { return stubView() } }

//    public typealias Body = some View
}

@available(iOS 17.0, macOS 14.0, tvOS 17.0, watchOS 10.0, *)
extension NavigationSplitView {

/// Creates a three-column navigation split view that enables programmatic
/// control over which column appears on top when the view collapses into a
/// single column in narrow sizes.
///
/// - Parameters:
///   - preferredCompactColumn: A ``Binding`` to state that controls which
///     column appears on top when the view collapses.
///   - sidebar: The view to show in the leading column.
///   - content: The view to show in the middle column.
///   - detail: The view to show in the detail area.
public init(preferredCompactColumn: Binding<NavigationSplitViewColumn>, @ViewBuilder sidebar: () -> Sidebar, @ViewBuilder content: () -> Content, @ViewBuilder detail: () -> Detail) { fatalError() }

/// Creates a three-column navigation split view that enables programmatic
/// control of leading columns' visibility in regular sizes and which column
/// appears on top when the view collapses into a single column in narrow
/// sizes.
///
/// - Parameters:
///   - columnVisibility: A ``Binding`` to state that controls the
///     visibility of the leading columns.
///   - preferredCompactColumn: A ``Binding`` to state that controls which
///     column appears on top when the view collapses.
///   - sidebar: The view to show in the leading column.
///   - content: The view to show in the middle column.
///   - detail: The view to show in the detail area.
public init(columnVisibility: Binding<NavigationSplitViewVisibility>, preferredCompactColumn: Binding<NavigationSplitViewColumn>, @ViewBuilder sidebar: () -> Sidebar, @ViewBuilder content: () -> Content, @ViewBuilder detail: () -> Detail) { fatalError() }

/// Creates a two-column navigation split view that enables programmatic
/// control over which column appears on top when the view collapses into a
/// single column in narrow sizes.
///
/// - Parameters:
///   - preferredCompactColumn: A ``Binding`` to state that controls which
///     column appears on top when the view collapses.
///   - sidebar: The view to show in the leading column.
///   - detail: The view to show in the detail area.
public init(preferredCompactColumn: Binding<NavigationSplitViewColumn>, @ViewBuilder sidebar: () -> Sidebar, @ViewBuilder detail: () -> Detail) where Content == EmptyView { fatalError() }

/// Creates a two-column navigation split view that enables programmatic
/// control of the sidebar's visibility in regular sizes and which column
/// appears on top when the view collapses into a single column in narrow
/// sizes.
///
/// - Parameters:
///   - columnVisibility: A ``Binding`` to state that controls the
///     visibility of the leading column.
///   - preferredCompactColumn: A ``Binding`` to state that controls which
///     column appears on top when the view collapses.
///   - sidebar: The view to show in the leading column.
///   - detail: The view to show in the detail area.
public init(columnVisibility: Binding<NavigationSplitViewVisibility>, preferredCompactColumn: Binding<NavigationSplitViewColumn>, @ViewBuilder sidebar: () -> Sidebar, @ViewBuilder detail: () -> Detail) where Content == EmptyView { fatalError() }
}

/// A view that represents a column in a navigation split view.
///
/// A ``NavigationSplitView`` collapses into a single stack in some contexts,
/// like on iPhone or Apple Watch. Use this type with the
/// `preferredCompactColumn` parameter to control which column of the navigation
/// split view appears on top of the collapsed stack.
@available(iOS 17.0, macOS 14.0, tvOS 17.0, watchOS 10.0, *)
public struct NavigationSplitViewColumn : Hashable, Sendable {

public static var sidebar: NavigationSplitViewColumn { get { fatalError() } }

public static var content: NavigationSplitViewColumn { get { fatalError() } }

public static var detail: NavigationSplitViewColumn { get { fatalError() } }
}

/// The properties of a navigation split view instance.
@available(iOS 16.0, macOS 13.0, tvOS 16.0, watchOS 9.0, *)
public struct NavigationSplitViewStyleConfiguration {
}

/// The visibility of the leading columns in a navigation split view.
///
/// Use a value of this type to control the visibility of the columns of a
/// ``NavigationSplitView``. Create a ``State`` property with a
/// value of this type, and pass a ``Binding`` to that state to the
/// ``NavigationSplitView/init(columnVisibility:sidebar:detail:)`` or
/// ``NavigationSplitView/init(columnVisibility:sidebar:content:detail:)``
/// initializer when you create the navigation split view. You can then
/// modify the value elsewhere in your code to:
///
/// * Hide all but the trailing column with ``detailOnly``.
/// * Hide the leading column of a three-column navigation split view
///   with ``doubleColumn``.
/// * Show all the columns with ``all``.
/// * Rely on the automatic behavior for the current context with ``automatic``.
///
/// >Note: Some platforms don't respect every option. For example, macOS always
/// displays the content column.
@available(iOS 16.0, macOS 13.0, tvOS 16.0, watchOS 9.0, *)
public struct NavigationSplitViewVisibility : Equatable, Codable, Sendable {

/// Hide the leading two columns of a three-column navigation split view, so
/// that just the detail area shows.
public static var detailOnly: NavigationSplitViewVisibility { get { fatalError() } }

/// Show the content column and detail area of a three-column navigation
/// split view, or the sidebar column and detail area of a two-column
/// navigation split view.
///
/// For a two-column navigation split view, `doubleColumn` is equivalent
/// to `all`.
public static var doubleColumn: NavigationSplitViewVisibility { get { fatalError() } }

/// Show all the columns of a three-column navigation split view.
public static var all: NavigationSplitViewVisibility { get { fatalError() } }

/// Use the default leading column visibility for the current device.
///
/// This computed property returns one of the three concrete cases:
/// ``detailOnly``, ``doubleColumn``, or ``all``.
public static var automatic: NavigationSplitViewVisibility { get { fatalError() } }

/// Encodes this value into the given encoder.
///
/// If the value fails to encode anything, `encoder` will encode an empty
/// keyed container in its place.
///
/// This function throws an error if any values are invalid for the given
/// encoder's format.
///
/// - Parameter encoder: The encoder to write data to.
public func encode(to encoder: Encoder) throws { fatalError() }

/// Creates a new instance by decoding from the given decoder.
///
/// This initializer throws an error if reading from the decoder fails, or
/// if the data read is corrupted or otherwise invalid.
///
/// - Parameter decoder: The decoder to read data from.
public init(from decoder: Decoder) throws { fatalError() }
}
*/
