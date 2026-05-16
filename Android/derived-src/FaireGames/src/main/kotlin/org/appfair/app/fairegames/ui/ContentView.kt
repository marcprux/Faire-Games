// Licensed under the GNU General Public License v2.0 or later
// SPDX-License-Identifier: GPL-2.0-or-later

package org.appfair.app.fairegames.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import skip.lib.*
import skip.lib.Array
import skip.lib.Set

import skip.ui.*
import app.fair.ui.*
import org.appfair.app.fairegames.gamemodel.*
import org.appfair.app.fairegames.games.blockblast.*
import org.appfair.app.fairegames.games.tetris.*
import org.appfair.app.fairegames.games.flappybird.*
import org.appfair.app.fairegames.games.breakout.*
import org.appfair.app.fairegames.games.sudoku.*
import org.appfair.app.fairegames.games.twentyfortyeight.*
import org.appfair.app.fairegames.games.drop7.*
import skip.foundation.*
import skip.model.*

internal val gamePreviewIconSpan = 120.0

// MARK: - FaireGameInfo

/// A self-contained description of a game tile on the home grid: stable
/// identifier, localized strings, view factories, and the reset action. All
/// per-game knowledge lives here so `ContentView` can render any game by
/// iterating over `FaireGameInfo` instances without per-game switches.
internal class FaireGameInfo: Identifiable<String> {
    override val id: String
    internal val title: () -> Text
    internal val previewIcon: () -> AnyView
    internal val destination: () -> AnyView
    internal val resetMenuLabel: () -> Text
    internal val resetDialogTitle: () -> Text
    internal val resetDialogMessage: () -> Text
    internal val reset: () -> Unit

    override fun equals(other: Any?): Boolean {
        if (other !is FaireGameInfo) {
            return false
        }
        val lhs = this
        val rhs = other
        return lhs.id == rhs.id
    }
    override fun hashCode(): Int {
        var hasher = Hasher()
        hash(into = InOut<Hasher>({ hasher }, { hasher = it }))
        return hasher.finalize()
    }
    internal fun hash(into: InOut<Hasher>) {
        val hasher = into
        hasher.value.combine(id)
    }

    constructor(id: String, title: () -> Text, previewIcon: () -> AnyView, destination: () -> AnyView, resetMenuLabel: () -> Text, resetDialogTitle: () -> Text, resetDialogMessage: () -> Text, reset: () -> Unit) {
        this.id = id
        this.title = title
        this.previewIcon = previewIcon
        this.destination = destination
        this.resetMenuLabel = resetMenuLabel
        this.resetDialogTitle = resetDialogTitle
        this.resetDialogMessage = resetDialogMessage
        this.reset = reset
    }

