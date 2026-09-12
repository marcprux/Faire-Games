// Licensed under the GNU General Public License v2.0 or later
// SPDX-License-Identifier: GPL-2.0-or-later

package org.appfair.app.fairegames.games.blockblast

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
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
import skip.kit.*
import org.appfair.app.fairegames.gamemodel.*
import skip.foundation.*
import skip.model.*

private val boardClearBonus: Int = 200

class BlockBlastContainerView: View {
    private var settings: BlockBlastSettings
        get() = _settings.wrappedValue
        set(newValue) {
            _settings.wrappedValue = newValue
        }
    private var _settings: skip.ui.State<BlockBlastSettings> = skip.ui.State(BlockBlastSettings())
    private var showInstructions: Boolean
        get() = _showInstructions.wrappedValue
        set(newValue) {
            _showInstructions.wrappedValue = newValue
        }
    private var _showInstructions: skip.ui.State<Boolean> = skip.ui.State(false)
    private val instructionsConfig = GameInstructionsConfig(key = "BlockBlast.instructions", bundle = Bundle.module, firstLaunchKey = "instructionsShown_BlockBlast", title = "Block Blast!")

    constructor() {
    }

    override fun body(): View {
        return ComposeBuilder { composectx: ComposeContext ->
            BlockBlastGameView(showInstructions = Binding({ _showInstructions.wrappedValue }, { it -> _showInstructions.wrappedValue = it }))
                .navigationTitle(LocalizedStringKey(stringLiteral = ""))
                .environment(settings)
                .sheet(isPresented = Binding({ _showInstructions.wrappedValue }, { it -> _showInstructions.wrappedValue = it })) { ->
                    ComposeBuilder { composectx: ComposeContext ->
                        GameInstructionsView(config = instructionsConfig).Compose(composectx)
                        ComposeResult.ok
                    }
                }
                .onAppear { ->
                    if (!instructionsConfig.hasShownToUser()) {
                        instructionsConfig.markShownToUser()
                        showInstructions = true
                    }
                }.Compose(composectx)
        }
    }

    @Composable
    @Suppress("UNCHECKED_CAST")
    override fun Evaluate(context: ComposeContext, options: Int): kotlin.collections.List<Renderable> {
        val rememberedsettings by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<BlockBlastSettings>, Any>) { mutableStateOf(_settings) }
        _settings = rememberedsettings

        val rememberedshowInstructions by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Boolean>, Any>) { mutableStateOf(_showInstructions) }
        _showInstructions = rememberedshowInstructions

        return super.Evaluate(context, options)
    }

    @androidx.annotation.Keep
    companion object {
    }
}

/// A mini block blast game
internal class BlockBlastGameView: View {
    internal var showInstructions: Boolean
        get() = _showInstructions.wrappedValue
        set(newValue) {
            _showInstructions.wrappedValue = newValue
        }
    internal var _showInstructions: Binding<Boolean>
    internal var game: GameModel
        get() = _game.wrappedValue
        set(newValue) {
            _game.wrappedValue = newValue
        }
    internal var _game: skip.ui.State<GameModel>
    internal var dragPieceIndex: Int
        get() = _dragPieceIndex.wrappedValue
        set(newValue) {
            _dragPieceIndex.wrappedValue = newValue
        }
    internal var _dragPieceIndex: skip.ui.State<Int>
    internal var dragOffset: CGSize
        get() = _dragOffset.wrappedValue.sref({ this.dragOffset = it })
        set(newValue) {
            _dragOffset.wrappedValue = newValue.sref()
        }
    internal var _dragOffset: skip.ui.State<CGSize>
    internal var dragLocation: CGPoint
        get() = _dragLocation.wrappedValue.sref({ this.dragLocation = it })
        set(newValue) {
            _dragLocation.wrappedValue = newValue.sref()
        }
    internal var _dragLocation: skip.ui.State<CGPoint>
    internal var isDragging: Boolean
        get() = _isDragging.wrappedValue
        set(newValue) {
            _isDragging.wrappedValue = newValue
        }
    internal var _isDragging: skip.ui.State<Boolean>
    internal var highlightRow: Int
        get() = _highlightRow.wrappedValue
        set(newValue) {
            _highlightRow.wrappedValue = newValue
        }
    internal var _highlightRow: skip.ui.State<Int>
    internal var highlightCol: Int
        get() = _highlightCol.wrappedValue
        set(newValue) {
            _highlightCol.wrappedValue = newValue
        }
    internal var _highlightCol: skip.ui.State<Int>
    internal var highlightValid: Boolean
        get() = _highlightValid.wrappedValue
        set(newValue) {
            _highlightValid.wrappedValue = newValue
        }
    internal var _highlightValid: skip.ui.State<Boolean>
    internal var boardOrigin: CGPoint
        get() = _boardOrigin.wrappedValue.sref({ this.boardOrigin = it })
        set(newValue) {
            _boardOrigin.wrappedValue = newValue.sref()
        }
    internal var _boardOrigin: skip.ui.State<CGPoint>
    internal var cellSize: Double
        get() = _cellSize.wrappedValue
        set(newValue) {
            _cellSize.wrappedValue = newValue
        }
    internal var _cellSize: skip.ui.State<Double>
    internal var showCombo: Boolean
        get() = _showCombo.wrappedValue
        set(newValue) {
            _showCombo.wrappedValue = newValue
        }
    internal var _showCombo: skip.ui.State<Boolean>
    internal var prevHighlightRow: Int
        get() = _prevHighlightRow.wrappedValue
        set(newValue) {
            _prevHighlightRow.wrappedValue = newValue
        }
    internal var _prevHighlightRow: skip.ui.State<Int>
    internal var prevHighlightCol: Int
        get() = _prevHighlightCol.wrappedValue
        set(newValue) {
            _prevHighlightCol.wrappedValue = newValue
        }
    internal var _prevHighlightCol: skip.ui.State<Int>
    internal var showSettings: Boolean
        get() = _showSettings.wrappedValue
        set(newValue) {
            _showSettings.wrappedValue = newValue
        }
    internal var _showSettings: skip.ui.State<Boolean>
    internal var showPauseMenu: Boolean
        get() = _showPauseMenu.wrappedValue
        set(newValue) {
            _showPauseMenu.wrappedValue = newValue
        }
    internal var _showPauseMenu: skip.ui.State<Boolean>
    internal var displayedScore: Int
        get() = _displayedScore.wrappedValue
        set(newValue) {
            _displayedScore.wrappedValue = newValue
        }
    internal var _displayedScore: skip.ui.State<Int>
    internal var displayedHighScore: Int
        get() = _displayedHighScore.wrappedValue
        set(newValue) {
            _displayedHighScore.wrappedValue = newValue
        }
    internal var _displayedHighScore: skip.ui.State<Int>
    internal var scoreAnimTimer: Timer?
        get() = _scoreAnimTimer.wrappedValue
        set(newValue) {
            _scoreAnimTimer.wrappedValue = newValue
        }
    internal var _scoreAnimTimer: skip.ui.State<Timer?> = skip.ui.State(null)
    internal lateinit var dismiss: DismissAction
    internal var settings: BlockBlastSettings
        get() = _settings.wrappedValue
        set(newValue) {
            _settings.wrappedValue = newValue
        }
    internal var _settings = skip.ui.Environment<BlockBlastSettings>()

