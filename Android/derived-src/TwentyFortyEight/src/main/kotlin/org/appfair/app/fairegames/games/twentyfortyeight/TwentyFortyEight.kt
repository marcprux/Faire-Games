// Licensed under the GNU General Public License v2.0 or later
// SPDX-License-Identifier: GPL-2.0-or-later

package org.appfair.app.fairegames.games.twentyfortyeight

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

class TwentyFortyEightContainerView: View {
    private var settings: TwentyFortyEightSettings
        get() = _settings.wrappedValue
        set(newValue) {
            _settings.wrappedValue = newValue
        }
    private var _settings: skip.ui.State<TwentyFortyEightSettings> = skip.ui.State(TwentyFortyEightSettings())
    private var showInstructions: Boolean
        get() = _showInstructions.wrappedValue
        set(newValue) {
            _showInstructions.wrappedValue = newValue
        }
    private var _showInstructions: skip.ui.State<Boolean> = skip.ui.State(false)
    private val instructionsConfig = GameInstructionsConfig(key = "TwentyFortyEight.instructions", bundle = Bundle.module, firstLaunchKey = "instructionsShown_TwentyFortyEight", title = "2048")

    constructor() {
    }

    override fun body(): View {
        return ComposeBuilder { composectx: ComposeContext ->
            TwentyFortyEightGameView(showInstructions = Binding({ _showInstructions.wrappedValue }, { it -> _showInstructions.wrappedValue = it }))
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
        val rememberedsettings by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<TwentyFortyEightSettings>, Any>) { mutableStateOf(_settings) }
        _settings = rememberedsettings

        val rememberedshowInstructions by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Boolean>, Any>) { mutableStateOf(_showInstructions) }
        _showInstructions = rememberedshowInstructions

        return super.Evaluate(context, options)
    }

    @androidx.annotation.Keep
    companion object {
    }
}

fun resetTwentyFortyEightHighScore(): Unit = UserDefaults.standard.set(0, forKey = "twentyfortyeight_highscore")

// MARK: - Constants

private val gridSize: Int = 4
private val gridSpacing: Double = 6.0
private val tileCornerRadius: Double = 6.0

// Tile colors keyed by value
private val tileColors: Dictionary<Int, Tuple3<Double, Double, Double>> = dictionaryOf(
    Tuple2(0, Tuple3(0.80, 0.76, 0.71)),
    Tuple2(2, Tuple3(0.93, 0.89, 0.85)),
    Tuple2(4, Tuple3(0.93, 0.88, 0.78)),
    Tuple2(8, Tuple3(0.95, 0.69, 0.47)),
    Tuple2(16, Tuple3(0.96, 0.58, 0.39)),
    Tuple2(32, Tuple3(0.96, 0.49, 0.37)),
    Tuple2(64, Tuple3(0.96, 0.37, 0.23)),
    Tuple2(128, Tuple3(0.93, 0.81, 0.45)),
    Tuple2(256, Tuple3(0.93, 0.80, 0.38)),
    Tuple2(512, Tuple3(0.93, 0.78, 0.31)),
    Tuple2(1024, Tuple3(0.93, 0.77, 0.25)),
    Tuple2(2048, Tuple3(0.93, 0.76, 0.18))
)

private fun tileColor(for_: Int): Color {
    val value = for_
    tileColors[value]?.let { c ->
        return Color(red = c.element0, green = c.element1, blue = c.element2)
    }
    // Values beyond 2048 get a dark color
    return Color(red = 0.24, green = 0.23, blue = 0.20)
}

private fun tileForeground(for_: Int): Color {
    val value = for_
    if (value <= 4) {
        return Color(red = 0.47, green = 0.43, blue = 0.40)
    }
    return Color.white
}

private fun tileFontSize(for_: Int, cellSize: Double): Double {
    val value = for_
    if (value < 100) {
        return cellSize * 0.42
    }
    if (value < 1000) {
        return cellSize * 0.34
    }
    if (value < 10000) {
        return cellSize * 0.28
    }
    return cellSize * 0.22
}

// MARK: - Difficulty

@androidx.annotation.Keep
internal enum class TwentyFortyEightDifficulty(override val rawValue: Int, @Suppress("UNUSED_PARAMETER") unusedp: Nothing? = null): CaseIterable, RawRepresentable<Int> {
    easy(0),
    normal(1),
    hard(2);

    internal val label: String
        get() {
            when (this) {
                TwentyFortyEightDifficulty.easy -> return "Easy"
                TwentyFortyEightDifficulty.normal -> return "Normal"
                TwentyFortyEightDifficulty.hard -> return "Hard"
            }
        }

    internal val description: String
        get() {
            when (this) {
                TwentyFortyEightDifficulty.easy -> return "Only 2s spawn. 3 undos per game."
                TwentyFortyEightDifficulty.normal -> return "Classic rules. 90% twos, 10% fours."
                TwentyFortyEightDifficulty.hard -> return "20% fours. Two tiles spawn per move."
            }
        }

    internal val accentColor: Color
        get() {
            when (this) {
                TwentyFortyEightDifficulty.easy -> return Color(red = 0.35, green = 0.75, blue = 0.45)
                TwentyFortyEightDifficulty.normal -> return Color(red = 0.30, green = 0.60, blue = 0.95)
                TwentyFortyEightDifficulty.hard -> return Color(red = 0.90, green = 0.35, blue = 0.30)
            }
        }

    /// Probability of spawning a 4 instead of a 2
    internal val fourSpawnChance: Double
        get() {
            when (this) {
                TwentyFortyEightDifficulty.easy -> return 0.0
                TwentyFortyEightDifficulty.normal -> return 0.1
                TwentyFortyEightDifficulty.hard -> return 0.2
            }
        }

    /// How many tiles to spawn per move
    internal val tilesPerSpawn: Int
        get() {
            when (this) {
                TwentyFortyEightDifficulty.easy -> return 1
                TwentyFortyEightDifficulty.normal -> return 1
                TwentyFortyEightDifficulty.hard -> return 2
            }
        }

    /// Whether undo is available
    internal val undoAllowed: Boolean
        get() {
            when (this) {
                TwentyFortyEightDifficulty.easy -> return true
                TwentyFortyEightDifficulty.normal -> return false
                TwentyFortyEightDifficulty.hard -> return false
            }
        }

    override fun toString(): String = description

    @androidx.annotation.Keep
    companion object: CaseIterableCompanion<TwentyFortyEightDifficulty> {
        fun init(rawValue: Int): TwentyFortyEightDifficulty? {
            return when (rawValue) {
                0 -> TwentyFortyEightDifficulty.easy
                1 -> TwentyFortyEightDifficulty.normal
                2 -> TwentyFortyEightDifficulty.hard
                else -> null
            }
        }

        override val allCases: Array<TwentyFortyEightDifficulty>
            get() = arrayOf(easy, normal, hard)
    }
}

internal fun TwentyFortyEightDifficulty(rawValue: Int): TwentyFortyEightDifficulty? = TwentyFortyEightDifficulty.init(rawValue = rawValue)

// MARK: - Direction

internal enum class Direction {
    up,
    down,
    left,
    right;
}

// MARK: - Move Preview

/// One tile's movement under a hypothetical move. Used to drive the live
/// drag-preview animation in the view layer.
///
/// - `startCell`: the grid index where the tile currently lives.
/// - `endCell`: the grid index where it will visually end up.
/// - `isAbsorbedSource`: true if this tile is the *second* tile in a merge
///   pair — i.e., it disappears into the destination tile and the destination
///   ends up holding the doubled value. The corresponding *destination* tile
///   shares the same `endCell` but has `isAbsorbedSource == false`.
/// - `value`: the tile's pre-move value, used by the view for highlighting.
internal class TwentyFortyEightTileMovement {
    internal val startCell: Int
    internal val endCell: Int
    internal val isAbsorbedSource: Boolean
    internal val value: Int

    constructor(startCell: Int, endCell: Int, isAbsorbedSource: Boolean, value: Int) {
        this.startCell = startCell
        this.endCell = endCell
        this.isAbsorbedSource = isAbsorbedSource
        this.value = value
    }
}

internal class TwentyFortyEightMovePreview {
    internal val direction: Direction
    internal val movements: Array<TwentyFortyEightTileMovement>

    /// True if any tile actually changes position under this move. If false
    /// the gesture should be ignored (the user is dragging into a wall).
    internal val anyMovement: Boolean
        get() {
            var i = 0
            while (i < movements.count) {
                if (movements[i].startCell != movements[i].endCell) {
                    return true
                }
                i += 1
            }
            return false
        }

    constructor(direction: Direction, movements: Array<TwentyFortyEightTileMovement>) {
        this.direction = direction
        this.movements = movements.sref()
    }
}

// MARK: - Saved State

@androidx.annotation.Keep
@Suppress("MUST_BE_INITIALIZED")
internal class TwentyFortyEightSavedState: Codable, MutableStruct {
    internal var grid: Array<Int>
        get() = field.sref({ this.grid = it })
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
    internal var isGameOver: Boolean
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    internal var hasWon: Boolean
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    internal var continueAfterWin: Boolean
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    internal var difficultyRaw: Int
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    internal var undoGrid: Array<Int>
        get() = field.sref({ this.undoGrid = it })
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
            willmutate()
            field = newValue
            didmutate()
        }
    internal var undoScore: Int
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    internal var hasUndo: Boolean
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    internal var undosRemaining: Int
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }

    constructor(grid: Array<Int>, score: Int, isGameOver: Boolean, hasWon: Boolean, continueAfterWin: Boolean, difficultyRaw: Int, undoGrid: Array<Int>, undoScore: Int, hasUndo: Boolean, undosRemaining: Int) {
        this.grid = grid
        this.score = score
        this.isGameOver = isGameOver
        this.hasWon = hasWon
        this.continueAfterWin = continueAfterWin
        this.difficultyRaw = difficultyRaw
        this.undoGrid = undoGrid
        this.undoScore = undoScore
        this.hasUndo = hasUndo
        this.undosRemaining = undosRemaining
    }

    override var supdate: ((Any) -> Unit)? = null
    override var smutatingcount = 0
    override fun scopy(): MutableStruct = TwentyFortyEightSavedState(grid, score, isGameOver, hasWon, continueAfterWin, difficultyRaw, undoGrid, undoScore, hasUndo, undosRemaining)

    private enum class CodingKeys(override val rawValue: String, @Suppress("UNUSED_PARAMETER") unusedp: Nothing? = null): CodingKey, RawRepresentable<String> {
        grid("grid"),
        score("score"),
        isGameOver("isGameOver"),
        hasWon("hasWon"),
        continueAfterWin("continueAfterWin"),
        difficultyRaw("difficultyRaw"),
        undoGrid("undoGrid"),
        undoScore("undoScore"),
        hasUndo("hasUndo"),
        undosRemaining("undosRemaining");

        @androidx.annotation.Keep
        companion object {
            fun init(rawValue: String): CodingKeys? {
                return when (rawValue) {
                    "grid" -> CodingKeys.grid
                    "score" -> CodingKeys.score
                    "isGameOver" -> CodingKeys.isGameOver
                    "hasWon" -> CodingKeys.hasWon
                    "continueAfterWin" -> CodingKeys.continueAfterWin
                    "difficultyRaw" -> CodingKeys.difficultyRaw
                    "undoGrid" -> CodingKeys.undoGrid
                    "undoScore" -> CodingKeys.undoScore
                    "hasUndo" -> CodingKeys.hasUndo
                    "undosRemaining" -> CodingKeys.undosRemaining
                    else -> null
                }
            }
        }
    }

    override fun encode(to: Encoder) {
        val container = to.container(keyedBy = CodingKeys::class)
        container.encode(grid, forKey = CodingKeys.grid)
        container.encode(score, forKey = CodingKeys.score)
        container.encode(isGameOver, forKey = CodingKeys.isGameOver)
        container.encode(hasWon, forKey = CodingKeys.hasWon)
        container.encode(continueAfterWin, forKey = CodingKeys.continueAfterWin)
        container.encode(difficultyRaw, forKey = CodingKeys.difficultyRaw)
        container.encode(undoGrid, forKey = CodingKeys.undoGrid)
        container.encode(undoScore, forKey = CodingKeys.undoScore)
        container.encode(hasUndo, forKey = CodingKeys.hasUndo)
        container.encode(undosRemaining, forKey = CodingKeys.undosRemaining)
    }

    constructor(from: Decoder) {
        val container = from.container(keyedBy = CodingKeys::class)
        this.grid = container.decode(Array::class, elementType = Int::class, forKey = CodingKeys.grid)
        this.score = container.decode(Int::class, forKey = CodingKeys.score)
        this.isGameOver = container.decode(Boolean::class, forKey = CodingKeys.isGameOver)
        this.hasWon = container.decode(Boolean::class, forKey = CodingKeys.hasWon)
        this.continueAfterWin = container.decode(Boolean::class, forKey = CodingKeys.continueAfterWin)
        this.difficultyRaw = container.decode(Int::class, forKey = CodingKeys.difficultyRaw)
        this.undoGrid = container.decode(Array::class, elementType = Int::class, forKey = CodingKeys.undoGrid)
        this.undoScore = container.decode(Int::class, forKey = CodingKeys.undoScore)
        this.hasUndo = container.decode(Boolean::class, forKey = CodingKeys.hasUndo)
        this.undosRemaining = container.decode(Int::class, forKey = CodingKeys.undosRemaining)
    }

    @androidx.annotation.Keep
    companion object: DecodableCompanion<TwentyFortyEightSavedState> {
        override fun init(from: Decoder): TwentyFortyEightSavedState = TwentyFortyEightSavedState(from = from)

        private fun CodingKeys(rawValue: String): CodingKeys? = CodingKeys.init(rawValue = rawValue)
    }
}