    @androidx.annotation.Keep
    companion object {

        internal val twentyFortyEight = FaireGameInfo(id = "twentyFortyEight", title = { -> Text(LocalizedStringKey(stringLiteral = "2048"), bundle = Bundle.module) }, previewIcon = { -> AnyView(TwentyFortyEightPreviewIcon()) }, destination = { -> AnyView(TwentyFortyEightContainerView()) }, resetMenuLabel = { -> Text(LocalizedStringKey(stringLiteral = "Reset High Score"), bundle = Bundle.module) }, resetDialogTitle = { -> Text(LocalizedStringKey(stringLiteral = "Reset 2048 High Score?"), bundle = Bundle.module) }, resetDialogMessage = { -> Text(LocalizedStringKey(stringLiteral = "This will permanently reset your 2048 high score to zero."), bundle = Bundle.module) }, reset = { -> resetTwentyFortyEightHighScore() })

        internal val blockBlast = FaireGameInfo(id = "blockBlast", title = { -> Text(LocalizedStringKey(stringLiteral = "Block Blast!"), bundle = Bundle.module) }, previewIcon = { -> AnyView(BlockBlastPreviewIcon()) }, destination = { -> AnyView(BlockBlastContainerView()) }, resetMenuLabel = { -> Text(LocalizedStringKey(stringLiteral = "Reset High Score"), bundle = Bundle.module) }, resetDialogTitle = { -> Text(LocalizedStringKey(stringLiteral = "Reset Block Blast High Score?"), bundle = Bundle.module) }, resetDialogMessage = { -> Text(LocalizedStringKey(stringLiteral = "This will permanently reset your Block Blast high score to zero."), bundle = Bundle.module) }, reset = { -> resetBlockBlastHighScore() })

        internal val drop7 = FaireGameInfo(id = "drop7", title = { -> Text(LocalizedStringKey(stringLiteral = "Drop 7"), bundle = Bundle.module) }, previewIcon = { -> AnyView(Drop7PreviewIcon()) }, destination = { -> AnyView(Drop7ContainerView()) }, resetMenuLabel = { -> Text(LocalizedStringKey(stringLiteral = "Reset High Score"), bundle = Bundle.module) }, resetDialogTitle = { -> Text(LocalizedStringKey(stringLiteral = "Reset Drop 7 High Score?"), bundle = Bundle.module) }, resetDialogMessage = { -> Text(LocalizedStringKey(stringLiteral = "This will permanently reset your Drop 7 high score to zero."), bundle = Bundle.module) }, reset = { -> resetDrop7HighScore() })

        internal val sudoku = FaireGameInfo(id = "sudoku", title = { -> Text(LocalizedStringKey(stringLiteral = "Sudoku"), bundle = Bundle.module) }, previewIcon = { -> AnyView(SudokuPreviewIcon()) }, destination = { -> AnyView(SudokuContainerView()) }, resetMenuLabel = { -> Text(LocalizedStringKey(stringLiteral = "Reset Records"), bundle = Bundle.module) }, resetDialogTitle = { -> Text(LocalizedStringKey(stringLiteral = "Reset Sudoku Records?"), bundle = Bundle.module) }, resetDialogMessage = { -> Text(LocalizedStringKey(stringLiteral = "This will permanently reset your Sudoku best times and puzzle counts."), bundle = Bundle.module) }, reset = { -> resetSudokuRecords() })

        internal val sirtet = FaireGameInfo(id = "sirtet", title = { -> Text(LocalizedStringKey(stringLiteral = "Sirtet"), bundle = Bundle.module) }, previewIcon = { -> AnyView(TetrisPreviewIcon()) }, destination = { -> AnyView(TetrisContainerView()) }, resetMenuLabel = { -> Text(LocalizedStringKey(stringLiteral = "Reset High Score"), bundle = Bundle.module) }, resetDialogTitle = { -> Text(LocalizedStringKey(stringLiteral = "Reset Sirtet High Score?"), bundle = Bundle.module) }, resetDialogMessage = { -> Text(LocalizedStringKey(stringLiteral = "This will permanently reset your Sirtet high score to zero."), bundle = Bundle.module) }, reset = { -> resetTetrisHighScore() })

        internal val breakout = FaireGameInfo(id = "breakout", title = { -> Text(LocalizedStringKey(stringLiteral = "Breakout"), bundle = Bundle.module) }, previewIcon = { -> AnyView(BreakoutPreviewIcon()) }, destination = { -> AnyView(BreakoutContainerView()) }, resetMenuLabel = { -> Text(LocalizedStringKey(stringLiteral = "Reset High Score"), bundle = Bundle.module) }, resetDialogTitle = { -> Text(LocalizedStringKey(stringLiteral = "Reset Breakout High Score?"), bundle = Bundle.module) }, resetDialogMessage = { -> Text(LocalizedStringKey(stringLiteral = "This will permanently reset your Breakout high score to zero."), bundle = Bundle.module) }, reset = { -> resetBreakoutHighScore() })

        internal val flappyBird = FaireGameInfo(id = "flappyBird", title = { -> Text(LocalizedStringKey(stringLiteral = "Flappy Bird"), bundle = Bundle.module) }, previewIcon = { -> AnyView(FlappyBirdPreviewIcon()) }, destination = { -> AnyView(FlappyBirdContainerView()) }, resetMenuLabel = { -> Text(LocalizedStringKey(stringLiteral = "Reset High Score"), bundle = Bundle.module) }, resetDialogTitle = { -> Text(LocalizedStringKey(stringLiteral = "Reset Flappy Bird High Score?"), bundle = Bundle.module) }, resetDialogMessage = { -> Text(LocalizedStringKey(stringLiteral = "This will permanently reset your Flappy Bird high score to zero."), bundle = Bundle.module) }, reset = { -> resetFlappyBirdHighScore() })

        /// Canonical default ordering of every shipping game.
        internal val allGames: Array<FaireGameInfo> = arrayOf(
            FaireGameInfo.twentyFortyEight,
            FaireGameInfo.blockBlast,
            FaireGameInfo.drop7,
            FaireGameInfo.sudoku,
            FaireGameInfo.sirtet,
            FaireGameInfo.breakout,
            FaireGameInfo.flappyBird
        )

        internal fun lookup(id: String): FaireGameInfo? {
            return allGames.first(where = { it -> it.id == id })
        }
    }
}