    override fun body(): View {
        return ComposeBuilder { composectx: ComposeContext ->
            ZStack { ->
                ComposeBuilder { composectx: ComposeContext ->
                    // Background gradient
                    LinearGradient(colors = arrayOf(
                        Color(red = 0.12, green = 0.13, blue = 0.25),
                        Color(red = 0.08, green = 0.08, blue = 0.18)
                    ), startPoint = UnitPoint.top, endPoint = UnitPoint.bottom)
                        .ignoresSafeArea().Compose(composectx)

                    VStack(spacing = 12.0) { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            // Score header
                            scoreHeader.Compose(composectx)

                            // Game board
                            gameBoard.Compose(composectx)

                            // Piece tray
                            pieceTray.Compose(composectx)

                            Spacer(minLength = 0.0).Compose(composectx)
                            ComposeResult.ok
                        }
                    }
                    .padding(Edge.Set.horizontal, 8.0)
                    .padding(Edge.Set.top, 8.0).Compose(composectx)

                    // Game over overlay
                    if (game.isGameOver) {
                        gameOverOverlay.Compose(composectx)
                    }

                    // Pause menu overlay
                    if (showPauseMenu && !game.isGameOver) {
                        pauseMenuOverlay.Compose(composectx)
                    }

                    // Combo popup
                    if (showCombo && game.lastLinesCleared > 0) {
                        comboPopup.Compose(composectx)
                    }

                    ComposeResult.ok
                }
            }
            .navigationBarBackButtonHidden()
            .sheet(isPresented = Binding({ _showSettings.wrappedValue }, { it -> _showSettings.wrappedValue = it })) { ->
                ComposeBuilder { composectx: ComposeContext ->
                    BlockBlastSettingsView(settings = settings)
                        .presentationDetents(setOf(PresentationDetent.medium, PresentationDetent.large)).Compose(composectx)
                    ComposeResult.ok
                }
            }
            .onAppear { ->
                val matchtarget_0 = GameModel.loadSavedState()
                if (matchtarget_0 != null) {
                    val savedState = matchtarget_0
                    game.restoreState(savedState)
                    displayedScore = game.score
                    displayedHighScore = game.highScore
                } else {
                    displayedScore = game.score
                    displayedHighScore = game.highScore
                }
                game.solvabilityAttempts = settings.solvabilityAttempts
            }
            .onDisappear { -> stopScoreAnimation() }
            .onChange(of = game.score) { _, newScore ->
                if (newScore == 0) {
                    displayedScore = 0
                } else {
                    startScoreAnimation()
                }
            }
            .onChange(of = game.highScore) { _, newHighScore ->
                if (newHighScore == 0) {
                    displayedHighScore = 0
                } else {
                    startScoreAnimation()
                }
            }
            .onChange(of = settings.difficulty) { _, _ -> game.solvabilityAttempts = settings.solvabilityAttempts }.Compose(composectx)
        }
    }

    @Composable
    @Suppress("UNCHECKED_CAST")
    override fun Evaluate(context: ComposeContext, options: Int): kotlin.collections.List<Renderable> {
        val rememberedgame by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<GameModel>, Any>) { mutableStateOf(_game) }
        _game = rememberedgame

        val remembereddragPieceIndex by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Int>, Any>) { mutableStateOf(_dragPieceIndex) }
        _dragPieceIndex = remembereddragPieceIndex

        val remembereddragOffset by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<CGSize>, Any>) { mutableStateOf(_dragOffset) }
        _dragOffset = remembereddragOffset

        val remembereddragLocation by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<CGPoint>, Any>) { mutableStateOf(_dragLocation) }
        _dragLocation = remembereddragLocation

        val rememberedisDragging by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Boolean>, Any>) { mutableStateOf(_isDragging) }
        _isDragging = rememberedisDragging

        val rememberedhighlightRow by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Int>, Any>) { mutableStateOf(_highlightRow) }
        _highlightRow = rememberedhighlightRow

        val rememberedhighlightCol by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Int>, Any>) { mutableStateOf(_highlightCol) }
        _highlightCol = rememberedhighlightCol

        val rememberedhighlightValid by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Boolean>, Any>) { mutableStateOf(_highlightValid) }
        _highlightValid = rememberedhighlightValid

        val rememberedboardOrigin by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<CGPoint>, Any>) { mutableStateOf(_boardOrigin) }
        _boardOrigin = rememberedboardOrigin

        val rememberedcellSize by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Double>, Any>) { mutableStateOf(_cellSize) }
        _cellSize = rememberedcellSize

        val rememberedshowCombo by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Boolean>, Any>) { mutableStateOf(_showCombo) }
        _showCombo = rememberedshowCombo

        val rememberedprevHighlightRow by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Int>, Any>) { mutableStateOf(_prevHighlightRow) }
        _prevHighlightRow = rememberedprevHighlightRow

        val rememberedprevHighlightCol by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Int>, Any>) { mutableStateOf(_prevHighlightCol) }
        _prevHighlightCol = rememberedprevHighlightCol

        val rememberedshowSettings by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Boolean>, Any>) { mutableStateOf(_showSettings) }
        _showSettings = rememberedshowSettings

        val rememberedshowPauseMenu by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Boolean>, Any>) { mutableStateOf(_showPauseMenu) }
        _showPauseMenu = rememberedshowPauseMenu

        val remembereddisplayedScore by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Int>, Any>) { mutableStateOf(_displayedScore) }
        _displayedScore = remembereddisplayedScore

        val remembereddisplayedHighScore by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Int>, Any>) { mutableStateOf(_displayedHighScore) }
        _displayedHighScore = remembereddisplayedHighScore

        val rememberedscoreAnimTimer by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Timer?>, Any>) { mutableStateOf(_scoreAnimTimer) }
        _scoreAnimTimer = rememberedscoreAnimTimer

        this.dismiss = EnvironmentValues.shared.dismiss
        _settings.wrappedValue = EnvironmentValues.shared.environmentObject(type = BlockBlastSettings::class)!!

        return super.Evaluate(context, options)
    }

    // MARK: - Score Header

    internal val scoreHeader: View
        get() {
            return HStack(spacing = 12.0) { ->
                ComposeBuilder { composectx: ComposeContext ->
                    Button(action = { -> dismiss() }) { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            Image("close", bundle = Bundle.module)
                                .font(Font.system(size = 13.0, weight = Font.Weight.bold))
                                .foregroundStyle(Color.white.opacity(0.7))
                                .frame(width = 30.0, height = 30.0)
                                .background(Color.white.opacity(0.12))
                                .clipShape(Circle()).Compose(composectx)
                            ComposeResult.ok
                        }
                    }.Compose(composectx)

                    VStack(alignment = HorizontalAlignment.leading, spacing = 2.0) { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            Text(LocalizedStringKey(stringLiteral = "SCORE"), bundle = Bundle.module)
                                .font(Font.caption)
                                .fontWeight(Font.Weight.bold)
                                .foregroundStyle(Color.white.opacity(0.6)).Compose(composectx)
                            Text({
                                val str = LocalizedStringKey.StringInterpolation(literalCapacity = 0, interpolationCount = 0)
                                str.appendInterpolation(displayedScore)
                                LocalizedStringKey(stringInterpolation = str)
                            }())
                                .font(Font.title2)
                                .fontWeight(Font.Weight.bold)
                                .foregroundStyle(Color.white)
                                .monospaced().Compose(composectx)
                            ComposeResult.ok
                        }
                    }.Compose(composectx)

                    Spacer().Compose(composectx)

                    Text(LocalizedStringKey(stringLiteral = "Block Blast"), bundle = Bundle.module)
                        .font(Font.title3)
                        .fontWeight(Font.Weight.heavy)
                        .foregroundStyle(Color.white).Compose(composectx)

                    Spacer().Compose(composectx)

                    VStack(alignment = HorizontalAlignment.trailing, spacing = 2.0) { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            Text(LocalizedStringKey(stringLiteral = "BEST"), bundle = Bundle.module)
                                .font(Font.caption)
                                .fontWeight(Font.Weight.bold)
                                .foregroundStyle(Color.white.opacity(0.6)).Compose(composectx)
                            Text({
                                val str = LocalizedStringKey.StringInterpolation(literalCapacity = 0, interpolationCount = 0)
                                str.appendInterpolation(displayedHighScore)
                                LocalizedStringKey(stringInterpolation = str)
                            }())
                                .font(Font.title2)
                                .fontWeight(Font.Weight.bold)
                                .foregroundStyle(Color.yellow)
                                .monospaced().Compose(composectx)
                            ComposeResult.ok
                        }
                    }.Compose(composectx)

                    Button(action = { -> showPauseMenu = true }) { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            Image("pause_circle", bundle = Bundle.module)
                                .font(Font.system(size = 13.0, weight = Font.Weight.bold))
                                .foregroundStyle(Color.white.opacity(0.7))
                                .frame(width = 30.0, height = 30.0)
                                .background(Color.white.opacity(0.12))
                                .clipShape(Circle()).Compose(composectx)
                            ComposeResult.ok
                        }
                    }.Compose(composectx)
                    ComposeResult.ok
                }
            }
            .padding(Edge.Set.leading, 12.0)
            .padding(Edge.Set.trailing, 12.0)
            .padding(Edge.Set.vertical, 10.0)
            .background(RoundedRectangle(cornerRadius = 12.0)
                .fill(Color.white.opacity(0.1)))
        }

    // MARK: - Game Board

    internal val gameBoard: View
        get() {
            return GeometryReader { geo ->
                ComposeBuilder { composectx: ComposeContext ->
                    val boardSize = min(geo.size.width, geo.size.height)
                    val cs = boardSize / Double(GameModel.gridSize)
                    val originX = (geo.size.width - boardSize) / 2.0
                    val originY: Double = 0.0

                    ZStack(alignment = Alignment.topLeading) { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            // Board background
                            RoundedRectangle(cornerRadius = 8.0)
                                .fill(Color(red = 0.15, green = 0.16, blue = 0.3))
                                .frame(width = boardSize, height = boardSize)
                                .offset(x = originX).Compose(composectx)

                            // Grid cells
                            ForEach(0..<GameModel.gridSize, id = { it }) { row ->
                                ComposeBuilder { composectx: ComposeContext ->
                                    ForEach(0..<GameModel.gridSize, id = { it }) { col ->
                                        ComposeBuilder { composectx: ComposeContext ->
                                            val cellValue = game.grid[row][col]
                                            val isHighlight = isDragging && isHighlightCell(row = row, col = col)

                                            cellView(colorIndex = cellValue, isHighlight = isHighlight, isValidHighlight = highlightValid, size = cs)
                                                .offset(x = originX + Double(col) * cs, y = originY + Double(row) * cs).Compose(composectx)
                                            ComposeResult.ok
                                        }
                                    }.Compose(composectx)
                                    ComposeResult.ok
                                }
                            }.Compose(composectx)

                            // Floating drag piece — rendered inside the board ZStack so it
                            // shares the exact same coordinate space as the grid cells.
                            if (isDragging && dragPieceIndex >= 0 && dragPieceIndex < game.currentPieces.count) {
                                game.currentPieces[dragPieceIndex]?.let { piece ->
                                    floatingPiece(piece = piece, boardOriginX = originX, cs = cs).Compose(composectx)
                                }
                            }
                            ComposeResult.ok
                        }
                    }
                    .onAppear { -> cellSize = cs }
                    .onChange(of = geo.size) { ->
                        val newBoardSize = min(geo.size.width, geo.size.height)
                        cellSize = newBoardSize / Double(GameModel.gridSize)
                    }
                    .background(GeometryReader { boardGeo ->
                        ComposeBuilder { composectx: ComposeContext ->
                            Color.clear
                                .onAppear { -> updateBoardOrigin(boardGeo) }
                                .onChange(of = boardGeo.size) { -> updateBoardOrigin(boardGeo) }
                                .onChange(of = isDragging) { -> updateBoardOrigin(boardGeo) }.Compose(composectx)
                            ComposeResult.ok
                        }
                    }
                    .frame(width = boardSize, height = boardSize)
                    .offset(x = originX)).Compose(composectx)
                    ComposeResult.ok
                }
            }
            .aspectRatio(1.0, contentMode = ContentMode.fit)
        }

    internal fun updateBoardOrigin(geo: GeometryProxy) {
        val frame = geo.frame(in_ = GlobalCoordinateSpace.global)
        boardOrigin = CGPoint(x = frame.minX, y = frame.minY)
    }

    internal fun isHighlightCell(row: Int, col: Int): Boolean {
        if (highlightRow < 0 || highlightCol < 0) {
            return false
        }
        if (dragPieceIndex < 0 || dragPieceIndex >= game.currentPieces.count) {
            return false
        }
        val piece_0 = game.currentPieces[dragPieceIndex]
        if (piece_0 == null) {
            return false
        }
        for (cell in piece_0.shape.cells.sref()) {
            if (row == highlightRow + cell.row && col == highlightCol + cell.col) {
                return true
            }
        }
        return false
    }

    internal fun cellView(colorIndex: Int, isHighlight: Boolean, isValidHighlight: Boolean, size: Double): View {
        val inset: Double = 1.5
        return ZStack { ->
            ComposeBuilder { composectx: ComposeContext ->
                if (isHighlight) {
                    RoundedRectangle(cornerRadius = 3.0)
                        .fill(if (isValidHighlight) Color.white.opacity(0.4) else Color.red.opacity(0.3))
                        .frame(width = size - inset * 2, height = size - inset * 2).Compose(composectx)
                } else if (colorIndex >= 0) {
                    RoundedRectangle(cornerRadius = 3.0)
                        .fill(BlockColors.color(for_ = colorIndex))
                        .frame(width = size - inset * 2, height = size - inset * 2).Compose(composectx)
                    RoundedRectangle(cornerRadius = 3.0)
                        .fill(LinearGradient(colors = arrayOf(Color.white.opacity(0.3), Color.clear), startPoint = UnitPoint.topLeading, endPoint = UnitPoint.bottomTrailing))
                        .frame(width = size - inset * 2, height = size - inset * 2).Compose(composectx)
                } else {
                    RoundedRectangle(cornerRadius = 3.0)
                        .fill(Color.white.opacity(0.05))
                        .frame(width = size - inset * 2, height = size - inset * 2).Compose(composectx)
                }
                ComposeResult.ok
            }
        }
        .frame(width = size, height = size)
    }

    // MARK: - Piece Tray

    internal val pieceTray: View
        get() {
            return HStack(spacing = 16.0) { ->
                ComposeBuilder { composectx: ComposeContext ->
                    ForEach(0..<3, id = { it }) { index ->
                        ComposeBuilder { composectx: ComposeContext ->
                            pieceView(index = index).Compose(composectx)
                            ComposeResult.ok
                        }
                    }.Compose(composectx)
                    ComposeResult.ok
                }
            }
            .padding(Edge.Set.vertical, 12.0)
            .padding(Edge.Set.horizontal, 8.0)
            .background(RoundedRectangle(cornerRadius = 12.0)
                .fill(Color.white.opacity(0.08)))
        }

    /// Fixed height for each piece slot — accommodates the tallest piece (5 cells)
    internal val pieceSlotHeight: Double
        get() {
            val pieceScale: Double = if (cellSize > 0.0) cellSize * 0.55 else 16.0
            return max(pieceScale * 5.0, 60.0)
        }

    internal fun pieceView(index: Int): View {
        val piece = game.currentPieces[index]
        val pieceScale: Double = if (cellSize > 0.0) cellSize * 0.55 else 16.0
        val isBeingDragged = isDragging && dragPieceIndex == index
        val slotHeight = pieceSlotHeight

        return ZStack { ->
            ComposeBuilder { composectx: ComposeContext ->
                if (piece != null) {
                    val w = piece.shape.width
                    val h = piece.shape.height
                    val pieceWidth = Double(w) * pieceScale
                    val pieceHeight = Double(h) * pieceScale

                    ZStack(alignment = Alignment.topLeading) { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            ForEach(0..<piece.shape.cells.count, id = { it }) { ci ->
                                ComposeBuilder { composectx: ComposeContext ->
                                    val cell = piece.shape.cells[ci]
                                    RoundedRectangle(cornerRadius = 2.0)
                                        .fill(BlockColors.color(for_ = piece.shape.colorIndex))
                                        .frame(width = pieceScale - 2, height = pieceScale - 2)
                                        .offset(x = Double(cell.col) * pieceScale + 1, y = Double(cell.row) * pieceScale + 1).Compose(composectx)
                                    ComposeResult.ok
                                }
                            }.Compose(composectx)
                            ComposeResult.ok
                        }
                    }
                    .frame(width = pieceWidth, height = pieceHeight, alignment = Alignment.topLeading)
                    .opacity(if (isBeingDragged) 0.3 else 1.0).Compose(composectx)
                }
                // Invisible hit area that always maintains the fixed slot size
                Color.white.opacity(0.001).Compose(composectx)
                ComposeResult.ok
            }
        }
        .frame(maxWidth = Double.infinity)
        .frame(height = slotHeight)
        .gesture(DragGesture(coordinateSpace = GlobalCoordinateSpace.global)
            .onChanged { value ->
                if (!showPauseMenu && game.currentPieces[index] != null) {
                    handleDragChanged(index = index, value = value)
                }
            }
            .onEnded { value ->
                if (!showPauseMenu && game.currentPieces[index] != null) {
                    handleDragEnded(index = index, value = value)
                }
            })
    }

    // MARK: - Floating Drag Piece

    internal fun floatingPiece(piece: GamePiece, boardOriginX: Double, cs: Double): View {
        val shape = piece.shape
        val pieceWidth = Double(shape.width) * cs
        val pieceHeight = Double(shape.height) * cs
        val fingerOffset: Double = cs * 2.5

        // Use boardOrigin (global coords of the board) to convert the global
        // dragLocation into board-relative coordinates. This is the same math
        // used for the ghost highlight in handleDragChanged, ensuring they
        // always align perfectly on all platforms.
        //
        // Board-relative top-left of the piece:
        val boardRelX = dragLocation.x - boardOrigin.x - pieceWidth / 2.0
        val boardRelY = dragLocation.y - boardOrigin.y - fingerOffset - pieceHeight / 2.0

        // In the board's ZStack, grid cells are placed at (boardOriginX + col*cs, row*cs).
        // boardOrigin tracks the global position of the board background, which starts
        // at boardOriginX within the ZStack. So to place in ZStack coords:
        val offsetX = boardOriginX + boardRelX
        val offsetY = boardRelY

        return ZStack(alignment = Alignment.topLeading) { ->
            ComposeBuilder { composectx: ComposeContext ->
                ForEach(0..<shape.cells.count, id = { it }) { ci ->
                    ComposeBuilder { composectx: ComposeContext ->
                        val cell = shape.cells[ci]
                        val inset: Double = 1.5
                        ZStack { ->
                            ComposeBuilder { composectx: ComposeContext ->
                                RoundedRectangle(cornerRadius = 3.0)
                                    .fill(BlockColors.color(for_ = shape.colorIndex))
                                    .frame(width = cs - inset * 2, height = cs - inset * 2).Compose(composectx)
                                RoundedRectangle(cornerRadius = 3.0)
                                    .fill(LinearGradient(colors = arrayOf(Color.white.opacity(0.3), Color.clear), startPoint = UnitPoint.topLeading, endPoint = UnitPoint.bottomTrailing))
                                    .frame(width = cs - inset * 2, height = cs - inset * 2).Compose(composectx)
                                ComposeResult.ok
                            }
                        }
                        .frame(width = cs, height = cs)
                        .offset(x = Double(cell.col) * cs, y = Double(cell.row) * cs).Compose(composectx)
                        ComposeResult.ok
                    }
                }.Compose(composectx)
                ComposeResult.ok
            }
        }
        .frame(width = pieceWidth, height = pieceHeight, alignment = Alignment.topLeading)
        .shadow(color = Color.black.opacity(0.5), radius = 8.0, y = 4.0)
        .offset(x = offsetX, y = offsetY)
        .allowsHitTesting(false)
    }

    // MARK: - Haptic Helper

    internal fun playHaptic(pattern: HapticPattern) {
        if (settings.vibrations) {
            HapticFeedback.play(pattern)
        }
    }

    // MARK: - Score Animation

    internal fun startScoreAnimation() {
        if (scoreAnimTimer != null) {
            return
        }
        scoreAnimTimer = Timer.scheduledTimer(withTimeInterval = 0.02, repeats = true) { _ -> tickScoreAnimation() }
    }

    internal fun tickScoreAnimation() {
        var changed = false

        if (displayedScore != game.score) {
            val diff = game.score - displayedScore
            if (diff > 0) {
                val step = max(1, diff / 8)
                displayedScore = min(displayedScore + step, game.score)
            } else {
                displayedScore = game.score
            }
            changed = true
        }

        if (displayedHighScore != game.highScore) {
            val diff = game.highScore - displayedHighScore
            if (diff > 0) {
                val step = max(1, diff / 8)
                displayedHighScore = min(displayedHighScore + step, game.highScore)
            } else {
                displayedHighScore = game.highScore
            }
            changed = true
        }

        if (!changed) {
            scoreAnimTimer?.invalidate()
            scoreAnimTimer = null
        }
    }

    internal fun stopScoreAnimation() {
        scoreAnimTimer?.invalidate()
        scoreAnimTimer = null
    }

    // MARK: - Drag Handling

    internal fun handleDragChanged(index: Int, value: DragGesture.Value) {
        val wasAlreadyDragging = isDragging
        isDragging = true
        dragPieceIndex = index
        dragOffset = value.translation
        dragLocation = value.location

        if (!wasAlreadyDragging) {
            playHaptic(HapticPattern.pick)
        }
        val piece_1 = game.currentPieces[index]
        if (piece_1 == null) {
            return
        }
        val shape = piece_1.shape

        // Offset the target point so the shape centers on the finger
        // with a vertical offset so the piece appears above the finger
        val fingerOffset: Double = cellSize * 2.5
        val targetX = dragLocation.x - boardOrigin.x - Double(shape.width) * cellSize / 2.0
        val targetY = dragLocation.y - boardOrigin.y - fingerOffset - Double(shape.height) * cellSize / 2.0

        val col = Int(round(targetX / cellSize))
        val row = Int(round(targetY / cellSize))

        // Fire snap haptic when moving to a new valid grid cell
        if (row != prevHighlightRow || col != prevHighlightCol) {
            val isValid = game.canPlace(shape = shape, atRow = row, col = col)
            if (isValid) {
                playHaptic(HapticPattern.snap)
            }
            prevHighlightRow = row
            prevHighlightCol = col
        }

        highlightRow = row
        highlightCol = col
        highlightValid = game.canPlace(shape = shape, atRow = row, col = col)
    }

    internal fun handleDragEnded(index: Int, value: DragGesture.Value) {
        if (highlightValid && highlightRow >= 0 && highlightCol >= 0) {
            game.currentPieces[index]?.let { piece ->
                game.placeShape(shape = piece.shape, atRow = highlightRow, col = highlightCol, pieceIndex = index)

                if (game.comboStreak > 2) {
                    playHaptic(HapticPattern.combo(streak = game.comboStreak))
                } else if (game.lastLinesCleared > 1) {
                    playHaptic(HapticPattern.bigCelebrate)
                } else if (game.lastLinesCleared > 0) {
                    playHaptic(HapticPattern.celebrate)
                } else {
                    playHaptic(HapticPattern.place)
                }

                if (game.lastLinesCleared > 0) {
                    showCombo = true
                    val popupDuration = if (game.boardCleared) 2.0 else 1.0
                    DispatchQueue.main.asyncAfter(deadline = Double.now() + popupDuration) { -> showCombo = false }
                }

                if (game.isGameOver) {
                    playHaptic(HapticPattern.error)
                }

                game.saveState()
            }
        } else if (isDragging) {
            playHaptic(HapticPattern.warning)
        }

        isDragging = false
        dragPieceIndex = -1
        dragOffset = CGSize.zero
        highlightRow = -1
        highlightCol = -1
        highlightValid = false
        prevHighlightRow = -1
        prevHighlightCol = -1
    }

    // MARK: - Pause Menu Overlay

    internal val pauseMenuOverlay: View
        get() {
            return ZStack { ->
                ComposeBuilder { composectx: ComposeContext ->
                    Color.black.opacity(0.7)
                        .ignoresSafeArea().Compose(composectx)

                    VStack(spacing = 16.0) { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            Text(LocalizedStringKey(stringLiteral = "PAUSED"), bundle = Bundle.module)
                                .font(Font.largeTitle)
                                .fontWeight(Font.Weight.black)
                                .foregroundStyle(Color.white).Compose(composectx)

                            Button(action = { -> showPauseMenu = false }) { ->
                                ComposeBuilder { composectx: ComposeContext ->
                                    Text(LocalizedStringKey(stringLiteral = "Resume"), bundle = Bundle.module)
                                        .font(Font.headline)
                                        .fontWeight(Font.Weight.bold)
                                        .foregroundStyle(Color.white)
                                        .frame(width = 160.0).Compose(composectx)
                                    ComposeResult.ok
                                }
                            }
                            .buttonStyle(ButtonStyle.borderedProminent)
                            .tint(Color.green).Compose(composectx)

                            Button(action = { ->
                                showPauseMenu = false
                                GameModel.clearSavedState()
                                game.newGame()
                                stopScoreAnimation()
                                displayedScore = 0
                                displayedHighScore = game.highScore
                            }) { ->
                                ComposeBuilder { composectx: ComposeContext ->
                                    Text(LocalizedStringKey(stringLiteral = "New Game"), bundle = Bundle.module)
                                        .font(Font.headline)
                                        .fontWeight(Font.Weight.bold)
                                        .foregroundStyle(Color.white)
                                        .frame(width = 160.0).Compose(composectx)
                                    ComposeResult.ok
                                }
                            }
                            .buttonStyle(ButtonStyle.borderedProminent)
                            .tint(Color(red = 0.30, green = 0.55, blue = 0.95)).Compose(composectx)

                            Button(action = { ->
                                showPauseMenu = false
                                showSettings = true
                            }) { ->
                                ComposeBuilder { composectx: ComposeContext ->
                                    Text(LocalizedStringKey(stringLiteral = "Settings"), bundle = Bundle.module)
                                        .font(Font.headline)
                                        .fontWeight(Font.Weight.bold)
                                        .foregroundStyle(Color.white)
                                        .frame(width = 160.0).Compose(composectx)
                                    ComposeResult.ok
                                }
                            }
                            .buttonStyle(ButtonStyle.borderedProminent)
                            .tint(Color(red = 0.3, green = 0.4, blue = 0.6)).Compose(composectx)

                            Button(action = { ->
                                showPauseMenu = false
                                showInstructions = true
                            }) { ->
                                ComposeBuilder { composectx: ComposeContext ->
                                    Text(LocalizedStringKey(stringLiteral = "Instructions"), bundle = Bundle.module)
                                        .font(Font.headline)
                                        .fontWeight(Font.Weight.bold)
                                        .foregroundStyle(Color.white)
                                        .frame(width = 160.0).Compose(composectx)
                                    ComposeResult.ok
                                }
                            }
                            .buttonStyle(ButtonStyle.borderedProminent)
                            .tint(Color(red = 0.4, green = 0.4, blue = 0.7)).Compose(composectx)

                            Button(action = { -> dismiss() }) { ->
                                ComposeBuilder { composectx: ComposeContext ->
                                    Text(LocalizedStringKey(stringLiteral = "Quit Game"), bundle = Bundle.module)
                                        .font(Font.headline)
                                        .fontWeight(Font.Weight.bold)
                                        .foregroundStyle(Color.white)
                                        .frame(width = 160.0).Compose(composectx)
                                    ComposeResult.ok
                                }
                            }
                            .buttonStyle(ButtonStyle.borderedProminent)
                            .tint(Color.red).Compose(composectx)
                            ComposeResult.ok
                        }
                    }
                    .padding(28.0)
                    .background(RoundedRectangle(cornerRadius = 20.0)
                        .fill(Color(red = 0.08, green = 0.08, blue = 0.18))).Compose(composectx)
                    ComposeResult.ok
                }
            }
        }

    // MARK: - Game Over Overlay

    internal val gameOverOverlay: View
        get() {
            return ZStack { ->
                ComposeBuilder { composectx: ComposeContext ->
                    Color.black.opacity(0.7)
                        .ignoresSafeArea().Compose(composectx)

                    VStack(spacing = 20.0) { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            Text(LocalizedStringKey(stringLiteral = "Game Over"), bundle = Bundle.module)
                                .font(Font.largeTitle)
                                .fontWeight(Font.Weight.heavy)
                                .foregroundStyle(Color.white).Compose(composectx)

                            VStack(spacing = 8.0) { ->
                                ComposeBuilder { composectx: ComposeContext ->
                                    Text(LocalizedStringKey(stringLiteral = "Score"), bundle = Bundle.module)
                                        .font(Font.headline)
                                        .foregroundStyle(Color.white.opacity(0.7)).Compose(composectx)
                                    Text({
                                        val str = LocalizedStringKey.StringInterpolation(literalCapacity = 0, interpolationCount = 0)
                                        str.appendInterpolation(displayedScore)
                                        LocalizedStringKey(stringInterpolation = str)
                                    }())
                                        .font(Font.system(size = 48.0))
                                        .fontWeight(Font.Weight.bold)
                                        .foregroundStyle(Color.yellow)
                                        .monospaced().Compose(composectx)
                                    ComposeResult.ok
                                }
                            }.Compose(composectx)

                            VStack(spacing = 2.0) { ->
                                ComposeBuilder { composectx: ComposeContext ->
                                    Text(LocalizedStringKey(stringLiteral = "Difficulty"), bundle = Bundle.module)
                                        .font(Font.caption)
                                        .foregroundStyle(Color.white.opacity(0.6)).Compose(composectx)
                                    Text({
                                        val str = LocalizedStringKey.StringInterpolation(literalCapacity = 0, interpolationCount = 0)
                                        str.appendInterpolation(settings.difficulty)
                                        LocalizedStringKey(stringInterpolation = str)
                                    }())
                                        .font(Font.title3)
                                        .fontWeight(Font.Weight.bold)
                                        .foregroundStyle(Color.white).Compose(composectx)
                                    ComposeResult.ok
                                }
                            }.Compose(composectx)

                            if (game.score >= game.highScore && game.score > 0) {
                                Text(LocalizedStringKey(stringLiteral = "New High Score!"), bundle = Bundle.module)
                                    .font(Font.title3)
                                    .fontWeight(Font.Weight.bold)
                                    .foregroundStyle(Color.yellow).Compose(composectx)
                            }

                            Button(action = { ->
                                GameModel.clearSavedState()
                                game.newGame()
                                stopScoreAnimation()
                                displayedScore = 0
                                displayedHighScore = game.highScore
                            }) { ->
                                ComposeBuilder { composectx: ComposeContext ->
                                    Text(LocalizedStringKey(stringLiteral = "Play Again"), bundle = Bundle.module)
                                        .font(Font.title3)
                                        .fontWeight(Font.Weight.bold)
                                        .foregroundStyle(Color.white)
                                        .frame(width = 160.0).Compose(composectx)
                                    ComposeResult.ok
                                }
                            }
                            .buttonStyle(ButtonStyle.borderedProminent)
                            .tint(Color.blue)
                            .padding(Edge.Set.top, 8.0).Compose(composectx)

                            Button(action = { -> dismiss() }) { ->
                                ComposeBuilder { composectx: ComposeContext ->
                                    Text(LocalizedStringKey(stringLiteral = "Quit Game"), bundle = Bundle.module)
                                        .font(Font.headline)
                                        .fontWeight(Font.Weight.bold)
                                        .foregroundStyle(Color.white)
                                        .frame(width = 160.0).Compose(composectx)
                                    ComposeResult.ok
                                }
                            }
                            .buttonStyle(ButtonStyle.borderedProminent)
                            .tint(Color.red).Compose(composectx)

                            ShareLink(item = "I scored ${game.score} in Block Blast (difficulty ${settings.difficulty}) on Faire Games! Can you beat it?\nhttps://appfair.net", subject = Text(LocalizedStringKey(stringLiteral = "Block Blast Score"), bundle = Bundle.module), message = Text({
                                val str = LocalizedStringKey.StringInterpolation(literalCapacity = 0, interpolationCount = 0)
                                str.appendLiteral("I scored ")
                                str.appendInterpolation(game.score)
                                str.appendLiteral(" in Block Blast!")
                                LocalizedStringKey(stringInterpolation = str)
                            }())) { ->
                                ComposeBuilder { composectx: ComposeContext ->
                                    Label(title = { ->
                                        ComposeBuilder { composectx: ComposeContext ->
                                            Text(LocalizedStringKey(stringLiteral = "Share"), bundle = Bundle.module).Compose(composectx)
                                            ComposeResult.ok
                                        }
                                    }, icon = { ->
                                        ComposeBuilder { composectx: ComposeContext ->
                                            Image(systemName = "square.and.arrow.up").Compose(composectx)
                                            ComposeResult.ok
                                        }
                                    })
                                    .font(Font.subheadline)
                                    .foregroundStyle(Color.white.opacity(0.7)).Compose(composectx)
                                    ComposeResult.ok
                                }
                            }.Compose(composectx)
                            ComposeResult.ok
                        }
                    }
                    .padding(32.0)
                    .background(RoundedRectangle(cornerRadius = 20.0)
                        .fill(Color(red = 0.15, green = 0.16, blue = 0.3))).Compose(composectx)
                    ComposeResult.ok
                }
            }
        }

    // MARK: - Combo Popup

    internal val comboPopup: View
        get() {
            return VStack(spacing = 4.0) { ->
                ComposeBuilder { composectx: ComposeContext ->
                    if (game.boardCleared) {
                        Text({
                            val str = LocalizedStringKey.StringInterpolation(literalCapacity = 0, interpolationCount = 0)
                            str.appendLiteral("Board Clear! +")
                            str.appendInterpolation(boardClearBonus)
                            LocalizedStringKey(stringInterpolation = str)
                        }())
                            .font(Font.title)
                            .fontWeight(Font.Weight.heavy)
                            .foregroundStyle(Color.green).Compose(composectx)
                    }
                    if (game.lastLinesCleared > 1) {
                        Text({
                            val str = LocalizedStringKey.StringInterpolation(literalCapacity = 0, interpolationCount = 0)
                            str.appendInterpolation(game.lastLinesCleared)
                            str.appendLiteral("x Lines!")
                            LocalizedStringKey(stringInterpolation = str)
                        }())
                            .font(Font.title)
                            .fontWeight(Font.Weight.heavy)
                            .foregroundStyle(Color.yellow).Compose(composectx)
                    }
                    if (game.comboStreak > 1) {
                        Text({
                            val str = LocalizedStringKey.StringInterpolation(literalCapacity = 0, interpolationCount = 0)
                            str.appendLiteral("Combo x")
                            str.appendInterpolation(game.comboStreak)
                            str.appendLiteral("!")
                            LocalizedStringKey(stringInterpolation = str)
                        }())
                            .font(Font.title2)
                            .fontWeight(Font.Weight.bold)
                            .foregroundStyle(Color.orange).Compose(composectx)
                    } else if (game.lastLinesCleared == 1) {
                        Text(LocalizedStringKey(stringLiteral = "Line Clear!"), bundle = Bundle.module)
                            .font(Font.title2)
                            .fontWeight(Font.Weight.bold)
                            .foregroundStyle(Color.cyan).Compose(composectx)
                    }
                    ComposeResult.ok
                }
            }
            .padding(Edge.Set.horizontal, 24.0)
            .padding(Edge.Set.vertical, 12.0)
            .background(RoundedRectangle(cornerRadius = 12.0)
                .fill(Color.black.opacity(0.7)))
            .allowsHitTesting(false)
            .transition(AnyTransition.opacity)
        }

    constructor(showInstructions: Binding<Boolean>, game: GameModel = GameModel(), dragPieceIndex: Int = -1, dragOffset: CGSize = CGSize.zero.sref(), dragLocation: CGPoint = CGPoint.zero.sref(), isDragging: Boolean = false, highlightRow: Int = -1, highlightCol: Int = -1, highlightValid: Boolean = false, boardOrigin: CGPoint = CGPoint.zero.sref(), cellSize: Double = 0.0, showCombo: Boolean = false, prevHighlightRow: Int = -1, prevHighlightCol: Int = -1, showSettings: Boolean = false, showPauseMenu: Boolean = false, displayedScore: Int = 0, displayedHighScore: Int = 0, scoreAnimTimer: Timer? = null) {
        this._showInstructions = showInstructions
        this._game = skip.ui.State(game)
        this._dragPieceIndex = skip.ui.State(dragPieceIndex)
        this._dragOffset = skip.ui.State(dragOffset.sref())
        this._dragLocation = skip.ui.State(dragLocation.sref())
        this._isDragging = skip.ui.State(isDragging)
        this._highlightRow = skip.ui.State(highlightRow)
        this._highlightCol = skip.ui.State(highlightCol)
        this._highlightValid = skip.ui.State(highlightValid)
        this._boardOrigin = skip.ui.State(boardOrigin.sref())
        this._cellSize = skip.ui.State(cellSize)
        this._showCombo = skip.ui.State(showCombo)
        this._prevHighlightRow = skip.ui.State(prevHighlightRow)
        this._prevHighlightCol = skip.ui.State(prevHighlightCol)
        this._showSettings = skip.ui.State(showSettings)
        this._showPauseMenu = skip.ui.State(showPauseMenu)
        this._displayedScore = skip.ui.State(displayedScore)
        this._displayedHighScore = skip.ui.State(displayedHighScore)
        this._scoreAnimTimer = skip.ui.State(scoreAnimTimer)
    }
}