// MARK: - Game Model

/// Internal record used by `Drop7Model.simulateLine` to describe a tile's
/// movement within a single line (row or column) under a hypothetical move.
private class LineMovement {
    internal val startInLine: Int
    internal val endInLine: Int
    internal val isAbsorbedSource: Boolean
    internal val value: Int

    constructor(startInLine: Int, endInLine: Int, isAbsorbedSource: Boolean, value: Int) {
        this.startInLine = startInLine
        this.endInLine = endInLine
        this.isAbsorbedSource = isAbsorbedSource
        this.value = value
    }
}

@Stable
internal class TwentyFortyEightModel: Observable {
    // Grid stored as flat array [row * gridSize + col], row-major
    internal var grid: Array<Int>
        get() = _grid.wrappedValue.sref({ this.grid = it })
        set(newValue) {
            _grid.wrappedValue = newValue.sref()
        }
    internal var _grid: skip.model.Observed<Array<Int>> = skip.model.Observed(Array(repeating = 0, count = gridSize * gridSize))

    internal var score: Int
        get() = _score.wrappedValue
        set(newValue) {
            _score.wrappedValue = newValue
        }
    internal var _score: skip.model.Observed<Int> = skip.model.Observed(0)
    internal var highScore: Int
        get() = _highScore.wrappedValue
        set(newValue) {
            _highScore.wrappedValue = newValue
        }
    internal var _highScore: skip.model.Observed<Int> = skip.model.Observed(UserDefaults.standard.integer(forKey = "twentyfortyeight_highscore"))
    internal var isGameOver: Boolean
        get() = _isGameOver.wrappedValue
        set(newValue) {
            _isGameOver.wrappedValue = newValue
        }
    internal var _isGameOver: skip.model.Observed<Boolean> = skip.model.Observed(false)
    internal var hasWon: Boolean
        get() = _hasWon.wrappedValue
        set(newValue) {
            _hasWon.wrappedValue = newValue
        }
    internal var _hasWon: skip.model.Observed<Boolean> = skip.model.Observed(false)
    internal var continueAfterWin: Boolean
        get() = _continueAfterWin.wrappedValue
        set(newValue) {
            _continueAfterWin.wrappedValue = newValue
        }
    internal var _continueAfterWin: skip.model.Observed<Boolean> = skip.model.Observed(false)
    internal var difficulty: TwentyFortyEightDifficulty
        get() = _difficulty.wrappedValue
        set(newValue) {
            _difficulty.wrappedValue = newValue
        }
    internal var _difficulty: skip.model.Observed<TwentyFortyEightDifficulty> = skip.model.Observed(TwentyFortyEightDifficulty.normal)

    // Undo support
    internal var undoGrid: Array<Int>
        get() = _undoGrid.wrappedValue.sref({ this.undoGrid = it })
        set(newValue) {
            _undoGrid.wrappedValue = newValue.sref()
        }
    internal var _undoGrid: skip.model.Observed<Array<Int>> = skip.model.Observed(Array(repeating = 0, count = gridSize * gridSize))
    internal var undoScore: Int
        get() = _undoScore.wrappedValue
        set(newValue) {
            _undoScore.wrappedValue = newValue
        }
    internal var _undoScore: skip.model.Observed<Int> = skip.model.Observed(0)
    internal var hasUndo: Boolean
        get() = _hasUndo.wrappedValue
        set(newValue) {
            _hasUndo.wrappedValue = newValue
        }
    internal var _hasUndo: skip.model.Observed<Boolean> = skip.model.Observed(false)
    internal var undosRemaining: Int
        get() = _undosRemaining.wrappedValue
        set(newValue) {
            _undosRemaining.wrappedValue = newValue
        }
    internal var _undosRemaining: skip.model.Observed<Int> = skip.model.Observed(3)

    // Animation tracking
    internal var mergedIndices: Array<Int>
        get() = _mergedIndices.wrappedValue.sref({ this.mergedIndices = it })
        set(newValue) {
            _mergedIndices.wrappedValue = newValue.sref()
        }
    internal var _mergedIndices: skip.model.Observed<Array<Int>> = skip.model.Observed(arrayOf())
    internal var spawnedIndex: Int
        get() = _spawnedIndex.wrappedValue
        set(newValue) {
            _spawnedIndex.wrappedValue = newValue
        }
    internal var _spawnedIndex: skip.model.Observed<Int> = skip.model.Observed(-1)
    private var lineMergePositions: Array<Int>
        get() = _lineMergePositions.wrappedValue.sref({ this.lineMergePositions = it })
        set(newValue) {
            _lineMergePositions.wrappedValue = newValue.sref()
        }
    private var _lineMergePositions: skip.model.Observed<Array<Int>> = skip.model.Observed(arrayOf())

    internal fun tile(row: Int, col: Int): Int = grid[row * gridSize + col]

    internal fun setTile(row: Int, col: Int, value: Int) {
        grid[row * gridSize + col] = value
    }

    internal fun newGame(diff: TwentyFortyEightDifficulty? = null) {
        if (diff != null) {
            difficulty = diff
        }
        grid = Array(repeating = 0, count = gridSize * gridSize)
        score = 0
        isGameOver = false
        hasWon = false
        continueAfterWin = false
        mergedIndices = arrayOf()
        spawnedIndex = -1
        hasUndo = false
        undosRemaining = 3
        undoGrid = Array(repeating = 0, count = gridSize * gridSize)
        undoScore = 0
        spawnTile()
        spawnTile()
    }

    /// Save current state for undo before a move
    internal fun saveUndoState() {
        if (difficulty.undoAllowed && undosRemaining > 0) {
            undoGrid = grid
            undoScore = score
            hasUndo = true
        }
    }

    /// Restore the last saved undo state
    internal fun undo() {
        if (!hasUndo || !difficulty.undoAllowed || undosRemaining <= 0) {
            return
        }
        grid = undoGrid
        score = undoScore
        isGameOver = false
        hasUndo = false
        undosRemaining -= 1
        mergedIndices = arrayOf()
        spawnedIndex = -1
    }

    internal fun spawnTile() {
        var empties: Array<Int> = arrayOf()
        for (i in 0..<(gridSize * gridSize)) {
            if (grid[i] == 0) {
                empties.append(i)
            }
        }
        if (empties.isEmpty) {
            return
        }
        val idx = empties[Int.random(in_ = 0..<empties.count)]
        grid[idx] = if (Double.random(in_ = 0.0..1.0) < difficulty.fourSpawnChance) 4 else 2
        spawnedIndex = idx
    }

    /// Spawn the appropriate number of tiles for the current difficulty
    internal fun spawnTilesForMove() {
        for (unusedbinding in 0..<difficulty.tilesPerSpawn) {
            spawnTile()
        }
    }

    // MARK: - Move preview (non-mutating)

    /// Compute what would happen if the given move were committed, without
    /// touching the grid. The view layer uses this to drive a live drag
    /// preview: every tile that would shift gets `startCell != endCell`, and
    /// the absorbed-source tile of a merge pair is flagged so the view can
    /// highlight it. Safe to call repeatedly.
    internal fun previewMove(direction: Direction): TwentyFortyEightMovePreview {
        var movements: Array<TwentyFortyEightTileMovement> = arrayOf()
        when (direction) {
            Direction.left -> {
                var r = 0
                while (r < gridSize) {
                    val row = extractRow(r)
                    val lineMovs = simulateLine(row)
                    for (m in lineMovs.sref()) {
                        movements.append(TwentyFortyEightTileMovement(startCell = r * gridSize + m.startInLine, endCell = r * gridSize + m.endInLine, isAbsorbedSource = m.isAbsorbedSource, value = m.value))
                    }
                    r += 1
                }
            }
            Direction.right -> {
                var r = 0
                while (r < gridSize) {
                    val row = extractRow(r)
                    val reversedLine = Array(row.reversed())
                    val lineMovs = simulateLine(reversedLine)
                    for (m in lineMovs.sref()) {
                        val startCol = (gridSize - 1) - m.startInLine
                        val endCol = (gridSize - 1) - m.endInLine
                        movements.append(TwentyFortyEightTileMovement(startCell = r * gridSize + startCol, endCell = r * gridSize + endCol, isAbsorbedSource = m.isAbsorbedSource, value = m.value))
                    }
                    r += 1
                }
            }
            Direction.up -> {
                var c = 0
                while (c < gridSize) {
                    val col = extractCol(c)
                    val lineMovs = simulateLine(col)
                    for (m in lineMovs.sref()) {
                        movements.append(TwentyFortyEightTileMovement(startCell = m.startInLine * gridSize + c, endCell = m.endInLine * gridSize + c, isAbsorbedSource = m.isAbsorbedSource, value = m.value))
                    }
                    c += 1
                }
            }
            Direction.down -> {
                var c = 0
                while (c < gridSize) {
                    val col = extractCol(c)
                    val reversedLine = Array(col.reversed())
                    val lineMovs = simulateLine(reversedLine)
                    for (m in lineMovs.sref()) {
                        val startRow = (gridSize - 1) - m.startInLine
                        val endRow = (gridSize - 1) - m.endInLine
                        movements.append(TwentyFortyEightTileMovement(startCell = startRow * gridSize + c, endCell = endRow * gridSize + c, isAbsorbedSource = m.isAbsorbedSource, value = m.value))
                    }
                    c += 1
                }
            }
        }
        return TwentyFortyEightMovePreview(direction = direction, movements = movements)
    }

    /// Per-line simulation shared by `previewMove`. Given a line oriented so
    /// the merge target is index 0, returns one record per non-zero tile with
    /// its post-move position (still in the same line orientation) and merge
    /// role. Mirrors the structure of `mergeLine` but never mutates state.
    private fun simulateLine(line: Array<Int>): Array<LineMovement> {
        var nonZeroIndices: Array<Int> = arrayOf()
        var nonZeroValues: Array<Int> = arrayOf()
        var i = 0
        while (i < line.count) {
            if (line[i] != 0) {
                nonZeroIndices.append(i)
                nonZeroValues.append(line[i])
            }
            i += 1
        }

        var result: Array<LineMovement> = arrayOf()
        var resultPos = 0
        var k = 0
        while (k < nonZeroValues.count) {
            val curIdx = nonZeroIndices[k]
            val curVal = nonZeroValues[k]
            if (k + 1 < nonZeroValues.count && nonZeroValues[k + 1] == curVal) {
                val nextIdx = nonZeroIndices[k + 1]
                // First of the pair stays at resultPos as the merge destination.
                result.append(LineMovement(startInLine = curIdx, endInLine = resultPos, isAbsorbedSource = false, value = curVal))
                // Second of the pair is absorbed into the same resultPos.
                result.append(LineMovement(startInLine = nextIdx, endInLine = resultPos, isAbsorbedSource = true, value = curVal))
                k += 2
            } else {
                result.append(LineMovement(startInLine = curIdx, endInLine = resultPos, isAbsorbedSource = false, value = curVal))
                k += 1
            }
            resultPos += 1
        }
        return result.sref()
    }

