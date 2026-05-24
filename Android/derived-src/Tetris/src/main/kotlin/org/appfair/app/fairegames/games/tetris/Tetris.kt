// Licensed under the GNU General Public License v2.0 or later
// SPDX-License-Identifier: GPL-2.0-or-later

package org.appfair.app.fairegames.games.tetris

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

class TetrisContainerView: View {
    private var settings: TetrisSettings
        get() = _settings.wrappedValue
        set(newValue) {
            _settings.wrappedValue = newValue
        }
    private var _settings: skip.ui.State<TetrisSettings> = skip.ui.State(TetrisSettings())
    private var showInstructions: Boolean
        get() = _showInstructions.wrappedValue
        set(newValue) {
            _showInstructions.wrappedValue = newValue
        }
    private var _showInstructions: skip.ui.State<Boolean> = skip.ui.State(false)
    private val instructionsConfig = GameInstructionsConfig(key = "Tetris.instructions", bundle = Bundle.module, firstLaunchKey = "instructionsShown_Tetris", title = "Sirtet")

    constructor() {
    }

    override fun body(): View {
        return ComposeBuilder { composectx: ComposeContext ->
            TetrisGameView(showInstructions = Binding({ _showInstructions.wrappedValue }, { it -> _showInstructions.wrappedValue = it }))
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
        val rememberedsettings by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<TetrisSettings>, Any>) { mutableStateOf(_settings) }
        _settings = rememberedsettings

        val rememberedshowInstructions by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Boolean>, Any>) { mutableStateOf(_showInstructions) }
        _showInstructions = rememberedshowInstructions

        return super.Evaluate(context, options)
    }

    @androidx.annotation.Keep
    companion object {
    }
}

// MARK: - Cell Position

/// A row/col coordinate on the board. Uses `final class` for reliable transpilation.
internal class CellPos {
    internal val r: Int
    internal val c: Int
    internal constructor(r: Int, c: Int) {
        this.r = r
        this.c = c
    }
}

// MARK: - Tetromino Definitions

/// The seven standard Tetris tetrominoes.
/// Raw values 0–6 are stored in the board grid to track placed block colors.
@androidx.annotation.Keep
internal enum class TetrominoKind(override val rawValue: Int, @Suppress("UNUSED_PARAMETER") unusedp: Nothing? = null): CaseIterable, RawRepresentable<Int> {
    i(0),
    o(1),
    t(2),
    s(3),
    z(4),
    j(5),
    l(6);

    private fun rawRotations(): Array<Array<CellPos>> {
        when (this) {
            TetrominoKind.i -> return arrayOf(
                arrayOf(CellPos(0, -1), CellPos(0, 0), CellPos(0, 1), CellPos(0, 2)),
                arrayOf(CellPos(-1, 0), CellPos(0, 0), CellPos(1, 0), CellPos(2, 0)),
                arrayOf(CellPos(0, -1), CellPos(0, 0), CellPos(0, 1), CellPos(0, 2)),
                arrayOf(CellPos(-1, 0), CellPos(0, 0), CellPos(1, 0), CellPos(2, 0))
            )
            TetrominoKind.o -> return arrayOf(
                arrayOf(CellPos(0, 0), CellPos(0, 1), CellPos(1, 0), CellPos(1, 1)),
                arrayOf(CellPos(0, 0), CellPos(0, 1), CellPos(1, 0), CellPos(1, 1)),
                arrayOf(CellPos(0, 0), CellPos(0, 1), CellPos(1, 0), CellPos(1, 1)),
                arrayOf(CellPos(0, 0), CellPos(0, 1), CellPos(1, 0), CellPos(1, 1))
            )
            TetrominoKind.t -> return arrayOf(
                arrayOf(CellPos(0, -1), CellPos(0, 0), CellPos(0, 1), CellPos(-1, 0)),
                arrayOf(CellPos(-1, 0), CellPos(0, 0), CellPos(1, 0), CellPos(0, 1)),
                arrayOf(CellPos(0, -1), CellPos(0, 0), CellPos(0, 1), CellPos(1, 0)),
                arrayOf(CellPos(-1, 0), CellPos(0, 0), CellPos(1, 0), CellPos(0, -1))
            )
            TetrominoKind.s -> return arrayOf(
                arrayOf(CellPos(0, -1), CellPos(0, 0), CellPos(-1, 0), CellPos(-1, 1)),
                arrayOf(CellPos(-1, 0), CellPos(0, 0), CellPos(0, 1), CellPos(1, 1)),
                arrayOf(CellPos(0, -1), CellPos(0, 0), CellPos(-1, 0), CellPos(-1, 1)),
                arrayOf(CellPos(-1, 0), CellPos(0, 0), CellPos(0, 1), CellPos(1, 1))
            )
            TetrominoKind.z -> return arrayOf(
                arrayOf(CellPos(-1, -1), CellPos(-1, 0), CellPos(0, 0), CellPos(0, 1)),
                arrayOf(CellPos(0, 0), CellPos(1, 0), CellPos(0, 1), CellPos(-1, 1)),
                arrayOf(CellPos(-1, -1), CellPos(-1, 0), CellPos(0, 0), CellPos(0, 1)),
                arrayOf(CellPos(0, 0), CellPos(1, 0), CellPos(0, 1), CellPos(-1, 1))
            )
            TetrominoKind.j -> return arrayOf(
                arrayOf(CellPos(-1, -1), CellPos(0, -1), CellPos(0, 0), CellPos(0, 1)),
                arrayOf(CellPos(-1, 0), CellPos(0, 0), CellPos(1, 0), CellPos(-1, 1)),
                arrayOf(CellPos(0, -1), CellPos(0, 0), CellPos(0, 1), CellPos(1, 1)),
                arrayOf(CellPos(1, 0), CellPos(0, 0), CellPos(-1, 0), CellPos(1, -1))
            )
            TetrominoKind.l -> return arrayOf(
                arrayOf(CellPos(0, -1), CellPos(0, 0), CellPos(0, 1), CellPos(-1, 1)),
                arrayOf(CellPos(-1, 0), CellPos(0, 0), CellPos(1, 0), CellPos(1, 1)),
                arrayOf(CellPos(0, -1), CellPos(0, 0), CellPos(0, 1), CellPos(1, -1)),
                arrayOf(CellPos(-1, -1), CellPos(-1, 0), CellPos(0, 0), CellPos(1, 0))
            )
        }
    }

    internal fun offsets(rotation: Int): Array<CellPos> = TetrominoKind.rotationTable[this.rawValue][rotation].sref()

    // Bright face color
    internal val color: Color
        get() {
            when (this) {
                TetrominoKind.i -> return Color(red = 0.2, green = 0.88, blue = 0.92)
                TetrominoKind.o -> return Color(red = 0.96, green = 0.88, blue = 0.28)
                TetrominoKind.t -> return Color(red = 0.72, green = 0.34, blue = 0.85)
                TetrominoKind.s -> return Color(red = 0.3, green = 0.88, blue = 0.42)
                TetrominoKind.z -> return Color(red = 0.92, green = 0.28, blue = 0.28)
                TetrominoKind.j -> return Color(red = 0.3, green = 0.45, blue = 0.92)
                TetrominoKind.l -> return Color(red = 0.96, green = 0.6, blue = 0.2)
            }
        }

    // Light highlight color (top/left edges for 3D bevel)
    internal val highlightColor: Color
        get() {
            when (this) {
                TetrominoKind.i -> return Color(red = 0.55, green = 0.96, blue = 0.98)
                TetrominoKind.o -> return Color(red = 0.99, green = 0.96, blue = 0.6)
                TetrominoKind.t -> return Color(red = 0.86, green = 0.6, blue = 0.95)
                TetrominoKind.s -> return Color(red = 0.6, green = 0.96, blue = 0.65)
                TetrominoKind.z -> return Color(red = 0.97, green = 0.55, blue = 0.55)
                TetrominoKind.j -> return Color(red = 0.55, green = 0.65, blue = 0.97)
                TetrominoKind.l -> return Color(red = 0.99, green = 0.78, blue = 0.48)
            }
        }