// MARK: - Color Palette

/// Block colors used throughout the game
internal class BlockColors {

    @androidx.annotation.Keep
    companion object {
        internal fun color(for_: Int): Color {
            val index = for_
            when (index) {
                0 -> return Color.red
                1 -> return Color.blue
                2 -> return Color.green
                3 -> return Color.orange
                4 -> return Color.purple
                5 -> return Color.yellow
                6 -> return Color.pink
                else -> return Color.gray
            }
        }

        internal fun darkColor(for_: Int): Color {
            val index = for_
            when (index) {
                0 -> return Color(red = 0.7, green = 0.1, blue = 0.1)
                1 -> return Color(red = 0.1, green = 0.2, blue = 0.7)
                2 -> return Color(red = 0.1, green = 0.5, blue = 0.1)
                3 -> return Color(red = 0.8, green = 0.4, blue = 0.0)
                4 -> return Color(red = 0.5, green = 0.1, blue = 0.5)
                5 -> return Color(red = 0.7, green = 0.6, blue = 0.0)
                6 -> return Color(red = 0.8, green = 0.3, blue = 0.5)
                else -> return Color.gray
            }
        }
    }
}

// MARK: - Block Shape Definitions

/// Represents a single cell offset within a block shape
internal class CellOffset {
    internal val row: Int
    internal val col: Int