    internal fun move(direction: Direction): Boolean {
        val before = grid.sref()
        val scoreBefore = score
        mergedIndices = arrayOf()

        when (direction) {
            Direction.left -> {
                for (r in 0..<gridSize) {
                    val row = extractRow(r)
                    val merged = mergeLine(row)
                    setRow(r, merged)
                    for (pos in lineMergePositions.sref()) {
                        mergedIndices.append(r * gridSize + pos)
                    }
                }
            }
            Direction.right -> {
                for (r in 0..<gridSize) {
                    val row = extractRow(r)
                    val merged = mergeLine(row.reversed()).reversed()
                    setRow(r, Array(merged))
                    for (pos in lineMergePositions.sref()) {
                        mergedIndices.append(r * gridSize + (gridSize - 1 - pos))
                    }
                }
            }
            Direction.up -> {
                for (c in 0..<gridSize) {
                    val col = extractCol(c)
                    val merged = mergeLine(col)
                    setCol(c, merged)
                    for (pos in lineMergePositions.sref()) {
                        mergedIndices.append(pos * gridSize + c)
                    }
                }
            }
            Direction.down -> {
                for (c in 0..<gridSize) {
                    val col = extractCol(c)
                    val merged = mergeLine(col.reversed()).reversed()
                    setCol(c, Array(merged))
                    for (pos in lineMergePositions.sref()) {
                        mergedIndices.append((gridSize - 1 - pos) * gridSize + c)
                    }
                }
            }
        }

        val moved = grid != before || score != scoreBefore
        return moved
    }

    private fun extractRow(r: Int): Array<Int> {
        var result: Array<Int> = arrayOf()
        for (c in 0..<gridSize) {
            result.append(tile(r, c))
        }
        return result.sref()
    }

    private fun setRow(r: Int, values: Array<Int>) {
        for (c in 0..<gridSize) {
            setTile(r, c, values[c])
        }
    }

    private fun extractCol(c: Int): Array<Int> {
        var result: Array<Int> = arrayOf()
        for (r in 0..<gridSize) {
            result.append(tile(r, c))
        }
        return result.sref()
    }

    private fun setCol(c: Int, values: Array<Int>) {
        for (r in 0..<gridSize) {
            setTile(r, c, values[r])
        }
    }

    // Slide non-zeros left, merge adjacent equal pairs, slide again
    private fun mergeLine(line: Array<Int>): Array<Int> {
        lineMergePositions = arrayOf()

        // Remove zeros
        var compact: Array<Int> = arrayOf()
        for (v in line.sref()) {
            if (v != 0) {
                compact.append(v)
            }
        }

        // Merge
        var merged: Array<Int> = arrayOf()
        var skip = false
        for (i in 0..<compact.count) {
            if (skip) {
                skip = false
                continue
            }
            if (i + 1 < compact.count && compact[i] == compact[i + 1]) {
                val val_ = compact[i] * 2
                val mergePos = merged.count
                merged.append(val_)
                score += val_
                lineMergePositions.append(mergePos)
                skip = true
            } else {
                merged.append(compact[i])
            }
        }

        // Pad with zeros
        while (merged.count < gridSize) {
            merged.append(0)
        }
        return merged.sref()
    }

    internal fun checkGameState() {
        // Check for 2048 win
        if (!continueAfterWin) {
            for (v in grid.sref()) {
                if (v >= 2048) {
                    hasWon = true
                    saveHighScore()
                    return
                }
            }
        }

        // Check for any empty cell
        for (v in grid.sref()) {
            if (v == 0) {
                return
            }
        }

        // Check for any possible merge
        for (r in 0..<gridSize) {
            for (c in 0..<gridSize) {
                val v = tile(r, c)
                if (c + 1 < gridSize && tile(r, c + 1) == v) {
                    return
                }
                if (r + 1 < gridSize && tile(r + 1, c) == v) {
                    return
                }
            }
        }

        // No moves left
        isGameOver = true
        saveHighScore()
    }

    internal fun continueGame() {
        continueAfterWin = true
        hasWon = false
    }

    internal fun saveHighScore() {
        if (score > highScore) {
            highScore = score
            UserDefaults.standard.set(highScore, forKey = "twentyfortyeight_highscore")
        }
    }

    // MARK: - State Persistence

    internal fun makeSavedState(): TwentyFortyEightSavedState = TwentyFortyEightSavedState(grid = grid, score = score, isGameOver = isGameOver, hasWon = hasWon, continueAfterWin = continueAfterWin, difficultyRaw = difficulty.rawValue, undoGrid = undoGrid, undoScore = undoScore, hasUndo = hasUndo, undosRemaining = undosRemaining)

    internal fun restoreState(state: TwentyFortyEightSavedState) {
        grid = state.grid
        score = state.score
        isGameOver = state.isGameOver
        hasWon = state.hasWon
        continueAfterWin = state.continueAfterWin
        difficulty = TwentyFortyEightDifficulty(rawValue = state.difficultyRaw) ?: TwentyFortyEightDifficulty.normal
        undoGrid = state.undoGrid
        undoScore = state.undoScore
        hasUndo = state.hasUndo
        undosRemaining = state.undosRemaining
        highScore = UserDefaults.standard.integer(forKey = "twentyfortyeight_highscore")
        mergedIndices = arrayOf()
        spawnedIndex = -1
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
        UserDefaults.standard.set(json_0, forKey = "twentyfortyeight_saved_state")
    }

    @androidx.annotation.Keep
    companion object {

        internal fun loadSavedState(): TwentyFortyEightSavedState? {
            val json_1 = UserDefaults.standard.string(forKey = "twentyfortyeight_saved_state")
            if (json_1 == null) {
                return null
            }
            val data_1 = json_1.data(using = StringEncoding.utf8)
            if (data_1 == null) {
                return null
            }
            return try { JSONDecoder().decode(TwentyFortyEightSavedState::class, from = data_1) } catch (_: Throwable) { null }
        }

        internal fun clearSavedState(): Unit = UserDefaults.standard.removeObject(forKey = "twentyfortyeight_saved_state")
    }
}

// MARK: - Game View

internal class TwentyFortyEightGameView: View {
    internal var showInstructions: Boolean
        get() = _showInstructions.wrappedValue
        set(newValue) {
            _showInstructions.wrappedValue = newValue
        }
    internal var _showInstructions: Binding<Boolean>
    private var game: TwentyFortyEightModel
        get() = _game.wrappedValue
        set(newValue) {
            _game.wrappedValue = newValue
        }
    private var _game: skip.ui.State<TwentyFortyEightModel>
    private var showSettings: Boolean
        get() = _showSettings.wrappedValue
        set(newValue) {
            _showSettings.wrappedValue = newValue
        }
    private var _showSettings: skip.ui.State<Boolean>
    private var showPauseMenu: Boolean
        get() = _showPauseMenu.wrappedValue
        set(newValue) {
            _showPauseMenu.wrappedValue = newValue
        }
    private var _showPauseMenu: skip.ui.State<Boolean>
    private var showDifficultyPicker: Boolean
        get() = _showDifficultyPicker.wrappedValue
        set(newValue) {
            _showDifficultyPicker.wrappedValue = newValue
        }
    private var _showDifficultyPicker: skip.ui.State<Boolean>
    private var hasInitialized: Boolean
        get() = _hasInitialized.wrappedValue
        set(newValue) {
            _hasInitialized.wrappedValue = newValue
        }
    private var _hasInitialized: skip.ui.State<Boolean>
    private var tileScales: Array<Double>
        get() = _tileScales.wrappedValue.sref({ this.tileScales = it })
        set(newValue) {
            _tileScales.wrappedValue = newValue.sref()
        }
    private var _tileScales: skip.ui.State<Array<Double>>
    private var animTimer: Timer?
        get() = _animTimer.wrappedValue
        set(newValue) {
            _animTimer.wrappedValue = newValue
        }
    private var _animTimer: skip.ui.State<Timer?> = skip.ui.State(null)
    private var displayedScore: Int
        get() = _displayedScore.wrappedValue
        set(newValue) {
            _displayedScore.wrappedValue = newValue
        }
    private var _displayedScore: skip.ui.State<Int>
    private var displayedHighScore: Int
        get() = _displayedHighScore.wrappedValue
        set(newValue) {
            _displayedHighScore.wrappedValue = newValue
        }
    private var _displayedHighScore: skip.ui.State<Int>
    private var scoreAnimTimer: Timer?
        get() = _scoreAnimTimer.wrappedValue
        set(newValue) {
            _scoreAnimTimer.wrappedValue = newValue
        }
    private var _scoreAnimTimer: skip.ui.State<Timer?> = skip.ui.State(null)

    // MARK: - Drag-preview state

    /// Locked direction for the current drag, or nil if no drag in progress.
    private var dragDirection: Direction?
        get() = _dragDirection.wrappedValue
        set(newValue) {
            _dragDirection.wrappedValue = newValue
        }
    private var _dragDirection: skip.ui.State<Direction?> = skip.ui.State(null)
    /// The preview corresponding to `dragDirection`.
    private var dragPreview: TwentyFortyEightMovePreview?
        get() = _dragPreview.wrappedValue
        set(newValue) {
            _dragPreview.wrappedValue = newValue
        }
    private var _dragPreview: skip.ui.State<TwentyFortyEightMovePreview?> = skip.ui.State(null)
    /// Per-cell visual offset applied while dragging (in points).
    private var tileOffsetX: Array<Double>
        get() = _tileOffsetX.wrappedValue.sref({ this.tileOffsetX = it })
        set(newValue) {
            _tileOffsetX.wrappedValue = newValue.sref()
        }
    private var _tileOffsetX: skip.ui.State<Array<Double>>
    private var tileOffsetY: Array<Double>
        get() = _tileOffsetY.wrappedValue.sref({ this.tileOffsetY = it })
        set(newValue) {
            _tileOffsetY.wrappedValue = newValue.sref()
        }
    private var _tileOffsetY: skip.ui.State<Array<Double>>
    /// Per-cell maximum offset magnitude along the locked drag axis (positive).
    /// Used to clamp the live drag offset to the tile's intended destination.
    private var tileMaxAxisOffset: Array<Double>
        get() = _tileMaxAxisOffset.wrappedValue.sref({ this.tileMaxAxisOffset = it })
        set(newValue) {
            _tileMaxAxisOffset.wrappedValue = newValue.sref()
        }
    private var _tileMaxAxisOffset: skip.ui.State<Array<Double>>
    /// 0..1 glow intensity for the *absorbed source* of a merge — the tile
    /// that disappears into another. Rendered as a bright gold border.
    private var tileMergeGlow: Array<Double>
        get() = _tileMergeGlow.wrappedValue.sref({ this.tileMergeGlow = it })
        set(newValue) {
            _tileMergeGlow.wrappedValue = newValue.sref()
        }
    private var _tileMergeGlow: skip.ui.State<Array<Double>>
    /// 0..1 glow intensity for the *destination* tile of a merge — the tile
    /// that will hold the doubled value. Rendered as a softer accent border.
    private var tileDestGlow: Array<Double>
        get() = _tileDestGlow.wrappedValue.sref({ this.tileDestGlow = it })
        set(newValue) {
            _tileDestGlow.wrappedValue = newValue.sref()
        }
    private var _tileDestGlow: skip.ui.State<Array<Double>>
    /// Latest measured cell size (in points). Captured from the GeometryReader
    /// so gesture handlers can compute pixel offsets without rebuilding the view.
    private var measuredCellSize: Double
        get() = _measuredCellSize.wrappedValue
        set(newValue) {
            _measuredCellSize.wrappedValue = newValue
        }
    private var _measuredCellSize: skip.ui.State<Double>
    /// Step size between cell origins (cellSize + gridSpacing). The distance a
    /// tile travels for a one-cell shift.
    private var measuredStepSize: Double
        get() = _measuredStepSize.wrappedValue
        set(newValue) {
            _measuredStepSize.wrappedValue = newValue
        }
    private var _measuredStepSize: skip.ui.State<Double>
    /// True when the current drag-attempt direction has no possible movements
    /// (dragging into a wall). Suppresses the "lock failed" haptic repeat.
    private var dragRejected: Boolean
        get() = _dragRejected.wrappedValue
        set(newValue) {
            _dragRejected.wrappedValue = newValue
        }
    private var _dragRejected: skip.ui.State<Boolean>