    // Dark shadow color (bottom/right edges for 3D bevel)
    internal val shadowColor: Color
        get() {
            when (this) {
                TetrominoKind.i -> return Color(red = 0.05, green = 0.55, blue = 0.58)
                TetrominoKind.o -> return Color(red = 0.6, green = 0.52, blue = 0.08)
                TetrominoKind.t -> return Color(red = 0.4, green = 0.12, blue = 0.5)
                TetrominoKind.s -> return Color(red = 0.08, green = 0.5, blue = 0.15)
                TetrominoKind.z -> return Color(red = 0.55, green = 0.1, blue = 0.1)
                TetrominoKind.j -> return Color(red = 0.1, green = 0.18, blue = 0.55)
                TetrominoKind.l -> return Color(red = 0.6, green = 0.32, blue = 0.06)
            }
        }

    @androidx.annotation.Keep
    companion object: CaseIterableCompanion<TetrominoKind> {

        /// Cell offsets for each of the four rotations.
        internal val rotationTable: Array<Array<Array<CellPos>>> = buildRotationTable()

        private fun buildRotationTable(): Array<Array<Array<CellPos>>> {
            var table: Array<Array<Array<CellPos>>> = arrayOf()
            for (kind in TetrominoKind.allCases.sref()) {
                table.append(kind.rawRotations())
            }
            return table.sref()
        }

        internal fun colorForRaw(raw: Int): Color = (TetrominoKind(rawValue = raw) ?: TetrominoKind.t).color

        internal fun highlightForRaw(raw: Int): Color = (TetrominoKind(rawValue = raw) ?: TetrominoKind.t).highlightColor

        internal fun shadowForRaw(raw: Int): Color = (TetrominoKind(rawValue = raw) ?: TetrominoKind.t).shadowColor

        fun init(rawValue: Int): TetrominoKind? {
            return when (rawValue) {
                0 -> TetrominoKind.i
                1 -> TetrominoKind.o
                2 -> TetrominoKind.t
                3 -> TetrominoKind.s
                4 -> TetrominoKind.z
                5 -> TetrominoKind.j
                6 -> TetrominoKind.l
                else -> null
            }
        }

        override val allCases: Array<TetrominoKind>
            get() = arrayOf(i, o, t, s, z, j, l)
    }
}

internal fun TetrominoKind(rawValue: Int): TetrominoKind? = TetrominoKind.init(rawValue = rawValue)

// MARK: - Saved State

@androidx.annotation.Keep
@Suppress("MUST_BE_INITIALIZED")
internal class TetrisSavedState: Codable, MutableStruct {
    internal var grid: Array<Array<Int>>
        get() = field.sref({ this.grid = it })
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
            willmutate()
            field = newValue
            didmutate()
        }
    internal var currentKindRaw: Int
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    internal var currentRotation: Int
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    internal var currentRow: Int
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    internal var currentCol: Int
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    internal var nextPieceRaws: Array<Int>
        get() = field.sref({ this.nextPieceRaws = it })
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
    internal var level: Int
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    internal var totalLinesCleared: Int
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

    constructor(grid: Array<Array<Int>>, currentKindRaw: Int, currentRotation: Int, currentRow: Int, currentCol: Int, nextPieceRaws: Array<Int>, score: Int, level: Int, totalLinesCleared: Int, isGameOver: Boolean) {
        this.grid = grid
        this.currentKindRaw = currentKindRaw
        this.currentRotation = currentRotation
        this.currentRow = currentRow
        this.currentCol = currentCol
        this.nextPieceRaws = nextPieceRaws
        this.score = score
        this.level = level
        this.totalLinesCleared = totalLinesCleared
        this.isGameOver = isGameOver
    }

    override var supdate: ((Any) -> Unit)? = null
    override var smutatingcount = 0
    override fun scopy(): MutableStruct = TetrisSavedState(grid, currentKindRaw, currentRotation, currentRow, currentCol, nextPieceRaws, score, level, totalLinesCleared, isGameOver)

    private enum class CodingKeys(override val rawValue: String, @Suppress("UNUSED_PARAMETER") unusedp: Nothing? = null): CodingKey, RawRepresentable<String> {
        grid("grid"),
        currentKindRaw("currentKindRaw"),
        currentRotation("currentRotation"),
        currentRow("currentRow"),
        currentCol("currentCol"),
        nextPieceRaws("nextPieceRaws"),
        score("score"),
        level("level"),
        totalLinesCleared("totalLinesCleared"),
        isGameOver("isGameOver");

        @androidx.annotation.Keep
        companion object {
            fun init(rawValue: String): CodingKeys? {
                return when (rawValue) {
                    "grid" -> CodingKeys.grid
                    "currentKindRaw" -> CodingKeys.currentKindRaw
                    "currentRotation" -> CodingKeys.currentRotation
                    "currentRow" -> CodingKeys.currentRow
                    "currentCol" -> CodingKeys.currentCol
                    "nextPieceRaws" -> CodingKeys.nextPieceRaws
                    "score" -> CodingKeys.score
                    "level" -> CodingKeys.level
                    "totalLinesCleared" -> CodingKeys.totalLinesCleared
                    "isGameOver" -> CodingKeys.isGameOver
                    else -> null
                }
            }
        }
    }

    override fun encode(to: Encoder) {
        val container = to.container(keyedBy = CodingKeys::class)
        container.encode(grid, forKey = CodingKeys.grid)
        container.encode(currentKindRaw, forKey = CodingKeys.currentKindRaw)
        container.encode(currentRotation, forKey = CodingKeys.currentRotation)
        container.encode(currentRow, forKey = CodingKeys.currentRow)
        container.encode(currentCol, forKey = CodingKeys.currentCol)
        container.encode(nextPieceRaws, forKey = CodingKeys.nextPieceRaws)
        container.encode(score, forKey = CodingKeys.score)
        container.encode(level, forKey = CodingKeys.level)
        container.encode(totalLinesCleared, forKey = CodingKeys.totalLinesCleared)
        container.encode(isGameOver, forKey = CodingKeys.isGameOver)
    }

    constructor(from: Decoder) {
        val container = from.container(keyedBy = CodingKeys::class)
        this.grid = container.decode(Array::class, elementType = Array::class, nestedElementType = Int::class, forKey = CodingKeys.grid)
        this.currentKindRaw = container.decode(Int::class, forKey = CodingKeys.currentKindRaw)
        this.currentRotation = container.decode(Int::class, forKey = CodingKeys.currentRotation)
        this.currentRow = container.decode(Int::class, forKey = CodingKeys.currentRow)
        this.currentCol = container.decode(Int::class, forKey = CodingKeys.currentCol)
        this.nextPieceRaws = container.decode(Array::class, elementType = Int::class, forKey = CodingKeys.nextPieceRaws)
        this.score = container.decode(Int::class, forKey = CodingKeys.score)
        this.level = container.decode(Int::class, forKey = CodingKeys.level)
        this.totalLinesCleared = container.decode(Int::class, forKey = CodingKeys.totalLinesCleared)
        this.isGameOver = container.decode(Boolean::class, forKey = CodingKeys.isGameOver)
    }

    @androidx.annotation.Keep
    companion object: DecodableCompanion<TetrisSavedState> {
        override fun init(from: Decoder): TetrisSavedState = TetrisSavedState(from = from)

        private fun CodingKeys(rawValue: String): CodingKeys? = CodingKeys.init(rawValue = rawValue)
    }
}

// MARK: - Game Model