    constructor(row: Int, col: Int) {
        this.row = row
        this.col = col
    }

    override fun equals(other: Any?): Boolean {
        if (other !is CellOffset) return false
        return row == other.row && col == other.col
    }

    override fun hashCode(): Int {
        var result = 1
        result = Hasher.combine(result, row)
        result = Hasher.combine(result, col)
        return result
    }
}

/// All available block shapes in the game (no rotation)
internal class BlockShape: Identifiable<String> {
    override val id: String
    internal val cells: Array<CellOffset>
    internal val colorIndex: Int

    internal constructor(id: String, cells: Array<CellOffset>, colorIndex: Int) {
        this.id = id
        this.cells = cells.sref()
        this.colorIndex = colorIndex
    }

    override fun equals(other: Any?): Boolean {
        if (other !is BlockShape) {
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

    internal val width: Int
        get() {
            var maxCol = 0
            for (c in cells.sref()) {
                if (c.col > maxCol) {
                    maxCol = c.col
                }
            }
            return maxCol + 1
        }

    internal val height: Int
        get() {
            var maxRow = 0
            for (c in cells.sref()) {
                if (c.row > maxRow) {
                    maxRow = c.row
                }
            }
            return maxRow + 1
        }
}

/// All the shapes available in the game
internal class ShapeLibrary {

    @androidx.annotation.Keep
    companion object {
        internal val allShapes: Array<BlockShape> = arrayOf(
            BlockShape(id = "dot", cells = arrayOf(CellOffset(row = 0, col = 0)), colorIndex = 0),
            BlockShape(id = "h2", cells = arrayOf(
                CellOffset(row = 0, col = 0),
                CellOffset(row = 0, col = 1)
            ), colorIndex = 1),
            BlockShape(id = "h3", cells = arrayOf(
                CellOffset(row = 0, col = 0),
                CellOffset(row = 0, col = 1),
                CellOffset(row = 0, col = 2)
            ), colorIndex = 2),
            BlockShape(id = "h4", cells = arrayOf(
                CellOffset(row = 0, col = 0),
                CellOffset(row = 0, col = 1),
                CellOffset(row = 0, col = 2),
                CellOffset(row = 0, col = 3)
            ), colorIndex = 3),
            BlockShape(id = "h5", cells = arrayOf(
                CellOffset(row = 0, col = 0),
                CellOffset(row = 0, col = 1),
                CellOffset(row = 0, col = 2),
                CellOffset(row = 0, col = 3),
                CellOffset(row = 0, col = 4)
            ), colorIndex = 4),
            BlockShape(id = "v2", cells = arrayOf(
                CellOffset(row = 0, col = 0),
                CellOffset(row = 1, col = 0)
            ), colorIndex = 1),
            BlockShape(id = "v3", cells = arrayOf(
                CellOffset(row = 0, col = 0),
                CellOffset(row = 1, col = 0),
                CellOffset(row = 2, col = 0)
            ), colorIndex = 2),
            BlockShape(id = "v4", cells = arrayOf(
                CellOffset(row = 0, col = 0),
                CellOffset(row = 1, col = 0),
                CellOffset(row = 2, col = 0),
                CellOffset(row = 3, col = 0)
            ), colorIndex = 3),
            BlockShape(id = "v5", cells = arrayOf(
                CellOffset(row = 0, col = 0),
                CellOffset(row = 1, col = 0),
                CellOffset(row = 2, col = 0),
                CellOffset(row = 3, col = 0),
                CellOffset(row = 4, col = 0)
            ), colorIndex = 4),
            BlockShape(id = "sq2", cells = arrayOf(
                CellOffset(row = 0, col = 0),
                CellOffset(row = 0, col = 1),
                CellOffset(row = 1, col = 0),
                CellOffset(row = 1, col = 1)
            ), colorIndex = 5),
            BlockShape(id = "sq3", cells = arrayOf(
                CellOffset(row = 0, col = 0),
                CellOffset(row = 0, col = 1),
                CellOffset(row = 0, col = 2),
                CellOffset(row = 1, col = 0),
                CellOffset(row = 1, col = 1),
                CellOffset(row = 1, col = 2),
                CellOffset(row = 2, col = 0),
                CellOffset(row = 2, col = 1),
                CellOffset(row = 2, col = 2)
            ), colorIndex = 6),
            BlockShape(id = "L_bl", cells = arrayOf(
                CellOffset(row = 0, col = 0),
                CellOffset(row = 1, col = 0),
                CellOffset(row = 1, col = 1)
            ), colorIndex = 0),
            BlockShape(id = "L_br", cells = arrayOf(
                CellOffset(row = 0, col = 1),
                CellOffset(row = 1, col = 0),
                CellOffset(row = 1, col = 1)
            ), colorIndex = 1),
            BlockShape(id = "L_tl", cells = arrayOf(
                CellOffset(row = 0, col = 0),
                CellOffset(row = 0, col = 1),
                CellOffset(row = 1, col = 0)
            ), colorIndex = 2),
            BlockShape(id = "L_tr", cells = arrayOf(
                CellOffset(row = 0, col = 0),
                CellOffset(row = 0, col = 1),
                CellOffset(row = 1, col = 1)
            ), colorIndex = 3),
            BlockShape(id = "L23_bl", cells = arrayOf(
                CellOffset(row = 0, col = 0),
                CellOffset(row = 1, col = 0),
                CellOffset(row = 2, col = 0),
                CellOffset(row = 2, col = 1)
            ), colorIndex = 4),
            BlockShape(id = "L23_br", cells = arrayOf(
                CellOffset(row = 0, col = 1),
                CellOffset(row = 1, col = 1),
                CellOffset(row = 2, col = 0),
                CellOffset(row = 2, col = 1)
            ), colorIndex = 5),
            BlockShape(id = "L23_tl", cells = arrayOf(
                CellOffset(row = 0, col = 0),
                CellOffset(row = 0, col = 1),
                CellOffset(row = 1, col = 0),
                CellOffset(row = 2, col = 0)
            ), colorIndex = 6),
            BlockShape(id = "L23_tr", cells = arrayOf(
                CellOffset(row = 0, col = 0),
                CellOffset(row = 0, col = 1),
                CellOffset(row = 1, col = 1),
                CellOffset(row = 2, col = 1)
            ), colorIndex = 0),
            BlockShape(id = "rect2x3", cells = arrayOf(
                CellOffset(row = 0, col = 0),
                CellOffset(row = 0, col = 1),
                CellOffset(row = 1, col = 0),
                CellOffset(row = 1, col = 1),
                CellOffset(row = 2, col = 0),
                CellOffset(row = 2, col = 1)
            ), colorIndex = 1),
            BlockShape(id = "rect3x2", cells = arrayOf(
                CellOffset(row = 0, col = 0),
                CellOffset(row = 0, col = 1),
                CellOffset(row = 0, col = 2),
                CellOffset(row = 1, col = 0),
                CellOffset(row = 1, col = 1),
                CellOffset(row = 1, col = 2)
            ), colorIndex = 2),
            BlockShape(id = "T_up", cells = arrayOf(
                CellOffset(row = 0, col = 0),
                CellOffset(row = 0, col = 1),
                CellOffset(row = 0, col = 2),
                CellOffset(row = 1, col = 1)
            ), colorIndex = 1),
            BlockShape(id = "T_dn", cells = arrayOf(
                CellOffset(row = 0, col = 1),
                CellOffset(row = 1, col = 0),
                CellOffset(row = 1, col = 1),
                CellOffset(row = 1, col = 2)
            ), colorIndex = 2),
            BlockShape(id = "T_lt", cells = arrayOf(
                CellOffset(row = 0, col = 0),
                CellOffset(row = 1, col = 0),
                CellOffset(row = 1, col = 1),
                CellOffset(row = 2, col = 0)
            ), colorIndex = 3),
            BlockShape(id = "T_rt", cells = arrayOf(
                CellOffset(row = 0, col = 1),
                CellOffset(row = 1, col = 0),
                CellOffset(row = 1, col = 1),
                CellOffset(row = 2, col = 1)
            ), colorIndex = 4),
            BlockShape(id = "S_h", cells = arrayOf(
                CellOffset(row = 0, col = 1),
                CellOffset(row = 0, col = 2),
                CellOffset(row = 1, col = 0),
                CellOffset(row = 1, col = 1)
            ), colorIndex = 5),
            BlockShape(id = "Z_h", cells = arrayOf(
                CellOffset(row = 0, col = 0),
                CellOffset(row = 0, col = 1),
                CellOffset(row = 1, col = 1),
                CellOffset(row = 1, col = 2)
            ), colorIndex = 6),
            BlockShape(id = "S_v", cells = arrayOf(
                CellOffset(row = 0, col = 0),
                CellOffset(row = 1, col = 0),
                CellOffset(row = 1, col = 1),
                CellOffset(row = 2, col = 1)
            ), colorIndex = 5),
            BlockShape(id = "Z_v", cells = arrayOf(
                CellOffset(row = 0, col = 1),
                CellOffset(row = 1, col = 0),
                CellOffset(row = 1, col = 1),
                CellOffset(row = 2, col = 0)
            ), colorIndex = 6)
        )

        internal fun randomShape(): BlockShape {
            val index = Int.random(in_ = 0..<allShapes.count)
            return allShapes[index]
        }

        internal fun randomSet(): Array<BlockShape> = arrayOf(randomShape(), randomShape(), randomShape())
    }
}

// MARK: - Saved State

@androidx.annotation.Keep
@Suppress("MUST_BE_INITIALIZED")
internal class BlockBlastSavedState: Codable, MutableStruct {
    internal var grid: Array<Array<Int>>
        get() = field.sref({ this.grid = it })
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
            willmutate()
            field = newValue
            didmutate()
        }
    internal var pieceShapeIds: Array<String>
        get() = field.sref({ this.pieceShapeIds = it })
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
            willmutate()
            field = newValue
            didmutate()
        }
    internal var score: Int
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    internal var highScore: Int
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    internal var isGameOver: Boolean
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    internal var comboStreak: Int
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    internal var boardCleared: Boolean
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }

    constructor(grid: Array<Array<Int>>, pieceShapeIds: Array<String>, score: Int, highScore: Int, isGameOver: Boolean, comboStreak: Int, boardCleared: Boolean) {
        this.grid = grid
        this.pieceShapeIds = pieceShapeIds
        this.score = score
        this.highScore = highScore
        this.isGameOver = isGameOver
        this.comboStreak = comboStreak
        this.boardCleared = boardCleared
    }

    override var supdate: ((Any) -> Unit)? = null
    override var smutatingcount = 0
    override fun scopy(): MutableStruct = BlockBlastSavedState(grid, pieceShapeIds, score, highScore, isGameOver, comboStreak, boardCleared)

    private enum class CodingKeys(override val rawValue: String, @Suppress("UNUSED_PARAMETER") unusedp: Nothing? = null): CodingKey, RawRepresentable<String> {
        grid("grid"),
        pieceShapeIds("pieceShapeIds"),
        score("score"),
        highScore("highScore"),
        isGameOver("isGameOver"),
        comboStreak("comboStreak"),
        boardCleared("boardCleared");

        @androidx.annotation.Keep
        companion object {
            fun init(rawValue: String): CodingKeys? {
                return when (rawValue) {
                    "grid" -> CodingKeys.grid
                    "pieceShapeIds" -> CodingKeys.pieceShapeIds
                    "score" -> CodingKeys.score
                    "highScore" -> CodingKeys.highScore
                    "isGameOver" -> CodingKeys.isGameOver
                    "comboStreak" -> CodingKeys.comboStreak
                    "boardCleared" -> CodingKeys.boardCleared
                    else -> null
                }
            }
        }
    }

    override fun encode(to: Encoder) {
        val container = to.container(keyedBy = CodingKeys::class)
        container.encode(grid, forKey = CodingKeys.grid)
        container.encode(pieceShapeIds, forKey = CodingKeys.pieceShapeIds)
        container.encode(score, forKey = CodingKeys.score)
        container.encode(highScore, forKey = CodingKeys.highScore)
        container.encode(isGameOver, forKey = CodingKeys.isGameOver)
        container.encode(comboStreak, forKey = CodingKeys.comboStreak)
        container.encode(boardCleared, forKey = CodingKeys.boardCleared)
    }

    constructor(from: Decoder) {
        val container = from.container(keyedBy = CodingKeys::class)
        this.grid = container.decode(Array::class, elementType = Array::class, nestedElementType = Int::class, forKey = CodingKeys.grid)
        this.pieceShapeIds = container.decode(Array::class, elementType = String::class, forKey = CodingKeys.pieceShapeIds)
        this.score = container.decode(Int::class, forKey = CodingKeys.score)
        this.highScore = container.decode(Int::class, forKey = CodingKeys.highScore)
        this.isGameOver = container.decode(Boolean::class, forKey = CodingKeys.isGameOver)
        this.comboStreak = container.decode(Int::class, forKey = CodingKeys.comboStreak)
        this.boardCleared = container.decode(Boolean::class, forKey = CodingKeys.boardCleared)
    }

    @androidx.annotation.Keep
    companion object: DecodableCompanion<BlockBlastSavedState> {
        override fun init(from: Decoder): BlockBlastSavedState = BlockBlastSavedState(from = from)

        private fun CodingKeys(rawValue: String): CodingKeys? = CodingKeys.init(rawValue = rawValue)
    }
}