    internal lateinit var dismiss: DismissAction
    internal var settings: TwentyFortyEightSettings
        get() = _settings.wrappedValue
        set(newValue) {
            _settings.wrappedValue = newValue
        }
    internal var _settings = skip.ui.Environment<TwentyFortyEightSettings>()

    internal val theme: TwentyFortyEightTheme
        get() = settings.theme

    internal fun playHaptic(pattern: HapticPattern) {
        if (settings.vibrations) {
            HapticFeedback.play(pattern)
        }
    }

    override fun body(): View {
        return ComposeBuilder { composectx: ComposeContext ->
            GeometryReader { geo ->
                ComposeBuilder { composectx: ComposeContext ->
                    val boardSize = min(geo.size.width - 32.0, geo.size.height * 0.55)
                    val cellSize = (boardSize - gridSpacing * Double(gridSize + 1)) / Double(gridSize)

                    VStack(spacing = 0.0) { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            // HUD
                            hudView
                                .frame(height = 44.0).Compose(composectx)

                            // Score row
                            HStack(spacing = 12.0) { ->
                                ComposeBuilder { composectx: ComposeContext ->
                                    scoreBox(label = "SCORE", value = displayedScore).Compose(composectx)
                                    scoreBox(label = "BEST", value = displayedHighScore).Compose(composectx)
                                    ComposeResult.ok
                                }
                            }
                            .padding(Edge.Set.horizontal, 16.0)
                            .padding(Edge.Set.bottom, 16.0).Compose(composectx)

                            Spacer().Compose(composectx)

                            // Board
                            ZStack { ->
                                ComposeBuilder { composectx: ComposeContext ->
                                    // Board background
                                    RoundedRectangle(cornerRadius = 8.0)
                                        .fill(theme.boardBackground)
                                        .frame(width = boardSize, height = boardSize).Compose(composectx)

                                    // Empty cell placeholders
                                    VStack(spacing = gridSpacing) { ->
                                        ComposeBuilder { composectx: ComposeContext ->
                                            ForEach(0..<gridSize, id = { it }) { r ->
                                                ComposeBuilder { composectx: ComposeContext ->
                                                    HStack(spacing = gridSpacing) { ->
                                                        ComposeBuilder { composectx: ComposeContext ->
                                                            ForEach(0..<gridSize, id = { it }) { c ->
                                                                ComposeBuilder { composectx: ComposeContext ->
                                                                    RoundedRectangle(cornerRadius = tileCornerRadius)
                                                                        .fill(theme.emptyCellBackground.opacity(theme.emptyCellOpacity))
                                                                        .frame(width = cellSize, height = cellSize).Compose(composectx)
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

                                    // Tile values — single ZStack with absolute positioning so
                                    // every tile is a *sibling* of every other tile. zIndex
                                    // applied to a sibling actually controls global stacking
                                    // order, which a VStack-of-HStacks layout cannot do (a
                                    // tile in row 0's HStack can never render above row 1's
                                    // HStack, no matter what zIndex it carries).
                                    //
                                    // Offsets are expressed relative to the *center* of the
                                    // ZStack (its default alignment): a tile's natural layout
                                    // position is the ZStack center, and the per-cell offset
                                    // shifts it out from there. Center-relative math avoids
                                    // depending on `alignment: .topLeading`, which Skip can
                                    // fall back to `.center` for and would otherwise drop the
                                    // whole grid into the bottom-right quadrant of the board.
                                    ZStack { ->
                                        ComposeBuilder { composectx: ComposeContext ->
                                            ForEach(0..<(gridSize * gridSize), id = { it }) { index ->
                                                ComposeBuilder { composectx: ComposeContext ->
                                                    val r = index / gridSize
                                                    val c = index % gridSize
                                                    val stepSize = cellSize + gridSpacing
                                                    val centerOffset = Double(gridSize - 1) / 2.0
                                                    val cellOffsetX = (Double(c) - centerOffset) * stepSize
                                                    val cellOffsetY = (Double(r) - centerOffset) * stepSize
                                                    tileView(value = game.tile(r, c), cellSize = cellSize)
                                                        .scaleEffect(if (game.tile(r, c) > 0) tileScales[index] else 1.0)
                                                        .overlay(mergeHighlightOverlay(idx = index, cellSize = cellSize))
                                                        .offset(x = cellOffsetX + tileOffsetX[index], y = cellOffsetY + tileOffsetY[index])
                                                        .zIndex(dragZIndex(for_ = index)).Compose(composectx)
                                                    ComposeResult.ok
                                                }
                                            }.Compose(composectx)
                                            ComposeResult.ok
                                        }
                                    }
                                    .frame(width = boardSize, height = boardSize).Compose(composectx)

                                    // Win overlay
                                    if (game.hasWon) {
                                        winOverlay(boardSize = boardSize).Compose(composectx)
                                    }

                                    // Game over overlay
                                    if (game.isGameOver) {
                                        gameOverOverlay(boardSize = boardSize).Compose(composectx)
                                    }

                                    // Pause menu overlay
                                    if (showPauseMenu && !game.isGameOver && !game.hasWon) {
                                        pauseMenuOverlay(boardSize = boardSize).Compose(composectx)
                                    }
                                    ComposeResult.ok
                                }
                            }.Compose(composectx)

                            Spacer().Compose(composectx)
                            Spacer().Compose(composectx)
                            ComposeResult.ok
                        }
                    }
                    .gesture(DragGesture(minimumDistance = 8.0)
                        .onChanged { value -> handleDragChanged(translationWidth = Double(value.translation.width), translationHeight = Double(value.translation.height)) }
                        .onEnded { value -> handleDragEnded(translationWidth = Double(value.translation.width), translationHeight = Double(value.translation.height)) })
                    .background(theme.background.ignoresSafeArea())
                    .onAppear { ->
                        measuredCellSize = cellSize
                        measuredStepSize = cellSize + gridSpacing
                    }
                    .onChange(of = cellSize) { _, newValue ->
                        measuredCellSize = newValue
                        measuredStepSize = newValue + gridSpacing
                    }.Compose(composectx)
                    ComposeResult.ok
                }
            }
            .navigationBarBackButtonHidden()
            .onAppear { ->
                if (!hasInitialized) {
                    hasInitialized = true
                    val matchtarget_0 = TwentyFortyEightModel.loadSavedState()
                    if (matchtarget_0 != null) {
                        val state = matchtarget_0
                        game.restoreState(state)
                    } else {
                        showDifficultyPicker = true
                    }
                }
                displayedScore = game.score
                displayedHighScore = game.highScore
                resetScales()
            }
            .onDisappear { ->
                animTimer?.invalidate()
                animTimer = null
                stopScoreAnimation()
            }
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
            .sheet(isPresented = Binding({ _showSettings.wrappedValue }, { it -> _showSettings.wrappedValue = it })) { ->
                ComposeBuilder { composectx: ComposeContext ->
                    TwentyFortyEightSettingsView(settings = settings)
                        .presentationDetents(setOf(PresentationDetent.medium, PresentationDetent.large)).Compose(composectx)
                    ComposeResult.ok
                }
            }
            .sheet(isPresented = Binding({ _showDifficultyPicker.wrappedValue }, { it -> _showDifficultyPicker.wrappedValue = it })) { ->
                ComposeBuilder { composectx: ComposeContext ->
                    TwentyFortyEightDifficultyPickerView(theme = theme) { newDifficulty ->
                        TwentyFortyEightModel.clearSavedState()
                        game.newGame(diff = newDifficulty)
                        resetScales()
                        stopScoreAnimation()
                        displayedScore = 0
                        displayedHighScore = game.highScore
                        showDifficultyPicker = false
                        showPauseMenu = false
                        playHaptic(HapticPattern.snap)
                    }.Compose(composectx)
                    ComposeResult.ok
                }
            }.Compose(composectx)
        }
    }

    @Composable
    @Suppress("UNCHECKED_CAST")
    override fun Evaluate(context: ComposeContext, options: Int): kotlin.collections.List<Renderable> {
        val rememberedgame by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<TwentyFortyEightModel>, Any>) { mutableStateOf(_game) }
        _game = rememberedgame

        val rememberedshowSettings by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Boolean>, Any>) { mutableStateOf(_showSettings) }
        _showSettings = rememberedshowSettings

        val rememberedshowPauseMenu by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Boolean>, Any>) { mutableStateOf(_showPauseMenu) }
        _showPauseMenu = rememberedshowPauseMenu

        val rememberedshowDifficultyPicker by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Boolean>, Any>) { mutableStateOf(_showDifficultyPicker) }
        _showDifficultyPicker = rememberedshowDifficultyPicker

        val rememberedhasInitialized by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Boolean>, Any>) { mutableStateOf(_hasInitialized) }
        _hasInitialized = rememberedhasInitialized

        val rememberedtileScales by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Array<Double>>, Any>) { mutableStateOf(_tileScales) }
        _tileScales = rememberedtileScales

        val rememberedanimTimer by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Timer?>, Any>) { mutableStateOf(_animTimer) }
        _animTimer = rememberedanimTimer

        val remembereddisplayedScore by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Int>, Any>) { mutableStateOf(_displayedScore) }
        _displayedScore = remembereddisplayedScore

