// Licensed under the GNU General Public License v2.0 or later
// SPDX-License-Identifier: GPL-2.0-or-later

package org.appfair.app.fairegames.games.drop7

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

class Drop7ContainerView: View {
    private var settings: Drop7Settings
        get() = _settings.wrappedValue
        set(newValue) {
            _settings.wrappedValue = newValue
        }
    private var _settings: skip.ui.State<Drop7Settings> = skip.ui.State(Drop7Settings())
    private var showInstructions: Boolean
        get() = _showInstructions.wrappedValue
        set(newValue) {
            _showInstructions.wrappedValue = newValue
        }
    private var _showInstructions: skip.ui.State<Boolean> = skip.ui.State(false)
    private val instructionsConfig = GameInstructionsConfig(key = "Drop7.instructions", bundle = Bundle.module, firstLaunchKey = "instructionsShown_Drop7", title = "Drop 7")

    constructor() {
    }

    override fun body(): View {
        return ComposeBuilder { composectx: ComposeContext ->
            Drop7GameView(showInstructions = Binding({ _showInstructions.wrappedValue }, { it -> _showInstructions.wrappedValue = it }))
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
        val rememberedsettings by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Drop7Settings>, Any>) { mutableStateOf(_settings) }
        _settings = rememberedsettings

        val rememberedshowInstructions by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Boolean>, Any>) { mutableStateOf(_showInstructions) }
        _showInstructions = rememberedshowInstructions

        return super.Evaluate(context, options)
    }

    @androidx.annotation.Keep
    companion object {
    }
}

fun resetDrop7HighScore(): Unit = UserDefaults.standard.set(0, forKey = "drop7_highscore")

// MARK: - Constants

private val gridCols: Int = 7
private val gridRows: Int = 7
private val cellGap: Double = 4.0
private val discCornerRadius: Double = 6.0

// Cell states
private val stateEmpty: Int = 0
private val stateNormal: Int = 1 // visible numbered disc
private val stateCracked: Int = 2 // value hidden, edges visible
private val stateWrapped: Int = 3 // fully wrapped, value hidden

// Sentinel for "fall source came from below the board" (push-up new bottom row)
private val fallFromBelowSentinel: Int = -2

// Disc colors keyed by value (1..7)
private fun discColor(for_: Int): Color {
    val value = for_
    when (value) {
        1 -> return Color(red = 0.86, green = 0.22, blue = 0.22)
        2 -> return Color(red = 0.95, green = 0.55, blue = 0.18)
        3 -> return Color(red = 0.96, green = 0.85, blue = 0.22)
        4 -> return Color(red = 0.34, green = 0.74, blue = 0.32)
        5 -> return Color(red = 0.22, green = 0.52, blue = 0.94)
        6 -> return Color(red = 0.55, green = 0.32, blue = 0.85)
        7 -> return Color(red = 0.95, green = 0.32, blue = 0.66)
        else -> return Color(red = 0.5, green = 0.5, blue = 0.5)
    }
}

private fun discTextColor(for_: Int): Color {
    val value = for_
    if (value == 3) {
        return Color(red = 0.25, green = 0.20, blue = 0.10)
    }
    return Color.white
}

private val wrappedColor: Color = Color(red = 0.32, green = 0.30, blue = 0.36)
private val crackedColor: Color = Color(red = 0.55, green = 0.50, blue = 0.55)
private val emptyCellColor: Color = Color(red = 0.15, green = 0.15, blue = 0.20)
private val boardBackground: Color = Color(red = 0.10, green = 0.10, blue = 0.16)
private val pageBackground: Color = Color(red = 0.06, green = 0.06, blue = 0.12)

// Chain bonus per chain step (1-indexed)
private val chainBonusTable: Array<Int> = arrayOf(7, 39, 109, 224, 391, 617, 907, 1267, 1701, 2213, 2809, 3491, 4257, 5111, 6051)

private fun chainBonus(forStep: Int): Int {
    val step = forStep
    if (step <= 0) {
        return chainBonusTable[0]
    }
    if (step <= chainBonusTable.count) {
        return chainBonusTable[step - 1]
    }
    val last = chainBonusTable[chainBonusTable.count - 1]
    return last + (step - chainBonusTable.count) * 1000
}

// MARK: - Difficulty

@androidx.annotation.Keep
internal enum class Drop7Difficulty(override val rawValue: Int, @Suppress("UNUSED_PARAMETER") unusedp: Nothing? = null): CaseIterable, RawRepresentable<Int> {
    easy(0),
    normal(1),
    hard(2);

    internal val label: String
        get() {
            when (this) {
                Drop7Difficulty.easy -> return "Easy"
                Drop7Difficulty.normal -> return "Normal"
                Drop7Difficulty.hard -> return "Hard"
            }
        }

    internal val description: String
        get() {
            when (this) {
                Drop7Difficulty.easy -> return "3 starting rows. New row every 40 drops."
                Drop7Difficulty.normal -> return "5 starting rows. New row every 30 drops."
                Drop7Difficulty.hard -> return "6 starting rows. New row every 25 drops."
            }
        }

    internal val accentColor: Color
        get() {
            when (this) {
                Drop7Difficulty.easy -> return Color(red = 0.35, green = 0.75, blue = 0.45)
                Drop7Difficulty.normal -> return Color(red = 0.30, green = 0.60, blue = 0.95)
                Drop7Difficulty.hard -> return Color(red = 0.90, green = 0.35, blue = 0.30)
            }
        }

    internal val startingRows: Int
        get() {
            when (this) {
                Drop7Difficulty.easy -> return 3
                Drop7Difficulty.normal -> return 5
                Drop7Difficulty.hard -> return 6
            }
        }

    /// Drops needed to advance to the *first* push-up. Subsequent levels need
    /// progressively fewer drops (see `Drop7Model.currentLevelTarget`).
    internal val dropsPerLevel: Int
        get() {
            when (this) {
                Drop7Difficulty.easy -> return 40
                Drop7Difficulty.normal -> return 30
                Drop7Difficulty.hard -> return 25
            }
        }

    /// The minimum number of drops per level once the cadence has decayed.
    internal val minDropsPerLevel: Int
        get() {
            when (this) {
                Drop7Difficulty.easy -> return 8
                Drop7Difficulty.normal -> return 5
                Drop7Difficulty.hard -> return 3
            }
        }

    /// Points awarded each time the player completes a level (a push-up fires).
    internal val levelBonus: Int
        get() {
            when (this) {
                Drop7Difficulty.easy -> return 5_000
                Drop7Difficulty.normal -> return 7_000
                Drop7Difficulty.hard -> return 14_000
            }
        }

    override fun toString(): String = description

    @androidx.annotation.Keep
    companion object: CaseIterableCompanion<Drop7Difficulty> {
        fun init(rawValue: Int): Drop7Difficulty? {
            return when (rawValue) {
                0 -> Drop7Difficulty.easy
                1 -> Drop7Difficulty.normal
                2 -> Drop7Difficulty.hard
                else -> null
            }
        }

        override val allCases: Array<Drop7Difficulty>
            get() = arrayOf(easy, normal, hard)
    }
}

internal fun Drop7Difficulty(rawValue: Int): Drop7Difficulty? = Drop7Difficulty.init(rawValue = rawValue)

// MARK: - Saved State

@androidx.annotation.Keep
@Suppress("MUST_BE_INITIALIZED")
internal class Drop7SavedState: Codable, MutableStruct {
    internal var stateGrid: Array<Int>
        get() = field.sref({ this.stateGrid = it })
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
            willmutate()
            field = newValue
            didmutate()
        }
    internal var valueGrid: Array<Int>
        get() = field.sref({ this.valueGrid = it })
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
            willmutate()
            field = newValue
            didmutate()
        }
    internal var currentPiece: Int
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    internal var nextPiece: Int
        set(newValue) {
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
    internal var dropsThisLevel: Int
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
    internal var isGameOver: Boolean
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

    constructor(stateGrid: Array<Int>, valueGrid: Array<Int>, currentPiece: Int, nextPiece: Int, score: Int, dropsThisLevel: Int, level: Int, isGameOver: Boolean, difficultyRaw: Int) {
        this.stateGrid = stateGrid
        this.valueGrid = valueGrid
        this.currentPiece = currentPiece
        this.nextPiece = nextPiece
        this.score = score
        this.dropsThisLevel = dropsThisLevel
        this.level = level
        this.isGameOver = isGameOver
        this.difficultyRaw = difficultyRaw
    }

    override var supdate: ((Any) -> Unit)? = null
    override var smutatingcount = 0
    override fun scopy(): MutableStruct = Drop7SavedState(stateGrid, valueGrid, currentPiece, nextPiece, score, dropsThisLevel, level, isGameOver, difficultyRaw)

    private enum class CodingKeys(override val rawValue: String, @Suppress("UNUSED_PARAMETER") unusedp: Nothing? = null): CodingKey, RawRepresentable<String> {
        stateGrid("stateGrid"),
        valueGrid("valueGrid"),
        currentPiece("currentPiece"),
        nextPiece("nextPiece"),
        score("score"),
        dropsThisLevel("dropsThisLevel"),
        level("level"),
        isGameOver("isGameOver"),
        difficultyRaw("difficultyRaw");

        @androidx.annotation.Keep
        companion object {
            fun init(rawValue: String): CodingKeys? {
                return when (rawValue) {
                    "stateGrid" -> CodingKeys.stateGrid
                    "valueGrid" -> CodingKeys.valueGrid
                    "currentPiece" -> CodingKeys.currentPiece
                    "nextPiece" -> CodingKeys.nextPiece
                    "score" -> CodingKeys.score
                    "dropsThisLevel" -> CodingKeys.dropsThisLevel
                    "level" -> CodingKeys.level
                    "isGameOver" -> CodingKeys.isGameOver
                    "difficultyRaw" -> CodingKeys.difficultyRaw
                    else -> null
                }
            }
        }
    }

    override fun encode(to: Encoder) {
        val container = to.container(keyedBy = CodingKeys::class)
        container.encode(stateGrid, forKey = CodingKeys.stateGrid)
        container.encode(valueGrid, forKey = CodingKeys.valueGrid)
        container.encode(currentPiece, forKey = CodingKeys.currentPiece)
        container.encode(nextPiece, forKey = CodingKeys.nextPiece)
        container.encode(score, forKey = CodingKeys.score)
        container.encode(dropsThisLevel, forKey = CodingKeys.dropsThisLevel)
        container.encode(level, forKey = CodingKeys.level)
        container.encode(isGameOver, forKey = CodingKeys.isGameOver)
        container.encode(difficultyRaw, forKey = CodingKeys.difficultyRaw)
    }

    constructor(from: Decoder) {
        val container = from.container(keyedBy = CodingKeys::class)
        this.stateGrid = container.decode(Array::class, elementType = Int::class, forKey = CodingKeys.stateGrid)
        this.valueGrid = container.decode(Array::class, elementType = Int::class, forKey = CodingKeys.valueGrid)
        this.currentPiece = container.decode(Int::class, forKey = CodingKeys.currentPiece)
        this.nextPiece = container.decode(Int::class, forKey = CodingKeys.nextPiece)
        this.score = container.decode(Int::class, forKey = CodingKeys.score)
        this.dropsThisLevel = container.decode(Int::class, forKey = CodingKeys.dropsThisLevel)
        this.level = container.decode(Int::class, forKey = CodingKeys.level)
        this.isGameOver = container.decode(Boolean::class, forKey = CodingKeys.isGameOver)
        this.difficultyRaw = container.decode(Int::class, forKey = CodingKeys.difficultyRaw)
    }

    @androidx.annotation.Keep
    companion object: DecodableCompanion<Drop7SavedState> {
        override fun init(from: Decoder): Drop7SavedState = Drop7SavedState(from = from)

        private fun CodingKeys(rawValue: String): CodingKeys? = CodingKeys.init(rawValue = rawValue)
    }
}