// MARK: - PreferenceKey for tile frame tracking

/// Reports each tile's laid-out frame, keyed by `FaireGameInfo.id`. Used by
/// the drag-to-reorder hit-test and to size the floating drag overlay.
@androidx.annotation.Keep
private class TileFramePreferenceKey: PreferenceKey<Dictionary<String, CGRect>> {

    @androidx.annotation.Keep
    companion object: PreferenceKeyCompanion<Dictionary<String, CGRect>> {
        override val defaultValue: Dictionary<String, CGRect> = dictionaryOf()
        override fun reduce(value: InOut<Dictionary<String, CGRect>>, nextValue: () -> Dictionary<String, CGRect>) {
            for ((k, v) in nextValue()) {
                value.value[k] = v.sref()
            }
        }
    }
}

// MARK: - ContentView

internal class ContentView: View {
    internal var gamePreferences: GamePreferences
        get() = _gamePreferences.wrappedValue
        set(newValue) {
            _gamePreferences.wrappedValue = newValue
        }
    internal var _gamePreferences: skip.ui.State<GamePreferences>
    internal var showSettings: Boolean
        get() = _showSettings.wrappedValue
        set(newValue) {
            _showSettings.wrappedValue = newValue
        }
    internal var _showSettings: skip.ui.State<Boolean>

    /// Whether the home grid is in rearrange mode. Entered via long-press on
    /// a tile, exited via the toolbar Done button.
    private var isReordering: Boolean
        get() = _isReordering.wrappedValue
        set(newValue) {
            _isReordering.wrappedValue = newValue
        }
    private var _isReordering: skip.ui.State<Boolean>
    /// Identifier of the tile currently being dragged, if any.
    private var draggedID: String?
        get() = _draggedID.wrappedValue
        set(newValue) {
            _draggedID.wrappedValue = newValue
        }
    private var _draggedID: skip.ui.State<String?> = skip.ui.State(null)
    /// Current finger position in global coordinates.
    private var dragLocation: CGPoint
        get() = _dragLocation.wrappedValue.sref({ this.dragLocation = it })
        set(newValue) {
            _dragLocation.wrappedValue = newValue.sref()
        }
    private var _dragLocation: skip.ui.State<CGPoint>
    /// Where in the dragged tile (relative to its origin) the finger first
    /// touched, in global coordinates. The drag overlay subtracts this so the
    /// tile stays glued to the finger's original grab point — even after the
    /// grid reflows underneath.
    private var dragTouchOffsetInTile: CGPoint
        get() = _dragTouchOffsetInTile.wrappedValue.sref({ this.dragTouchOffsetInTile = it })
        set(newValue) {
            _dragTouchOffsetInTile.wrappedValue = newValue.sref()
        }
    private var _dragTouchOffsetInTile: skip.ui.State<CGPoint>
    /// Most recent measured frame of each tile, in global coordinates.
    private var tileFrames: Dictionary<String, CGRect>
        get() = _tileFrames.wrappedValue.sref({ this.tileFrames = it })
        set(newValue) {
            _tileFrames.wrappedValue = newValue.sref()
        }
    private var _tileFrames: skip.ui.State<Dictionary<String, CGRect>>
    /// The game whose reset confirmation dialog is currently being prompted.
    private var pendingReset: FaireGameInfo?
        get() = _pendingReset.wrappedValue
        set(newValue) {
            _pendingReset.wrappedValue = newValue
        }
    private var _pendingReset: skip.ui.State<FaireGameInfo?> = skip.ui.State(null)

    /// Resolved order: stored preference (filtered to known IDs) plus any
    /// games not yet present, falling back to the canonical default.
    private val orderedGames: Array<FaireGameInfo>
        get() {
            val stored = gamePreferences.gameOrder.compactMap { it -> FaireGameInfo.lookup(id = it) }
            val storedIDs = Set(stored.map { it -> it.id })
            val missing = FaireGameInfo.allGames.filter { it -> !storedIDs.contains(it.id) }
            val combined = (stored + missing).sref()
            return if (combined.isEmpty) FaireGameInfo.allGames else combined
        }