        val remembereddisplayedHighScore by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Int>, Any>) { mutableStateOf(_displayedHighScore) }
        _displayedHighScore = remembereddisplayedHighScore

        val rememberedscoreAnimTimer by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Timer?>, Any>) { mutableStateOf(_scoreAnimTimer) }
        _scoreAnimTimer = rememberedscoreAnimTimer

        val remembereddragDirection by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Direction?>, Any>) { mutableStateOf(_dragDirection) }
        _dragDirection = remembereddragDirection

        val remembereddragPreview by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<TwentyFortyEightMovePreview?>, Any>) { mutableStateOf(_dragPreview) }
        _dragPreview = remembereddragPreview

        val rememberedtileOffsetX by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Array<Double>>, Any>) { mutableStateOf(_tileOffsetX) }
        _tileOffsetX = rememberedtileOffsetX

        val rememberedtileOffsetY by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Array<Double>>, Any>) { mutableStateOf(_tileOffsetY) }
        _tileOffsetY = rememberedtileOffsetY

        val rememberedtileMaxAxisOffset by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Array<Double>>, Any>) { mutableStateOf(_tileMaxAxisOffset) }
        _tileMaxAxisOffset = rememberedtileMaxAxisOffset

        val rememberedtileMergeGlow by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Array<Double>>, Any>) { mutableStateOf(_tileMergeGlow) }
        _tileMergeGlow = rememberedtileMergeGlow

        val rememberedtileDestGlow by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Array<Double>>, Any>) { mutableStateOf(_tileDestGlow) }
        _tileDestGlow = rememberedtileDestGlow

        val rememberedmeasuredCellSize by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Double>, Any>) { mutableStateOf(_measuredCellSize) }
        _measuredCellSize = rememberedmeasuredCellSize

        val rememberedmeasuredStepSize by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Double>, Any>) { mutableStateOf(_measuredStepSize) }
        _measuredStepSize = rememberedmeasuredStepSize

        val remembereddragRejected by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Boolean>, Any>) { mutableStateOf(_dragRejected) }
        _dragRejected = remembereddragRejected

        this.dismiss = EnvironmentValues.shared.dismiss
        _settings.wrappedValue = EnvironmentValues.shared.environmentObject(type = TwentyFortyEightSettings::class)!!

        return super.Evaluate(context, options)
    }

    // MARK: - Pause Menu

    internal fun pauseMenuOverlay(boardSize: Double): View {
        return ZStack { ->
            ComposeBuilder { composectx: ComposeContext ->
                RoundedRectangle(cornerRadius = 8.0)
                    .fill(theme.boardBackground.opacity(0.92))
                    .frame(width = boardSize, height = boardSize).Compose(composectx)

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
                            showDifficultyPicker = true
                            playHaptic(HapticPattern.snap)
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
                }.Compose(composectx)
                ComposeResult.ok
            }
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

    // MARK: - Animation

    internal fun resetScales() {
        animTimer?.invalidate()
        animTimer = null
        for (i in 0..<(gridSize * gridSize)) {
            tileScales[i] = 1.0
        }
    }

    internal fun triggerAnimations() {
        animTimer?.invalidate()

        // Phase 1: set exaggerated starting values (renders this frame)
        for (idx in game.mergedIndices.sref()) {
            tileScales[idx] = 1.2
        }
        if (game.spawnedIndex >= 0 && game.spawnedIndex < gridSize * gridSize) {
            tileScales[game.spawnedIndex] = 0.1
        }

        // Phase 2: after one frame, animate back to 1.0
        animTimer = Timer.scheduledTimer(withTimeInterval = 0.03, repeats = false) { _ ->
            withAnimation(Animation.spring(response = 0.2, dampingFraction = 0.6)) { ->
                for (i in 0..<(gridSize * gridSize)) {
                    tileScales[i] = 1.0
                }
            }
        }
    }

    // MARK: - Drag preview

    /// Visual lock threshold: how many points of motion are required before
    /// we lock a drag direction. The DragGesture itself uses
    /// `minimumDistance: 8`, and we add a small extra buffer so accidental
    /// taps that just barely register as drags don't lock a direction.
    private val dragLockThreshold: Double
        get() = 10.0

    internal fun handleDragChanged(translationWidth: Double, translationHeight: Double) {
        if (game.isGameOver || game.hasWon || showPauseMenu || showDifficultyPicker) {
            return
        }

        val dx = translationWidth
        val dy = translationHeight

        if (dragDirection == null) {
            val absX = if (dx < 0.0) -dx else dx
            val absY = if (dy < 0.0) -dy else dy
            val largest = max(absX, absY)
            if (largest < dragLockThreshold) {
                return
            }

            val candidate: Direction
            if (absX > absY) {
                candidate = if (dx > 0.0) Direction.right else Direction.left
            } else {
                candidate = if (dy > 0.0) Direction.down else Direction.up
            }

            val preview = game.previewMove(candidate)
            if (!preview.anyMovement) {
                if (!dragRejected) {
                    dragRejected = true
                    playHaptic(HapticPattern(arrayOf(HapticEvent(HapticEventType.tick, intensity = 0.25))))
                }
                return
            }

            lockDirection(candidate, preview = preview)
        }
        val dir_0 = dragDirection
        if (dir_0 == null) {
            return
        }
        val axisDrag: Double
        when (dir_0) {
            Direction.right -> axisDrag = if (dx > 0.0) dx else 0.0
            Direction.left -> axisDrag = if (dx < 0.0) -dx else 0.0
            Direction.down -> axisDrag = if (dy > 0.0) dy else 0.0
            Direction.up -> axisDrag = if (dy < 0.0) -dy else 0.0
        }
        applyAxisDrag(axisDrag, direction = dir_0)
    }

    internal fun handleDragEnded(translationWidth: Double, translationHeight: Double) {
        var deferaction_0: (() -> Unit)? = null
        try {
            deferaction_0 = {
                dragRejected = false
            }
            val dir_1 = dragDirection
            if (dir_1 == null) {
                return
            }

            val dx = translationWidth
            val dy = translationHeight
            val axisDrag: Double
            when (dir_1) {
                Direction.right -> axisDrag = if (dx > 0.0) dx else 0.0
                Direction.left -> axisDrag = if (dx < 0.0) -dx else 0.0
                Direction.down -> axisDrag = if (dy > 0.0) dy else 0.0
                Direction.up -> axisDrag = if (dy < 0.0) -dy else 0.0
            }

            // Commit if the user dragged at least halfway across one cell. Past
            // that point each tile is visually closer to its destination than its
            // origin (capped at destination for tiles that have already arrived).
            val commitThreshold = max(measuredStepSize * 0.5, 24.0)
            if (axisDrag >= commitThreshold) {
                commitMove(direction = dir_1)
            } else {
                cancelDrag()
            }
        } finally {
            deferaction_0?.invoke()
        }
    }

    /// Lock the drag direction and prep per-cell animation targets from
    /// `preview`. Called once at the start of a drag.
    internal fun lockDirection(direction: Direction, preview: TwentyFortyEightMovePreview) {
        dragDirection = direction
        dragPreview = preview

        var i = 0
        while (i < gridSize * gridSize) {
            tileOffsetX[i] = 0.0
            tileOffsetY[i] = 0.0
            tileMaxAxisOffset[i] = 0.0
            tileMergeGlow[i] = 0.0
            tileDestGlow[i] = 0.0
            i += 1
        }

        for (m in preview.movements.sref()) {
            val deltaCells: Int
            when (direction) {
                Direction.left, Direction.right -> {
                    val startCol = m.startCell % gridSize
                    val endCol = m.endCell % gridSize
                    val d = endCol - startCol
                    deltaCells = if (d < 0) -d else d
                }
                Direction.up, Direction.down -> {
                    val startRow = m.startCell / gridSize
                    val endRow = m.endCell / gridSize
                    val d = endRow - startRow
                    deltaCells = if (d < 0) -d else d
                }
            }
            if (deltaCells > 0) {
                tileMaxAxisOffset[m.startCell] = Double(deltaCells) * measuredStepSize
            }
        }

        // Build the set of endCells that are merge targets (so we can identify
        // the destination tile of each merge — the one we want the soft accent
        // glow on). Then the destination's glow is attached to its *startCell*
        // so the highlight follows the tile while it slides into the merge,
        // not the empty cell it will end up at.
        var mergeDestEndCells: Set<Int> = setOf()
        for (m in preview.movements.sref()) {
            if (m.isAbsorbedSource) {
                mergeDestEndCells.insert(m.endCell)
            }
        }

        withAnimation(Animation.easeOut(duration = 0.16)) { ->
            for (m in preview.movements.sref()) {
                if (m.isAbsorbedSource) {
                    tileMergeGlow[m.startCell] = 1.0
                } else if (mergeDestEndCells.contains(m.endCell)) {
                    tileDestGlow[m.startCell] = 1.0
                }
            }
        }

        playHaptic(HapticPattern(arrayOf(HapticEvent(HapticEventType.tick, intensity = 0.4))))
    }

    /// Update per-cell offsets from the current axis drag magnitude.
    internal fun applyAxisDrag(axisDrag: Double, direction: Direction) {
        var i = 0
        while (i < gridSize * gridSize) {
            val maxOff = tileMaxAxisOffset[i]
            if (maxOff <= 0.0) {
                tileOffsetX[i] = 0.0
                tileOffsetY[i] = 0.0
                i += 1
                continue
            }
            val clamped = if (axisDrag < maxOff) axisDrag else maxOff
            when (direction) {
                Direction.right -> {
                    tileOffsetX[i] = clamped
                    tileOffsetY[i] = 0.0
                }
                Direction.left -> {
                    tileOffsetX[i] = -clamped
                    tileOffsetY[i] = 0.0
                }
                Direction.down -> {
                    tileOffsetX[i] = 0.0
                    tileOffsetY[i] = clamped
                }
                Direction.up -> {
                    tileOffsetX[i] = 0.0
                    tileOffsetY[i] = -clamped
                }
            }
            i += 1
        }
    }

    /// User released past the commit threshold — finish the move.
    internal fun commitMove(direction: Direction) {
        // Snap any laggy tile offsets all the way to their destination so the
        // model mutation lines up with the rendered position.
        var i = 0
        while (i < gridSize * gridSize) {
            val maxOff = tileMaxAxisOffset[i]
            if (maxOff > 0.0) {
                when (direction) {
                    Direction.right -> tileOffsetX[i] = maxOff
                    Direction.left -> tileOffsetX[i] = -maxOff
                    Direction.down -> tileOffsetY[i] = maxOff
                    Direction.up -> tileOffsetY[i] = -maxOff
                }
            }
            i += 1
        }

        // Fade the merge highlights out as the merger happens.
        withAnimation(Animation.easeOut(duration = 0.10)) { ->
            var k = 0
            while (k < gridSize * gridSize) {
                tileMergeGlow[k] = 0.0
                tileDestGlow[k] = 0.0
                k += 1
            }
        }

        game.saveUndoState()
        val moved = game.move(direction)
        if (moved) {
            game.spawnTilesForMove()
            // Once the model has settled the new grid, reset offsets — every
            // tile is now at its natural grid position.
            var j = 0
            while (j < gridSize * gridSize) {
                tileOffsetX[j] = 0.0
                tileOffsetY[j] = 0.0
                tileMaxAxisOffset[j] = 0.0
                j += 1
            }
            triggerAnimations()
            playMergeHaptics()
        }
        game.checkGameState()
        if (game.isGameOver) {
            playHaptic(HapticPattern.impact)
        }
        game.saveState()

        dragDirection = null
        dragPreview = null
    }

    /// User released without crossing the commit threshold — slide tiles back
    /// to their origin and fade highlights out.
    internal fun cancelDrag() {
        withAnimation(Animation.spring(response = 0.26, dampingFraction = 0.78)) { ->
            var i = 0
            while (i < gridSize * gridSize) {
                tileOffsetX[i] = 0.0
                tileOffsetY[i] = 0.0
                tileMergeGlow[i] = 0.0
                tileDestGlow[i] = 0.0
                i += 1
            }
        }
        playHaptic(HapticPattern(arrayOf(HapticEvent(HapticEventType.tick, intensity = 0.28))))
        dragDirection = null
        dragPreview = null
        // Clear max offsets after the cancel animation completes.
        animTimer?.invalidate()
        animTimer = Timer.scheduledTimer(withTimeInterval = 0.28, repeats = false) { _ -> clearTileMaxAxisOffset() }
    }

    internal fun clearTileMaxAxisOffset() {
        var i = 0
        while (i < gridSize * gridSize) {
            tileMaxAxisOffset[i] = 0.0
            i += 1
        }
    }

    /// Z-order for a tile during a drag preview. Sliding tiles must render
    /// above the stationary cells they pass over (each cell's `tileView`
    /// paints its own background even when empty, so without lifting moving
    /// tiles they get covered by the empty-cell background of later siblings
    /// in the row/column iteration order). Absorbed-source tiles are lifted
    /// higher still so they render above the merge destination they're
    /// sliding into.
    internal fun dragZIndex(for_: Int): Double {
        val index = for_
        if (tileMergeGlow[index] > 0.0) {
            return 2.0
        }
        if (tileMaxAxisOffset[index] > 0.0) {
            return 1.0
        }
        return 0.0
    }

    // MARK: - Merge highlight overlay

    /// A "beautiful" highlight rendered over a tile during a drag preview.
    /// Tiles being *absorbed* (the source of a merge) get a bright gold
    /// border with an outer glow; tiles that will *receive* the merge get a
    /// softer accent border. Both fade in/out via withAnimation.
    internal fun mergeHighlightOverlay(idx: Int, cellSize: Double): View {
        return ComposeBuilder { composectx: ComposeContext ->
            val mergeGlow = tileMergeGlow[idx]
            val destGlow = tileDestGlow[idx]
            if (mergeGlow > 0.0 || destGlow > 0.0) {
                ZStack { ->
                    ComposeBuilder { composectx: ComposeContext ->
                        if (destGlow > 0.0) {
                            RoundedRectangle(cornerRadius = tileCornerRadius)
                                .stroke(Color(red = 1.0, green = 0.92, blue = 0.55), lineWidth = 2.5)
                                .frame(width = cellSize, height = cellSize)
                                .opacity(destGlow * 0.9).Compose(composectx)
                        }
                        if (mergeGlow > 0.0) {
                            RoundedRectangle(cornerRadius = tileCornerRadius)
                                .stroke(Color(red = 1.0, green = 0.83, blue = 0.30), lineWidth = 3.0)
                                .frame(width = cellSize, height = cellSize)
                                .shadow(color = Color(red = 1.0, green = 0.85, blue = 0.30).opacity(0.75 * mergeGlow), radius = 8.0)
                                .opacity(mergeGlow).Compose(composectx)
                            // Inner highlight ring for extra polish.
                            RoundedRectangle(cornerRadius = tileCornerRadius - 1.0)
                                .stroke(Color.white.opacity(0.55 * mergeGlow), lineWidth = 1.0)
                                .frame(width = cellSize - 4.0, height = cellSize - 4.0).Compose(composectx)
                        }
                        ComposeResult.ok
                    }
                }
                .allowsHitTesting(false).Compose(composectx)
            } else {
                EmptyView().Compose(composectx)
            }
            ComposeResult.ok
        }
    }

    // MARK: - Merge Haptics

    internal fun playMergeHaptics() {
        if (!settings.vibrations) {
            return
        }

        // Collect merged values from the grid
        var mergedValues: Array<Int> = arrayOf()
        for (idx in game.mergedIndices.sref()) {
            mergedValues.append(game.grid[idx])
        }

        // No merges — just a slide, play a punchy snap
        if (mergedValues.isEmpty) {
            HapticFeedback.play(HapticPattern.place)
            return
        }

        // Sort ascending so we can iterate from the end (largest first)
        mergedValues.sort()

        // Check if any merge reached 2048+ — play the win song
        if (mergedValues[mergedValues.count - 1] >= 2048) {
            playWinSong()
            return
        }

        // Build a single pattern: largest merge first, then smaller ones
        var events: Array<HapticEvent> = arrayOf()
        var isFirst = true
        var vi = mergedValues.count - 1
        while (vi >= 0) {
            val mergeEvents = hapticEventsForMerge(value = mergedValues[vi])
            for (j in 0..<mergeEvents.count) {
                val e = mergeEvents[j]
                if (j == 0 && !isFirst) {
                    // Inter-merge gap before this merge's first event
                    events.append(HapticEvent(e.type, intensity = e.intensity, delay = e.delay + 0.1))
                } else {
                    events.append(e)
                }
            }
            isFirst = false
            vi -= 1
        }

        HapticFeedback.play(HapticPattern(events))
    }

    internal fun hapticEventsForMerge(value: Int): Array<HapticEvent> {
        if (value <= 4) {
            // 2+2 → 4: punchy tap + tick
            return arrayOf(
                HapticEvent(HapticEventType.tap, intensity = 0.7),
                HapticEvent(HapticEventType.tick, intensity = 0.5, delay = 0.04)
            )
        } else if (value <= 8) {
            // 4+4 → 8: strong tap + thud kick
            return arrayOf(
                HapticEvent(HapticEventType.tap, intensity = 0.8),
                HapticEvent(HapticEventType.thud, intensity = 0.5, delay = 0.04)
            )
        } else if (value <= 16) {
            // 8+8 → 16: thud + tap snap
            return arrayOf(
                HapticEvent(HapticEventType.thud, intensity = 0.7),
                HapticEvent(HapticEventType.tap, intensity = 0.7, delay = 0.04)
            )
        } else if (value <= 32) {
            // 16+16 → 32: heavy thud + tap + tick
            return arrayOf(
                HapticEvent(HapticEventType.thud, intensity = 0.8),
                HapticEvent(HapticEventType.tap, intensity = 0.8, delay = 0.04),
                HapticEvent(HapticEventType.tick, intensity = 0.6, delay = 0.04)
            )
        } else if (value <= 64) {
            // 32+32 → 64: double thud + tap
            return arrayOf(
                HapticEvent(HapticEventType.thud, intensity = 0.9),
                HapticEvent(HapticEventType.thud, intensity = 0.7, delay = 0.05),
                HapticEvent(HapticEventType.tap, intensity = 0.8, delay = 0.04)
            )
        } else if (value <= 128) {
            // 64+64 → 128: triple hit
            return arrayOf(
                HapticEvent(HapticEventType.thud, intensity = 1.0),
                HapticEvent(HapticEventType.tap, intensity = 0.9, delay = 0.04),
                HapticEvent(HapticEventType.thud, intensity = 0.7, delay = 0.05),
                HapticEvent(HapticEventType.tick, intensity = 0.6, delay = 0.04)
            )
        } else if (value <= 256) {
            // 128+128 → 256: rising slam
            return arrayOf(
                HapticEvent(HapticEventType.rise, intensity = 0.9),
                HapticEvent(HapticEventType.thud, intensity = 1.0, delay = 0.06),
                HapticEvent(HapticEventType.tap, intensity = 0.9, delay = 0.04),
                HapticEvent(HapticEventType.thud, intensity = 0.8, delay = 0.05)
            )
        } else if (value <= 512) {
            // 256+256 → 512: earthquake
            return arrayOf(
                HapticEvent(HapticEventType.thud, intensity = 1.0),
                HapticEvent(HapticEventType.thud, intensity = 1.0, delay = 0.05),
                HapticEvent(HapticEventType.tap, intensity = 1.0, delay = 0.04),
                HapticEvent(HapticEventType.thud, intensity = 0.9, delay = 0.05),
                HapticEvent(HapticEventType.tick, intensity = 0.7, delay = 0.04)
            )
        } else if (value <= 1024) {
            // 512+512 → 1024: seismic cascade
            return arrayOf(
                HapticEvent(HapticEventType.thud, intensity = 1.0),
                HapticEvent(HapticEventType.thud, intensity = 1.0, delay = 0.04),
                HapticEvent(HapticEventType.thud, intensity = 1.0, delay = 0.04),
                HapticEvent(HapticEventType.tap, intensity = 1.0, delay = 0.04),
                HapticEvent(HapticEventType.rise, intensity = 1.0, delay = 0.05),
                HapticEvent(HapticEventType.thud, intensity = 1.0, delay = 0.06)
            )
        } else {
            // 2048+ continuing after win: absolute devastation
            return arrayOf(
                HapticEvent(HapticEventType.thud, intensity = 1.0),
                HapticEvent(HapticEventType.thud, intensity = 1.0, delay = 0.03),
                HapticEvent(HapticEventType.thud, intensity = 1.0, delay = 0.03),
                HapticEvent(HapticEventType.rise, intensity = 1.0, delay = 0.04),
                HapticEvent(HapticEventType.thud, intensity = 1.0, delay = 0.05),
                HapticEvent(HapticEventType.tap, intensity = 1.0, delay = 0.03),
                HapticEvent(HapticEventType.thud, intensity = 1.0, delay = 0.04)
            )
        }
    }

    internal fun playWinSong() {
        // Intense celebratory "haptic melody" for reaching 2048
        // Big opener → rapid fire build → massive slam → fireworks → grand finale
        val events: Array<HapticEvent> = arrayOf(
            HapticEvent(HapticEventType.thud, intensity = 1.0),
            HapticEvent(HapticEventType.thud, intensity = 1.0, delay = 0.06),
            HapticEvent(HapticEventType.thud, intensity = 1.0, delay = 0.06),
            HapticEvent(HapticEventType.tap, intensity = 0.7, delay = 0.1),
            HapticEvent(HapticEventType.tap, intensity = 0.8, delay = 0.07),
            HapticEvent(HapticEventType.tap, intensity = 0.9, delay = 0.07),
            HapticEvent(HapticEventType.tap, intensity = 1.0, delay = 0.07),
            HapticEvent(HapticEventType.rise, intensity = 1.0, delay = 0.08),
            HapticEvent(HapticEventType.thud, intensity = 1.0, delay = 0.1),
            HapticEvent(HapticEventType.thud, intensity = 1.0, delay = 0.05),
            HapticEvent(HapticEventType.tick, intensity = 1.0, delay = 0.12),
            HapticEvent(HapticEventType.tap, intensity = 0.9, delay = 0.05),
            HapticEvent(HapticEventType.tick, intensity = 1.0, delay = 0.05),
            HapticEvent(HapticEventType.tap, intensity = 0.8, delay = 0.05),
            HapticEvent(HapticEventType.tick, intensity = 1.0, delay = 0.05),
            HapticEvent(HapticEventType.tap, intensity = 1.0, delay = 0.05),
            HapticEvent(HapticEventType.rise, intensity = 1.0, delay = 0.1),
            HapticEvent(HapticEventType.thud, intensity = 1.0, delay = 0.12),
            HapticEvent(HapticEventType.thud, intensity = 1.0, delay = 0.06),
            HapticEvent(HapticEventType.fall, intensity = 1.0, delay = 0.1),
            HapticEvent(HapticEventType.thud, intensity = 1.0, delay = 0.15)
        )
        HapticFeedback.play(HapticPattern(events))
    }

    // MARK: - Score Box

    internal fun scoreBox(label: String, value: Int): View {
        return VStack(spacing = 2.0) { ->
            ComposeBuilder { composectx: ComposeContext ->
                Text(label)
                    .font(Font.caption2)
                    .fontWeight(Font.Weight.bold)
                    .foregroundStyle(theme.scoreBoxLabel).Compose(composectx)
                Text({
                    val str = LocalizedStringKey.StringInterpolation(literalCapacity = 0, interpolationCount = 0)
                    str.appendInterpolation(value)
                    LocalizedStringKey(stringInterpolation = str)
                }())
                    .font(Font.title3)
                    .fontWeight(Font.Weight.bold)
                    .foregroundStyle(theme.scoreBoxValue)
                    .monospaced().Compose(composectx)
                ComposeResult.ok
            }
        }
        .frame(minWidth = 80.0)
        .padding(Edge.Set.vertical, 8.0)
        .padding(Edge.Set.horizontal, 16.0)
        .background(RoundedRectangle(cornerRadius = 6.0)
            .fill(theme.scoreBoxBackground))
    }

    // MARK: - Tile

    internal fun tileView(value: Int, cellSize: Double): View {
        return ZStack { ->
            ComposeBuilder { composectx: ComposeContext ->
                RoundedRectangle(cornerRadius = tileCornerRadius)
                    .fill(if (value > 0) theme.tileColor(for_ = value) else Color.clear)
                    .frame(width = cellSize, height = cellSize).Compose(composectx)

                if (value > 0) {
                    Text({
                        val str = LocalizedStringKey.StringInterpolation(literalCapacity = 0, interpolationCount = 0)
                        str.appendInterpolation(value)
                        LocalizedStringKey(stringInterpolation = str)
                    }())
                        .font(Font.system(size = tileFontSize(for_ = value, cellSize = cellSize), weight = Font.Weight.bold, design = Font.Design.rounded))
                        .foregroundStyle(theme.tileForeground(for_ = value))
                        .minimumScaleFactor(0.5)
                        .lineLimit(1).Compose(composectx)
                }
                ComposeResult.ok
            }
        }
    }

    // MARK: - HUD

    internal val hudView: View
        get() {
            return HStack(spacing = 8.0) { ->
                ComposeBuilder { composectx: ComposeContext ->
                    Button(action = { -> dismiss() }) { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            Image("cancel", bundle = Bundle.module)
                                .font(Font.title2)
                                .foregroundStyle(theme.hudForeground).Compose(composectx)
                            ComposeResult.ok
                        }
                    }.Compose(composectx)

                    Spacer().Compose(composectx)

                    HStack(spacing = 0.0) { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            Text(LocalizedStringKey(stringLiteral = "2048"), bundle = Bundle.module)
                                .font(Font.title)
                                .fontWeight(Font.Weight.black)
                                .foregroundStyle(theme.hudForeground).Compose(composectx)
                            if (game.difficulty != TwentyFortyEightDifficulty.normal) {
                                Text({
                                    val str = LocalizedStringKey.StringInterpolation(literalCapacity = 0, interpolationCount = 0)
                                    str.appendLiteral(" (")
                                    str.appendInterpolation(game.difficulty.label)
                                    str.appendLiteral(")")
                                    LocalizedStringKey(stringInterpolation = str)
                                }())
                                    .font(Font.title3)
                                    .fontWeight(Font.Weight.bold)
                                    .foregroundStyle(game.difficulty.accentColor).Compose(composectx)
                            }
                            ComposeResult.ok
                        }
                    }.Compose(composectx)

                    Spacer().Compose(composectx)

                    if (game.difficulty.undoAllowed) {
                        Button(action = { ->
                            game.undo()
                            resetScales()
                            playHaptic(HapticPattern.snap)
                            game.saveState()
                        }) { ->
                            ComposeBuilder { composectx: ComposeContext ->
                                HStack(spacing = 2.0) { ->
                                    ComposeBuilder { composectx: ComposeContext ->
                                        Image("undo", bundle = Bundle.module)
                                            .font(Font.title2).Compose(composectx)
                                        Text({
                                            val str = LocalizedStringKey.StringInterpolation(literalCapacity = 0, interpolationCount = 0)
                                            str.appendInterpolation(game.undosRemaining)
                                            LocalizedStringKey(stringInterpolation = str)
                                        }())
                                            .font(Font.caption2)
                                            .fontWeight(Font.Weight.bold).Compose(composectx)
                                        ComposeResult.ok
                                    }
                                }
                                .foregroundStyle(if (game.hasUndo && game.undosRemaining > 0) theme.hudForeground else theme.hudForeground.opacity(0.3)).Compose(composectx)
                                ComposeResult.ok
                            }
                        }
                        .disabled(!game.hasUndo || game.undosRemaining <= 0).Compose(composectx)
                    }

                    Button(action = { -> showPauseMenu = true }) { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            Image("pause_circle", bundle = Bundle.module)
                                .font(Font.title2)
                                .foregroundStyle(theme.hudForeground).Compose(composectx)
                            ComposeResult.ok
                        }
                    }.Compose(composectx)
                    ComposeResult.ok
                }
            }
            .padding(Edge.Set.horizontal, 12.0)
            .padding(Edge.Set.vertical, 6.0)
            .background(theme.hudBackground)
        }

    // MARK: - Win Overlay

    internal fun winOverlay(boardSize: Double): View {
        return ZStack { ->
            ComposeBuilder { composectx: ComposeContext ->
                RoundedRectangle(cornerRadius = 8.0)
                    .fill(theme.tile2048.opacity(0.65))
                    .frame(width = boardSize, height = boardSize).Compose(composectx)

                VStack(spacing = 16.0) { ->
                    ComposeBuilder { composectx: ComposeContext ->
                        Text(LocalizedStringKey(stringLiteral = "You Win!"), bundle = Bundle.module)
                            .font(Font.largeTitle)
                            .fontWeight(Font.Weight.black)
                            .foregroundStyle(Color.white).Compose(composectx)

                        Text({
                            val str = LocalizedStringKey.StringInterpolation(literalCapacity = 0, interpolationCount = 0)
                            str.appendLiteral("Score: ")
                            str.appendInterpolation(displayedScore)
                            LocalizedStringKey(stringInterpolation = str)
                        }())
                            .font(Font.title2)
                            .fontWeight(Font.Weight.bold)
                            .foregroundStyle(Color.white)
                            .monospaced().Compose(composectx)

                        Button(action = { -> game.continueGame() }) { ->
                            ComposeBuilder { composectx: ComposeContext ->
                                Text(LocalizedStringKey(stringLiteral = "Keep Going"), bundle = Bundle.module)
                                    .font(Font.headline)
                                    .fontWeight(Font.Weight.bold)
                                    .foregroundStyle(Color.white)
                                    .frame(width = 160.0, height = 44.0)
                                    .background(RoundedRectangle(cornerRadius = 8.0)
                                        .fill(theme.boardBackground)).Compose(composectx)
                                ComposeResult.ok
                            }
                        }
                        .buttonStyle(ButtonStyle.plain).Compose(composectx)

                        Button(action = { -> showDifficultyPicker = true }) { ->
                            ComposeBuilder { composectx: ComposeContext ->
                                Text(LocalizedStringKey(stringLiteral = "New Game"), bundle = Bundle.module)
                                    .font(Font.headline)
                                    .fontWeight(Font.Weight.bold)
                                    .foregroundStyle(Color.white)
                                    .frame(width = 160.0, height = 44.0)
                                    .background(RoundedRectangle(cornerRadius = 8.0)
                                        .fill(theme.boardBackground)).Compose(composectx)
                                ComposeResult.ok
                            }
                        }
                        .buttonStyle(ButtonStyle.plain).Compose(composectx)
                        ComposeResult.ok
                    }
                }.Compose(composectx)
                ComposeResult.ok
            }
        }
    }

    // MARK: - Game Over Overlay

    internal fun gameOverOverlay(boardSize: Double): View {
        return ZStack { ->
            ComposeBuilder { composectx: ComposeContext ->
                RoundedRectangle(cornerRadius = 8.0)
                    .fill(theme.background.opacity(0.85))
                    .frame(width = boardSize, height = boardSize).Compose(composectx)

                VStack(spacing = 16.0) { ->
                    ComposeBuilder { composectx: ComposeContext ->
                        Text(LocalizedStringKey(stringLiteral = "Game Over!"), bundle = Bundle.module)
                            .font(Font.largeTitle)
                            .fontWeight(Font.Weight.black)
                            .foregroundStyle(theme.hudForeground).Compose(composectx)

                        VStack(spacing = 4.0) { ->
                            ComposeBuilder { composectx: ComposeContext ->
                                Text(LocalizedStringKey(stringLiteral = "Score"), bundle = Bundle.module)
                                    .font(Font.headline)
                                    .foregroundStyle(theme.hudForeground.opacity(0.7)).Compose(composectx)
                                Text({
                                    val str = LocalizedStringKey.StringInterpolation(literalCapacity = 0, interpolationCount = 0)
                                    str.appendInterpolation(displayedScore)
                                    LocalizedStringKey(stringInterpolation = str)
                                }())
                                    .font(Font.system(size = 44.0))
                                    .fontWeight(Font.Weight.bold)
                                    .foregroundStyle(theme.hudForeground)
                                    .monospaced().Compose(composectx)
                                ComposeResult.ok
                            }
                        }.Compose(composectx)

                        if (game.score >= game.highScore && game.score > 0) {
                            Text(LocalizedStringKey(stringLiteral = "New High Score!"), bundle = Bundle.module)
                                .font(Font.title3)
                                .fontWeight(Font.Weight.bold)
                                .foregroundStyle(theme.tile64).Compose(composectx)
                        }

                        Button(action = { -> showDifficultyPicker = true }) { ->
                            ComposeBuilder { composectx: ComposeContext ->
                                Text(LocalizedStringKey(stringLiteral = "Try Again"), bundle = Bundle.module)
                                    .font(Font.headline)
                                    .fontWeight(Font.Weight.bold)
                                    .foregroundStyle(Color.white)
                                    .frame(width = 160.0, height = 44.0)
                                    .background(RoundedRectangle(cornerRadius = 8.0)
                                        .fill(theme.boardBackground)).Compose(composectx)
                                ComposeResult.ok
                            }
                        }
                        .buttonStyle(ButtonStyle.plain).Compose(composectx)

                        ShareLink(item = "I scored ${game.score} in 2048 on Faire Games! Can you beat it?\nhttps://appfair.net", subject = Text(LocalizedStringKey(stringLiteral = "2048 Score"), bundle = Bundle.module), message = Text({
                            val str = LocalizedStringKey.StringInterpolation(literalCapacity = 0, interpolationCount = 0)
                            str.appendLiteral("I scored ")
                            str.appendInterpolation(game.score)
                            str.appendLiteral(" in 2048!")
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
                                .foregroundStyle(theme.hudForeground.opacity(0.7)).Compose(composectx)
                                ComposeResult.ok
                            }
                        }.Compose(composectx)
                        ComposeResult.ok
                    }
                }.Compose(composectx)
                ComposeResult.ok
            }
        }
    }

    private constructor(showInstructions: Binding<Boolean>, game: TwentyFortyEightModel = TwentyFortyEightModel(), showSettings: Boolean = false, showPauseMenu: Boolean = false, showDifficultyPicker: Boolean = false, hasInitialized: Boolean = false, tileScales: Array<Double> = Array(repeating = 1.0, count = gridSize * gridSize), animTimer: Timer? = null, displayedScore: Int = 0, displayedHighScore: Int = 0, scoreAnimTimer: Timer? = null, dragDirection: Direction? = null, dragPreview: TwentyFortyEightMovePreview? = null, tileOffsetX: Array<Double> = Array(repeating = 0.0, count = gridSize * gridSize), tileOffsetY: Array<Double> = Array(repeating = 0.0, count = gridSize * gridSize), tileMaxAxisOffset: Array<Double> = Array(repeating = 0.0, count = gridSize * gridSize), tileMergeGlow: Array<Double> = Array(repeating = 0.0, count = gridSize * gridSize), tileDestGlow: Array<Double> = Array(repeating = 0.0, count = gridSize * gridSize), measuredCellSize: Double = 0.0, measuredStepSize: Double = 0.0, dragRejected: Boolean = false, privatep: Nothing? = null) {
        this._showInstructions = showInstructions
        this._game = skip.ui.State(game)
        this._showSettings = skip.ui.State(showSettings)
        this._showPauseMenu = skip.ui.State(showPauseMenu)
        this._showDifficultyPicker = skip.ui.State(showDifficultyPicker)
        this._hasInitialized = skip.ui.State(hasInitialized)
        this._tileScales = skip.ui.State(tileScales.sref())
        this._animTimer = skip.ui.State(animTimer)
        this._displayedScore = skip.ui.State(displayedScore)
        this._displayedHighScore = skip.ui.State(displayedHighScore)
        this._scoreAnimTimer = skip.ui.State(scoreAnimTimer)
        this._dragDirection = skip.ui.State(dragDirection)
        this._dragPreview = skip.ui.State(dragPreview)
        this._tileOffsetX = skip.ui.State(tileOffsetX.sref())
        this._tileOffsetY = skip.ui.State(tileOffsetY.sref())
        this._tileMaxAxisOffset = skip.ui.State(tileMaxAxisOffset.sref())
        this._tileMergeGlow = skip.ui.State(tileMergeGlow.sref())
        this._tileDestGlow = skip.ui.State(tileDestGlow.sref())
        this._measuredCellSize = skip.ui.State(measuredCellSize)
        this._measuredStepSize = skip.ui.State(measuredStepSize)
        this._dragRejected = skip.ui.State(dragRejected)
    }

    constructor(showInstructions: Binding<Boolean>): this(showInstructions = showInstructions, privatep = null) {
    }
}