// MARK: - Animation Step Records

@Suppress("MUST_BE_INITIALIZED")
internal class Drop7ExplosionStep: MutableStruct {
    internal var exploded: Array<Int>
        get() = field.sref({ this.exploded = it })
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
            willmutate()
            field = newValue
            didmutate()
        }
    internal var explodedValues: Array<Int>
        get() = field.sref({ this.explodedValues = it })
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
            willmutate()
            field = newValue
            didmutate()
        }
    internal var revealed: Array<Int>
        get() = field.sref({ this.revealed = it })
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
            willmutate()
            field = newValue
            didmutate()
        }
    internal var fallSources: Array<Int>
        get() = field.sref({ this.fallSources = it })
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
            willmutate()
            field = newValue
            didmutate()
        }
    internal var scoreGained: Int
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    internal var stepNumber: Int
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    internal var screenCleared: Boolean
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }

    constructor(exploded: Array<Int>, explodedValues: Array<Int>, revealed: Array<Int>, fallSources: Array<Int>, scoreGained: Int, stepNumber: Int, screenCleared: Boolean) {
        this.exploded = exploded
        this.explodedValues = explodedValues
        this.revealed = revealed
        this.fallSources = fallSources
        this.scoreGained = scoreGained
        this.stepNumber = stepNumber
        this.screenCleared = screenCleared
    }

    override var supdate: ((Any) -> Unit)? = null
    override var smutatingcount = 0
    override fun scopy(): MutableStruct = Drop7ExplosionStep(exploded, explodedValues, revealed, fallSources, scoreGained, stepNumber, screenCleared)
}

internal val drop7ScreenClearBonus: Int = 70_000

@Suppress("MUST_BE_INITIALIZED")
internal class Drop7AdvanceResult: MutableStruct {
    internal var didPushUp: Boolean
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    internal var gameOver: Boolean
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    internal var fallSources: Array<Int>
        get() = field.sref({ this.fallSources = it })
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
            willmutate()
            field = newValue
            didmutate()
        }
    internal var levelBonusGained: Int
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }

    constructor(didPushUp: Boolean, gameOver: Boolean, fallSources: Array<Int>, levelBonusGained: Int) {
        this.didPushUp = didPushUp
        this.gameOver = gameOver
        this.fallSources = fallSources
        this.levelBonusGained = levelBonusGained
    }

    override var supdate: ((Any) -> Unit)? = null
    override var smutatingcount = 0
    override fun scopy(): MutableStruct = Drop7AdvanceResult(didPushUp, gameOver, fallSources, levelBonusGained)
}

// MARK: - Game Model

@Stable
internal class Drop7Model: Observable {
    internal var stateGrid: Array<Int>
        get() = _stateGrid.wrappedValue.sref({ this.stateGrid = it })
        set(newValue) {
            _stateGrid.wrappedValue = newValue.sref()
        }
    internal var _stateGrid: skip.model.Observed<Array<Int>> = skip.model.Observed(Array(repeating = 0, count = gridCols * gridRows))
    internal var valueGrid: Array<Int>
        get() = _valueGrid.wrappedValue.sref({ this.valueGrid = it })
        set(newValue) {
            _valueGrid.wrappedValue = newValue.sref()
        }
    internal var _valueGrid: skip.model.Observed<Array<Int>> = skip.model.Observed(Array(repeating = 0, count = gridCols * gridRows))

    internal var currentPiece: Int
        get() = _currentPiece.wrappedValue
        set(newValue) {
            _currentPiece.wrappedValue = newValue
        }
    internal var _currentPiece: skip.model.Observed<Int> = skip.model.Observed(1)
    internal var nextPiece: Int
        get() = _nextPiece.wrappedValue
        set(newValue) {
            _nextPiece.wrappedValue = newValue
        }
    internal var _nextPiece: skip.model.Observed<Int> = skip.model.Observed(1)
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
    internal var _highScore: skip.model.Observed<Int> = skip.model.Observed(UserDefaults.standard.integer(forKey = "drop7_highscore"))
    internal var dropsThisLevel: Int
        get() = _dropsThisLevel.wrappedValue
        set(newValue) {
            _dropsThisLevel.wrappedValue = newValue
        }
    internal var _dropsThisLevel: skip.model.Observed<Int> = skip.model.Observed(0)
    internal var level: Int
        get() = _level.wrappedValue
        set(newValue) {
            _level.wrappedValue = newValue
        }
    internal var _level: skip.model.Observed<Int> = skip.model.Observed(1)
    internal var lastChainCount: Int
        get() = _lastChainCount.wrappedValue
        set(newValue) {
            _lastChainCount.wrappedValue = newValue
        }
    internal var _lastChainCount: skip.model.Observed<Int> = skip.model.Observed(0)
    internal var isGameOver: Boolean
        get() = _isGameOver.wrappedValue
        set(newValue) {
            _isGameOver.wrappedValue = newValue
        }
    internal var _isGameOver: skip.model.Observed<Boolean> = skip.model.Observed(false)
    internal var difficulty: Drop7Difficulty
        get() = _difficulty.wrappedValue
        set(newValue) {
            _difficulty.wrappedValue = newValue
        }
    internal var _difficulty: skip.model.Observed<Drop7Difficulty> = skip.model.Observed(Drop7Difficulty.normal)

    internal fun cellIndex(row: Int, col: Int): Int = row * gridCols + col

    internal fun getState(row: Int, col: Int): Int = stateGrid[row * gridCols + col]

    internal fun getValue(row: Int, col: Int): Int = valueGrid[row * gridCols + col]

    internal fun newGame(diff: Drop7Difficulty? = null) {
        diff?.let { d ->
            difficulty = d
        }
        stateGrid = Array(repeating = 0, count = gridCols * gridRows)
        valueGrid = Array(repeating = 0, count = gridCols * gridRows)
        score = 0
        dropsThisLevel = 0
        level = 1
        lastChainCount = 0
        isGameOver = false

        val startRows = difficulty.startingRows
        var r = gridRows - startRows
        while (r < gridRows) {
            var c = 0
            while (c < gridCols) {
                val idx = cellIndex(r, c)
                stateGrid[idx] = stateWrapped
                valueGrid[idx] = Int.random(in_ = 1..7)
                c += 1
            }
            r += 1
        }
        currentPiece = randomPieceValue()
        nextPiece = randomPieceValue()
    }

    internal fun randomPieceValue(): Int = Int.random(in_ = 1..7)

    /// Place currentPiece into the bottom-most empty cell of the given column.
    /// Returns the cell index of the landing position, or -1 if invalid.
    internal fun placePiece(column: Int): Int {
        val col = column
        if (isGameOver) {
            return -1
        }
        if (col < 0 || col >= gridCols) {
            return -1
        }
        var landingRow: Int = -1
        var r = gridRows - 1
        while (r >= 0) {
            if (stateGrid[cellIndex(r, col)] == stateEmpty) {
                landingRow = r
                break
            }
            r -= 1
        }
        if (landingRow < 0) {
            return -1
        }
        val idx = cellIndex(landingRow, col)
        stateGrid[idx] = stateNormal
        valueGrid[idx] = currentPiece
        return idx
    }

    /// If any cells should explode given the current grid, perform one explosion+settle step.
    /// Returns the step description, or nil if nothing exploded.
    internal fun runOneExplosionStep(stepNumber: Int): Drop7ExplosionStep? {
        val exploding = findExploding()
        if (exploding.isEmpty) {
            return null
        }

        var explodedValues: Array<Int> = arrayOf()
        for (ex in exploding.sref()) {
            explodedValues.append(valueGrid[ex])
        }

        val bonus = chainBonus(forStep = stepNumber)
        val gained = bonus * exploding.count
        score += gained

        // Reveal/crack neighbors before clearing
        val explodingSet = Set(exploding)
        var revealed: Array<Int> = arrayOf()
        for (ex in exploding.sref()) {
            val er = ex / gridCols
            val ec = ex % gridCols
            val nrs: Array<Int> = arrayOf(er - 1, er + 1, er, er)
            val ncs: Array<Int> = arrayOf(ec, ec, ec - 1, ec + 1)
            var k = 0
            while (k < 4) {
                val nr = nrs[k]
                val nc = ncs[k]
                k += 1
                if (nr < 0 || nr >= gridRows || nc < 0 || nc >= gridCols) {
                    continue
                }
                val nidx = cellIndex(nr, nc)
                if (explodingSet.contains(nidx)) {
                    continue
                }
                val st = stateGrid[nidx]
                if (st == stateWrapped) {
                    stateGrid[nidx] = stateCracked
                    revealed.append(nidx)
                } else if (st == stateCracked) {
                    stateGrid[nidx] = stateNormal
                    revealed.append(nidx)
                }
            }
        }

        // Clear exploded cells
        for (ex in exploding.sref()) {
            stateGrid[ex] = stateEmpty
            valueGrid[ex] = 0
        }

        // Settle with tracking
        val fallSources = settleWithTracking()

        // Screen-clear bonus: if the board is now empty, award the standard 70,000.
        var totalGained = gained
        var cleared = false
        if (isBoardEmpty()) {
            score += drop7ScreenClearBonus
            totalGained += drop7ScreenClearBonus
            cleared = true
        }

        return Drop7ExplosionStep(exploded = exploding, explodedValues = explodedValues, revealed = revealed, fallSources = fallSources, scoreGained = totalGained, stepNumber = stepNumber, screenCleared = cleared)
    }

    internal fun isBoardEmpty(): Boolean {
        var i = 0
        while (i < stateGrid.count) {
            if (stateGrid[i] != stateEmpty) {
                return false
            }
            i += 1
        }
        return true
    }

    private fun findExploding(): Array<Int> {
        // Drop 7 rule: a disc explodes when its value matches the length of the
        // contiguous run of non-empty cells (including wrapped/cracked) that
        // contains it, in either its row or its column.
        var result: Array<Int> = arrayOf()
        var r = 0
        while (r < gridRows) {
            var c = 0
            while (c < gridCols) {
                val idx = cellIndex(r, c)
                if (stateGrid[idx] == stateNormal) {
                    val v = valueGrid[idx]
                    if (v == rowRunLength(row = r, col = c) || v == colRunLength(row = r, col = c)) {
                        result.append(idx)
                    }
                }
                c += 1
            }
            r += 1
        }
        return result.sref()
    }