@Stable
internal class TetrisModel: Observable {

    /// Board grid: -1 = empty, 0–6 = tetromino kind raw value
    internal var grid: Array<Array<Int>>
        get() = _grid.wrappedValue.sref({ this.grid = it })
        set(newValue) {
            _grid.wrappedValue = newValue.sref()
        }
    internal var _grid: skip.model.Observed<Array<Array<Int>>> = skip.model.Observed(Array(repeating = Array(repeating = -1, count = 10), count = 20))
    internal var currentKind: TetrominoKind
        get() = _currentKind.wrappedValue
        set(newValue) {
            _currentKind.wrappedValue = newValue
        }
    internal var _currentKind: skip.model.Observed<TetrominoKind> = skip.model.Observed(TetrominoKind.t)
    internal var currentRotation: Int
        get() = _currentRotation.wrappedValue
        set(newValue) {
            _currentRotation.wrappedValue = newValue
        }
    internal var _currentRotation: skip.model.Observed<Int> = skip.model.Observed(0)
    internal var currentRow: Int
        get() = _currentRow.wrappedValue
        set(newValue) {
            _currentRow.wrappedValue = newValue
        }
    internal var _currentRow: skip.model.Observed<Int> = skip.model.Observed(0)
    internal var currentCol: Int
        get() = _currentCol.wrappedValue
        set(newValue) {
            _currentCol.wrappedValue = newValue
        }
    internal var _currentCol: skip.model.Observed<Int> = skip.model.Observed(4)
    internal var nextPieces: Array<TetrominoKind>
        get() = _nextPieces.wrappedValue.sref({ this.nextPieces = it })
        set(newValue) {
            _nextPieces.wrappedValue = newValue.sref()
        }
    internal var _nextPieces: skip.model.Observed<Array<TetrominoKind>> = skip.model.Observed(arrayOf())
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
    internal var _highScore: skip.model.Observed<Int> = skip.model.Observed(0)
    internal var level: Int
        get() = _level.wrappedValue
        set(newValue) {
            _level.wrappedValue = newValue
        }
    internal var _level: skip.model.Observed<Int> = skip.model.Observed(1)
    internal var totalLinesCleared: Int
        get() = _totalLinesCleared.wrappedValue
        set(newValue) {
            _totalLinesCleared.wrappedValue = newValue
        }
    internal var _totalLinesCleared: skip.model.Observed<Int> = skip.model.Observed(0)
    internal var isGameOver: Boolean
        get() = _isGameOver.wrappedValue
        set(newValue) {
            _isGameOver.wrappedValue = newValue
        }
    internal var _isGameOver: skip.model.Observed<Boolean> = skip.model.Observed(false)
    internal var isPaused: Boolean
        get() = _isPaused.wrappedValue
        set(newValue) {
            _isPaused.wrappedValue = newValue
        }
    internal var _isPaused: skip.model.Observed<Boolean> = skip.model.Observed(false)
    internal var clearingRows: Array<Int>
        get() = _clearingRows.wrappedValue.sref({ this.clearingRows = it })
        set(newValue) {
            _clearingRows.wrappedValue = newValue.sref()
        }
    internal var _clearingRows: skip.model.Observed<Array<Int>> = skip.model.Observed(arrayOf())
    internal var isClearingAnimation: Boolean
        get() = _isClearingAnimation.wrappedValue
        set(newValue) {
            _isClearingAnimation.wrappedValue = newValue
        }
    internal var _isClearingAnimation: skip.model.Observed<Boolean> = skip.model.Observed(false)
    internal var lastClearCount: Int
        get() = _lastClearCount.wrappedValue
        set(newValue) {
            _lastClearCount.wrappedValue = newValue
        }
    internal var _lastClearCount: skip.model.Observed<Int> = skip.model.Observed(0)
    internal var ghostRow: Int
        get() = _ghostRow.wrappedValue
        set(newValue) {
            _ghostRow.wrappedValue = newValue
        }
    internal var _ghostRow: skip.model.Observed<Int> = skip.model.Observed(0)

    private var bag: Array<TetrominoKind>
        get() = _bag.wrappedValue.sref({ this.bag = it })
        set(newValue) {
            _bag.wrappedValue = newValue.sref()
        }
    private var _bag: skip.model.Observed<Array<TetrominoKind>> = skip.model.Observed(arrayOf())

    internal constructor() {
        loadHighScore()
        fillBag()
        // Fill the 3-piece preview queue
        this._nextPieces = skip.model.Observed(arrayOf(nextFromBag(), nextFromBag(), nextFromBag()))
        spawnPiece()
    }

    // MARK: Current Piece Helpers

    internal fun currentCells(): Array<CellPos> {
        val offsets = currentKind.offsets(rotation = currentRotation)
        var result: Array<CellPos> = arrayOf()
        for (o in offsets.sref()) {
            result.append(CellPos(currentRow + o.r, currentCol + o.c))
        }
        return result.sref()
    }

    internal fun ghostCells(): Array<CellPos> {
        val dr = ghostRow - currentRow
        val offsets = currentKind.offsets(rotation = currentRotation)
        var result: Array<CellPos> = arrayOf()
        for (o in offsets.sref()) {
            result.append(CellPos(currentRow + o.r + dr, currentCol + o.c))
        }
        return result.sref()
    }

    // MARK: Random Bag (7-piece system)

    private fun fillBag() {
        var pieces = TetrominoKind.allCases.sref()
        var i = pieces.count - 1
        while (i > 0) {
            val j = Int.random(in_ = 0..i)
            val tmp = pieces[i]
            pieces[i] = pieces[j]
            pieces[j] = tmp
            i -= 1
        }
        bag = pieces
    }

    private fun nextFromBag(): TetrominoKind {
        if (bag.isEmpty) {
            fillBag()
        }
        return bag.removeFirst()
    }

    // MARK: Spawning

    internal fun spawnPiece() {
        // Take the first from the preview queue and refill
        currentKind = nextPieces[0]
        nextPieces.removeFirst()
        nextPieces.append(nextFromBag())

        currentRotation = 0
        currentRow = 0
        currentCol = 4

        val offsets = currentKind.offsets(rotation = 0)
        var minR = offsets[0].r
        for (o in offsets.sref()) {
            if (o.r < minR) {
                minR = o.r
            }
        }
        if (minR > 0) {
            currentRow = currentRow - minR
        }

        updateGhost()

        if (!isValidPosition(row = currentRow, col = currentCol, rotation = currentRotation, kind = currentKind)) {
            isGameOver = true
            if (score > highScore) {
                highScore = score
                saveHighScore()
            }
        }
    }

    // MARK: Validation

    internal fun isValidPosition(row: Int, col: Int, rotation: Int, kind: TetrominoKind): Boolean {
        val offsets = kind.offsets(rotation = rotation)
        for (o in offsets.sref()) {
            val r = row + o.r
            val c = col + o.c
            if (c < 0 || c >= TetrisModel.cols) {
                return false
            }
            if (r >= TetrisModel.rows) {
                return false
            }
            if (r >= 0 && grid[r][c] != -1) {
                return false
            }
        }
        return true
    }

    // MARK: Ghost

    internal fun updateGhost() {
        var testRow = currentRow
        while (isValidPosition(row = testRow + 1, col = currentCol, rotation = currentRotation, kind = currentKind)) {
            testRow += 1
        }
        ghostRow = testRow
    }

    // MARK: Movement

    internal fun moveLeft(): Boolean {
        if (isValidPosition(row = currentRow, col = currentCol - 1, rotation = currentRotation, kind = currentKind)) {
            currentCol -= 1
            updateGhost()
            return true
        }
        return false
    }

    internal fun moveRight(): Boolean {
        if (isValidPosition(row = currentRow, col = currentCol + 1, rotation = currentRotation, kind = currentKind)) {
            currentCol += 1
            updateGhost()
            return true
        }
        return false
    }