    override fun body(): View {
        return ComposeBuilder { composectx: ComposeContext ->
            NavigationStack { ->
                ComposeBuilder { composectx: ComposeContext ->
                    ScrollView { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            VStack(spacing = 20.0) { ->
                                ComposeBuilder { composectx: ComposeContext ->
                                    LazyVGrid(columns = arrayOf(GridItem(GridItem.Size.adaptive(minimum = gamePreviewIconSpan + 30.0), spacing = 16.0)), spacing = 16.0) { ->
                                        ComposeBuilder { composectx: ComposeContext ->
                                            ForEach(orderedGames) { info ->
                                                ComposeBuilder { composectx: ComposeContext ->
                                                    tileWrapper(for_ = info).Compose(composectx)
                                                    ComposeResult.ok
                                                }
                                            }.Compose(composectx)
                                            ComposeResult.ok
                                        }
                                    }
                                    .padding(Edge.Set.horizontal, 16.0).Compose(composectx)
                                    ComposeResult.ok
                                }
                            }
                            .padding(Edge.Set.bottom, 20.0).Compose(composectx)
                            ComposeResult.ok
                        }
                    }
                    .scrollDisabled(isReordering)
                    .background(LinearGradient(colors = arrayOf(Color(red = 0.1, green = 0.1, blue = 0.2), Color(red = 0.05, green = 0.05, blue = 0.15)), startPoint = UnitPoint.top, endPoint = UnitPoint.bottom)
                        .ignoresSafeArea())
                    .onPreferenceChange(TileFramePreferenceKey::class) { frames -> tileFrames = frames }
                    .overlay(alignment = Alignment.topLeading) { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            draggedTileOverlay().Compose(composectx)
                            ComposeResult.ok
                        }
                    }
                    .toolbar { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            ToolbarItem(placement = ToolbarItemPlacement.automatic) { ->
                                ComposeBuilder { composectx: ComposeContext ->
                                    if (isReordering) {
                                        Button(action = { ->
                                            withAnimation { -> isReordering = false }
                                        }) { ->
                                            ComposeBuilder { composectx: ComposeContext ->
                                                Text(LocalizedStringKey(stringLiteral = "Done"), bundle = Bundle.module)
                                                    .foregroundStyle(Color.white).Compose(composectx)
                                                ComposeResult.ok
                                            }
                                        }.Compose(composectx)
                                    } else {
                                        Button(action = { -> showSettings = true }) { ->
                                            ComposeBuilder { composectx: ComposeContext ->
                                                Image("settings", bundle = Bundle.module)
                                                    .foregroundStyle(Color.white).Compose(composectx)
                                                ComposeResult.ok
                                            }
                                        }.Compose(composectx)
                                    }
                                    ComposeResult.ok
                                }
                            }.Compose(composectx)
                            ComposeResult.ok
                        }
                    }
                    .sheet(isPresented = Binding({ _showSettings.wrappedValue }, { it -> _showSettings.wrappedValue = it })) { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            SettingsView(gamePreferences = gamePreferences)
                                .presentationDetents(setOf(PresentationDetent.medium, PresentationDetent.large)).Compose(composectx)
                            ComposeResult.ok
                        }
                    }
                    .confirmationDialog(pendingReset?.resetDialogTitle?.invoke() ?: Text(verbatim = ""), isPresented = Binding(get = { -> pendingReset != null }, set = { newValue ->
                        if (!newValue) {
                            pendingReset = null
                        }
                    }), titleVisibility = Visibility.visible, actions = { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            Button(role = ButtonRole.destructive, action = { ->
                                pendingReset?.reset?.invoke()
                                pendingReset = null
                            }) { ->
                                ComposeBuilder { composectx: ComposeContext ->
                                    Text(LocalizedStringKey(stringLiteral = "Reset"), bundle = Bundle.module).Compose(composectx)
                                    ComposeResult.ok
                                }
                            }.Compose(composectx)
                            ComposeResult.ok
                        }
                    }, message = { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            pendingReset?.resetDialogMessage?.invoke() ?: Text(verbatim = "")
                            ComposeResult.ok
                        }
                    }).Compose(composectx)
                    ComposeResult.ok
                }
            }
            .preferredColorScheme(ColorScheme.dark).Compose(composectx)
        }
    }

    @Composable
    @Suppress("UNCHECKED_CAST")
    override fun Evaluate(context: ComposeContext, options: Int): kotlin.collections.List<Renderable> {
        val rememberedgamePreferences by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<GamePreferences>, Any>) { mutableStateOf(_gamePreferences) }
        _gamePreferences = rememberedgamePreferences

        val rememberedshowSettings by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Boolean>, Any>) { mutableStateOf(_showSettings) }
        _showSettings = rememberedshowSettings

        val rememberedisReordering by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Boolean>, Any>) { mutableStateOf(_isReordering) }
        _isReordering = rememberedisReordering

        val remembereddraggedID by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<String?>, Any>) { mutableStateOf(_draggedID) }
        _draggedID = remembereddraggedID

        val remembereddragLocation by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<CGPoint>, Any>) { mutableStateOf(_dragLocation) }
        _dragLocation = remembereddragLocation

        val remembereddragTouchOffsetInTile by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<CGPoint>, Any>) { mutableStateOf(_dragTouchOffsetInTile) }
        _dragTouchOffsetInTile = remembereddragTouchOffsetInTile

        val rememberedtileFrames by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Dictionary<String, CGRect>>, Any>) { mutableStateOf(_tileFrames) }
        _tileFrames = rememberedtileFrames

        val rememberedpendingReset by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<FaireGameInfo?>, Any>) { mutableStateOf(_pendingReset) }
        _pendingReset = rememberedpendingReset

        return super.Evaluate(context, options)
    }

    // MARK: - Tile wrapping (NavigationLink vs. drag-to-reorder)

    private fun tileWrapper(for_: FaireGameInfo): View {
        val info = for_
        return ComposeBuilder { composectx: ComposeContext ->
            val isDragging = draggedID == info.id
            Group { ->
                ComposeBuilder { composectx: ComposeContext ->
                    if (isReordering) {
                        // While being dragged, hide the in-grid tile — the floating
                        // overlay shows the moving tile instead. The hidden tile
                        // still occupies its layout slot so the grid reflow looks
                        // natural.
                        gameTileContent(for_ = info)
                            .opacity(if (isDragging) 0.0 else 1.0)
                            .gesture(reorderDragGesture(for_ = info)).Compose(composectx)
                    } else {
                        NavigationLink(destination = info.destination()) { ->
                            ComposeBuilder { composectx: ComposeContext ->
                                gameTileContent(for_ = info).Compose(composectx)
                                ComposeResult.ok
                            }
                        }
                        .buttonStyle(ButtonStyle.plain)
                        .contextMenu { ->
                            ComposeBuilder { composectx: ComposeContext ->
                                contextMenuItems(for_ = info).Compose(composectx)
                                ComposeResult.ok
                            }
                        }
                        .onLongPressGesture(minimumDuration = 0.5) { ->
                            withAnimation { -> isReordering = true }
                        }.Compose(composectx)
                    }
                    ComposeResult.ok
                }
            }
            .background(GeometryReader { geo ->
                ComposeBuilder { composectx: ComposeContext ->
                    Color.clear
                        .preference(key = TileFramePreferenceKey::class, value = dictionaryOf(Tuple2(info.id, geo.frame(in_ = GlobalCoordinateSpace.global)))).Compose(composectx)
                    ComposeResult.ok
                }
            }).Compose(composectx)
            ComposeResult.ok
        }
    }

    // MARK: - Floating drag overlay

    /// Mirrors the dragged tile and positions it so the original grab point
    /// stays under the user's finger, independently of how the grid reflows.
    private fun draggedTileOverlay(): View {
        return ComposeBuilder { composectx: ComposeContext ->
            GeometryReader { geo ->
                ComposeBuilder { composectx: ComposeContext ->
                    val containerOrigin = geo.frame(in_ = GlobalCoordinateSpace.global).origin.sref()
                    draggedID?.let { id ->
                        FaireGameInfo.lookup(id = id)?.let { info ->
                            tileFrames[id].sref()?.let { frame ->
                                val originX = dragLocation.x - dragTouchOffsetInTile.x - containerOrigin.x
                                val originY = dragLocation.y - dragTouchOffsetInTile.y - containerOrigin.y
                                gameTileContent(for_ = info)
                                    .frame(width = frame.width, height = frame.height)
                                    .scaleEffect(1.06)
                                    .shadow(color = Color.black.opacity(0.4), radius = 10.0, x = 0.0, y = 6.0)
                                    .position(x = originX + frame.width / 2.0, y = originY + frame.height / 2.0)
                                    .allowsHitTesting(false).Compose(composectx)
                            }
                        }
                    }
                    ComposeResult.ok
                }
            }.Compose(composectx)
        }
    }

    // MARK: - Reorder drag gesture

    private fun reorderDragGesture(for_: FaireGameInfo): Gesture<*> {
        val info = for_
        return DragGesture(coordinateSpace = GlobalCoordinateSpace.global)
            .onChanged l@{ value ->
                if (draggedID == null) {
                    draggedID = info.id
                    val tileOrigin = (tileFrames[info.id]?.origin ?: CGPoint.zero).sref()
                    dragTouchOffsetInTile = CGPoint(x = value.startLocation.x - tileOrigin.x, y = value.startLocation.y - tileOrigin.y)
                }
                if (draggedID != info.id) {
                    return@l
                }
                dragLocation = value.location
                handleDragMoved(info = info, location = value.location)
            }
            .onEnded { _ ->
                withAnimation(Animation.spring(response = 0.3, dampingFraction = 0.75)) { ->
                    draggedID = null
                    dragLocation = CGPoint.zero
                    dragTouchOffsetInTile = CGPoint.zero
                }
            }
    }

    private fun handleDragMoved(info: FaireGameInfo, location: CGPoint) {
        val targetID_0 = tileFrames.first(where = { it -> it.key != info.id && it.value.contains(location) })?.key
        if (targetID_0 == null) {
            return
        }
        var order = orderedGames.sref()
        val from_0 = order.firstIndex(where = { it -> it.id == info.id })
        if (from_0 == null) {
            return
        }
        val to_0 = order.firstIndex(where = { it -> it.id == targetID_0 })
        if ((to_0 == null) || (from_0 == to_0)) {
            return
        }
        val item = order.remove(at = from_0)
        order.insert(item, at = to_0)
        val newRaw = order.map { it -> it.id }
        if (newRaw != gamePreferences.gameOrder) {
            withAnimation(Animation.spring(response = 0.3, dampingFraction = 0.75)) { -> gamePreferences.gameOrder = newRaw }
        }
    }

    // MARK: - Tile content + context menu

    private fun gameTileContent(for_: FaireGameInfo): View {
        val info = for_
        return ComposeBuilder { composectx: ComposeContext ->
            VStack(spacing = 10.0) { ->
                ComposeBuilder { composectx: ComposeContext ->
                    info.previewIcon()
                        .frame(width = gamePreviewIconSpan, height = gamePreviewIconSpan)
                        .clipShape(RoundedRectangle(cornerRadius = 16.0)).Compose(composectx)
                    info.title()
                        .font(Font.headline)
                        .foregroundStyle(Color.white).Compose(composectx)
                    ComposeResult.ok
                }
            }
            .padding(12.0)
            .frame(maxWidth = Double.infinity)
            .background(Color.white.opacity(0.08))
            .cornerRadius(20.0).Compose(composectx)
        }
    }

    private fun contextMenuItems(for_: FaireGameInfo): View {
        val info = for_
        return ComposeBuilder { composectx: ComposeContext ->
            Button(role = ButtonRole.destructive, action = { -> pendingReset = info }) { ->
                ComposeBuilder { composectx: ComposeContext ->
                    Label(title = { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            info.resetMenuLabel().Compose(composectx)
                            ComposeResult.ok
                        }
                    }, icon = { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            Image("restart_alt", bundle = Bundle.module).Compose(composectx)
                            ComposeResult.ok
                        }
                    }).Compose(composectx)
                    ComposeResult.ok
                }
            }.Compose(composectx)
            Button(action = { ->
                withAnimation { -> isReordering = true }
            }) { ->
                ComposeBuilder { composectx: ComposeContext ->
                    Label(title = { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            Text(LocalizedStringKey(stringLiteral = "Rearrange"), bundle = Bundle.module).Compose(composectx)
                            ComposeResult.ok
                        }
                    }, icon = { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            Image("swap_vert", bundle = Bundle.module).Compose(composectx)
                            ComposeResult.ok
                        }
                    }).Compose(composectx)
                    ComposeResult.ok
                }
            }.Compose(composectx)
            ComposeResult.ok
        }
    }

    private constructor(gamePreferences: GamePreferences = GamePreferences(), showSettings: Boolean = false, isReordering: Boolean = false, draggedID: String? = null, dragLocation: CGPoint = CGPoint.zero.sref(), dragTouchOffsetInTile: CGPoint = CGPoint.zero.sref(), tileFrames: Dictionary<String, CGRect> = dictionaryOf(), pendingReset: FaireGameInfo? = null, privatep: Nothing? = null) {
        this._gamePreferences = skip.ui.State(gamePreferences)
        this._showSettings = skip.ui.State(showSettings)
        this._isReordering = skip.ui.State(isReordering)
        this._draggedID = skip.ui.State(draggedID)
        this._dragLocation = skip.ui.State(dragLocation.sref())
        this._dragTouchOffsetInTile = skip.ui.State(dragTouchOffsetInTile.sref())
        this._tileFrames = skip.ui.State(tileFrames.sref())
        this._pendingReset = skip.ui.State(pendingReset)
    }

    constructor(gamePreferences: GamePreferences = GamePreferences(), showSettings: Boolean = false): this(gamePreferences = gamePreferences, showSettings = showSettings, privatep = null) {
    }
}