    private fun rowRunLength(row: Int, col: Int): Int {
        val r = row
        val c = col
        var length = 1
        var cc = c - 1
        while (cc >= 0 && stateGrid[cellIndex(r, cc)] != stateEmpty) {
            length += 1
            cc -= 1
        }
        cc = c + 1
        while (cc < gridCols && stateGrid[cellIndex(r, cc)] != stateEmpty) {
            length += 1
            cc += 1
        }
        return length
    }

    private fun colRunLength(row: Int, col: Int): Int {
        val r = row
        val c = col
        var length = 1
        var rr = r - 1
        while (rr >= 0 && stateGrid[cellIndex(rr, c)] != stateEmpty) {
            length += 1
            rr -= 1
        }
        rr = r + 1
        while (rr < gridRows && stateGrid[cellIndex(rr, c)] != stateEmpty) {
            length += 1
            rr += 1
        }
        return length
    }

    /// Apply gravity and return per-new-cell mapping of the original (pre-settle) cell index.
    /// fallSources[newIdx] == oldIdx if a disc fell there; -1 if cell is empty (or unchanged but empty).
    private fun settleWithTracking(): Array<Int> {
        var fallSources: Array<Int> = Array(repeating = -1, count = gridCols * gridRows)
        var c = 0
        while (c < gridCols) {
            var keepStates: Array<Int> = arrayOf()
            var keepValues: Array<Int> = arrayOf()
            var keepIndices: Array<Int> = arrayOf()
            var r = gridRows - 1
            while (r >= 0) {
                val idx = cellIndex(r, c)
                val st = stateGrid[idx]
                if (st != stateEmpty) {
                    keepStates.append(st)
                    keepValues.append(valueGrid[idx])
                    keepIndices.append(idx)
                }
                r -= 1
            }
            // Refill column from bottom
            var ri = gridRows - 1
            var i = 0
            while (i < keepStates.count) {
                val newIdx = cellIndex(ri, c)
                stateGrid[newIdx] = keepStates[i]
                valueGrid[newIdx] = keepValues[i]
                fallSources[newIdx] = keepIndices[i]
                ri -= 1
                i += 1
            }
            while (ri >= 0) {
                val idx = cellIndex(ri, c)
                stateGrid[idx] = stateEmpty
                valueGrid[idx] = 0
                ri -= 1
            }
            c += 1
        }
        return fallSources.sref()
    }

    /// Drops required to trigger the next push-up at the current level.
    /// In Normal Drop 7 the cadence accelerates: 30 drops, then 29, 28, 27...
    /// down to a per-difficulty floor.
    internal fun currentLevelTarget(): Int {
        val base = difficulty.dropsPerLevel
        val floor = difficulty.minDropsPerLevel
        val target = base - (level - 1)
        return max(floor, target)
    }

    /// Increment the level counter. If a new level should start, push up.
    /// If nothing changes, returns a no-op result.
    internal fun advanceLevel(): Drop7AdvanceResult {
        dropsThisLevel += 1
        if (dropsThisLevel >= currentLevelTarget()) {
            dropsThisLevel = 0
            level += 1
            val bonus = difficulty.levelBonus
            score += bonus
            var result = performPushUp()
            result.levelBonusGained = bonus
            return result.sref()
        }
        return Drop7AdvanceResult(didPushUp = false, gameOver = false, fallSources = arrayOf(), levelBonusGained = 0)
    }

    private fun performPushUp(): Drop7AdvanceResult {
        // Game over if any cell in top row is non-empty (would be pushed off)
        var c = 0
        while (c < gridCols) {
            if (stateGrid[cellIndex(0, c)] != stateEmpty) {
                isGameOver = true
                saveHighScore()
                return Drop7AdvanceResult(didPushUp = false, gameOver = true, fallSources = arrayOf(), levelBonusGained = 0)
            }
            c += 1
        }
        var fallSources: Array<Int> = Array(repeating = -1, count = gridCols * gridRows)
        var r = 0
        while (r < gridRows - 1) {
            var cc = 0
            while (cc < gridCols) {
                val dst = cellIndex(r, cc)
                val src = cellIndex(r + 1, cc)
                stateGrid[dst] = stateGrid[src]
                valueGrid[dst] = valueGrid[src]
                if (stateGrid[dst] != stateEmpty) {
                    fallSources[dst] = src
                }
                cc += 1
            }
            r += 1
        }
        val bottom = gridRows - 1
        var c2 = 0
        while (c2 < gridCols) {
            val idx = cellIndex(bottom, c2)
            stateGrid[idx] = stateWrapped
            valueGrid[idx] = Int.random(in_ = 1..7)
            fallSources[idx] = fallFromBelowSentinel
            c2 += 1
        }
        return Drop7AdvanceResult(didPushUp = true, gameOver = false, fallSources = fallSources, levelBonusGained = 0)
    }

    internal fun advanceToNextPiece() {
        if (!isGameOver) {
            currentPiece = nextPiece
            nextPiece = randomPieceValue()
        }
    }

    internal fun canDropAnywhere(): Boolean {
        var c = 0
        while (c < gridCols) {
            if (stateGrid[cellIndex(0, c)] == stateEmpty) {
                return true
            }
            c += 1
        }
        return false
    }

    /// Synchronous full drop (used by tests / non-animated path).
    internal fun drop(column: Int): Boolean {
        val col = column
        val idx = placePiece(column = col)
        if (idx < 0) {
            return false
        }
        var totalChain = 0
        var step = 1
        while (runOneExplosionStep(stepNumber = step) != null) {
            step += 1
            totalChain += 1
        }
        val result = advanceLevel()
        if (result.didPushUp && !result.gameOver) {
            var s = 1
            while (runOneExplosionStep(stepNumber = s) != null) {
                s += 1
                totalChain += 1
            }
        }
        lastChainCount = totalChain
        if (!isGameOver && !canDropAnywhere()) {
            isGameOver = true
            saveHighScore()
        }
        if (!isGameOver) {
            advanceToNextPiece()
        }
        saveHighScore()
        return true
    }

    internal fun saveHighScore() {
        if (score > highScore) {
            highScore = score
            UserDefaults.standard.set(highScore, forKey = "drop7_highscore")
        }
    }

    // MARK: - State Persistence

    internal fun makeSavedState(): Drop7SavedState = Drop7SavedState(stateGrid = stateGrid, valueGrid = valueGrid, currentPiece = currentPiece, nextPiece = nextPiece, score = score, dropsThisLevel = dropsThisLevel, level = level, isGameOver = isGameOver, difficultyRaw = difficulty.rawValue)

    internal fun restoreState(s: Drop7SavedState) {
        stateGrid = s.stateGrid
        valueGrid = s.valueGrid
        currentPiece = s.currentPiece
        nextPiece = s.nextPiece
        score = s.score
        dropsThisLevel = s.dropsThisLevel
        level = s.level
        isGameOver = s.isGameOver
        difficulty = Drop7Difficulty(rawValue = s.difficultyRaw) ?: Drop7Difficulty.normal
        highScore = UserDefaults.standard.integer(forKey = "drop7_highscore")
        lastChainCount = 0
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
        UserDefaults.standard.set(json_0, forKey = "drop7_saved_state")
    }

    @androidx.annotation.Keep
    companion object {

        internal fun loadSavedState(): Drop7SavedState? {
            val json_1 = UserDefaults.standard.string(forKey = "drop7_saved_state")
            if (json_1 == null) {
                return null
            }
            val data_1 = json_1.data(using = StringEncoding.utf8)
            if (data_1 == null) {
                return null
            }
            return try { JSONDecoder().decode(Drop7SavedState::class, from = data_1) } catch (_: Throwable) { null }
        }

        internal fun clearSavedState(): Unit = UserDefaults.standard.removeObject(forKey = "drop7_saved_state")
    }
}

// MARK: - Game View

internal class Drop7GameView: View {
    internal var showInstructions: Boolean
        get() = _showInstructions.wrappedValue
        set(newValue) {
            _showInstructions.wrappedValue = newValue
        }
    internal var _showInstructions: Binding<Boolean>
    private var game: Drop7Model
        get() = _game.wrappedValue
        set(newValue) {
            _game.wrappedValue = newValue
        }
    private var _game: skip.ui.State<Drop7Model>
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

    // Animation pipeline state
    private var isAnimating: Boolean
        get() = _isAnimating.wrappedValue
        set(newValue) {
            _isAnimating.wrappedValue = newValue
        }
    private var _isAnimating: skip.ui.State<Boolean>
    private var currentChainStep: Int
        get() = _currentChainStep.wrappedValue
        set(newValue) {
            _currentChainStep.wrappedValue = newValue
        }
    private var _currentChainStep: skip.ui.State<Int>
    private var hasAdvancedThisDrop: Boolean
        get() = _hasAdvancedThisDrop.wrappedValue
        set(newValue) {
            _hasAdvancedThisDrop.wrappedValue = newValue
        }
    private var _hasAdvancedThisDrop: skip.ui.State<Boolean>
    private var measuredCellSize: Double
        get() = _measuredCellSize.wrappedValue
        set(newValue) {
            _measuredCellSize.wrappedValue = newValue
        }
    private var _measuredCellSize: skip.ui.State<Double>
    private var lastChainShown: Int
        get() = _lastChainShown.wrappedValue
        set(newValue) {
            _lastChainShown.wrappedValue = newValue
        }
    private var _lastChainShown: skip.ui.State<Int>
    private var chainPulse: Double
        get() = _chainPulse.wrappedValue
        set(newValue) {
            _chainPulse.wrappedValue = newValue
        }
    private var _chainPulse: skip.ui.State<Double>
    private var turnMaxChain: Int
        get() = _turnMaxChain.wrappedValue
        set(newValue) {
            _turnMaxChain.wrappedValue = newValue
        }
    private var _turnMaxChain: skip.ui.State<Int>
    private var screenClearOpacity: Double
        get() = _screenClearOpacity.wrappedValue
        set(newValue) {
            _screenClearOpacity.wrappedValue = newValue
        }
    private var _screenClearOpacity: skip.ui.State<Double>
    private var screenClearScale: Double
        get() = _screenClearScale.wrappedValue
        set(newValue) {
            _screenClearScale.wrappedValue = newValue
        }
    private var _screenClearScale: skip.ui.State<Double>

    // Per-cell animation arrays — sized to gridCols * gridRows (49)
    private var cellOffsetY: Array<Double>
        get() = _cellOffsetY.wrappedValue.sref({ this.cellOffsetY = it })
        set(newValue) {
            _cellOffsetY.wrappedValue = newValue.sref()
        }
    private var _cellOffsetY: skip.ui.State<Array<Double>>
    private var cellScale: Array<Double>
        get() = _cellScale.wrappedValue.sref({ this.cellScale = it })
        set(newValue) {
            _cellScale.wrappedValue = newValue.sref()
        }
    private var _cellScale: skip.ui.State<Array<Double>>
    private var cellScaleY: Array<Double>
        get() = _cellScaleY.wrappedValue.sref({ this.cellScaleY = it })
        set(newValue) {
            _cellScaleY.wrappedValue = newValue.sref()
        }
    private var _cellScaleY: skip.ui.State<Array<Double>>
    private var cellOpacity: Array<Double>
        get() = _cellOpacity.wrappedValue.sref({ this.cellOpacity = it })
        set(newValue) {
            _cellOpacity.wrappedValue = newValue.sref()
        }
    private var _cellOpacity: skip.ui.State<Array<Double>>