    internal fun moveDown(): Boolean {
        if (isValidPosition(row = currentRow + 1, col = currentCol, rotation = currentRotation, kind = currentKind)) {
            currentRow += 1
            return true
        }
        return false
    }

    internal fun rotate(): Boolean {
        val newRot = (currentRotation + 1) % 4
        if (isValidPosition(row = currentRow, col = currentCol, rotation = newRot, kind = currentKind)) {
            currentRotation = newRot
            updateGhost()
            return true
        }
        val kicks = arrayOf(1, -1, 2, -2)
        for (kick in kicks.sref()) {
            if (isValidPosition(row = currentRow, col = currentCol + kick, rotation = newRot, kind = currentKind)) {
                currentCol += kick
                currentRotation = newRot
                updateGhost()
                return true
            }
        }
        return false
    }

    // MARK: Lock & Clear

    internal fun lockPiece() {
        val cells = currentCells()
        for (cell in cells.sref()) {
            val r = cell.r
            val c = cell.c
            if (r >= 0 && r < TetrisModel.rows && c >= 0 && c < TetrisModel.cols) {
                grid[r][c] = currentKind.rawValue
            }
        }
    }

    internal fun findFullRows(): Array<Int> {
        var full: Array<Int> = arrayOf()
        for (r in 0..<TetrisModel.rows) {
            var isFull = true
            for (c in 0..<TetrisModel.cols) {
                if (grid[r][c] == -1) {
                    isFull = false
                    break
                }
            }
            if (isFull) {
                full.append(r)
            }
        }
        return full.sref()
    }

    internal fun removeRows(rows: Array<Int>) {
        val sorted = rows.sorted()
        for (r in sorted.sref()) {
            var rr = r
            while (rr > 0) {
                grid[rr] = grid[rr - 1].sref()
                rr -= 1
            }
            grid[0] = Array(repeating = -1, count = TetrisModel.cols)
        }
    }

    internal fun addScore(linesCount: Int, dropBonus: Int) {
        val basePoints: Int
        when (linesCount) {
            1 -> basePoints = 100
            2 -> basePoints = 300
            3 -> basePoints = 500
            4 -> basePoints = 800
            else -> basePoints = 0
        }
        score += basePoints * level + dropBonus * 2
        totalLinesCleared += linesCount
        lastClearCount = linesCount

        val newLevel = (totalLinesCleared / 10) + 1
        if (newLevel > level) {
            level = min(newLevel, 15)
        }

        if (score > highScore) {
            highScore = score
            saveHighScore()
        }
    }

    // MARK: Tick Speed

    internal val tickInterval: Double
        get() {
            val base = 0.8
            val speed = base - (Double(level - 1) * 0.045)
            if (speed < 0.1) {
                return 0.1
            }
            return speed
        }

    // MARK: New Game

    internal fun newGame() {
        grid = Array(repeating = Array(repeating = -1, count = TetrisModel.cols), count = TetrisModel.rows)
        score = 0
        level = 1
        totalLinesCleared = 0
        isGameOver = false
        isPaused = false
        clearingRows = arrayOf()
        isClearingAnimation = false
        lastClearCount = 0
        bag = arrayOf()
        fillBag()
        nextPieces = arrayOf(nextFromBag(), nextFromBag(), nextFromBag())
        spawnPiece()
    }

    // MARK: Persistence

    private fun saveHighScore(): Unit = UserDefaults.standard.set(highScore, forKey = "tetris_highscore")

    internal fun loadHighScore() {
        highScore = UserDefaults.standard.integer(forKey = "tetris_highscore")
    }

    // MARK: - Game State Persistence

    internal fun makeSavedState(): TetrisSavedState {
        var raws: Array<Int> = arrayOf()
        for (p in nextPieces.sref()) {
            raws.append(p.rawValue)
        }
        return TetrisSavedState(grid = grid, currentKindRaw = currentKind.rawValue, currentRotation = currentRotation, currentRow = currentRow, currentCol = currentCol, nextPieceRaws = raws, score = score, level = level, totalLinesCleared = totalLinesCleared, isGameOver = isGameOver)
    }

    internal fun restoreState(state: TetrisSavedState) {
        grid = state.grid
        currentKind = TetrominoKind(rawValue = state.currentKindRaw) ?: TetrominoKind.t
        currentRotation = state.currentRotation
        currentRow = state.currentRow
        currentCol = state.currentCol
        score = state.score
        level = state.level
        totalLinesCleared = state.totalLinesCleared
        isGameOver = state.isGameOver
        isPaused = true
        loadHighScore()

        var restoredNext: Array<TetrominoKind> = arrayOf()
        for (raw in state.nextPieceRaws.sref()) {
            restoredNext.append(TetrominoKind(rawValue = raw) ?: TetrominoKind.t)
        }
        nextPieces = restoredNext

        updateGhost()
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
        UserDefaults.standard.set(json_0, forKey = "tetris_saved_state")
    }

    @androidx.annotation.Keep
    companion object {
        internal val rows = 20
        internal val cols = 10

        /// Resets the persisted high score to zero.
        internal fun resetHighScore(): Unit = UserDefaults.standard.set(0, forKey = "tetris_highscore")

        internal fun loadSavedState(): TetrisSavedState? {
            val json_1 = UserDefaults.standard.string(forKey = "tetris_saved_state")
            if (json_1 == null) {
                return null
            }
            val data_1 = json_1.data(using = StringEncoding.utf8)
            if (data_1 == null) {
                return null
            }
            return try { JSONDecoder().decode(TetrisSavedState::class, from = data_1) } catch (_: Throwable) { null }
        }

        internal fun clearSavedState(): Unit = UserDefaults.standard.removeObject(forKey = "tetris_saved_state")
    }
}

/// Resets the Tetris high score to zero.
fun resetTetrisHighScore(): Unit = TetrisModel.resetHighScore()

// MARK: - Game View

internal class TetrisGameView: View {
    internal var showInstructions: Boolean
        get() = _showInstructions.wrappedValue
        set(newValue) {
            _showInstructions.wrappedValue = newValue
        }
    internal var _showInstructions: Binding<Boolean>
    internal var game: TetrisModel
        get() = _game.wrappedValue
        set(newValue) {
            _game.wrappedValue = newValue
        }
    internal var _game: skip.ui.State<TetrisModel>
    internal var tickTimer: Timer?
        get() = _tickTimer.wrappedValue
        set(newValue) {
            _tickTimer.wrappedValue = newValue
        }
    internal var _tickTimer: skip.ui.State<Timer?> = skip.ui.State(null)
    internal var dragAccumulatedX: Double
        get() = _dragAccumulatedX.wrappedValue
        set(newValue) {
            _dragAccumulatedX.wrappedValue = newValue
        }
    internal var _dragAccumulatedX: skip.ui.State<Double>
    internal var dragAccumulatedY: Double
        get() = _dragAccumulatedY.wrappedValue
        set(newValue) {
            _dragAccumulatedY.wrappedValue = newValue
        }
    internal var _dragAccumulatedY: skip.ui.State<Double>
    internal var showClearEffect: Boolean
        get() = _showClearEffect.wrappedValue
        set(newValue) {
            _showClearEffect.wrappedValue = newValue
        }
    internal var _showClearEffect: skip.ui.State<Boolean>
    internal var clearEffectText: String
        get() = _clearEffectText.wrappedValue
        set(newValue) {
            _clearEffectText.wrappedValue = newValue
        }
    internal var _clearEffectText: skip.ui.State<String>
    internal var showSettings: Boolean
        get() = _showSettings.wrappedValue
        set(newValue) {
            _showSettings.wrappedValue = newValue
        }
    internal var _showSettings: skip.ui.State<Boolean>
    internal lateinit var dismiss: DismissAction
    internal lateinit var scenePhase: ScenePhase
    internal var settings: TetrisSettings
        get() = _settings.wrappedValue
        set(newValue) {
            _settings.wrappedValue = newValue
        }
    internal var _settings = skip.ui.Environment<TetrisSettings>()