// MARK: - Game Model

/// A piece the player can place, with a unique identity for tracking
internal class GamePiece: Identifiable<String> {
    override val id: String
    internal val shape: BlockShape

    internal constructor(shape: BlockShape) {
        this.id = UUID().uuidString
        this.shape = shape
    }

    override fun equals(other: Any?): Boolean {
        if (other !is GamePiece) {
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
}

/// The main game model
@Stable
internal class GameModel: Observable {

    /// The 8x8 grid. Each cell is -1 if empty, or a colorIndex (0-6) if filled.
    internal var grid: Array<Array<Int>>
        get() = _grid.wrappedValue.sref({ this.grid = it })
        set(newValue) {
            _grid.wrappedValue = newValue.sref()
        }
    internal var _grid: skip.model.Observed<Array<Array<Int>>> = skip.model.Observed(Array(repeating = Array(repeating = -1, count = 8), count = 8))

    /// The current set of three pieces available to place
    internal var currentPieces: Array<GamePiece?>
        get() = _currentPieces.wrappedValue.sref({ this.currentPieces = it })
        set(newValue) {
            _currentPieces.wrappedValue = newValue.sref()
        }
    internal var _currentPieces: skip.model.Observed<Array<GamePiece?>> = skip.model.Observed(arrayOf(null, null, null))

    /// Current score
    internal var score: Int
        get() = _score.wrappedValue
        set(newValue) {
            _score.wrappedValue = newValue
        }
    internal var _score: skip.model.Observed<Int> = skip.model.Observed(0)

    /// High score
    internal var highScore: Int
        get() = _highScore.wrappedValue
        set(newValue) {
            _highScore.wrappedValue = newValue
        }
    internal var _highScore: skip.model.Observed<Int> = skip.model.Observed(0)

    /// Whether the game is over
    internal var isGameOver: Boolean
        get() = _isGameOver.wrappedValue
        set(newValue) {
            _isGameOver.wrappedValue = newValue
        }
    internal var _isGameOver: skip.model.Observed<Boolean> = skip.model.Observed(false)

    /// Number of lines cleared in last move (for combo display)
    internal var lastLinesCleared: Int
        get() = _lastLinesCleared.wrappedValue
        set(newValue) {
            _lastLinesCleared.wrappedValue = newValue
        }
    internal var _lastLinesCleared: skip.model.Observed<Int> = skip.model.Observed(0)

    /// Combo streak counter
    internal var comboStreak: Int
        get() = _comboStreak.wrappedValue
        set(newValue) {
            _comboStreak.wrappedValue = newValue
        }
    internal var _comboStreak: skip.model.Observed<Int> = skip.model.Observed(0)

    /// Whether the board was completely cleared on the last move
    internal var boardCleared: Boolean
        get() = _boardCleared.wrappedValue
        set(newValue) {
            _boardCleared.wrappedValue = newValue
        }
    internal var _boardCleared: skip.model.Observed<Boolean> = skip.model.Observed(false)

    /// Set of cells to animate as clearing
    internal var clearingCells: Set<Int>
        get() = _clearingCells.wrappedValue.sref({ this.clearingCells = it })
        set(newValue) {
            _clearingCells.wrappedValue = newValue.sref()
        }
    internal var _clearingCells: skip.model.Observed<Set<Int>> = skip.model.Observed(setOf())

    /// Number of attempts to make when generating a solvable piece set.
    /// 0 means no validation (purely random), higher values try harder to
    /// find a solvable set. Set by the view from the player's difficulty preference.
    internal var solvabilityAttempts: Int
        get() = _solvabilityAttempts.wrappedValue
        set(newValue) {
            _solvabilityAttempts.wrappedValue = newValue
        }
    internal var _solvabilityAttempts: skip.model.Observed<Int> = skip.model.Observed(20)

    internal constructor() {
        loadHighScore()
        spawnNewPieces()
    }

    // MARK: - Core Game Logic

    internal fun newGame() {
        grid = Array(repeating = Array(repeating = -1, count = 8), count = 8)
        score = 0
        isGameOver = false
        lastLinesCleared = 0
        comboStreak = 0
        boardCleared = false
        clearingCells = setOf()
        spawnNewPieces()
    }

    internal fun spawnNewPieces() {
        // Try up to `solvabilityAttempts` random sets to find a solvable one.
        // When attempts is 0 (max difficulty), we skip directly to a pure random pick.
        for (unusedbinding in 0..<solvabilityAttempts) {
            val shapes = ShapeLibrary.randomSet()
            if (isSolvableSet(shapes = shapes)) {
                currentPieces = arrayOf(
                    GamePiece(shape = shapes[0]),
                    GamePiece(shape = shapes[1]),
                    GamePiece(shape = shapes[2])
                )
                return
            }
        }
        // Fallback (or pure-random when attempts == 0): use whatever we got
        val shapes = ShapeLibrary.randomSet()
        currentPieces = arrayOf(
            GamePiece(shape = shapes[0]),
            GamePiece(shape = shapes[1]),
            GamePiece(shape = shapes[2])
        )
    }

    /// Check if there exists at least one ordering of the 3 shapes where
    /// all can be placed sequentially on the current board.
    /// For each permutation, greedily places each shape at the first valid
    /// position found, applying line clears between placements.
    private fun isSolvableSet(shapes: Array<BlockShape>): Boolean {
        val gs = GameModel.gridSize
        // Try all 6 permutations of 3 shapes
        val perms = arrayOf(arrayOf(0, 1, 2), arrayOf(0, 2, 1), arrayOf(1, 0, 2), arrayOf(1, 2, 0), arrayOf(2, 0, 1), arrayOf(2, 1, 0))
        for (perm in perms.sref()) {
            // Copy the grid for simulation
            var simGrid: Array<Array<Int>> = arrayOf()
            for (r in 0..<gs) {
                simGrid.append(grid[r])
            }
            var allPlaced = true
            for (idx in perm.sref()) {
                val shape = shapes[idx]
                val placed = simulatePlace(shape = shape, grid = InOut({ simGrid }, { simGrid = it }))
                if (!placed) {
                    allPlaced = false
                    break
                }
                simulateClearLines(grid = InOut({ simGrid }, { simGrid = it }))
            }
            if (allPlaced) {
                return true
            }
        }
        return false
    }

    /// Try to place a shape anywhere on the simulated grid. Returns true if placed.
    private fun simulatePlace(shape: BlockShape, grid: InOut<Array<Array<Int>>>): Boolean {
        val gs = GameModel.gridSize
        for (r in 0..<gs) {
            for (c in 0..<gs) {
                var fits = true
                for (cell in shape.cells.sref()) {
                    val cr = r + cell.row
                    val cc = c + cell.col
                    if (cr < 0 || cr >= gs || cc < 0 || cc >= gs || grid.value[cr][cc] != -1) {
                        fits = false
                        break
                    }
                }
                if (fits) {
                    for (cell in shape.cells.sref()) {
                        grid.value[r + cell.row][c + cell.col] = shape.colorIndex
                    }
                    return true
                }
            }
        }
        return false
    }

    /// Clear any completed rows/columns in the simulated grid.
    private fun simulateClearLines(grid: InOut<Array<Array<Int>>>) {
        val gs = GameModel.gridSize
        // Find full rows
        for (r in 0..<gs) {
            var full = true
            for (c in 0..<gs) {
                if (grid.value[r][c] == -1) {
                    full = false
                    break
                }
            }
            if (full) {
                for (c in 0..<gs) {
                    grid.value[r][c] = -1
                }
            }
        }
        // Find full columns
        for (c in 0..<gs) {
            var full = true
            for (r in 0..<gs) {
                if (grid.value[r][c] == -1) {
                    full = false
                    break
                }
            }
            if (full) {
                for (r in 0..<gs) {
                    grid.value[r][c] = -1
                }
            }
        }
    }

    /// Check if a shape can be placed at the given grid position
    internal fun canPlace(shape: BlockShape, atRow: Int, col: Int): Boolean {
        val row = atRow
        for (cell in shape.cells.sref()) {
            val r = row + cell.row
            val c = col + cell.col
            if (r < 0 || r >= GameModel.gridSize || c < 0 || c >= GameModel.gridSize) {
                return false
            }
            if (grid[r][c] != -1) {
                return false
            }
        }
        return true
    }

    /// Check if every cell on the board is empty
    internal fun isBoardEmpty(): Boolean {
        for (r in 0..<GameModel.gridSize) {
            for (c in 0..<GameModel.gridSize) {
                if (grid[r][c] != -1) {
                    return false
                }
            }
        }
        return true
    }

    /// Place a shape on the grid and handle scoring/clearing
    internal fun placeShape(shape: BlockShape, atRow: Int, col: Int, pieceIndex: Int) {
        val row = atRow
        boardCleared = false

        // Place the cells
        for (cell in shape.cells.sref()) {
            val r = row + cell.row
            val c = col + cell.col
            grid[r][c] = shape.colorIndex
        }

        // Add points for placing (1 point per cell)
        score += shape.cells.count

        // Remove the placed piece
        currentPieces[pieceIndex] = null

        // Check and clear completed lines
        val linesCleared = clearCompletedLines()
        lastLinesCleared = linesCleared

        if (linesCleared > 0) {
            comboStreak += 1
            // Scoring: 10 points per line, bonus for combos and multi-line clears
            val linePoints = linesCleared * 10
            val comboBonus = if (comboStreak > 1) comboStreak * 5 else 0
            val multiLineBonus = if (linesCleared > 1) linesCleared * 5 else 0
            score += linePoints + comboBonus + multiLineBonus

            // Board clear bonus
            if (isBoardEmpty()) {
                boardCleared = true
                score += boardClearBonus
            }
        } else {
            comboStreak = 0
        }

        // Check if all three pieces are placed
        val allPlaced = currentPieces[0] == null && currentPieces[1] == null && currentPieces[2] == null
        if (allPlaced) {
            spawnNewPieces()
        }

        // Check for game over
        if (checkGameOver()) {
            isGameOver = true
            if (score > highScore) {
                highScore = score
                saveHighScore()
            }
        }
    }

    /// Clear any completed rows and columns, returns count cleared
    internal fun clearCompletedLines(): Int {
        var rowsToClear: Array<Int> = arrayOf()
        var colsToClear: Array<Int> = arrayOf()

        // Check rows
        for (r in 0..<GameModel.gridSize) {
            var full = true
            for (c in 0..<GameModel.gridSize) {
                if (grid[r][c] == -1) {
                    full = false
                    break
                }
            }
            if (full) {
                rowsToClear.append(r)
            }
        }

        // Check columns
        for (c in 0..<GameModel.gridSize) {
            var full = true
            for (r in 0..<GameModel.gridSize) {
                if (grid[r][c] == -1) {
                    full = false
                    break
                }
            }
            if (full) {
                colsToClear.append(c)
            }
        }

        // Build set of cells to clear for animation
        var cellsToClear = Set<Int>()
        for (r in rowsToClear.sref()) {
            for (c in 0..<GameModel.gridSize) {
                cellsToClear.insert(r * GameModel.gridSize + c)
            }
        }
        for (c in colsToClear.sref()) {
            for (r in 0..<GameModel.gridSize) {
                cellsToClear.insert(r * GameModel.gridSize + c)
            }
        }
        clearingCells = cellsToClear

        // Clear the rows
        for (r in rowsToClear.sref()) {
            for (c in 0..<GameModel.gridSize) {
                grid[r][c] = -1
            }
        }

        // Clear the columns
        for (c in colsToClear.sref()) {
            for (r in 0..<GameModel.gridSize) {
                grid[r][c] = -1
            }
        }

        return rowsToClear.count + colsToClear.count
    }

    /// Check if any remaining piece can be placed anywhere
    internal fun checkGameOver(): Boolean {
        for (piece in currentPieces.sref()) {
            if (piece == null) {
                continue
            }
            for (r in 0..<GameModel.gridSize) {
                for (c in 0..<GameModel.gridSize) {
                    if (canPlace(shape = piece.shape, atRow = r, col = c)) {
                        return false
                    }
                }
            }
        }
        return true
    }

    /// Check if a specific piece can be placed anywhere on the board
    internal fun canPieceFit(piece: GamePiece): Boolean {
        for (r in 0..<GameModel.gridSize) {
            for (c in 0..<GameModel.gridSize) {
                if (canPlace(shape = piece.shape, atRow = r, col = c)) {
                    return true
                }
            }
        }
        return false
    }

    // MARK: - Persistence

    private fun saveHighScore(): Unit = UserDefaults.standard.set(highScore, forKey = "blockblast_highscore")

    private fun loadHighScore() {
        highScore = UserDefaults.standard.integer(forKey = "blockblast_highscore")
    }

    // MARK: - Game State Persistence

    internal fun makeSavedState(): BlockBlastSavedState {
        var pieceShapeIds: Array<String> = arrayOf()
        for (piece in currentPieces.sref()) {
            if (piece != null) {
                pieceShapeIds.append(piece.shape.id)
            } else {
                pieceShapeIds.append("")
            }
        }
        return BlockBlastSavedState(grid = grid, pieceShapeIds = pieceShapeIds, score = score, highScore = highScore, isGameOver = isGameOver, comboStreak = comboStreak, boardCleared = boardCleared)
    }

    internal fun restoreState(state: BlockBlastSavedState) {
        grid = state.grid
        score = state.score
        highScore = state.highScore
        isGameOver = state.isGameOver
        comboStreak = state.comboStreak
        boardCleared = state.boardCleared

        var restoredPieces: Array<GamePiece?> = arrayOf()
        for (shapeId in state.pieceShapeIds.sref()) {
            if (shapeId == "") {
                restoredPieces.append(null)
            } else {
                var foundShape: BlockShape? = null
                for (shape in ShapeLibrary.allShapes.sref()) {
                    if (shape.id == shapeId) {
                        foundShape = shape
                        break
                    }
                }
                if (foundShape != null) {
                    val shape = foundShape
                    restoredPieces.append(GamePiece(shape = shape))
                } else {
                    restoredPieces.append(null)
                }
            }
        }
        currentPieces = restoredPieces
    }

    internal fun saveState() {
        val data_0 = try { JSONEncoder().encode(makeSavedState()) } catch (_: Throwable) { null }
        if (data_0 == null) {
            return
        }
        val json_0 = String(data = data_0, encoding = StringEncoding.utf8)
        if (json_0 == null) {
            return
        }
        UserDefaults.standard.set(json_0, forKey = "blockblast_saved_state")
    }

    @androidx.annotation.Keep
    companion object {
        internal val gridSize = 8

        /// Resets the persisted high score to zero.
        internal fun resetHighScore(): Unit = UserDefaults.standard.set(0, forKey = "blockblast_highscore")

        internal fun loadSavedState(): BlockBlastSavedState? {
            val json_1 = UserDefaults.standard.string(forKey = "blockblast_saved_state")
            if (json_1 == null) {
                return null
            }
            val data_1 = json_1.data(using = StringEncoding.utf8)
            if (data_1 == null) {
                return null
            }
            return try { JSONDecoder().decode(BlockBlastSavedState::class, from = data_1) } catch (_: Throwable) { null }
        }

        internal fun clearSavedState(): Unit = UserDefaults.standard.removeObject(forKey = "blockblast_saved_state")
    }
}

// MARK: - Public High Score Reset

/// Resets the Block Blast high score to zero.
fun resetBlockBlastHighScore(): Unit = GameModel.resetHighScore()

// MARK: - Preview Icon

/// Returns the color index for a Block Blast preview icon cell, or -1 if empty.
private fun blockBlastPreviewColorIndex(row: Int, col: Int): Int {
    // Bottom two rows filled
    if (row == 3) {
        return 0 // red
    } // red
    if (row == 4) {
        return 1 // blue
    } // blue
    // Left column stack
    if (col == 0 && row >= 0 && row <= 2) {
        return 2 // green
    } // green
    // Small orange block
    if (row == 2 && (col == 1 || col == 2)) {
        return 3 // orange
    } // orange
    // Purple square
    if ((row == 1 || row == 2) && (col == 3 || col == 4)) {
        return 4 // purple
    } // purple
    return -1
}

/// A preview icon for the Block Blast game, using the same 3D cell rendering as the game.
class BlockBlastPreviewIcon: View {
    constructor() {
    }

    override fun body(): View {
        return ComposeBuilder { composectx: ComposeContext ->
            GeometryReader { geo ->
                ComposeBuilder { composectx: ComposeContext ->
                    val gridSize = 5
                    val spacing: Double = 1.0
                    val totalSpacing = spacing * Double(gridSize - 1)
                    val padding: Double = 4.0
                    val available = min(geo.size.width, geo.size.height) - padding * 2
                    val cellSize = (available - totalSpacing) / Double(gridSize)

                    VStack(spacing = spacing) { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            ForEach(0..<gridSize, id = { it }) { row ->
                                ComposeBuilder { composectx: ComposeContext ->
                                    HStack(spacing = spacing) { ->
                                        ComposeBuilder { composectx: ComposeContext ->
                                            ForEach(0..<gridSize, id = { it }) { col ->
                                                ComposeBuilder { composectx: ComposeContext ->
                                                    val colorIndex = blockBlastPreviewColorIndex(row = row, col = col)
                                                    ZStack { ->
                                                        ComposeBuilder { composectx: ComposeContext ->
                                                            if (colorIndex >= 0) {
                                                                RoundedRectangle(cornerRadius = 3.0)
                                                                    .fill(BlockColors.color(for_ = colorIndex))
                                                                    .frame(width = cellSize, height = cellSize).Compose(composectx)
                                                                RoundedRectangle(cornerRadius = 3.0)
                                                                    .fill(LinearGradient(colors = arrayOf(Color.white.opacity(0.3), Color.clear), startPoint = UnitPoint.topLeading, endPoint = UnitPoint.bottomTrailing))
                                                                    .frame(width = cellSize, height = cellSize).Compose(composectx)
                                                            } else {
                                                                RoundedRectangle(cornerRadius = 3.0)
                                                                    .fill(Color.white.opacity(0.05))
                                                                    .frame(width = cellSize, height = cellSize).Compose(composectx)
                                                            }
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
                            ComposeResult.ok
                        }
                    }
                    .padding(padding)
                    .frame(width = geo.size.width, height = geo.size.height).Compose(composectx)
                    ComposeResult.ok
                }
            }
            .aspectRatio(1.0, contentMode = ContentMode.fit)
            .background(RoundedRectangle(cornerRadius = 8.0)
                .fill(Color(red = 0.12, green = 0.12, blue = 0.22))).Compose(composectx)
        }
    }

    @androidx.annotation.Keep
    companion object {
    }
}

// MARK: - In-Game Settings Sheet

internal class BlockBlastSettingsView: View {
    internal var settings: BlockBlastSettings
        get() = _settings.wrappedValue
        set(newValue) {
            _settings.wrappedValue = newValue
        }
    internal var _settings: skip.ui.Bindable<BlockBlastSettings>
    internal lateinit var dismiss: DismissAction

    override fun body(): View {
        return ComposeBuilder { composectx: ComposeContext ->
            NavigationStack { ->
                ComposeBuilder { composectx: ComposeContext ->
                    Form { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            Section(header = Text(LocalizedStringKey(stringLiteral = "Block Blast"), bundle = Bundle.module)) { ->
                                ComposeBuilder { composectx: ComposeContext ->
                                    Toggle(isOn = Binding({ _settings.wrappedValue.vibrations }, { it -> _settings.wrappedValue.vibrations = it })) { ->
                                        ComposeBuilder { composectx: ComposeContext ->
                                            Text(LocalizedStringKey(stringLiteral = "Vibrations"), bundle = Bundle.module).Compose(composectx)
                                            ComposeResult.ok
                                        }
                                    }.Compose(composectx)
                                    ComposeResult.ok
                                }
                            }.Compose(composectx)
                            Section(header = Text(LocalizedStringKey(stringLiteral = "Difficulty"), bundle = Bundle.module)) { ->
                                ComposeBuilder { composectx: ComposeContext ->
                                    VStack(alignment = HorizontalAlignment.leading, spacing = 8.0) { ->
                                        ComposeBuilder { composectx: ComposeContext ->
                                            HStack { ->
                                                ComposeBuilder { composectx: ComposeContext ->
                                                    Text(LocalizedStringKey(stringLiteral = "Level"), bundle = Bundle.module).Compose(composectx)
                                                    Spacer().Compose(composectx)
                                                    Text({
                                                        val str = LocalizedStringKey.StringInterpolation(literalCapacity = 0, interpolationCount = 0)
                                                        str.appendInterpolation(settings.difficulty)
                                                        LocalizedStringKey(stringInterpolation = str)
                                                    }())
                                                        .foregroundStyle(Color.secondary)
                                                        .monospaced().Compose(composectx)
                                                    ComposeResult.ok
                                                }
                                            }.Compose(composectx)
                                            Slider(value = Binding(get = { -> Double(settings.difficulty) }, set = { it -> settings.difficulty = Int(it.rounded()) }), in_ = 0.0..10.0, step = 1.0).Compose(composectx)
                                            HStack { ->
                                                ComposeBuilder { composectx: ComposeContext ->
                                                    Text(LocalizedStringKey(stringLiteral = "Easy"), bundle = Bundle.module)
                                                        .font(Font.caption2)
                                                        .foregroundStyle(Color.secondary).Compose(composectx)
                                                    Spacer().Compose(composectx)
                                                    Text(LocalizedStringKey(stringLiteral = "Hard"), bundle = Bundle.module)
                                                        .font(Font.caption2)
                                                        .foregroundStyle(Color.secondary).Compose(composectx)
                                                    ComposeResult.ok
                                                }
                                            }.Compose(composectx)
                                            ComposeResult.ok
                                        }
                                    }.Compose(composectx)
                                    ComposeResult.ok
                                }
                            }.Compose(composectx)
                            Section { ->
                                ComposeBuilder { composectx: ComposeContext ->
                                    Text(LocalizedStringKey(stringLiteral = "At difficulty 0, the game tries 20 times to offer a solvable set of blocks. At difficulty 10, blocks are picked purely at random and the game may become unwinnable."), bundle = Bundle.module)
                                        .font(Font.caption)
                                        .foregroundStyle(Color.secondary).Compose(composectx)
                                    ComposeResult.ok
                                }
                            }.Compose(composectx)
                            ComposeResult.ok
                        }
                    }
                    .navigationTitle(Text(LocalizedStringKey(stringLiteral = "Settings"), bundle = Bundle.module))
                    .toolbar { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            ToolbarItem(placement = ToolbarItemPlacement.confirmationAction) { ->
                                ComposeBuilder { composectx: ComposeContext ->
                                    Button(action = { -> dismiss() }) { ->
                                        ComposeBuilder { composectx: ComposeContext ->
                                            Text(LocalizedStringKey(stringLiteral = "Done"), bundle = Bundle.module).Compose(composectx)
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

    constructor(settings: BlockBlastSettings) {
        this._settings = skip.ui.Bindable(settings)
    }
}

/// Settings specific to the Block Blast game.
@Stable
open class BlockBlastSettings: Observable {
    /// Whether vibrations (haptic feedback) are enabled for Block Blast.
    open var vibrations: Boolean
        get() = _vibrations.wrappedValue
        set(newValue) {
            _vibrations.wrappedValue = newValue
            defaults.set(vibrations, forKey = "blockBlastVibrations")
        }
    var _vibrations: skip.model.Observed<Boolean> = skip.model.Observed(defaults.value(forKey = "blockBlastVibrations", default = true))

    /// Difficulty level from 0 (easiest — always solvable) to 10 (hardest — pure
    /// random). The number of solvability attempts when generating a piece set
    /// is `20 - 2 * difficulty`, so 0 → 20 attempts, 5 → 10 attempts, 10 → 0.
    open var difficulty: Int
        get() = _difficulty.wrappedValue
        set(newValue) {
            _difficulty.wrappedValue = newValue
            defaults.set(difficulty, forKey = "blockBlastDifficulty")
        }
    var _difficulty: skip.model.Observed<Int> = skip.model.Observed(defaults.value(forKey = "blockBlastDifficulty", default = 0))

    /// The number of solvability attempts implied by the current difficulty.
    open val solvabilityAttempts: Int
        get() = max(0, 20 - difficulty * 2)

    constructor() {
    }

    @androidx.annotation.Keep
    companion object: CompanionClass() {
    }
    open class CompanionClass {
    }
}


private val defaults = UserDefaults.standard

internal fun <T> UserDefaults.value(forKey: String, default: T): T {
    val key = forKey
    val defaultValue = default
    return (UserDefaults.standard.object_(forKey = key) as? T ?: defaultValue).sref()
}