    // Ghost layer (renders the disc that's exploding while the model has already cleared it)
    private var ghostValue: Array<Int>
        get() = _ghostValue.wrappedValue.sref({ this.ghostValue = it })
        set(newValue) {
            _ghostValue.wrappedValue = newValue.sref()
        }
    private var _ghostValue: skip.ui.State<Array<Int>>
    private var ghostScale: Array<Double>
        get() = _ghostScale.wrappedValue.sref({ this.ghostScale = it })
        set(newValue) {
            _ghostScale.wrappedValue = newValue.sref()
        }
    private var _ghostScale: skip.ui.State<Array<Double>>
    private var ghostOpacity: Array<Double>
        get() = _ghostOpacity.wrappedValue.sref({ this.ghostOpacity = it })
        set(newValue) {
            _ghostOpacity.wrappedValue = newValue.sref()
        }
    private var _ghostOpacity: skip.ui.State<Array<Double>>

    // Burst ring overlay (a colored expanding ring at each exploded position)
    private var burstColor: Array<Int>
        get() = _burstColor.wrappedValue.sref({ this.burstColor = it })
        set(newValue) {
            _burstColor.wrappedValue = newValue.sref()
        }
    private var _burstColor: skip.ui.State<Array<Int>>
    private var burstScale: Array<Double>
        get() = _burstScale.wrappedValue.sref({ this.burstScale = it })
        set(newValue) {
            _burstScale.wrappedValue = newValue.sref()
        }
    private var _burstScale: skip.ui.State<Array<Double>>
    private var burstOpacity: Array<Double>
        get() = _burstOpacity.wrappedValue.sref({ this.burstOpacity = it })
        set(newValue) {
            _burstOpacity.wrappedValue = newValue.sref()
        }
    private var _burstOpacity: skip.ui.State<Array<Double>>

    // Reveal pulse (cells that just got cracked or revealed)
    private var revealScale: Array<Double>
        get() = _revealScale.wrappedValue.sref({ this.revealScale = it })
        set(newValue) {
            _revealScale.wrappedValue = newValue.sref()
        }
    private var _revealScale: skip.ui.State<Array<Double>>

    // Camera shake (for big chains and game over)
    private var shakeOffsetX: Double
        get() = _shakeOffsetX.wrappedValue
        set(newValue) {
            _shakeOffsetX.wrappedValue = newValue
        }
    private var _shakeOffsetX: skip.ui.State<Double>

    // Animation timers
    private var animTimers: Array<Timer>
        get() = _animTimers.wrappedValue.sref({ this.animTimers = it })
        set(newValue) {
            _animTimers.wrappedValue = newValue.sref()
        }
    private var _animTimers: skip.ui.State<Array<Timer>>

    internal lateinit var dismiss: DismissAction
    internal var settings: Drop7Settings
        get() = _settings.wrappedValue
        set(newValue) {
            _settings.wrappedValue = newValue
        }
    internal var _settings = skip.ui.Environment<Drop7Settings>()

    internal fun playHaptic(pattern: HapticPattern) {
        if (settings.vibrations) {
            HapticFeedback.play(pattern)
        }
    }

    override fun body(): View {
        return ComposeBuilder { composectx: ComposeContext ->
            GeometryReader { geo ->
                ComposeBuilder { composectx: ComposeContext ->
                    val availableWidth = geo.size.width - 32.0
                    val availableHeight = geo.size.height * 0.62
                    val cellByWidth = (availableWidth - cellGap * Double(gridCols + 1)) / Double(gridCols)
                    val cellByHeight = (availableHeight - cellGap * Double(gridRows + 1)) / Double(gridRows)
                    val cellSize = min(cellByWidth, cellByHeight)
                    val boardWidth = cellSize * Double(gridCols) + cellGap * Double(gridCols + 1)
                    val boardHeight = cellSize * Double(gridRows) + cellGap * Double(gridRows + 1)

                    VStack(spacing = 0.0) { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            hudView
                                .frame(height = 44.0).Compose(composectx)

                            HStack(spacing = 10.0) { ->
                                ComposeBuilder { composectx: ComposeContext ->
                                    scoreBox(label = "SCORE", value = displayedScore).Compose(composectx)
                                    levelBox(level = game.level, drops = game.dropsThisLevel, target = game.currentLevelTarget()).Compose(composectx)
                                    scoreBox(label = "BEST", value = displayedHighScore).Compose(composectx)
                                    ComposeResult.ok
                                }
                            }
                            .padding(Edge.Set.horizontal, 12.0)
                            .padding(Edge.Set.bottom, 10.0).Compose(composectx)

                            HStack(spacing = 12.0) { ->
                                ComposeBuilder { composectx: ComposeContext ->
                                    Text(LocalizedStringKey(stringLiteral = "NEXT"), bundle = Bundle.module)
                                        .font(Font.caption)
                                        .fontWeight(Font.Weight.bold)
                                        .foregroundStyle(Color.white.opacity(0.7)).Compose(composectx)

                                    miniDisc(value = game.currentPiece, size = 32.0).Compose(composectx)
                                    Image(systemName = "arrow.right")
                                        .foregroundStyle(Color.white.opacity(0.5)).Compose(composectx)
                                    miniDisc(value = game.nextPiece, size = 24.0).Compose(composectx)

                                    Spacer().Compose(composectx)

                                    if (lastChainShown >= 2) {
                                        Text({
                                            val str = LocalizedStringKey.StringInterpolation(literalCapacity = 0, interpolationCount = 0)
                                            str.appendLiteral("Chain x")
                                            str.appendInterpolation(lastChainShown)
                                            str.appendLiteral("!")
                                            LocalizedStringKey(stringInterpolation = str)
                                        }(), bundle = Bundle.module)
                                            .font(Font.caption)
                                            .fontWeight(Font.Weight.bold)
                                            .foregroundStyle(Color(red = 1.0, green = 0.85, blue = 0.3))
                                            .scaleEffect(1.0 + chainPulse * 0.3)
                                            .opacity(0.6 + chainPulse * 0.4).Compose(composectx)
                                    }
                                    ComposeResult.ok
                                }
                            }
                            .padding(Edge.Set.horizontal, 16.0)
                            .padding(Edge.Set.bottom, 8.0).Compose(composectx)

                            Spacer(minLength = 0.0).Compose(composectx)

                            ZStack { ->
                                ComposeBuilder { composectx: ComposeContext ->
                                    RoundedRectangle(cornerRadius = 10.0)
                                        .fill(boardBackground)
                                        .frame(width = boardWidth, height = boardHeight).Compose(composectx)

                                    // Cells layer (with offsets/scales)
                                    cellsLayer(cellSize = cellSize, boardWidth = boardWidth, boardHeight = boardHeight).Compose(composectx)

                                    // Effects layer (ghost discs + burst rings, not hit-testable)
                                    effectsLayer(cellSize = cellSize, boardWidth = boardWidth, boardHeight = boardHeight)
                                        .allowsHitTesting(false).Compose(composectx)

                                    // Screen-clear banner
                                    if (screenClearOpacity > 0.0) {
                                        screenClearBanner()
                                            .scaleEffect(screenClearScale)
                                            .opacity(screenClearOpacity)
                                            .allowsHitTesting(false).Compose(composectx)
                                    }

                                    if (game.isGameOver) {
                                        gameOverOverlay(width = boardWidth, height = boardHeight).Compose(composectx)
                                    }

                                    if (showPauseMenu && !game.isGameOver) {
                                        pauseMenuOverlay(width = boardWidth, height = boardHeight).Compose(composectx)
                                    }
                                    ComposeResult.ok
                                }
                            }
                            .offset(x = shakeOffsetX).Compose(composectx)

                            Spacer(minLength = 0.0).Compose(composectx)
                            ComposeResult.ok
                        }
                    }
                    .background(pageBackground.ignoresSafeArea())
                    .onAppear { -> measuredCellSize = cellSize }
                    .onChange(of = cellSize) { _, newValue -> measuredCellSize = newValue }.Compose(composectx)
                    ComposeResult.ok
                }
            }
            .navigationBarBackButtonHidden()
            .onAppear { ->
                if (!hasInitialized) {
                    hasInitialized = true
                    val matchtarget_0 = Drop7Model.loadSavedState()
                    if (matchtarget_0 != null) {
                        val s = matchtarget_0
                        game.restoreState(s)
                    } else {
                        showDifficultyPicker = true
                    }
                }
                displayedScore = game.score
                displayedHighScore = game.highScore
            }
            .onDisappear { ->
                stopScoreAnimation()
                cancelAllAnimTimers()
            }
            .onChange(of = game.score) { _, _ -> startScoreAnimation() }
            .onChange(of = game.highScore) { _, _ -> startScoreAnimation() }
            .sheet(isPresented = Binding({ _showSettings.wrappedValue }, { it -> _showSettings.wrappedValue = it })) { ->
                ComposeBuilder { composectx: ComposeContext ->
                    Drop7SettingsView(settings = settings)
                        .presentationDetents(setOf(PresentationDetent.medium, PresentationDetent.large)).Compose(composectx)
                    ComposeResult.ok
                }
            }
            .sheet(isPresented = Binding({ _showDifficultyPicker.wrappedValue }, { it -> _showDifficultyPicker.wrappedValue = it })) { ->
                ComposeBuilder { composectx: ComposeContext ->
                    Drop7DifficultyPickerView { d ->
                        Drop7Model.clearSavedState()
                        cancelAllAnimTimers()
                        resetAllAnimationState()
                        game.newGame(diff = d)
                        stopScoreAnimation()
                        displayedScore = 0
                        displayedHighScore = game.highScore
                        lastChainShown = 0
                        showDifficultyPicker = false
                        showPauseMenu = false
                        isAnimating = false
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
        val rememberedgame by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Drop7Model>, Any>) { mutableStateOf(_game) }
        _game = rememberedgame

        val rememberedshowSettings by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Boolean>, Any>) { mutableStateOf(_showSettings) }
        _showSettings = rememberedshowSettings

        val rememberedshowPauseMenu by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Boolean>, Any>) { mutableStateOf(_showPauseMenu) }
        _showPauseMenu = rememberedshowPauseMenu

        val rememberedshowDifficultyPicker by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Boolean>, Any>) { mutableStateOf(_showDifficultyPicker) }
        _showDifficultyPicker = rememberedshowDifficultyPicker