// MARK: - Preview Icon

class TwentyFortyEightPreviewIcon: View {
    constructor() {
    }

    override fun body(): View {
        return ComposeBuilder { composectx: ComposeContext ->
            ZStack { ->
                ComposeBuilder { composectx: ComposeContext ->
                    // Warm background matching the game
                    Color(red = 0.98, green = 0.97, blue = 0.94).Compose(composectx)

                    // Board background
                    RoundedRectangle(cornerRadius = 4.0)
                        .fill(Color(red = 0.47, green = 0.43, blue = 0.40))
                        .padding(10.0).Compose(composectx)

                    // Mini grid
                    VStack(spacing = 3.0) { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            ForEach(0..<4, id = { it }) { r ->
                                ComposeBuilder { composectx: ComposeContext ->
                                    HStack(spacing = 3.0) { ->
                                        ComposeBuilder { composectx: ComposeContext ->
                                            ForEach(0..<4, id = { it }) { c ->
                                                ComposeBuilder { composectx: ComposeContext ->
                                                    miniTile(row = r, col = c).Compose(composectx)
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
                    .padding(14.0).Compose(composectx)
                    ComposeResult.ok
                }
            }
            .clipShape(RoundedRectangle(cornerRadius = 12.0)).Compose(composectx)
        }
    }

    internal fun miniTile(row: Int, col: Int): View {
        // Show a static arrangement suggesting a 2048 game
        val values = arrayOf(
            arrayOf(2, 4, 8, 16),
            arrayOf(0, 2, 4, 32),
            arrayOf(0, 0, 2, 64),
            arrayOf(0, 0, 0, 128)
        )
        val val_ = values[row][col]
        return RoundedRectangle(cornerRadius = 2.0)
            .fill(tileColor(for_ = val_))
            .frame(width = 18.0, height = 18.0)
    }

    @androidx.annotation.Keep
    companion object {
    }
}

// MARK: - Difficulty Picker

internal class TwentyFortyEightDifficultyPickerView: View {
    internal val theme: TwentyFortyEightTheme
    internal val onSelect: (TwentyFortyEightDifficulty) -> Unit
    internal lateinit var dismiss: DismissAction

    override fun body(): View {
        return ComposeBuilder { composectx: ComposeContext ->
            NavigationStack { ->
                ComposeBuilder { composectx: ComposeContext ->
                    ScrollView { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            VStack(spacing = 14.0) { ->
                                ComposeBuilder { composectx: ComposeContext ->
                                    Text(LocalizedStringKey(stringLiteral = "Choose Difficulty"), bundle = Bundle.module)
                                        .font(Font.title2)
                                        .fontWeight(Font.Weight.bold)
                                        .foregroundStyle(theme.hudForeground)
                                        .padding(Edge.Set.top, 10.0).Compose(composectx)

                                    ForEach(arrayOf(TwentyFortyEightDifficulty.easy, TwentyFortyEightDifficulty.normal, TwentyFortyEightDifficulty.hard), id = { it.rawValue }) { d ->
                                        ComposeBuilder { composectx: ComposeContext ->
                                            Button(action = { ->
                                                onSelect(d)
                                                dismiss()
                                            }) { ->
                                                ComposeBuilder { composectx: ComposeContext ->
                                                    HStack { ->
                                                        ComposeBuilder { composectx: ComposeContext ->
                                                            VStack(alignment = HorizontalAlignment.leading, spacing = 4.0) { ->
                                                                ComposeBuilder { composectx: ComposeContext ->
                                                                    Text(d.label)
                                                                        .font(Font.title3)
                                                                        .fontWeight(Font.Weight.bold)
                                                                        .foregroundStyle(theme.hudForeground).Compose(composectx)
                                                                    Text(d.description)
                                                                        .font(Font.caption)
                                                                        .foregroundStyle(theme.hudForeground.opacity(0.75)).Compose(composectx)
                                                                    ComposeResult.ok
                                                                }
                                                            }.Compose(composectx)
                                                            Spacer().Compose(composectx)
                                                            ComposeResult.ok
                                                        }
                                                    }
                                                    .padding(16.0)
                                                    .background(d.accentColor.opacity(0.12))
                                                    .cornerRadius(14.0)
                                                    .padding(1.0)
                                                    .background(d.accentColor.opacity(0.4))
                                                    .cornerRadius(15.0).Compose(composectx)
                                                    ComposeResult.ok
                                                }
                                            }
                                            .buttonStyle(ButtonStyle.plain).Compose(composectx)
                                            ComposeResult.ok
                                        }
                                    }.Compose(composectx)
                                    ComposeResult.ok
                                }
                            }
                            .padding(Edge.Set.horizontal, 20.0)
                            .padding(Edge.Set.bottom, 24.0).Compose(composectx)
                            ComposeResult.ok
                        }
                    }
                    .frame(maxWidth = Double.infinity, maxHeight = Double.infinity)
                    .background(theme.background.ignoresSafeArea())
                    .navigationTitle(Text(LocalizedStringKey(stringLiteral = "New Game"), bundle = Bundle.module))
                    .toolbar { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            ToolbarItem(placement = ToolbarItemPlacement.cancellationAction) { ->
                                ComposeBuilder { composectx: ComposeContext ->
                                    Button(action = { -> dismiss() }) { ->
                                        ComposeBuilder { composectx: ComposeContext ->
                                            Text(LocalizedStringKey(stringLiteral = "Cancel"), bundle = Bundle.module).Compose(composectx)
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
            .preferredColorScheme(if (theme.isDark) ColorScheme.dark else ColorScheme.light).Compose(composectx)
        }
    }

    @Composable
    override fun Evaluate(context: ComposeContext, options: Int): kotlin.collections.List<Renderable> {
        this.dismiss = EnvironmentValues.shared.dismiss

        return super.Evaluate(context, options)
    }

    constructor(theme: TwentyFortyEightTheme, onSelect: (TwentyFortyEightDifficulty) -> Unit) {
        this.theme = theme
        this.onSelect = onSelect
    }
}

// MARK: - Settings

internal class TwentyFortyEightSettingsView: View {
    internal var settings: TwentyFortyEightSettings
        get() = _settings.wrappedValue
        set(newValue) {
            _settings.wrappedValue = newValue
        }
    internal var _settings: skip.ui.Bindable<TwentyFortyEightSettings>
    internal lateinit var dismiss: DismissAction

    override fun body(): View {
        return ComposeBuilder { composectx: ComposeContext ->
            NavigationStack { ->
                ComposeBuilder { composectx: ComposeContext ->
                    Form { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            Section(header = Text(LocalizedStringKey(stringLiteral = "2048"), bundle = Bundle.module)) { ->
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
                            Section(header = Text(LocalizedStringKey(stringLiteral = "Theme"), bundle = Bundle.module)) { ->
                                ComposeBuilder { composectx: ComposeContext ->
                                    ForEach(TwentyFortyEightTheme.all, id = { it.id }) { t ->
                                        ComposeBuilder { composectx: ComposeContext ->
                                            TwentyFortyEightThemeRow(theme = t, isSelected = t.id == settings.themeID, onTap = { -> settings.themeID = t.id }).Compose(composectx)
                                            ComposeResult.ok
                                        }
                                    }.Compose(composectx)
                                    ComposeResult.ok
                                }
                            }.Compose(composectx)
                            Section(header = Text(LocalizedStringKey(stringLiteral = "Data"), bundle = Bundle.module)) { ->
                                ComposeBuilder { composectx: ComposeContext ->
                                    Button(role = ButtonRole.destructive, action = { -> resetTwentyFortyEightHighScore() }) { ->
                                        ComposeBuilder { composectx: ComposeContext ->
                                            Text(LocalizedStringKey(stringLiteral = "Reset High Score"), bundle = Bundle.module).Compose(composectx)
                                            ComposeResult.ok
                                        }
                                    }.Compose(composectx)
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

    constructor(settings: TwentyFortyEightSettings) {
        this._settings = skip.ui.Bindable(settings)
    }
}

/// A single row in the theme picker showing the localized name and a
/// palette-preview made of mini "tile" swatches. The whole row is tappable.
internal class TwentyFortyEightThemeRow: View {
    internal val theme: TwentyFortyEightTheme
    internal val isSelected: Boolean
    internal val onTap: () -> Unit

    override fun body(): View {
        return ComposeBuilder { composectx: ComposeContext ->
            Button(action = onTap) { ->
                ComposeBuilder { composectx: ComposeContext ->
                    HStack(spacing = 12.0) { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            ThemePalettePreview(theme = theme).Compose(composectx)
                            VStack(alignment = HorizontalAlignment.leading, spacing = 2.0) { ->
                                ComposeBuilder { composectx: ComposeContext ->
                                    theme.nameText()
                                        .font(Font.body)
                                        .fontWeight(Font.Weight.semibold)
                                        .foregroundStyle(Color.primary).Compose(composectx)
                                    if (theme.isDark) {
                                        Text(LocalizedStringKey(stringLiteral = "Dark"), bundle = Bundle.module, comment = "Subtitle under a dark-mode theme name in the 2048 theme picker")
                                            .font(Font.caption)
                                            .foregroundStyle(Color.secondary).Compose(composectx)
                                    } else {
                                        Text(LocalizedStringKey(stringLiteral = "Light"), bundle = Bundle.module, comment = "Subtitle under a light-mode theme name in the 2048 theme picker")
                                            .font(Font.caption)
                                            .foregroundStyle(Color.secondary).Compose(composectx)
                                    }
                                    ComposeResult.ok
                                }
                            }.Compose(composectx)
                            Spacer().Compose(composectx)
                            if (isSelected) {
                                Image(systemName = "checkmark.circle.fill")
                                    .font(Font.title3)
                                    .foregroundStyle(TintShapeStyle.tint).Compose(composectx)
                            }
                            ComposeResult.ok
                        }
                    }.Compose(composectx)
                    ComposeResult.ok
                }
            }
            .buttonStyle(ButtonStyle.plain)
            .accessibilityIdentifier("theme.row.${theme.id}").Compose(composectx)
        }
    }

    constructor(theme: TwentyFortyEightTheme, isSelected: Boolean, onTap: () -> Unit) {
        this.theme = theme
        this.isSelected = isSelected
        this.onTap = onTap
    }
}

/// Mini board preview shown next to a theme's name in the picker. Renders the
/// theme's board background framing a 2×3 grid of tile swatches drawn from the
/// theme's actual tile colors so the user sees the real palette they'll get.
internal class ThemePalettePreview: View {
    internal val theme: TwentyFortyEightTheme

    override fun body(): View {
        return ComposeBuilder { composectx: ComposeContext ->
            val swatches = theme.previewSwatches.sref()
            ZStack { ->
                ComposeBuilder { composectx: ComposeContext ->
                    RoundedRectangle(cornerRadius = 6.0)
                        .fill(theme.boardBackground).Compose(composectx)
                    VStack(spacing = 2.0) { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            HStack(spacing = 2.0) { ->
                                ComposeBuilder { composectx: ComposeContext ->
                                    swatch(swatches[0]).Compose(composectx)
                                    swatch(swatches[1]).Compose(composectx)
                                    swatch(swatches[2]).Compose(composectx)
                                    ComposeResult.ok
                                }
                            }.Compose(composectx)
                            HStack(spacing = 2.0) { ->
                                ComposeBuilder { composectx: ComposeContext ->
                                    swatch(swatches[3]).Compose(composectx)
                                    swatch(swatches[4]).Compose(composectx)
                                    swatch(theme.tileBeyondColor).Compose(composectx)
                                    ComposeResult.ok
                                }
                            }.Compose(composectx)
                            ComposeResult.ok
                        }
                    }
                    .padding(4.0).Compose(composectx)
                    ComposeResult.ok
                }
            }
            .frame(width = 56.0, height = 40.0).Compose(composectx)
            ComposeResult.ok
        }
    }

    private fun swatch(color: Color): View {
        return RoundedRectangle(cornerRadius = 2.0)
            .fill(color)
            .frame(maxWidth = Double.infinity, maxHeight = Double.infinity)
    }

    constructor(theme: TwentyFortyEightTheme) {
        this.theme = theme
    }
}

@Stable
open class TwentyFortyEightSettings: Observable {
    open var vibrations: Boolean
        get() = _vibrations.wrappedValue
        set(newValue) {
            _vibrations.wrappedValue = newValue
            defaults.set(vibrations, forKey = "twentyfortyeightVibrations")
        }
    var _vibrations: skip.model.Observed<Boolean> = skip.model.Observed(defaults.value(forKey = "twentyfortyeightVibrations", default = true))

    open var themeID: String
        get() = _themeID.wrappedValue
        set(newValue) {
            _themeID.wrappedValue = newValue
            defaults.set(themeID, forKey = "twentyfortyeightThemeID")
        }
    var _themeID: skip.model.Observed<String> = skip.model.Observed(defaults.value(forKey = "twentyfortyeightThemeID", default = TwentyFortyEightTheme.classic.id))

    open val theme: TwentyFortyEightTheme
        get() = TwentyFortyEightTheme.theme(forID = themeID)

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