internal class SettingsView: View {
    internal var gamePreferences: GamePreferences
        get() = _gamePreferences.wrappedValue
        set(newValue) {
            _gamePreferences.wrappedValue = newValue
        }
    internal var _gamePreferences: skip.ui.Bindable<GamePreferences>
    internal var confirmResetAll: Boolean
        get() = _confirmResetAll.wrappedValue
        set(newValue) {
            _confirmResetAll.wrappedValue = newValue
        }
    internal var _confirmResetAll: skip.ui.State<Boolean>

    override fun body(): View {
        return ComposeBuilder { composectx: ComposeContext ->
            AppFairSettings(bundle = Bundle.module) { ->
                ComposeBuilder { composectx: ComposeContext ->
                    Section(header = Text(LocalizedStringKey(stringLiteral = "Data"), bundle = Bundle.module)) { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            Button(role = ButtonRole.destructive, action = { -> confirmResetAll = true }) { ->
                                ComposeBuilder { composectx: ComposeContext ->
                                    Text(LocalizedStringKey(stringLiteral = "Reset All Progress"), bundle = Bundle.module).Compose(composectx)
                                    ComposeResult.ok
                                }
                            }
                            .confirmationDialog(Text(LocalizedStringKey(stringLiteral = "Reset All Progress?"), bundle = Bundle.module), isPresented = Binding({ _confirmResetAll.wrappedValue }, { it -> _confirmResetAll.wrappedValue = it }), titleVisibility = Visibility.visible, actions = { ->
                                ComposeBuilder { composectx: ComposeContext ->
                                    Button(role = ButtonRole.destructive, action = { ->
                                        for (info in FaireGameInfo.allGames.sref()) {
                                            info.reset()
                                        }
                                    }) { ->
                                        ComposeBuilder { composectx: ComposeContext ->
                                            Text(LocalizedStringKey(stringLiteral = "Reset All"), bundle = Bundle.module).Compose(composectx)
                                            ComposeResult.ok
                                        }
                                    }.Compose(composectx)
                                    ComposeResult.ok
                                }
                            }, message = { ->
                                ComposeBuilder { composectx: ComposeContext ->
                                    Text(LocalizedStringKey(stringLiteral = "This will permanently reset all high scores and game progress to zero."), bundle = Bundle.module).Compose(composectx)
                                    ComposeResult.ok
                                }
                            }).Compose(composectx)
                            ComposeResult.ok
                        }
                    }.Compose(composectx)
                    ComposeResult.ok
                }
            }.Compose(composectx)
        }
    }

    @Composable
    @Suppress("UNCHECKED_CAST")
    override fun Evaluate(context: ComposeContext, options: Int): kotlin.collections.List<Renderable> {
        val rememberedconfirmResetAll by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Boolean>, Any>) { mutableStateOf(_confirmResetAll) }
        _confirmResetAll = rememberedconfirmResetAll

        return super.Evaluate(context, options)
    }

    constructor(gamePreferences: GamePreferences, confirmResetAll: Boolean = false) {
        this._gamePreferences = skip.ui.Bindable(gamePreferences)
        this._confirmResetAll = skip.ui.State(confirmResetAll)
    }
}