        val rememberedhasInitialized by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Boolean>, Any>) { mutableStateOf(_hasInitialized) }
        _hasInitialized = rememberedhasInitialized

        val remembereddisplayedScore by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Int>, Any>) { mutableStateOf(_displayedScore) }
        _displayedScore = remembereddisplayedScore

        val remembereddisplayedHighScore by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Int>, Any>) { mutableStateOf(_displayedHighScore) }
        _displayedHighScore = remembereddisplayedHighScore

        val rememberedscoreAnimTimer by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Timer?>, Any>) { mutableStateOf(_scoreAnimTimer) }
        _scoreAnimTimer = rememberedscoreAnimTimer

        val rememberedisAnimating by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Boolean>, Any>) { mutableStateOf(_isAnimating) }
        _isAnimating = rememberedisAnimating

        val rememberedcurrentChainStep by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Int>, Any>) { mutableStateOf(_currentChainStep) }
        _currentChainStep = rememberedcurrentChainStep

        val rememberedhasAdvancedThisDrop by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Boolean>, Any>) { mutableStateOf(_hasAdvancedThisDrop) }
        _hasAdvancedThisDrop = rememberedhasAdvancedThisDrop

        val rememberedmeasuredCellSize by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Double>, Any>) { mutableStateOf(_measuredCellSize) }
        _measuredCellSize = rememberedmeasuredCellSize

        val rememberedlastChainShown by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Int>, Any>) { mutableStateOf(_lastChainShown) }
        _lastChainShown = rememberedlastChainShown

        val rememberedchainPulse by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Double>, Any>) { mutableStateOf(_chainPulse) }
        _chainPulse = rememberedchainPulse

        val rememberedturnMaxChain by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Int>, Any>) { mutableStateOf(_turnMaxChain) }
        _turnMaxChain = rememberedturnMaxChain

        val rememberedscreenClearOpacity by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Double>, Any>) { mutableStateOf(_screenClearOpacity) }
        _screenClearOpacity = rememberedscreenClearOpacity

        val rememberedscreenClearScale by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Double>, Any>) { mutableStateOf(_screenClearScale) }
        _screenClearScale = rememberedscreenClearScale

        val rememberedcellOffsetY by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Array<Double>>, Any>) { mutableStateOf(_cellOffsetY) }
        _cellOffsetY = rememberedcellOffsetY

        val rememberedcellScale by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Array<Double>>, Any>) { mutableStateOf(_cellScale) }
        _cellScale = rememberedcellScale

        val rememberedcellScaleY by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Array<Double>>, Any>) { mutableStateOf(_cellScaleY) }
        _cellScaleY = rememberedcellScaleY

        val rememberedcellOpacity by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Array<Double>>, Any>) { mutableStateOf(_cellOpacity) }
        _cellOpacity = rememberedcellOpacity

        val rememberedghostValue by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Array<Int>>, Any>) { mutableStateOf(_ghostValue) }
        _ghostValue = rememberedghostValue

        val rememberedghostScale by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Array<Double>>, Any>) { mutableStateOf(_ghostScale) }
        _ghostScale = rememberedghostScale

        val rememberedghostOpacity by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Array<Double>>, Any>) { mutableStateOf(_ghostOpacity) }
        _ghostOpacity = rememberedghostOpacity

        val rememberedburstColor by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Array<Int>>, Any>) { mutableStateOf(_burstColor) }
        _burstColor = rememberedburstColor

        val rememberedburstScale by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Array<Double>>, Any>) { mutableStateOf(_burstScale) }
        _burstScale = rememberedburstScale

        val rememberedburstOpacity by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Array<Double>>, Any>) { mutableStateOf(_burstOpacity) }
        _burstOpacity = rememberedburstOpacity

        val rememberedrevealScale by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Array<Double>>, Any>) { mutableStateOf(_revealScale) }
        _revealScale = rememberedrevealScale

        val rememberedshakeOffsetX by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Double>, Any>) { mutableStateOf(_shakeOffsetX) }
        _shakeOffsetX = rememberedshakeOffsetX

        val rememberedanimTimers by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Array<Timer>>, Any>) { mutableStateOf(_animTimers) }
        _animTimers = rememberedanimTimers

        this.dismiss = EnvironmentValues.shared.dismiss
        _settings.wrappedValue = EnvironmentValues.shared.environmentObject(type = Drop7Settings::class)!!

        return super.Evaluate(context, options)
    }

    // MARK: - Cells layer

    internal fun cellsLayer(cellSize: Double, boardWidth: Double, boardHeight: Double): View {
        return HStack(spacing = cellGap) { ->
            ComposeBuilder { composectx: ComposeContext ->
                ForEach(0..<gridCols, id = { it }) { c ->
                    ComposeBuilder { composectx: ComposeContext ->
                        VStack(spacing = cellGap) { ->
                            ComposeBuilder { composectx: ComposeContext ->
                                ForEach(0..<gridRows, id = { it }) { r ->
                                    ComposeBuilder { composectx: ComposeContext ->
                                        cellView(row = r, col = c, size = cellSize)
                                            .onTapGesture { it -> performDrop(column = c) }.Compose(composectx)
                                        ComposeResult.ok
                                    }
                                }.Compose(composectx)
                                ComposeResult.ok
                            }
                        }
                        .padding(Edge.Set.vertical, cellGap).Compose(composectx)
                        ComposeResult.ok
                    }
                }.Compose(composectx)
                ComposeResult.ok
            }
        }
        .frame(width = boardWidth, height = boardHeight)
    }

    internal fun cellView(row: Int, col: Int, size: Double): View {
        val r = row
        val c = col
        val idx = r * gridCols + c
        val st = game.getState(r, c)
        val v = game.getValue(r, c)
        return ZStack { ->
            ComposeBuilder { composectx: ComposeContext ->
                RoundedRectangle(cornerRadius = discCornerRadius)
                    .fill(emptyCellColor)
                    .frame(width = size, height = size).Compose(composectx)

                if (st == stateNormal) {
                    discContent(value = v, size = size)
                        .scaleEffect(x = cellScale[idx], y = cellScale[idx] * cellScaleY[idx])
                        .opacity(cellOpacity[idx]).Compose(composectx)
                } else if (st == stateCracked) {
                    ZStack { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            Circle()
                                .fill(crackedColor)
                                .frame(width = size * 0.86, height = size * 0.86).Compose(composectx)
                            Text(LocalizedStringKey(stringLiteral = "?"))
                                .font(Font.system(size = size * 0.45, weight = Font.Weight.bold, design = Font.Design.rounded))
                                .foregroundStyle(Color.white.opacity(0.85)).Compose(composectx)
                            ComposeResult.ok
                        }
                    }
                    .scaleEffect(revealScale[idx]).Compose(composectx)
                } else if (st == stateWrapped) {
                    Circle()
                        .fill(wrappedColor)
                        .frame(width = size * 0.86, height = size * 0.86)
                        .overlay(Circle()
                            .stroke(Color.black.opacity(0.4), lineWidth = 1.0)
                            .frame(width = size * 0.86, height = size * 0.86))
                        .scaleEffect(revealScale[idx]).Compose(composectx)
                }
                ComposeResult.ok
            }
        }
        .frame(width = size, height = size)
        .offset(y = cellOffsetY[idx])
    }

    internal fun discContent(value: Int, size: Double): View {
        val v = value
        return ZStack { ->
            ComposeBuilder { composectx: ComposeContext ->
                Circle()
                    .fill(discColor(for_ = v))
                    .frame(width = size * 0.92, height = size * 0.92).Compose(composectx)
                // subtle highlight for depth
                Circle()
                    .fill(Color.white.opacity(0.18))
                    .frame(width = size * 0.5, height = size * 0.5)
                    .offset(x = -size * 0.14, y = -size * 0.14)
                    .blur(radius = 2.0).Compose(composectx)
                Text({
                    val str = LocalizedStringKey.StringInterpolation(literalCapacity = 0, interpolationCount = 0)
                    str.appendInterpolation(v)
                    LocalizedStringKey(stringInterpolation = str)
                }())
                    .font(Font.system(size = size * 0.5, weight = Font.Weight.heavy, design = Font.Design.rounded))
                    .foregroundStyle(discTextColor(for_ = v)).Compose(composectx)
                ComposeResult.ok
            }
        }
    }

    // MARK: - Effects layer (ghost discs + burst rings)

    internal fun effectsLayer(cellSize: Double, boardWidth: Double, boardHeight: Double): View {
        return HStack(spacing = cellGap) { ->
            ComposeBuilder { composectx: ComposeContext ->
                ForEach(0..<gridCols, id = { it }) { c ->
                    ComposeBuilder { composectx: ComposeContext ->
                        VStack(spacing = cellGap) { ->
                            ComposeBuilder { composectx: ComposeContext ->
                                ForEach(0..<gridRows, id = { it }) { r ->
                                    ComposeBuilder { composectx: ComposeContext ->
                                        effectCell(row = r, col = c, size = cellSize).Compose(composectx)
                                        ComposeResult.ok
                                    }
                                }.Compose(composectx)
                                ComposeResult.ok
                            }
                        }
                        .padding(Edge.Set.vertical, cellGap).Compose(composectx)
                        ComposeResult.ok
                    }
                }.Compose(composectx)
                ComposeResult.ok
            }
        }
        .frame(width = boardWidth, height = boardHeight)
    }

    internal fun effectCell(row: Int, col: Int, size: Double): View {
        val r = row
        val c = col
        val idx = r * gridCols + c
        return ZStack { ->
            ComposeBuilder { composectx: ComposeContext ->
                // Burst ring — expanding outline at the exploded cell's color
                if (burstOpacity[idx] > 0.0) {
                    Circle()
                        .stroke(discColor(for_ = burstColor[idx]), lineWidth = 3.0)
                        .frame(width = size * 0.92, height = size * 0.92)
                        .scaleEffect(burstScale[idx])
                        .opacity(burstOpacity[idx]).Compose(composectx)
                    Circle()
                        .fill(discColor(for_ = burstColor[idx]).opacity(0.25))
                        .frame(width = size * 0.92, height = size * 0.92)
                        .scaleEffect(burstScale[idx] * 0.7)
                        .opacity(burstOpacity[idx]).Compose(composectx)
                }
                // Ghost disc — the original disc shrinking/fading away
                if (ghostOpacity[idx] > 0.0 && ghostValue[idx] > 0) {
                    discContent(value = ghostValue[idx], size = size)
                        .scaleEffect(ghostScale[idx])
                        .opacity(ghostOpacity[idx]).Compose(composectx)
                }
                ComposeResult.ok
            }
        }
        .frame(width = size, height = size)
    }

    // MARK: - Animation pipeline

    internal fun performDrop(column: Int) {
        val c = column
        if (isAnimating || game.isGameOver || showPauseMenu) {
            return
        }
        val idx = game.placePiece(column = c)
        if (idx < 0) {
            playHaptic(HapticPattern.impact)
            return
        }
        isAnimating = true
        hasAdvancedThisDrop = false
        currentChainStep = 1
        turnMaxChain = 0
        startDropAnimation(idx = idx)
    }

    internal fun startDropAnimation(idx: Int) {
        val row = idx / gridCols
        val stepHeight = measuredCellSize + cellGap

        // Reset cell anim values for this cell
        cellScale[idx] = 1.0
        cellScaleY[idx] = 1.0
        cellOpacity[idx] = 1.0
        // Start above the board so the piece visually falls in
        cellOffsetY[idx] = -(Double(row) + 1.5) * stepHeight

        // Pre-flight tick — small "click" feel as the piece is released
        playHaptic(HapticPattern(arrayOf(HapticEvent(HapticEventType.tick, intensity = 0.35))))

        val distance = Double(row) + 1.5
        val response = 0.18 + 0.04 * distance

        withAnimation(Animation.spring(response = response, dampingFraction = 0.78)) { -> cellOffsetY[idx] = 0.0 }

        scheduleAnim(after = response * 0.9) { -> onDropLanded(idx = idx, distance = distance) }
    }

    internal fun onDropLanded(idx: Int, distance: Double) {
        // Land haptic — heavier for deeper drops
        val intensity = min(1.0, 0.5 + distance * 0.06)
        playHaptic(HapticPattern(arrayOf(
            HapticEvent(HapticEventType.thud, intensity = intensity),
            HapticEvent(HapticEventType.tap, intensity = intensity * 0.7, delay = 0.04)
        )))

        // Squash
        withAnimation(Animation.easeOut(duration = 0.05)) { ->
            cellScaleY[idx] = 0.6
            cellScale[idx] = 1.15
        }
        scheduleAnim(after = 0.06) { ->
            withAnimation(Animation.spring(response = 0.22, dampingFraction = 0.45)) { ->
                cellScaleY[idx] = 1.0
                cellScale[idx] = 1.0
            }
            scheduleAnim(after = 0.10) { -> stepExplosionChain() }
        }
    }

    internal fun stepExplosionChain() {
        val matchtarget_1 = game.runOneExplosionStep(stepNumber = currentChainStep)
        if (matchtarget_1 != null) {
            val step = matchtarget_1
            animateExplosionStep(step = step)
        } else {
            afterChainComplete()
        }
    }

    internal fun animateExplosionStep(step: Drop7ExplosionStep) {
        val chainStep = step.stepNumber
        val count = step.exploded.count

        // Capture exploded values into ghost layer so the discs continue to be visible
        // even after the model has cleared them.
        var i = 0
        while (i < step.exploded.count) {
            val idx = step.exploded[i]
            ghostValue[idx] = step.explodedValues[i]
            ghostScale[idx] = 1.0
            ghostOpacity[idx] = 1.0
            burstColor[idx] = step.explodedValues[i]
            burstScale[idx] = 0.5
            burstOpacity[idx] = 0.0
            i += 1
        }

        // Phase 1: windup — ghost discs grow and brighten; burst opacity rises
        withAnimation(Animation.easeOut(duration = 0.08)) { ->
            for (ex in step.exploded.sref()) {
                ghostScale[ex] = 1.32
                burstOpacity[ex] = 1.0
            }
        }

        // Pre-pop tap haptic — quick anticipation
        playHaptic(HapticPattern(arrayOf(HapticEvent(HapticEventType.tap, intensity = 0.5))))

        // Camera shake for big chains
        if (chainStep >= 3) {
            triggerShake(intensity = min(1.0, 0.4 + 0.15 * Double(chainStep)))
        }

        scheduleAnim(after = 0.08) { ->
            // Phase 2: pop — ghost shrinks to nothing while burst expands and fades
            playExplosionHaptic(chainStep = chainStep, count = count)

            withAnimation(Animation.easeIn(duration = 0.16)) { ->
                for (ex in step.exploded.sref()) {
                    ghostScale[ex] = 0.0
                    ghostOpacity[ex] = 0.0
                }
            }
            withAnimation(Animation.easeOut(duration = 0.32)) { ->
                for (ex in step.exploded.sref()) {
                    burstScale[ex] = 2.6
                    burstOpacity[ex] = 0.0
                }
            }

            // Phase 2b (concurrent): reveal pulse for cracked / revealed neighbors
            if (!step.revealed.isEmpty) {
                for (rv in step.revealed.sref()) {
                    revealScale[rv] = 1.0
                }
                withAnimation(Animation.spring(response = 0.22, dampingFraction = 0.4)) { ->
                    for (rv in step.revealed.sref()) {
                        revealScale[rv] = 1.22
                    }
                }
                scheduleAnim(after = 0.04) { -> playHaptic(HapticPattern(arrayOf(HapticEvent(HapticEventType.tick, intensity = 0.55)))) }
                scheduleAnim(after = 0.18) { ->
                    withAnimation(Animation.spring(response = 0.18, dampingFraction = 0.55)) { ->
                        for (rv in step.revealed.sref()) {
                            revealScale[rv] = 1.0
                        }
                    }
                }
            }

            scheduleAnim(after = 0.20) { ->
                // Phase 3: settle — discs above empty spots fall into place
                // Reset exploded cells' anim values (they're now empty in the model)
                for (ex in step.exploded.sref()) {
                    cellScale[ex] = 1.0
                    cellOpacity[ex] = 1.0
                    cellScaleY[ex] = 1.0
                    ghostValue[ex] = 0
                    ghostScale[ex] = 0.0
                    ghostOpacity[ex] = 0.0
                    burstScale[ex] = 0.0
                    burstOpacity[ex] = 0.0
                }

                applyFallSourceOffsets(fallSources = step.fallSources)

                withAnimation(Animation.spring(response = 0.32, dampingFraction = 0.7)) { ->
                    var i = 0
                    while (i < cellOffsetY.count) {
                        cellOffsetY[i] = 0.0
                        i += 1
                    }
                }

                // Settle thud — a soft impact when discs land
                if (hasAnyFallSources(fallSources = step.fallSources)) {
                    scheduleAnim(after = 0.20) { -> playHaptic(HapticPattern(arrayOf(HapticEvent(HapticEventType.thud, intensity = 0.45)))) }
                }

                if (step.screenCleared) {
                    triggerScreenClearCelebration()
                }

                scheduleAnim(after = 0.32) { ->
                    currentChainStep += 1
                    stepExplosionChain()
                }
            }
        }
    }

    internal fun afterChainComplete() {
        val chainCount = currentChainStep - 1
        // Track the largest chain across both the drop's chain and any push-up
        // chain that follows in the same turn. Only re-pulse the indicator when
        // the running max actually grows — a smaller push-up chain shouldn't
        // overwrite a larger drop chain.
        if (chainCount > turnMaxChain) {
            turnMaxChain = chainCount
            if (turnMaxChain >= 2) {
                lastChainShown = turnMaxChain
                game.lastChainCount = turnMaxChain
                chainPulse = 1.0
                withAnimation(Animation.easeOut(duration = 0.6)) { -> chainPulse = 0.0 }
            }
        }

        if (!hasAdvancedThisDrop) {
            hasAdvancedThisDrop = true
            val result = game.advanceLevel()
            if (result.gameOver) {
                handleGameOver()
                return
            }
            if (result.didPushUp) {
                animatePushUp(result = result)
                return
            }
        }
        finalizeDrop()
    }

    internal fun animatePushUp(result: Drop7AdvanceResult) {
        val stepHeight = measuredCellSize + cellGap
        var i = 0
        while (i < cellOffsetY.count) {
            val src = result.fallSources[i]
            if (src == fallFromBelowSentinel) {
                // New bottom wrapped row — comes from below the board
                cellOffsetY[i] = stepHeight + measuredCellSize * 0.4
            } else if (src >= 0) {
                cellOffsetY[i] = stepHeight
            } else {
                cellOffsetY[i] = 0.0
            }
            i += 1
        }

        playHaptic(HapticPattern(arrayOf(
            HapticEvent(HapticEventType.rise, intensity = 0.7),
            HapticEvent(HapticEventType.thud, intensity = 0.5, delay = 0.12),
            HapticEvent(HapticEventType.thud, intensity = 0.6, delay = 0.06)
        )))

        withAnimation(Animation.spring(response = 0.45, dampingFraction = 0.78)) { ->
            var k = 0
            while (k < cellOffsetY.count) {
                cellOffsetY[k] = 0.0
                k += 1
            }
        }

        scheduleAnim(after = 0.46) { ->
            // Push-up may trigger explosions — re-enter the chain loop
            currentChainStep = 1
            stepExplosionChain()
        }
    }

    internal fun handleGameOver() {
        playHaptic(HapticPattern(arrayOf(
            HapticEvent(HapticEventType.thud, intensity = 1.0),
            HapticEvent(HapticEventType.thud, intensity = 1.0, delay = 0.08),
            HapticEvent(HapticEventType.fall, intensity = 1.0, delay = 0.16),
            HapticEvent(HapticEventType.thud, intensity = 0.9, delay = 0.18)
        )))
        triggerShake(intensity = 1.0)
        game.saveState()
        isAnimating = false
    }

    internal fun finalizeDrop() {
        if (!game.canDropAnywhere() && !game.isGameOver) {
            game.isGameOver = true
            game.saveHighScore()
            handleGameOver()
            return
        }
        if (!game.isGameOver) {
            game.advanceToNextPiece()
        }
        game.saveState()
        isAnimating = false
    }

    // MARK: - Helpers

    internal fun applyFallSourceOffsets(fallSources: Array<Int>) {
        val stepHeight = measuredCellSize + cellGap
        var i = 0
        while (i < fallSources.count) {
            val src = fallSources[i]
            if (src >= 0 && src != i) {
                val oldRow = src / gridCols
                val newRow = i / gridCols
                cellOffsetY[i] = Double(oldRow - newRow) * stepHeight
            } else {
                cellOffsetY[i] = 0.0
            }
            i += 1
        }
    }

    internal fun hasAnyFallSources(fallSources: Array<Int>): Boolean {
        var i = 0
        while (i < fallSources.count) {
            if (fallSources[i] >= 0 && fallSources[i] != i) {
                return true
            }
            i += 1
        }
        return false
    }

    internal fun resetAllAnimationState() {
        var i = 0
        while (i < gridCols * gridRows) {
            cellOffsetY[i] = 0.0
            cellScale[i] = 1.0
            cellScaleY[i] = 1.0
            cellOpacity[i] = 1.0
            ghostValue[i] = 0
            ghostScale[i] = 0.0
            ghostOpacity[i] = 0.0
            burstColor[i] = 0
            burstScale[i] = 0.0
            burstOpacity[i] = 0.0
            revealScale[i] = 1.0
            i += 1
        }
        shakeOffsetX = 0.0
        screenClearOpacity = 0.0
        screenClearScale = 0.5
        chainPulse = 0.0
        turnMaxChain = 0
    }

    internal fun scheduleAnim(after: Double, block: () -> Unit) {
        val delay = after
        val t = Timer.scheduledTimer(withTimeInterval = delay, repeats = false) { _ -> block() }
        animTimers.append(t)
    }

    internal fun cancelAllAnimTimers() {
        for (t in animTimers.sref()) {
            t.invalidate()
        }
        animTimers = arrayOf()
    }

    // MARK: - Camera shake

    internal fun triggerShake(intensity: Double) {
        // Rapid zigzag offsets that decay
        val amplitude = 8.0 * intensity
        withAnimation(Animation.linear(duration = 0.05)) { -> shakeOffsetX = amplitude }
        scheduleAnim(after = 0.05) { ->
            withAnimation(Animation.linear(duration = 0.05)) { -> shakeOffsetX = -amplitude * 0.8 }
            scheduleAnim(after = 0.05) { ->
                withAnimation(Animation.linear(duration = 0.05)) { -> shakeOffsetX = amplitude * 0.5 }
                scheduleAnim(after = 0.05) { ->
                    withAnimation(Animation.linear(duration = 0.06)) { -> shakeOffsetX = -amplitude * 0.3 }
                    scheduleAnim(after = 0.06) { ->
                        withAnimation(Animation.spring(response = 0.18, dampingFraction = 0.6)) { -> shakeOffsetX = 0.0 }
                    }
                }
            }
        }
    }

    // MARK: - Score animation

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
            stopScoreAnimation()
        }
    }

    internal fun stopScoreAnimation() {
        scoreAnimTimer?.invalidate()
        scoreAnimTimer = null
    }

    // MARK: - Haptics

    internal fun playExplosionHaptic(chainStep: Int, count: Int) {
        if (!settings.vibrations) {
            return
        }
        if (chainStep >= 5) {
            playMegaChainHaptic()
            return
        }
        val baseIntensity = min(1.0, 0.6 + Double(chainStep) * 0.1)
        val countBoost = min(0.3, Double(count) * 0.05)
        var events: Array<HapticEvent> = arrayOf()
        events.append(HapticEvent(HapticEventType.thud, intensity = min(1.0, baseIntensity + countBoost)))
        events.append(HapticEvent(HapticEventType.tap, intensity = baseIntensity, delay = 0.04))
        if (chainStep >= 2) {
            events.append(HapticEvent(HapticEventType.thud, intensity = baseIntensity * 0.85, delay = 0.05))
        }
        if (chainStep >= 3) {
            events.append(HapticEvent(HapticEventType.tick, intensity = baseIntensity * 0.6, delay = 0.04))
            events.append(HapticEvent(HapticEventType.tap, intensity = baseIntensity, delay = 0.04))
        }
        HapticFeedback.play(HapticPattern(events))
    }

    internal fun triggerScreenClearCelebration() {
        // Big haptic flourish for clearing the board
        playHaptic(HapticPattern(arrayOf(
            HapticEvent(HapticEventType.rise, intensity = 1.0),
            HapticEvent(HapticEventType.thud, intensity = 1.0, delay = 0.10),
            HapticEvent(HapticEventType.thud, intensity = 1.0, delay = 0.05),
            HapticEvent(HapticEventType.tap, intensity = 1.0, delay = 0.05),
            HapticEvent(HapticEventType.tick, intensity = 1.0, delay = 0.04),
            HapticEvent(HapticEventType.tap, intensity = 1.0, delay = 0.04),
            HapticEvent(HapticEventType.thud, intensity = 1.0, delay = 0.06),
            HapticEvent(HapticEventType.fall, intensity = 0.9, delay = 0.10)
        )))
        triggerShake(intensity = 1.0)

        // Banner pop-in
        screenClearScale = 0.5
        screenClearOpacity = 0.0
        withAnimation(Animation.spring(response = 0.35, dampingFraction = 0.55)) { ->
            screenClearScale = 1.0
            screenClearOpacity = 1.0
        }
        scheduleAnim(after = 1.4) { ->
            withAnimation(Animation.easeIn(duration = 0.4)) { ->
                screenClearOpacity = 0.0
                screenClearScale = 0.9
            }
        }
    }

    internal fun playMegaChainHaptic() {
        val events: Array<HapticEvent> = arrayOf(
            HapticEvent(HapticEventType.thud, intensity = 1.0),
            HapticEvent(HapticEventType.thud, intensity = 1.0, delay = 0.05),
            HapticEvent(HapticEventType.tap, intensity = 0.9, delay = 0.05),
            HapticEvent(HapticEventType.rise, intensity = 1.0, delay = 0.06),
            HapticEvent(HapticEventType.thud, intensity = 1.0, delay = 0.10),
            HapticEvent(HapticEventType.tick, intensity = 0.8, delay = 0.05),
            HapticEvent(HapticEventType.tap, intensity = 1.0, delay = 0.05),
            HapticEvent(HapticEventType.thud, intensity = 1.0, delay = 0.06),
            HapticEvent(HapticEventType.fall, intensity = 0.9, delay = 0.10)
        )
        HapticFeedback.play(HapticPattern(events))
    }

    // MARK: - Score / Level boxes

    internal fun scoreBox(label: String, value: Int): View {
        return VStack(spacing = 2.0) { ->
            ComposeBuilder { composectx: ComposeContext ->
                Text(label)
                    .font(Font.caption2)
                    .fontWeight(Font.Weight.bold)
                    .foregroundStyle(Color.white.opacity(0.7)).Compose(composectx)
                Text({
                    val str = LocalizedStringKey.StringInterpolation(literalCapacity = 0, interpolationCount = 0)
                    str.appendInterpolation(value)
                    LocalizedStringKey(stringInterpolation = str)
                }())
                    .font(Font.title3)
                    .fontWeight(Font.Weight.bold)
                    .foregroundStyle(Color.white)
                    .monospaced().Compose(composectx)
                ComposeResult.ok
            }
        }
        .frame(minWidth = 80.0)
        .padding(Edge.Set.vertical, 8.0)
        .padding(Edge.Set.horizontal, 12.0)
        .background(RoundedRectangle(cornerRadius = 8.0)
            .fill(Color.white.opacity(0.08)))
    }

    internal fun levelBox(level: Int, drops: Int, target: Int): View {
        return VStack(spacing = 2.0) { ->
            ComposeBuilder { composectx: ComposeContext ->
                Text(LocalizedStringKey(stringLiteral = "LEVEL"), bundle = Bundle.module)
                    .font(Font.caption2)
                    .fontWeight(Font.Weight.bold)
                    .foregroundStyle(Color.white.opacity(0.7)).Compose(composectx)
                Text({
                    val str = LocalizedStringKey.StringInterpolation(literalCapacity = 0, interpolationCount = 0)
                    str.appendInterpolation(level)
                    LocalizedStringKey(stringInterpolation = str)
                }())
                    .font(Font.title3)
                    .fontWeight(Font.Weight.bold)
                    .foregroundStyle(Color.white)
                    .monospaced().Compose(composectx)
                Text({
                    val str = LocalizedStringKey.StringInterpolation(literalCapacity = 0, interpolationCount = 0)
                    str.appendInterpolation(drops)
                    str.appendLiteral("/")
                    str.appendInterpolation(target)
                    LocalizedStringKey(stringInterpolation = str)
                }())
                    .font(Font.caption2)
                    .foregroundStyle(Color.white.opacity(0.55))
                    .monospaced().Compose(composectx)
                ComposeResult.ok
            }
        }
        .frame(minWidth = 70.0)
        .padding(Edge.Set.vertical, 6.0)
        .padding(Edge.Set.horizontal, 10.0)
        .background(RoundedRectangle(cornerRadius = 8.0)
            .fill(Color.white.opacity(0.08)))
    }

    // MARK: - Mini disc (for "next piece" UI)

    internal fun miniDisc(value: Int, size: Double): View {
        val v = value
        return ZStack { ->
            ComposeBuilder { composectx: ComposeContext ->
                Circle()
                    .fill(discColor(for_ = v))
                    .frame(width = size, height = size).Compose(composectx)
                Text({
                    val str = LocalizedStringKey.StringInterpolation(literalCapacity = 0, interpolationCount = 0)
                    str.appendInterpolation(v)
                    LocalizedStringKey(stringInterpolation = str)
                }())
                    .font(Font.system(size = size * 0.55, weight = Font.Weight.heavy, design = Font.Design.rounded))
                    .foregroundStyle(discTextColor(for_ = v)).Compose(composectx)
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
                                .foregroundStyle(Color.white).Compose(composectx)
                            ComposeResult.ok
                        }
                    }.Compose(composectx)

                    Spacer().Compose(composectx)

                    HStack(spacing = 0.0) { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            Text(LocalizedStringKey(stringLiteral = "Drop 7"), bundle = Bundle.module)
                                .font(Font.title)
                                .fontWeight(Font.Weight.black)
                                .foregroundStyle(Color.white).Compose(composectx)
                            if (game.difficulty != Drop7Difficulty.normal) {
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

                    Button(action = { -> showPauseMenu = true }) { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            Image("pause_circle", bundle = Bundle.module)
                                .font(Font.title2)
                                .foregroundStyle(Color.white).Compose(composectx)
                            ComposeResult.ok
                        }
                    }.Compose(composectx)
                    ComposeResult.ok
                }
            }
            .padding(Edge.Set.horizontal, 12.0)
            .padding(Edge.Set.vertical, 6.0)
            .background(pageBackground)
        }

    // MARK: - Pause menu

    internal fun pauseMenuOverlay(width: Double, height: Double): View {
        return ZStack { ->
            ComposeBuilder { composectx: ComposeContext ->
                RoundedRectangle(cornerRadius = 10.0)
                    .fill(Color.black.opacity(0.85))
                    .frame(width = width, height = height).Compose(composectx)

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

    // MARK: - Screen-clear banner

    internal fun screenClearBanner(): View {
        return VStack(spacing = 4.0) { ->
            ComposeBuilder { composectx: ComposeContext ->
                Text(LocalizedStringKey(stringLiteral = "SCREEN CLEAR!"), bundle = Bundle.module)
                    .font(Font.system(size = 32.0, weight = Font.Weight.black, design = Font.Design.rounded))
                    .foregroundStyle(Color(red = 1.0, green = 0.85, blue = 0.30)).Compose(composectx)
                Text({
                    val str = LocalizedStringKey.StringInterpolation(literalCapacity = 0, interpolationCount = 0)
                    str.appendLiteral("+")
                    str.appendInterpolation(drop7ScreenClearBonus)
                    LocalizedStringKey(stringInterpolation = str)
                }())
                    .font(Font.system(size = 22.0, weight = Font.Weight.bold, design = Font.Design.rounded))
                    .foregroundStyle(Color.white)
                    .monospaced().Compose(composectx)
                ComposeResult.ok
            }
        }
        .padding(Edge.Set.horizontal, 24.0)
        .padding(Edge.Set.vertical, 16.0)
        .background(RoundedRectangle(cornerRadius = 14.0)
            .fill(Color.black.opacity(0.78))
            .overlay(RoundedRectangle(cornerRadius = 14.0)
                .stroke(Color(red = 1.0, green = 0.85, blue = 0.30), lineWidth = 2.0)))
    }

    // MARK: - Game over overlay

    internal fun gameOverOverlay(width: Double, height: Double): View {
        return ZStack { ->
            ComposeBuilder { composectx: ComposeContext ->
                RoundedRectangle(cornerRadius = 10.0)
                    .fill(Color.black.opacity(0.78))
                    .frame(width = width, height = height).Compose(composectx)

                VStack(spacing = 16.0) { ->
                    ComposeBuilder { composectx: ComposeContext ->
                        Text(LocalizedStringKey(stringLiteral = "Game Over!"), bundle = Bundle.module)
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
                                    str.appendInterpolation(displayedScore)
                                    LocalizedStringKey(stringInterpolation = str)
                                }())
                                    .font(Font.system(size = 44.0))
                                    .fontWeight(Font.Weight.bold)
                                    .foregroundStyle(Color.white)
                                    .monospaced().Compose(composectx)
                                ComposeResult.ok
                            }
                        }.Compose(composectx)

                        if (game.score >= game.highScore && game.score > 0) {
                            Text(LocalizedStringKey(stringLiteral = "New High Score!"), bundle = Bundle.module)
                                .font(Font.title3)
                                .fontWeight(Font.Weight.bold)
                                .foregroundStyle(Color(red = 0.95, green = 0.69, blue = 0.30)).Compose(composectx)
                        }

                        Button(action = { -> showDifficultyPicker = true }) { ->
                            ComposeBuilder { composectx: ComposeContext ->
                                Text(LocalizedStringKey(stringLiteral = "Try Again"), bundle = Bundle.module)
                                    .font(Font.headline)
                                    .fontWeight(Font.Weight.bold)
                                    .foregroundStyle(Color.white)
                                    .frame(width = 160.0, height = 44.0)
                                    .background(RoundedRectangle(cornerRadius = 8.0)
                                        .fill(Color(red = 0.35, green = 0.55, blue = 0.95))).Compose(composectx)
                                ComposeResult.ok
                            }
                        }
                        .buttonStyle(ButtonStyle.plain).Compose(composectx)

                        ShareLink(item = "I scored ${game.score} in Drop 7 on Faire Games! Can you beat it?\nhttps://appfair.net", subject = Text(LocalizedStringKey(stringLiteral = "Drop 7 Score"), bundle = Bundle.module), message = Text({
                            val str = LocalizedStringKey.StringInterpolation(literalCapacity = 0, interpolationCount = 0)
                            str.appendLiteral("I scored ")
                            str.appendInterpolation(game.score)
                            str.appendLiteral(" in Drop 7!")
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
                }.Compose(composectx)
                ComposeResult.ok
            }
        }
    }

    private constructor(showInstructions: Binding<Boolean>, game: Drop7Model = Drop7Model(), showSettings: Boolean = false, showPauseMenu: Boolean = false, showDifficultyPicker: Boolean = false, hasInitialized: Boolean = false, displayedScore: Int = 0, displayedHighScore: Int = 0, scoreAnimTimer: Timer? = null, isAnimating: Boolean = false, currentChainStep: Int = 1, hasAdvancedThisDrop: Boolean = false, measuredCellSize: Double = 40.0, lastChainShown: Int = 0, chainPulse: Double = 0.0, turnMaxChain: Int = 0, screenClearOpacity: Double = 0.0, screenClearScale: Double = 0.5, cellOffsetY: Array<Double> = Array(repeating = 0.0, count = gridCols * gridRows), cellScale: Array<Double> = Array(repeating = 1.0, count = gridCols * gridRows), cellScaleY: Array<Double> = Array(repeating = 1.0, count = gridCols * gridRows), cellOpacity: Array<Double> = Array(repeating = 1.0, count = gridCols * gridRows), ghostValue: Array<Int> = Array(repeating = 0, count = gridCols * gridRows), ghostScale: Array<Double> = Array(repeating = 0.0, count = gridCols * gridRows), ghostOpacity: Array<Double> = Array(repeating = 0.0, count = gridCols * gridRows), burstColor: Array<Int> = Array(repeating = 0, count = gridCols * gridRows), burstScale: Array<Double> = Array(repeating = 0.0, count = gridCols * gridRows), burstOpacity: Array<Double> = Array(repeating = 0.0, count = gridCols * gridRows), revealScale: Array<Double> = Array(repeating = 1.0, count = gridCols * gridRows), shakeOffsetX: Double = 0.0, animTimers: Array<Timer> = arrayOf(), privatep: Nothing? = null) {
        this._showInstructions = showInstructions
        this._game = skip.ui.State(game)
        this._showSettings = skip.ui.State(showSettings)
        this._showPauseMenu = skip.ui.State(showPauseMenu)
        this._showDifficultyPicker = skip.ui.State(showDifficultyPicker)
        this._hasInitialized = skip.ui.State(hasInitialized)
        this._displayedScore = skip.ui.State(displayedScore)
        this._displayedHighScore = skip.ui.State(displayedHighScore)
        this._scoreAnimTimer = skip.ui.State(scoreAnimTimer)
        this._isAnimating = skip.ui.State(isAnimating)
        this._currentChainStep = skip.ui.State(currentChainStep)
        this._hasAdvancedThisDrop = skip.ui.State(hasAdvancedThisDrop)
        this._measuredCellSize = skip.ui.State(measuredCellSize)
        this._lastChainShown = skip.ui.State(lastChainShown)
        this._chainPulse = skip.ui.State(chainPulse)
        this._turnMaxChain = skip.ui.State(turnMaxChain)
        this._screenClearOpacity = skip.ui.State(screenClearOpacity)
        this._screenClearScale = skip.ui.State(screenClearScale)
        this._cellOffsetY = skip.ui.State(cellOffsetY.sref())
        this._cellScale = skip.ui.State(cellScale.sref())
        this._cellScaleY = skip.ui.State(cellScaleY.sref())
        this._cellOpacity = skip.ui.State(cellOpacity.sref())
        this._ghostValue = skip.ui.State(ghostValue.sref())
        this._ghostScale = skip.ui.State(ghostScale.sref())
        this._ghostOpacity = skip.ui.State(ghostOpacity.sref())
        this._burstColor = skip.ui.State(burstColor.sref())
        this._burstScale = skip.ui.State(burstScale.sref())
        this._burstOpacity = skip.ui.State(burstOpacity.sref())
        this._revealScale = skip.ui.State(revealScale.sref())
        this._shakeOffsetX = skip.ui.State(shakeOffsetX)
        this._animTimers = skip.ui.State(animTimers.sref())
    }

    constructor(showInstructions: Binding<Boolean>): this(showInstructions = showInstructions, privatep = null) {
    }
}

// MARK: - Preview Icon

class Drop7PreviewIcon: View {
    constructor() {
    }

    override fun body(): View {
        return ComposeBuilder { composectx: ComposeContext ->
            ZStack { ->
                ComposeBuilder { composectx: ComposeContext ->
                    pageBackground.Compose(composectx)

                    RoundedRectangle(cornerRadius = 4.0)
                        .fill(boardBackground)
                        .padding(8.0).Compose(composectx)

                    VStack(spacing = 2.0) { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            gridRow(values = arrayOf(0, 0, 0, 0, 0, 0, 0)).Compose(composectx)
                            gridRow(values = arrayOf(0, 0, 0, 0, 0, 0, 0)).Compose(composectx)
                            gridRow(values = arrayOf(0, 0, 0, 3, 0, 0, 0)).Compose(composectx)
                            gridRow(values = arrayOf(0, 0, 5, 2, 6, 0, 0)).Compose(composectx)
                            gridRow(values = arrayOf(0, 7, 1, 4, 1, 7, 0)).Compose(composectx)
                            gridRow(values = arrayOf(-1, 5, 6, 3, 2, 4, -1)).Compose(composectx)
                            gridRow(values = arrayOf(-1, -1, 7, -2, 5, -1, -1)).Compose(composectx)
                            ComposeResult.ok
                        }
                    }
                    .padding(12.0).Compose(composectx)
                    ComposeResult.ok
                }
            }
            .clipShape(RoundedRectangle(cornerRadius = 12.0)).Compose(composectx)
        }
    }

    internal fun gridRow(values: Array<Int>): View {
        return HStack(spacing = 2.0) { ->
            ComposeBuilder { composectx: ComposeContext ->
                ForEach(0..<values.count, id = { it }) { i ->
                    ComposeBuilder { composectx: ComposeContext ->
                        miniIconCell(value = values[i]).Compose(composectx)
                        ComposeResult.ok
                    }
                }.Compose(composectx)
                ComposeResult.ok
            }
        }
    }

    internal fun miniIconCell(value: Int): View {
        val v = value
        return ZStack { ->
            ComposeBuilder { composectx: ComposeContext ->
                RoundedRectangle(cornerRadius = 2.0)
                    .fill(emptyCellColor)
                    .frame(width = 12.0, height = 12.0).Compose(composectx)
                if (v > 0) {
                    Circle()
                        .fill(discColor(for_ = v))
                        .frame(width = 11.0, height = 11.0).Compose(composectx)
                } else if (v == -1) {
                    Circle()
                        .fill(wrappedColor)
                        .frame(width = 11.0, height = 11.0).Compose(composectx)
                } else if (v == -2) {
                    Circle()
                        .fill(crackedColor)
                        .frame(width = 11.0, height = 11.0).Compose(composectx)
                }
                ComposeResult.ok
            }
        }
    }

    @androidx.annotation.Keep
    companion object {
    }
}

// MARK: - Difficulty Picker

internal class Drop7DifficultyPickerView: View {
    internal val onSelect: (Drop7Difficulty) -> Unit
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
                                        .foregroundStyle(Color.white)
                                        .padding(Edge.Set.top, 10.0).Compose(composectx)

                                    ForEach(arrayOf(Drop7Difficulty.easy, Drop7Difficulty.normal, Drop7Difficulty.hard), id = { it.rawValue }) { d ->
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
                                                                        .foregroundStyle(Color.white).Compose(composectx)
                                                                    Text(d.description)
                                                                        .font(Font.caption)
                                                                        .foregroundStyle(Color.white.opacity(0.7)).Compose(composectx)
                                                                    ComposeResult.ok
                                                                }
                                                            }.Compose(composectx)
                                                            Spacer().Compose(composectx)
                                                            ComposeResult.ok
                                                        }
                                                    }
                                                    .padding(16.0)
                                                    .background(d.accentColor.opacity(0.18))
                                                    .cornerRadius(14.0)
                                                    .padding(1.0)
                                                    .background(d.accentColor.opacity(0.5))
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
                    .background(pageBackground.ignoresSafeArea())
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
            .preferredColorScheme(ColorScheme.dark).Compose(composectx)
        }
    }

    @Composable
    override fun Evaluate(context: ComposeContext, options: Int): kotlin.collections.List<Renderable> {
        this.dismiss = EnvironmentValues.shared.dismiss

        return super.Evaluate(context, options)
    }

    constructor(onSelect: (Drop7Difficulty) -> Unit) {
        this.onSelect = onSelect
    }
}