    internal fun playHaptic(pattern: HapticPattern) {
        if (settings.vibrations) {
            HapticFeedback.play(pattern)
        }
    }

    override fun body(): View {
        return ComposeBuilder { composectx: ComposeContext ->
            GeometryReader { geo ->
                ComposeBuilder { composectx: ComposeContext ->
                    // Reserve space for header (~36), stats (~50), padding (~20)
                    val chromeHeight: Double = 110.0
                    val maxCellFromHeight = (geo.size.height - chromeHeight) / Double(TetrisModel.rows)
                    val maxCellFromWidth = (geo.size.width - 16) / Double(TetrisModel.cols)
                    val cellSize = max(min(maxCellFromWidth, maxCellFromHeight), 8.0)

                    ZStack { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            // Background
                            LinearGradient(colors = arrayOf(
                                Color(red = 0.06, green = 0.06, blue = 0.18),
                                Color(red = 0.02, green = 0.02, blue = 0.08)
                            ), startPoint = UnitPoint.top, endPoint = UnitPoint.bottom)
                                .ignoresSafeArea().Compose(composectx)

                            VStack(spacing = 0.0) { ->
                                ComposeBuilder { composectx: ComposeContext ->
                                    // Header
                                    headerView
                                        .padding(Edge.Set.bottom, 4.0).Compose(composectx)

                                    // Stats row with next piece preview
                                    statsRow(cellSize = cellSize)
                                        .padding(Edge.Set.bottom, 6.0).Compose(composectx)

                                    // Game board — full width
                                    boardView(cellSize = cellSize).Compose(composectx)
                                    ComposeResult.ok
                                }
                            }
                            .padding(Edge.Set.horizontal, 8.0)
                            .padding(Edge.Set.top, 4.0).Compose(composectx)

                            if (game.isGameOver) {
                                gameOverOverlay.Compose(composectx)
                            }

                            if (game.isPaused && !game.isGameOver) {
                                pauseOverlay.Compose(composectx)
                            }

                            if (showClearEffect) {
                                clearPopup.Compose(composectx)
                            }
                            ComposeResult.ok
                        }
                    }.Compose(composectx)
                    ComposeResult.ok
                }
            }
            .navigationBarBackButtonHidden()
            .onAppear { ->
                val matchtarget_0 = TetrisModel.loadSavedState()
                if (matchtarget_0 != null) {
                    val savedState = matchtarget_0
                    game.restoreState(savedState)
                } else {
                    startTimer()
                }
            }
            .onDisappear { -> stopTimer() }
            .onChange(of = scenePhase) { oldPhase, newPhase ->
                if (newPhase != ScenePhase.active && !game.isGameOver && !game.isPaused) {
                    game.isPaused = true
                    stopTimer()
                }
                if (newPhase != ScenePhase.active && !game.isGameOver) {
                    game.saveState()
                }
            }
            .sheet(isPresented = Binding({ _showSettings.wrappedValue }, { it -> _showSettings.wrappedValue = it })) { ->
                ComposeBuilder { composectx: ComposeContext ->
                    TetrisSettingsView(settings = settings)
                        .presentationDetents(setOf(PresentationDetent.medium, PresentationDetent.large)).Compose(composectx)
                    ComposeResult.ok
                }
            }.Compose(composectx)
        }
    }

    @Composable
    @Suppress("UNCHECKED_CAST")
    override fun Evaluate(context: ComposeContext, options: Int): kotlin.collections.List<Renderable> {
        val rememberedgame by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<TetrisModel>, Any>) { mutableStateOf(_game) }
        _game = rememberedgame

        val rememberedtickTimer by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Timer?>, Any>) { mutableStateOf(_tickTimer) }
        _tickTimer = rememberedtickTimer

        val remembereddragAccumulatedX by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Double>, Any>) { mutableStateOf(_dragAccumulatedX) }
        _dragAccumulatedX = remembereddragAccumulatedX

        val remembereddragAccumulatedY by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Double>, Any>) { mutableStateOf(_dragAccumulatedY) }
        _dragAccumulatedY = remembereddragAccumulatedY

        val rememberedshowClearEffect by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Boolean>, Any>) { mutableStateOf(_showClearEffect) }
        _showClearEffect = rememberedshowClearEffect

        val rememberedclearEffectText by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<String>, Any>) { mutableStateOf(_clearEffectText) }
        _clearEffectText = rememberedclearEffectText

        val rememberedshowSettings by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Boolean>, Any>) { mutableStateOf(_showSettings) }
        _showSettings = rememberedshowSettings

        this.dismiss = EnvironmentValues.shared.dismiss
        this.scenePhase = EnvironmentValues.shared.scenePhase
        _settings.wrappedValue = EnvironmentValues.shared.environmentObject(type = TetrisSettings::class)!!

        return super.Evaluate(context, options)
    }

    // MARK: - Header

    internal val headerView: View
        get() {
            return HStack(spacing = 12.0) { ->
                ComposeBuilder { composectx: ComposeContext ->
                    Button(action = { -> dismiss() }) { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            Image("cancel", bundle = Bundle.module)
                                .font(Font.title2)
                                .foregroundStyle(Color.white.opacity(0.6)).Compose(composectx)
                            ComposeResult.ok
                        }
                    }.Compose(composectx)

                    Spacer().Compose(composectx)

                    Text(LocalizedStringKey(stringLiteral = "SIRTET"), bundle = Bundle.module)
                        .font(Font.headline)
                        .fontWeight(Font.Weight.black)
                        .foregroundStyle(Color.white).Compose(composectx)
                    Spacer().Compose(composectx)

                    Button(action = { ->
                        game.isPaused = !game.isPaused
                        if (game.isPaused) {
                            stopTimer()
                        } else {
                            startTimer()
                        }
                    }) { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            Image(if (game.isPaused) "play_circle" else "pause_circle", bundle = Bundle.module)
                                .font(Font.title2)
                                .foregroundStyle(Color.white.opacity(0.6)).Compose(composectx)
                            ComposeResult.ok
                        }
                    }.Compose(composectx)
                    ComposeResult.ok
                }
            }
            .padding(Edge.Set.horizontal, 4.0)
        }

    // MARK: - Stats Row

    internal fun statsRow(cellSize: Double): View {
        return HStack(spacing = 0.0) { ->
            ComposeBuilder { composectx: ComposeContext ->
                statBox(label = "SCORE", value = "${game.score}").Compose(composectx)
                Spacer().Compose(composectx)
                statBox(label = "LEVEL", value = "${game.level}").Compose(composectx)
                Spacer().Compose(composectx)
                nextPiecePreview(cellSize = cellSize * 0.45).Compose(composectx)
                Spacer().Compose(composectx)
                statBox(label = "LINES", value = "${game.totalLinesCleared}").Compose(composectx)
                Spacer().Compose(composectx)
                statBox(label = "HIGH", value = "${game.highScore}").Compose(composectx)
                ComposeResult.ok
            }
        }
        .padding(Edge.Set.horizontal, 4.0)
    }

    internal fun statBox(label: String, value: String): View {
        return VStack(spacing = 2.0) { ->
            ComposeBuilder { composectx: ComposeContext ->
                Text(label)
                    .font(Font.caption2)
                    .fontWeight(Font.Weight.bold)
                    .foregroundStyle(Color.white.opacity(0.5)).Compose(composectx)
                Text(value)
                    .font(Font.title3)
                    .fontWeight(Font.Weight.bold)
                    .foregroundStyle(Color.white)
                    .monospaced().Compose(composectx)
                ComposeResult.ok
            }
        }
    }

    // MARK: - Board

    internal fun boardView(cellSize: Double): View {
        val ghostPositions = game.ghostCells()
        val currentPositions = game.currentCells()

        return ZStack { ->
            ComposeBuilder { composectx: ComposeContext ->
                // Board background with subtle border
                RoundedRectangle(cornerRadius = 6.0)
                    .fill(Color(red = 0.04, green = 0.04, blue = 0.08))
                    .frame(width = cellSize * Double(TetrisModel.cols) + 4, height = cellSize * Double(TetrisModel.rows) + 4).Compose(composectx)

                VStack(spacing = 0.0) { ->
                    ComposeBuilder { composectx: ComposeContext ->
                        ForEach(0..<TetrisModel.rows, id = { it }) { r ->
                            ComposeBuilder { composectx: ComposeContext ->
                                HStack(spacing = 0.0) { ->
                                    ComposeBuilder { composectx: ComposeContext ->
                                        ForEach(0..<TetrisModel.cols, id = { it }) { c ->
                                            ComposeBuilder { composectx: ComposeContext ->
                                                singleCell(row = r, col = c, cellSize = cellSize, ghostPositions = ghostPositions, currentPositions = currentPositions).Compose(composectx)
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
        .gesture(DragGesture(minimumDistance = 5.0)
            .onChanged { value -> handleDrag(value = value, cellSize = cellSize) }
            .onEnded { _ -> handleDragEnd() })
        .onTapGesture { it -> handleTap() }
    }

    // MARK: - 3D Block Cell

    internal fun singleCell(row: Int, col: Int, cellSize: Double, ghostPositions: Array<CellPos>, currentPositions: Array<CellPos>): View {
        val gridVal = game.grid[row][col]
        val isClearing = game.clearingRows.contains(row)
        val isGhost = ghostPositions.contains(where = { it -> it.r == row && it.c == col })
        val isCurrent = currentPositions.contains(where = { it -> it.r == row && it.c == col })

        val isBlock = isCurrent || (gridVal != -1 && !isClearing)
        val blockKindRaw = if (isCurrent) game.currentKind.rawValue else gridVal
        val inset = cellSize * 0.08
        val cornerR = cellSize * 0.18

        return ZStack { ->
            ComposeBuilder { composectx: ComposeContext ->
                if (isClearing && gridVal != -1) {
                    // Flash white during clearing animation
                    RoundedRectangle(cornerRadius = cornerR)
                        .fill(Color.white)
                        .frame(width = cellSize - 1, height = cellSize - 1)
                        .opacity(0.8).Compose(composectx)
                } else if (isBlock) {
                    // Shadow base layer
                    RoundedRectangle(cornerRadius = cornerR)
                        .fill(TetrominoKind.shadowForRaw(blockKindRaw))
                        .frame(width = cellSize - 1, height = cellSize - 1).Compose(composectx)

                    // Main face with subtle gradient for soft 3D look
                    RoundedRectangle(cornerRadius = cornerR)
                        .fill(LinearGradient(colors = arrayOf(
                            TetrominoKind.highlightForRaw(blockKindRaw).opacity(0.5),
                            TetrominoKind.colorForRaw(blockKindRaw)
                        ), startPoint = UnitPoint.top, endPoint = UnitPoint.bottom))
                        .frame(width = cellSize - 2, height = cellSize - 2).Compose(composectx)
                } else if (isGhost) {
                    RoundedRectangle(cornerRadius = cornerR)
                        .fill(game.currentKind.color.opacity(0.12))
                        .frame(width = cellSize - 1, height = cellSize - 1)
                        .border(game.currentKind.color.opacity(0.25), width = 0.5).Compose(composectx)
                } else {
                    // Empty cell
                    RoundedRectangle(cornerRadius = 2.0)
                        .fill(Color(red = 0.07, green = 0.07, blue = 0.12))
                        .frame(width = cellSize - 1, height = cellSize - 1)
                        .border(Color(red = 0.1, green = 0.1, blue = 0.15), width = 0.25).Compose(composectx)
                }
                ComposeResult.ok
            }
        }
        .frame(width = cellSize, height = cellSize)
    }

    // MARK: - Next Piece Preview

    internal fun nextPiecePreview(cellSize: Double): View {
        val kind = game.nextPieces.first
        val offsets = (kind?.offsets(rotation = 0) ?: arrayOf()).sref()

        var minR = offsets.first?.r ?: 0
        var maxR = minR
        var minC = offsets.first?.c ?: 0
        var maxC = minC
        for (o in offsets.sref()) {
            if (o.r < minR) {
                minR = o.r
            }
            if (o.r > maxR) {
                maxR = o.r
            }
            if (o.c < minC) {
                minC = o.c
            }
            if (o.c > maxC) {
                maxC = o.c
            }
        }
        val previewRows = max(maxR - minR + 1, 1)
        val previewCols = max(maxC - minC + 1, 1)
        // Fixed dimensions: tallest piece is 2 rows, widest is 4 cols (I-piece)
        val maxRows = 2
        val maxCols = 4
        val cornerR = cellSize * 0.18
        // Capture colors once to avoid per-cell optional chaining issues on Android
        val blockColor = kind?.color ?: Color.clear
        val blockShadow = kind?.shadowColor ?: Color.clear
        val blockHighlight = kind?.highlightColor ?: Color.clear

        return VStack(spacing = 1.0) { ->
            ComposeBuilder { composectx: ComposeContext ->
                Text(LocalizedStringKey(stringLiteral = "NEXT"), bundle = Bundle.module)
                    .font(Font.caption2)
                    .fontWeight(Font.Weight.bold)
                    .foregroundStyle(Color.white.opacity(0.5)).Compose(composectx)
                ZStack { ->
                    ComposeBuilder { composectx: ComposeContext ->
                        // Fixed-size invisible frame so the area never resizes
                        Color.clear
                            .frame(width = cellSize * Double(maxCols), height = cellSize * Double(maxRows)).Compose(composectx)
                        VStack(spacing = 0.0) { ->
                            ComposeBuilder { composectx: ComposeContext ->
                                ForEach(0..<previewRows, id = { it }) { r ->
                                    ComposeBuilder { composectx: ComposeContext ->
                                        HStack(spacing = 0.0) { ->
                                            ComposeBuilder { composectx: ComposeContext ->
                                                ForEach(0..<previewCols, id = { it }) { c ->
                                                    ComposeBuilder { composectx: ComposeContext ->
                                                        val hasBlock = offsets.contains(where = { it -> it.r == r + minR && it.c == c + minC })
                                                        ZStack { ->
                                                            ComposeBuilder { composectx: ComposeContext ->
                                                                if (hasBlock) {
                                                                    RoundedRectangle(cornerRadius = cornerR)
                                                                        .fill(blockShadow)
                                                                        .frame(width = cellSize - 1, height = cellSize - 1).Compose(composectx)
                                                                    RoundedRectangle(cornerRadius = cornerR)
                                                                        .fill(LinearGradient(colors = arrayOf(
                                                                            blockHighlight.opacity(0.5),
                                                                            blockColor
                                                                        ), startPoint = UnitPoint.top, endPoint = UnitPoint.bottom))
                                                                        .frame(width = cellSize - 2, height = cellSize - 2).Compose(composectx)
                                                                }
                                                                ComposeResult.ok
                                                            }
                                                        }
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
                        ComposeResult.ok
                    }
                }.Compose(composectx)
                ComposeResult.ok
            }
        }
    }

    // MARK: - Gestures

    internal fun handleTap() {
        if (game.isGameOver || game.isPaused) {
            return
        }
        if (game.rotate()) {
            playHaptic(HapticPattern.snap)
        }
    }

    internal fun handleDrag(value: DragGesture.Value, cellSize: Double) {
        if (game.isGameOver || game.isPaused) {
            return
        }

        val dx = value.translation.width
        val dy = value.translation.height

        val threshold = cellSize * 0.8
        val totalDx = dx - dragAccumulatedX
        val colsMoved = Int(totalDx / threshold)
        if (colsMoved != 0) {
            var moved = false
            val dir = if (colsMoved > 0) 1 else -1
            var steps = colsMoved
            if (steps < 0) {
                steps = -steps
            }
            for (unusedbinding in 0..<steps) {
                if (dir > 0) {
                    if (game.moveRight()) {
                        moved = true
                    }
                } else if (game.moveLeft()) {
                    moved = true
                }
            }
            if (moved) {
                playHaptic(HapticPattern.snap)
            }
            dragAccumulatedX += Double(colsMoved) * threshold
        }

        // Downward drag: move one row per threshold
        val totalDy = dy - dragAccumulatedY
        if (totalDy > cellSize) {
            val rowsToMove = Int(totalDy / cellSize)
            for (unusedbinding in 0..<rowsToMove) {
                if (game.moveDown()) {
                    game.addScore(linesCount = 0, dropBonus = 1)
                }
            }
            dragAccumulatedY += Double(rowsToMove) * cellSize
        }
    }

    internal fun handleDragEnd() {
        dragAccumulatedX = 0.0
        dragAccumulatedY = 0.0
    }

    // MARK: - Timer

    internal fun startTimer() {
        stopTimer()
        val interval = game.tickInterval
        tickTimer = Timer.scheduledTimer(withTimeInterval = interval, repeats = true) { _ -> tick() }
    }

    internal fun stopTimer() {
        tickTimer?.invalidate()
        tickTimer = null
    }

    internal fun tick() {
        if (game.isGameOver || game.isPaused || game.isClearingAnimation) {
            return
        }
        if (!game.moveDown()) {
            lockAndClear(dropBonus = 0)
        }
    }

    internal fun lockAndClear(dropBonus: Int) {
        game.lockPiece()

        val fullRows = game.findFullRows()
        if (!fullRows.isEmpty) {
            game.isClearingAnimation = true
            game.clearingRows = fullRows

            if (fullRows.count >= 4) {
                playHaptic(HapticPattern.bigCelebrate)
                clearEffectText = "SIRTET!" // "TETRIS!"
            } else if (fullRows.count == 3) {
                playHaptic(HapticPattern.celebrate)
                clearEffectText = "TRIPLE"
            } else if (fullRows.count == 2) {
                playHaptic(HapticPattern.celebrate)
                clearEffectText = "DOUBLE"
            } else {
                playHaptic(HapticPattern.snap)
                clearEffectText = "SINGLE"
            }
            showClearEffect = true

            DispatchQueue.main.asyncAfter(deadline = Double.now() + 0.35) { ->
                game.removeRows(fullRows)
                game.addScore(linesCount = fullRows.count, dropBonus = dropBonus)
                game.clearingRows = arrayOf()
                game.isClearingAnimation = false
                game.spawnPiece()
                startTimer()
            }

            DispatchQueue.main.asyncAfter(deadline = Double.now() + 1.0) { -> showClearEffect = false }
        } else {
            game.addScore(linesCount = 0, dropBonus = dropBonus)
            playHaptic(HapticPattern.place)
            game.spawnPiece()
            startTimer()
        }

        if (game.isGameOver) {
            stopTimer()
            playHaptic(HapticPattern.error)
        }
    }

    // MARK: - Game Over Overlay

    internal val gameOverOverlay: View
        get() {
            return ZStack { ->
                ComposeBuilder { composectx: ComposeContext ->
                    Color.black.opacity(0.75)
                        .ignoresSafeArea().Compose(composectx)

                    VStack(spacing = 16.0) { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            Text(LocalizedStringKey(stringLiteral = "GAME OVER"), bundle = Bundle.module)
                                .font(Font.largeTitle)
                                .fontWeight(Font.Weight.black)
                                .foregroundStyle(Color.white).Compose(composectx)

                            VStack(spacing = 4.0) { ->
                                ComposeBuilder { composectx: ComposeContext ->
                                    Text(LocalizedStringKey(stringLiteral = "Score"), bundle = Bundle.module)
                                        .font(Font.headline)
                                        .foregroundStyle(Color.white.opacity(0.7)).Compose(composectx)
                                    Text({
                                        val str = LocalizedStringKey.StringInterpolation(literalCapacity = 0, interpolationCount = 0)
                                        str.appendInterpolation(game.score)
                                        LocalizedStringKey(stringInterpolation = str)
                                    }())
                                        .font(Font.system(size = 44.0))
                                        .fontWeight(Font.Weight.bold)
                                        .foregroundStyle(Color.yellow)
                                        .monospaced().Compose(composectx)
                                    ComposeResult.ok
                                }
                            }.Compose(composectx)

                            HStack(spacing = 24.0) { ->
                                ComposeBuilder { composectx: ComposeContext ->
                                    VStack(spacing = 2.0) { ->
                                        ComposeBuilder { composectx: ComposeContext ->
                                            Text(LocalizedStringKey(stringLiteral = "Level"), bundle = Bundle.module)
                                                .font(Font.caption)
                                                .foregroundStyle(Color.white.opacity(0.6)).Compose(composectx)
                                            Text({
                                                val str = LocalizedStringKey.StringInterpolation(literalCapacity = 0, interpolationCount = 0)
                                                str.appendInterpolation(game.level)
                                                LocalizedStringKey(stringInterpolation = str)
                                            }())
                                                .font(Font.title3)
                                                .fontWeight(Font.Weight.bold)
                                                .foregroundStyle(Color.white).Compose(composectx)
                                            ComposeResult.ok
                                        }
                                    }.Compose(composectx)
                                    VStack(spacing = 2.0) { ->
                                        ComposeBuilder { composectx: ComposeContext ->
                                            Text(LocalizedStringKey(stringLiteral = "Lines"), bundle = Bundle.module)
                                                .font(Font.caption)
                                                .foregroundStyle(Color.white.opacity(0.6)).Compose(composectx)
                                            Text({
                                                val str = LocalizedStringKey.StringInterpolation(literalCapacity = 0, interpolationCount = 0)
                                                str.appendInterpolation(game.totalLinesCleared)
                                                LocalizedStringKey(stringInterpolation = str)
                                            }())
                                                .font(Font.title3)
                                                .fontWeight(Font.Weight.bold)
                                                .foregroundStyle(Color.white).Compose(composectx)
                                            ComposeResult.ok
                                        }
                                    }.Compose(composectx)
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
                                TetrisModel.clearSavedState()
                                game.newGame()
                                startTimer()
                            }) { ->
                                ComposeBuilder { composectx: ComposeContext ->
                                    Text(LocalizedStringKey(stringLiteral = "Play Again"), bundle = Bundle.module)
                                        .font(Font.headline)
                                        .fontWeight(Font.Weight.bold)
                                        .foregroundStyle(Color.white)
                                        .frame(width = 160.0).Compose(composectx)
                                    ComposeResult.ok
                                }
                            }
                            .buttonStyle(ButtonStyle.borderedProminent)
                            .tint(Color.blue)
                            .padding(Edge.Set.top, 4.0).Compose(composectx)

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

                            ShareLink(item = "I scored ${game.score} (level ${game.level}, ${game.totalLinesCleared} lines) in Sirtet on Faire Games! Can you beat it?\nhttps://appfair.net", subject = Text(LocalizedStringKey(stringLiteral = "Sirtet Score"), bundle = Bundle.module), message = Text({
                                val str = LocalizedStringKey.StringInterpolation(literalCapacity = 0, interpolationCount = 0)
                                str.appendLiteral("I scored ")
                                str.appendInterpolation(game.score)
                                str.appendLiteral(" in Sirtet!")
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
                    .padding(28.0)
                    .background(RoundedRectangle(cornerRadius = 20.0)
                        .fill(Color(red = 0.1, green = 0.1, blue = 0.2))).Compose(composectx)
                    ComposeResult.ok
                }
            }
        }

    // MARK: - Pause Overlay

    internal val pauseOverlay: View
        get() {
            return ZStack { ->
                ComposeBuilder { composectx: ComposeContext ->
                    Color.black.opacity(0.6)
                        .ignoresSafeArea().Compose(composectx)

                    VStack(spacing = 20.0) { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            Text(LocalizedStringKey(stringLiteral = "PAUSED"), bundle = Bundle.module)
                                .font(Font.largeTitle)
                                .fontWeight(Font.Weight.black)
                                .foregroundStyle(Color.white).Compose(composectx)

                            Button(action = { ->
                                game.isPaused = false
                                startTimer()
                            }) { ->
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
                                TetrisModel.clearSavedState()
                                game.newGame()
                                startTimer()
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

                            Button(action = { -> showSettings = true }) { ->
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

                            Button(action = { -> showInstructions = true }) { ->
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

                            Button(action = { ->
                                stopTimer()
                                dismiss()
                            }) { ->
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
                        .fill(Color(red = 0.1, green = 0.1, blue = 0.2))).Compose(composectx)
                    ComposeResult.ok
                }
            }
        }

    // MARK: - Clear Popup

    internal val clearPopup: View
        get() {
            return VStack { ->
                ComposeBuilder { composectx: ComposeContext ->
                    Spacer().Compose(composectx)
                    Text(clearEffectText)
                        .font(Font.title)
                        .fontWeight(Font.Weight.black)
                        .foregroundStyle(if (game.lastClearCount >= 4) Color.yellow else Color.white)
                        .shadow(color = Color.blue.opacity(0.8), radius = 12.0)
                        .padding(Edge.Set.bottom, 100.0).Compose(composectx)
                    ComposeResult.ok
                }
            }
        }

    constructor(showInstructions: Binding<Boolean>, game: TetrisModel = TetrisModel(), tickTimer: Timer? = null, dragAccumulatedX: Double = 0.0, dragAccumulatedY: Double = 0.0, showClearEffect: Boolean = false, clearEffectText: String = "", showSettings: Boolean = false) {
        this._showInstructions = showInstructions
        this._game = skip.ui.State(game)
        this._tickTimer = skip.ui.State(tickTimer)
        this._dragAccumulatedX = skip.ui.State(dragAccumulatedX)
        this._dragAccumulatedY = skip.ui.State(dragAccumulatedY)
        this._showClearEffect = skip.ui.State(showClearEffect)
        this._clearEffectText = skip.ui.State(clearEffectText)
        this._showSettings = skip.ui.State(showSettings)
    }
}

// MARK: - Preview Icon

/// Returns the TetrominoKind for a Tetris preview icon cell, or nil if empty.
/// Uses an 8x8 grid with pieces positioned to look appealing as a square icon.
private fun tetrisPreviewKind(row: Int, col: Int): TetrominoKind? {
    // Bottom row - full line (I-piece cyan)
    if (row == 7 && col >= 1 && col <= 6) {
        return TetrominoKind.i
    }
    // L-piece (orange)
    if (row == 6 && col >= 1 && col <= 3) {
        return TetrominoKind.l
    }
    if (row == 5 && col == 1) {
        return TetrominoKind.l
    }
    // S-piece (green)
    if (row == 6 && (col == 4 || col == 5)) {
        return TetrominoKind.s
    }
    if (row == 5 && (col == 5 || col == 6)) {
        return TetrominoKind.s
    }
    // T-piece (purple)
    if (row == 5 && col >= 2 && col <= 4) {
        return TetrominoKind.t
    }
    if (row == 4 && col == 3) {
        return TetrominoKind.t
    }
    // Falling I-piece (cyan)
    if (col == 4 && row >= 1 && row <= 4) {
        return TetrominoKind.i
    }
    return null
}

/// A preview icon for the Tetris game, using the same 3D cell rendering as the game.
class TetrisPreviewIcon: View {
    constructor() {
    }

    override fun body(): View {
        return ComposeBuilder { composectx: ComposeContext ->
            GeometryReader { geo ->
                ComposeBuilder { composectx: ComposeContext ->
                    val gridSize = 8
                    val padding: Double = 4.0
                    val available = min(geo.size.width, geo.size.height) - padding * 2
                    val cellSize = available / Double(gridSize)
                    val cornerR = cellSize * 0.18

                    VStack(spacing = 0.0) { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            ForEach(0..<gridSize, id = { it }) { row ->
                                ComposeBuilder { composectx: ComposeContext ->
                                    HStack(spacing = 0.0) { ->
                                        ComposeBuilder { composectx: ComposeContext ->
                                            ForEach(0..<gridSize, id = { it }) { col ->
                                                ComposeBuilder { composectx: ComposeContext ->
                                                    val kind = tetrisPreviewKind(row = row, col = col)
                                                    ZStack { ->
                                                        ComposeBuilder { composectx: ComposeContext ->
                                                            if (kind != null) {
                                                                // Shadow base layer — same as singleCell in game
                                                                RoundedRectangle(cornerRadius = cornerR)
                                                                    .fill(kind.shadowColor)
                                                                    .frame(width = cellSize - 1, height = cellSize - 1).Compose(composectx)
                                                                // Main face with gradient — same as singleCell in game
                                                                RoundedRectangle(cornerRadius = cornerR)
                                                                    .fill(LinearGradient(colors = arrayOf(
                                                                        kind.highlightColor.opacity(0.5),
                                                                        kind.color
                                                                    ), startPoint = UnitPoint.top, endPoint = UnitPoint.bottom))
                                                                    .frame(width = cellSize - 2, height = cellSize - 2).Compose(composectx)
                                                            }
                                                            ComposeResult.ok
                                                        }
                                                    }
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
                    }
                    .padding(padding)
                    .frame(width = geo.size.width, height = geo.size.height).Compose(composectx)
                    ComposeResult.ok
                }
            }
            .aspectRatio(1.0, contentMode = ContentMode.fit)
            .background(RoundedRectangle(cornerRadius = 8.0)
                .fill(Color(red = 0.08, green = 0.08, blue = 0.18))).Compose(composectx)
        }
    }

    @androidx.annotation.Keep
    companion object {
    }
}

// MARK: - In-Game Settings Sheet

internal class TetrisSettingsView: View {
    internal var settings: TetrisSettings
        get() = _settings.wrappedValue
        set(newValue) {
            _settings.wrappedValue = newValue
        }
    internal var _settings: skip.ui.Bindable<TetrisSettings>
    internal lateinit var dismiss: DismissAction

    override fun body(): View {
        return ComposeBuilder { composectx: ComposeContext ->
            NavigationStack { ->
                ComposeBuilder { composectx: ComposeContext ->
                    Form { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            Section(header = Text(LocalizedStringKey(stringLiteral = "Sirtet"), bundle = Bundle.module)) { ->
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

    constructor(settings: TetrisSettings) {
        this._settings = skip.ui.Bindable(settings)
    }
}

/// Settings specific to the Sirtet (Tetris) game.
@Stable
open class TetrisSettings: Observable {
    /// Whether vibrations (haptic feedback) are enabled for Sirtet.
    open var vibrations: Boolean
        get() = _vibrations.wrappedValue
        set(newValue) {
            _vibrations.wrappedValue = newValue
            defaults.set(vibrations, forKey = "tetrisVibrations")
        }
    var _vibrations: skip.model.Observed<Boolean> = skip.model.Observed(defaults.value(forKey = "tetrisVibrations", default = true))

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