// MARK: - Settings

internal class Drop7SettingsView: View {
    internal var settings: Drop7Settings
        get() = _settings.wrappedValue
        set(newValue) {
            _settings.wrappedValue = newValue
        }
    internal var _settings: skip.ui.Bindable<Drop7Settings>
    internal lateinit var dismiss: DismissAction

    override fun body(): View {
        return ComposeBuilder { composectx: ComposeContext ->
            NavigationStack { ->
                ComposeBuilder { composectx: ComposeContext ->
                    Form { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            Section(header = Text(LocalizedStringKey(stringLiteral = "Drop 7"), bundle = Bundle.module)) { ->
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
                            Section(header = Text(LocalizedStringKey(stringLiteral = "Data"), bundle = Bundle.module)) { ->
                                ComposeBuilder { composectx: ComposeContext ->
                                    Button(role = ButtonRole.destructive, action = { -> resetDrop7HighScore() }) { ->
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

    constructor(settings: Drop7Settings) {
        this._settings = skip.ui.Bindable(settings)
    }
}

@Stable
open class Drop7Settings: Observable {
    open var vibrations: Boolean
        get() = _vibrations.wrappedValue
        set(newValue) {
            _vibrations.wrappedValue = newValue
            defaults.set(vibrations, forKey = "drop7Vibrations")
        }
    var _vibrations: skip.model.Observed<Boolean> = skip.model.Observed(defaults.value(forKey = "drop7Vibrations", default = true))

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
