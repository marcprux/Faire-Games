// Licensed under the GNU General Public License v2.0 or later
// SPDX-License-Identifier: GPL-2.0-or-later

package org.appfair.app.fairegames.games.sudoku

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

class SudokuContainerView: View {
    private var settings: SudokuSettings
        get() = _settings.wrappedValue
        set(newValue) {
            _settings.wrappedValue = newValue
        }
    private var _settings: skip.ui.State<SudokuSettings> = skip.ui.State(SudokuSettings())
    private var showInstructions: Boolean
        get() = _showInstructions.wrappedValue
        set(newValue) {
            _showInstructions.wrappedValue = newValue
        }
    private var _showInstructions: skip.ui.State<Boolean> = skip.ui.State(false)
    private val instructionsConfig = GameInstructionsConfig(key = "Sudoku.instructions", bundle = Bundle.module, firstLaunchKey = "instructionsShown_Sudoku", title = "Sudoku")

    constructor() {
    }

    override fun body(): View {
        return ComposeBuilder { composectx: ComposeContext ->
            SudokuGameView(showInstructions = Binding({ _showInstructions.wrappedValue }, { it -> _showInstructions.wrappedValue = it }))
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
        val rememberedsettings by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<SudokuSettings>, Any>) { mutableStateOf(_settings) }
        _settings = rememberedsettings

        val rememberedshowInstructions by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Boolean>, Any>) { mutableStateOf(_showInstructions) }
        _showInstructions = rememberedshowInstructions

        return super.Evaluate(context, options)
    }

    @androidx.annotation.Keep
    companion object {
    }
}

fun resetSudokuRecords() {
    UserDefaults.standard.removeObject(forKey = "sudoku_best_easy")
    UserDefaults.standard.removeObject(forKey = "sudoku_best_medium")
    UserDefaults.standard.removeObject(forKey = "sudoku_best_hard")
    UserDefaults.standard.removeObject(forKey = "sudoku_best_expert")
    UserDefaults.standard.removeObject(forKey = "sudoku_puzzles_solved")
}

// MARK: - Difficulty

@androidx.annotation.Keep
enum class SudokuDifficulty(override val rawValue: Int, @Suppress("UNUSED_PARAMETER") unusedp: Nothing? = null): CaseIterable, Identifiable<Int>, RawRepresentable<Int> {
    easy(0),
    medium(1),
    hard(2),
    expert(3);

    override val id: Int
        get() = rawValue

    internal val label: String
        get() {
            when (this) {
                SudokuDifficulty.easy -> return "Easy"
                SudokuDifficulty.medium -> return "Medium"
                SudokuDifficulty.hard -> return "Hard"
                SudokuDifficulty.expert -> return "Expert"
            }
        }

    /// Number of clues (filled cells) to leave in the puzzle.
    internal val cluesCount: Int
        get() {
            when (this) {
                SudokuDifficulty.easy -> return 46
                SudokuDifficulty.medium -> return 36
                SudokuDifficulty.hard -> return 30
                SudokuDifficulty.expert -> return 26
            }
        }

    internal val accentColor: Color
        get() {
            when (this) {
                SudokuDifficulty.easy -> return Color(red = 0.35, green = 0.75, blue = 0.45)
                SudokuDifficulty.medium -> return Color(red = 0.30, green = 0.60, blue = 0.95)
                SudokuDifficulty.hard -> return Color(red = 0.95, green = 0.55, blue = 0.15)
                SudokuDifficulty.expert -> return Color(red = 0.90, green = 0.30, blue = 0.40)
            }
        }

    internal val bestTimeKey: String
        get() {
            when (this) {
                SudokuDifficulty.easy -> return "sudoku_best_easy"
                SudokuDifficulty.medium -> return "sudoku_best_medium"
                SudokuDifficulty.hard -> return "sudoku_best_hard"
                SudokuDifficulty.expert -> return "sudoku_best_expert"
            }
        }

    /// Whether hints are available at this difficulty.
    internal val hintsEnabled: Boolean
        get() {
            when (this) {
                SudokuDifficulty.easy, SudokuDifficulty.medium -> return true
                SudokuDifficulty.hard, SudokuDifficulty.expert -> return false
            }
        }

    /// Easy mode grants unlimited hints; everything else is capped.
    internal val hasUnlimitedHints: Boolean
        get() = this == SudokuDifficulty.easy

    /// Starting hint budget. `Int.max` is used as the unlimited sentinel for Easy.
    internal val initialHints: Int
        get() {
            when (this) {
                SudokuDifficulty.easy -> return Int.max
                SudokuDifficulty.medium -> return 3
                SudokuDifficulty.hard, SudokuDifficulty.expert -> return 0
            }
        }

    /// Description shown in the difficulty picker.
    internal val detail: String
        get() {
            when (this) {
                SudokuDifficulty.easy -> return "${cluesCount} clues \u2022 unlimited hints"
                SudokuDifficulty.medium -> return "${cluesCount} clues \u2022 3 hints"
                SudokuDifficulty.hard -> return "${cluesCount} clues \u2022 no hints"
                SudokuDifficulty.expert -> return "${cluesCount} clues \u2022 no hints \u2022 no same-number highlight"
            }
        }

    @androidx.annotation.Keep
    companion object: CaseIterableCompanion<SudokuDifficulty> {
        fun init(rawValue: Int): SudokuDifficulty? {
            return when (rawValue) {
                0 -> SudokuDifficulty.easy
                1 -> SudokuDifficulty.medium
                2 -> SudokuDifficulty.hard
                3 -> SudokuDifficulty.expert
                else -> null
            }
        }

        override val allCases: Array<SudokuDifficulty>
            get() = arrayOf(easy, medium, hard, expert)
    }
}

fun SudokuDifficulty(rawValue: Int): SudokuDifficulty? = SudokuDifficulty.init(rawValue = rawValue)

// MARK: - Board Index Helpers

/// Cell index = row * 9 + col
private inline fun idx(row: Int, col: Int): Int = row * 9 + col

// MARK: - Puzzle Generator
//
// Strategy: start from a canonical valid solution and apply a series of
// structure-preserving random transformations (digit remap, row/col swaps
// within bands/stacks, band/stack swaps). The result is always a valid
// 9x9 Sudoku solution. We then remove cells symmetrically until we reach
// the target clue count for the given difficulty.

internal val canonicalSolution: Array<Int> = arrayOf(
    5,
    3,
    4,
    6,
    7,
    8,
    9,
    1,
    2,
    6,
    7,
    2,
    1,
    9,
    5,
    3,
    4,
    8,
    1,
    9,
    8,
    3,
    4,
    2,
    5,
    6,
    7,
    8,
    5,
    9,
    7,
    6,
    1,
    4,
    2,
    3,
    4,
    2,
    6,
    8,
    5,
    3,
    7,
    9,
    1,
    7,
    1,
    3,
    9,
    2,
    4,
    8,
    5,
    6,
    9,
    6,
    1,
    5,
    3,
    7,
    2,
    8,
    4,
    2,
    8,
    7,
    4,
    1,
    9,
    6,
    3,
    5,
    3,
    4,
    5,
    2,
    8,
    6,
    1,
    7,
    9
)

private fun shuffleDigits(grid: InOut<Array<Int>>) {
    // Random permutation of 1..9
    var perm = arrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9)
    perm.shuffle()
    val mapping: Array<Int> = (arrayOf(0) + perm).sref() // index 0 stays 0 (empty)
    for (i in 0..<grid.value.count) {
        grid.value[i] = mapping[grid.value[i]]
    }
}

/// True when the (possibly-incomplete) `grid` has no row, column, or 3×3 box
/// containing the same non-zero digit twice. A puzzle's starting clues MUST satisfy
/// this — otherwise the puzzle has no valid completion and the player is stuck.
/// Cells with value 0 are treated as empty and skipped.
internal fun isPuzzleConsistent(grid: Array<Int>): Boolean {
    if (grid.count != 81) {
        return false
    }
    for (r in 0..<9) {
        var seen = 0
        for (c in 0..<9) {
            val v = grid[idx(r, c)]
            if (v == 0) {
                continue
            }
            if (v < 1 || v > 9) {
                return false
            }
            val bit = 1 shl v
            if ((seen and bit) != 0) {
                return false
            }
            seen = seen or bit
        }
    }
    for (c in 0..<9) {
        var seen = 0
        for (r in 0..<9) {
            val v = grid[idx(r, c)]
            if (v == 0) {
                continue
            }
            val bit = 1 shl v
            if ((seen and bit) != 0) {
                return false
            }
            seen = seen or bit
        }
    }
    for (br in 0..<3) {
        for (bc in 0..<3) {
            var seen = 0
            for (r in (br * 3)..<(br * 3 + 3)) {
                for (c in (bc * 3)..<(bc * 3 + 3)) {
                    val v = grid[idx(r, c)]
                    if (v == 0) {
                        continue
                    }
                    val bit = 1 shl v
                    if ((seen and bit) != 0) {
                        return false
                    }
                    seen = seen or bit
                }
            }
        }
    }
    return true
}

/// True when `grid` is a fully-filled valid Sudoku solution.
internal fun isFullSudokuSolution(grid: Array<Int>): Boolean {
    if (grid.count != 81) {
        return false
    }
    for (v in grid.sref()) {
        if (v < 1 || v > 9) {
            return false
        }
    }
    return isPuzzleConsistent(grid)
}

private fun swapRows(grid: InOut<Array<Int>>, r1: Int, r2: Int) {
    for (c in 0..<9) {
        val tmp = grid.value[idx(r1, c)]
        grid.value[idx(r1, c)] = grid.value[idx(r2, c)]
        grid.value[idx(r2, c)] = tmp
    }
}

private fun swapCols(grid: InOut<Array<Int>>, c1: Int, c2: Int) {
    for (r in 0..<9) {
        val tmp = grid.value[idx(r, c1)]
        grid.value[idx(r, c1)] = grid.value[idx(r, c2)]
        grid.value[idx(r, c2)] = tmp
    }
}

private fun swapBands(grid: InOut<Array<Int>>, b1: Int, b2: Int) {
    for (i in 0..<3) {
        swapRows(InOut({ grid.value }, { grid.value = it }), b1 * 3 + i, b2 * 3 + i)
    }
}

private fun swapStacks(grid: InOut<Array<Int>>, s1: Int, s2: Int) {
    for (i in 0..<3) {
        swapCols(InOut({ grid.value }, { grid.value = it }), s1 * 3 + i, s2 * 3 + i)
    }
}

internal fun generateSolution(): Array<Int> {
    var grid = canonicalSolution.sref()
    // Remap digits
    shuffleDigits(InOut({ grid }, { grid = it }))
    // Do several random transformations
    for (unusedbinding in 0..<30) {
        val op = Int.random(in_ = 0..3)
        when (op) {
            0 -> {
                // Swap two rows in same band
                val band = Int.random(in_ = 0..2)
                val r1 = Int.random(in_ = 0..2)
                var r2 = Int.random(in_ = 0..2)
                while (r1 == r2) {
                    r2 = Int.random(in_ = 0..2)
                }
                swapRows(InOut({ grid }, { grid = it }), band * 3 + r1, band * 3 + r2)
            }
            1 -> {
                // Swap two cols in same stack
                val stack = Int.random(in_ = 0..2)
                val c1 = Int.random(in_ = 0..2)
                var c2 = Int.random(in_ = 0..2)
                while (c1 == c2) {
                    c2 = Int.random(in_ = 0..2)
                }
                swapCols(InOut({ grid }, { grid = it }), stack * 3 + c1, stack * 3 + c2)
            }
            2 -> {
                // Swap two bands
                val b1 = Int.random(in_ = 0..2)
                var b2 = Int.random(in_ = 0..2)
                while (b1 == b2) {
                    b2 = Int.random(in_ = 0..2)
                }
                swapBands(InOut({ grid }, { grid = it }), b1, b2)
            }
            else -> {
                // Swap two stacks
                val s1 = Int.random(in_ = 0..2)
                var s2 = Int.random(in_ = 0..2)
                while (s1 == s2) {
                    s2 = Int.random(in_ = 0..2)
                }
                swapStacks(InOut({ grid }, { grid = it }), s1, s2)
            }
        }
    }
    return grid.sref()
}

/// Build a starting puzzle by removing cells from a complete solution. The clue
/// count is approximate (driven by `targetClues` and 180° symmetric removal).
private fun removeCells(from: Array<Int>, targetClues: Int): Array<Int> {
    val solution = from
    var puzzle = solution.sref()
    var indices = Array(0..<81)
    indices.shuffle()
    var cluesRemaining = 81
    var i = 0
    while (cluesRemaining > targetClues && i < indices.count) {
        val cellIndex = indices[i].sref()
        i += 1
        if (puzzle[cellIndex] == 0) {
            continue
        }
        val mate = 80 - cellIndex // 180 rotation
        puzzle[cellIndex] = 0
        cluesRemaining -= 1
        if (cellIndex != mate && puzzle[mate] != 0 && cluesRemaining > targetClues) {
            puzzle[mate] = 0
            cluesRemaining -= 1
        }
    }
    return puzzle.sref()
}

private fun generatePuzzle(difficulty: SudokuDifficulty): Tuple2<Array<Int>, Array<Int>> {
    // Try to generate a fully-valid solution. The transformations applied by
    // `generateSolution` all preserve Sudoku validity, so this should succeed on
    // the first try — but we validate the output as a defensive check, retry on
    // failure, and finally fall back to the verified canonical solution so the
    // player can never end up staring at an unsolvable board.
    val targetClues = difficulty.cluesCount
    for (unusedbinding in 0..<5) {
        val solution = generateSolution()
        if (isFullSudokuSolution(solution)) {
            val puzzle = removeCells(from = solution, targetClues = targetClues)
            // The puzzle is `solution` with some cells erased, so consistency is
            // guaranteed mathematically — but assert anyway so any future change to
            // removal logic fails loudly in tests rather than silently in prod.
            if (isPuzzleConsistent(puzzle)) {
                return Tuple2(puzzle.sref(), solution.sref())
            }
        }
    }
    // Last-resort fallback: the hand-verified canonical solution. Always valid.
    val solution = canonicalSolution.sref()
    val puzzle = removeCells(from = solution, targetClues = targetClues)
    return Tuple2(puzzle.sref(), solution.sref())
}

// MARK: - Saved State

@androidx.annotation.Keep
@Suppress("MUST_BE_INITIALIZED")
internal class SudokuSavedState: Codable, MutableStruct {
    internal var values: Array<Int>
        get() = field.sref({ this.values = it })
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
            willmutate()
            field = newValue
            didmutate()
        }
    internal var isOriginal: Array<Boolean>
        get() = field.sref({ this.isOriginal = it })
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
            willmutate()
            field = newValue
            didmutate()
        }
    internal var solution: Array<Int>
        get() = field.sref({ this.solution = it })
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
            willmutate()
            field = newValue
            didmutate()
        }
    internal var notes: Array<Int>
        get() = field.sref({ this.notes = it })
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
            willmutate()
            field = newValue
            didmutate()
        }
    internal var isProvisional: Array<Boolean>
        get() = field.sref({ this.isProvisional = it })
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
            willmutate()
            field = newValue
            didmutate()
        }
    internal var isFilledByGiveUp: Array<Boolean>
        get() = field.sref({ this.isFilledByGiveUp = it })
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
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
    internal var hintsRemaining: Int
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    internal var elapsedSeconds: Int
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    internal var isComplete: Boolean
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    internal var hasGivenUp: Boolean
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    internal var checkpointActive: Boolean
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    internal var checkpointValues: Array<Int>
        get() = field.sref({ this.checkpointValues = it })
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
            willmutate()
            field = newValue
            didmutate()
        }
    internal var checkpointNotes: Array<Int>
        get() = field.sref({ this.checkpointNotes = it })
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
            willmutate()
            field = newValue
            didmutate()
        }
    internal var historyIndices: Array<Int>
        get() = field.sref({ this.historyIndices = it })
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
            willmutate()
            field = newValue
            didmutate()
        }
    internal var historyOldValues: Array<Int>
        get() = field.sref({ this.historyOldValues = it })
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
            willmutate()
            field = newValue
            didmutate()
        }
    internal var historyOldNotes: Array<Int>
        get() = field.sref({ this.historyOldNotes = it })
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
            willmutate()
            field = newValue
            didmutate()
        }
    internal var historyOldProvisional: Array<Boolean>
        get() = field.sref({ this.historyOldProvisional = it })
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
            willmutate()
            field = newValue
            didmutate()
        }
    internal var historyNewValues: Array<Int>
        get() = field.sref({ this.historyNewValues = it })
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
            willmutate()
            field = newValue
            didmutate()
        }
    internal var historyNewNotes: Array<Int>
        get() = field.sref({ this.historyNewNotes = it })
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
            willmutate()
            field = newValue
            didmutate()
        }
    internal var historyNewProvisional: Array<Boolean>
        get() = field.sref({ this.historyNewProvisional = it })
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
            willmutate()
            field = newValue
            didmutate()
        }
    internal var historyCursor: Int
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }

    constructor(values: Array<Int>, isOriginal: Array<Boolean>, solution: Array<Int>, notes: Array<Int>, isProvisional: Array<Boolean>, isFilledByGiveUp: Array<Boolean>, difficultyRaw: Int, hintsRemaining: Int, elapsedSeconds: Int, isComplete: Boolean, hasGivenUp: Boolean, checkpointActive: Boolean, checkpointValues: Array<Int>, checkpointNotes: Array<Int>, historyIndices: Array<Int>, historyOldValues: Array<Int>, historyOldNotes: Array<Int>, historyOldProvisional: Array<Boolean>, historyNewValues: Array<Int>, historyNewNotes: Array<Int>, historyNewProvisional: Array<Boolean>, historyCursor: Int) {
        this.values = values
        this.isOriginal = isOriginal
        this.solution = solution
        this.notes = notes
        this.isProvisional = isProvisional
        this.isFilledByGiveUp = isFilledByGiveUp
        this.difficultyRaw = difficultyRaw
        this.hintsRemaining = hintsRemaining
        this.elapsedSeconds = elapsedSeconds
        this.isComplete = isComplete
        this.hasGivenUp = hasGivenUp
        this.checkpointActive = checkpointActive
        this.checkpointValues = checkpointValues
        this.checkpointNotes = checkpointNotes
        this.historyIndices = historyIndices
        this.historyOldValues = historyOldValues
        this.historyOldNotes = historyOldNotes
        this.historyOldProvisional = historyOldProvisional
        this.historyNewValues = historyNewValues
        this.historyNewNotes = historyNewNotes
        this.historyNewProvisional = historyNewProvisional
        this.historyCursor = historyCursor
    }

    override var supdate: ((Any) -> Unit)? = null
    override var smutatingcount = 0
    override fun scopy(): MutableStruct = SudokuSavedState(values, isOriginal, solution, notes, isProvisional, isFilledByGiveUp, difficultyRaw, hintsRemaining, elapsedSeconds, isComplete, hasGivenUp, checkpointActive, checkpointValues, checkpointNotes, historyIndices, historyOldValues, historyOldNotes, historyOldProvisional, historyNewValues, historyNewNotes, historyNewProvisional, historyCursor)

    private enum class CodingKeys(override val rawValue: String, @Suppress("UNUSED_PARAMETER") unusedp: Nothing? = null): CodingKey, RawRepresentable<String> {
        values("values"),
        isOriginal("isOriginal"),
        solution("solution"),
        notes("notes"),
        isProvisional("isProvisional"),
        isFilledByGiveUp("isFilledByGiveUp"),
        difficultyRaw("difficultyRaw"),
        hintsRemaining("hintsRemaining"),
        elapsedSeconds("elapsedSeconds"),
        isComplete("isComplete"),
        hasGivenUp("hasGivenUp"),
        checkpointActive("checkpointActive"),
        checkpointValues("checkpointValues"),
        checkpointNotes("checkpointNotes"),
        historyIndices("historyIndices"),
        historyOldValues("historyOldValues"),
        historyOldNotes("historyOldNotes"),
        historyOldProvisional("historyOldProvisional"),
        historyNewValues("historyNewValues"),
        historyNewNotes("historyNewNotes"),
        historyNewProvisional("historyNewProvisional"),
        historyCursor("historyCursor");

        @androidx.annotation.Keep
        companion object {
            fun init(rawValue: String): CodingKeys? {
                return when (rawValue) {
                    "values" -> CodingKeys.values
                    "isOriginal" -> CodingKeys.isOriginal
                    "solution" -> CodingKeys.solution
                    "notes" -> CodingKeys.notes
                    "isProvisional" -> CodingKeys.isProvisional
                    "isFilledByGiveUp" -> CodingKeys.isFilledByGiveUp
                    "difficultyRaw" -> CodingKeys.difficultyRaw
                    "hintsRemaining" -> CodingKeys.hintsRemaining
                    "elapsedSeconds" -> CodingKeys.elapsedSeconds
                    "isComplete" -> CodingKeys.isComplete
                    "hasGivenUp" -> CodingKeys.hasGivenUp
                    "checkpointActive" -> CodingKeys.checkpointActive
                    "checkpointValues" -> CodingKeys.checkpointValues
                    "checkpointNotes" -> CodingKeys.checkpointNotes
                    "historyIndices" -> CodingKeys.historyIndices
                    "historyOldValues" -> CodingKeys.historyOldValues
                    "historyOldNotes" -> CodingKeys.historyOldNotes
                    "historyOldProvisional" -> CodingKeys.historyOldProvisional
                    "historyNewValues" -> CodingKeys.historyNewValues
                    "historyNewNotes" -> CodingKeys.historyNewNotes
                    "historyNewProvisional" -> CodingKeys.historyNewProvisional
                    "historyCursor" -> CodingKeys.historyCursor
                    else -> null
                }
            }
        }
    }

    override fun encode(to: Encoder) {
        val container = to.container(keyedBy = CodingKeys::class)
        container.encode(values, forKey = CodingKeys.values)
        container.encode(isOriginal, forKey = CodingKeys.isOriginal)
        container.encode(solution, forKey = CodingKeys.solution)
        container.encode(notes, forKey = CodingKeys.notes)
        container.encode(isProvisional, forKey = CodingKeys.isProvisional)
        container.encode(isFilledByGiveUp, forKey = CodingKeys.isFilledByGiveUp)
        container.encode(difficultyRaw, forKey = CodingKeys.difficultyRaw)
        container.encode(hintsRemaining, forKey = CodingKeys.hintsRemaining)
        container.encode(elapsedSeconds, forKey = CodingKeys.elapsedSeconds)
        container.encode(isComplete, forKey = CodingKeys.isComplete)
        container.encode(hasGivenUp, forKey = CodingKeys.hasGivenUp)
        container.encode(checkpointActive, forKey = CodingKeys.checkpointActive)
        container.encode(checkpointValues, forKey = CodingKeys.checkpointValues)
        container.encode(checkpointNotes, forKey = CodingKeys.checkpointNotes)
        container.encode(historyIndices, forKey = CodingKeys.historyIndices)
        container.encode(historyOldValues, forKey = CodingKeys.historyOldValues)
        container.encode(historyOldNotes, forKey = CodingKeys.historyOldNotes)
        container.encode(historyOldProvisional, forKey = CodingKeys.historyOldProvisional)
        container.encode(historyNewValues, forKey = CodingKeys.historyNewValues)
        container.encode(historyNewNotes, forKey = CodingKeys.historyNewNotes)
        container.encode(historyNewProvisional, forKey = CodingKeys.historyNewProvisional)
        container.encode(historyCursor, forKey = CodingKeys.historyCursor)
    }

    constructor(from: Decoder) {
        val container = from.container(keyedBy = CodingKeys::class)
        this.values = container.decode(Array::class, elementType = Int::class, forKey = CodingKeys.values)
        this.isOriginal = container.decode(Array::class, elementType = Boolean::class, forKey = CodingKeys.isOriginal)
        this.solution = container.decode(Array::class, elementType = Int::class, forKey = CodingKeys.solution)
        this.notes = container.decode(Array::class, elementType = Int::class, forKey = CodingKeys.notes)
        this.isProvisional = container.decode(Array::class, elementType = Boolean::class, forKey = CodingKeys.isProvisional)
        this.isFilledByGiveUp = container.decode(Array::class, elementType = Boolean::class, forKey = CodingKeys.isFilledByGiveUp)
        this.difficultyRaw = container.decode(Int::class, forKey = CodingKeys.difficultyRaw)
        this.hintsRemaining = container.decode(Int::class, forKey = CodingKeys.hintsRemaining)
        this.elapsedSeconds = container.decode(Int::class, forKey = CodingKeys.elapsedSeconds)
        this.isComplete = container.decode(Boolean::class, forKey = CodingKeys.isComplete)
        this.hasGivenUp = container.decode(Boolean::class, forKey = CodingKeys.hasGivenUp)
        this.checkpointActive = container.decode(Boolean::class, forKey = CodingKeys.checkpointActive)
        this.checkpointValues = container.decode(Array::class, elementType = Int::class, forKey = CodingKeys.checkpointValues)
        this.checkpointNotes = container.decode(Array::class, elementType = Int::class, forKey = CodingKeys.checkpointNotes)
        this.historyIndices = container.decode(Array::class, elementType = Int::class, forKey = CodingKeys.historyIndices)
        this.historyOldValues = container.decode(Array::class, elementType = Int::class, forKey = CodingKeys.historyOldValues)
        this.historyOldNotes = container.decode(Array::class, elementType = Int::class, forKey = CodingKeys.historyOldNotes)
        this.historyOldProvisional = container.decode(Array::class, elementType = Boolean::class, forKey = CodingKeys.historyOldProvisional)
        this.historyNewValues = container.decode(Array::class, elementType = Int::class, forKey = CodingKeys.historyNewValues)
        this.historyNewNotes = container.decode(Array::class, elementType = Int::class, forKey = CodingKeys.historyNewNotes)
        this.historyNewProvisional = container.decode(Array::class, elementType = Boolean::class, forKey = CodingKeys.historyNewProvisional)
        this.historyCursor = container.decode(Int::class, forKey = CodingKeys.historyCursor)
    }

    @androidx.annotation.Keep
    companion object: DecodableCompanion<SudokuSavedState> {
        override fun init(from: Decoder): SudokuSavedState = SudokuSavedState(from = from)

        private fun CodingKeys(rawValue: String): CodingKeys? = CodingKeys.init(rawValue = rawValue)
    }
}

// MARK: - Game Model

@Stable
internal class SudokuModel: Observable {
    // Board state — flat 81-element arrays for simple Skip-friendly mutation.
    internal var values: Array<Int>
        get() = _values.wrappedValue.sref({ this.values = it })
        set(newValue) {
            _values.wrappedValue = newValue.sref()
        }
    internal var _values: skip.model.Observed<Array<Int>> = skip.model.Observed(Array(repeating = 0, count = 81))
    internal var isOriginal: Array<Boolean>
        get() = _isOriginal.wrappedValue.sref({ this.isOriginal = it })
        set(newValue) {
            _isOriginal.wrappedValue = newValue.sref()
        }
    internal var _isOriginal: skip.model.Observed<Array<Boolean>> = skip.model.Observed(Array(repeating = false, count = 81))
    internal var solution: Array<Int>
        get() = _solution.wrappedValue.sref({ this.solution = it })
        set(newValue) {
            _solution.wrappedValue = newValue.sref()
        }
    internal var _solution: skip.model.Observed<Array<Int>> = skip.model.Observed(Array(repeating = 0, count = 81))
    /// Bitmask of candidate pencil marks per cell. Bit n (1-9) set means note n.
    internal var notes: Array<Int>
        get() = _notes.wrappedValue.sref({ this.notes = it })
        set(newValue) {
            _notes.wrappedValue = newValue.sref()
        }
    internal var _notes: skip.model.Observed<Array<Int>> = skip.model.Observed(Array(repeating = 0, count = 81))
    /// Whether each cell's value was placed during the current checkpoint session.
    internal var isProvisional: Array<Boolean>
        get() = _isProvisional.wrappedValue.sref({ this.isProvisional = it })
        set(newValue) {
            _isProvisional.wrappedValue = newValue.sref()
        }
    internal var _isProvisional: skip.model.Observed<Array<Boolean>> = skip.model.Observed(Array(repeating = false, count = 81))
    /// Whether each cell's value was inserted by Give Up (vs. placed by the user).
    /// Used after `hasGivenUp` to tint auto-filled cells distinctly from user-placed ones.
    internal var isFilledByGiveUp: Array<Boolean>
        get() = _isFilledByGiveUp.wrappedValue.sref({ this.isFilledByGiveUp = it })
        set(newValue) {
            _isFilledByGiveUp.wrappedValue = newValue.sref()
        }
    internal var _isFilledByGiveUp: skip.model.Observed<Array<Boolean>> = skip.model.Observed(Array(repeating = false, count = 81))

    // Interaction state
    internal var selectedIndex: Int?
        get() = _selectedIndex.wrappedValue
        set(newValue) {
            _selectedIndex.wrappedValue = newValue
        }
    internal var _selectedIndex: skip.model.Observed<Int?> = skip.model.Observed(null)
    internal var notesMode: Boolean
        get() = _notesMode.wrappedValue
        set(newValue) {
            _notesMode.wrappedValue = newValue
        }
    internal var _notesMode: skip.model.Observed<Boolean> = skip.model.Observed(false)

    // Checkpoint state
    internal var checkpointActive: Boolean
        get() = _checkpointActive.wrappedValue
        set(newValue) {
            _checkpointActive.wrappedValue = newValue
        }
    internal var _checkpointActive: skip.model.Observed<Boolean> = skip.model.Observed(false)
    /// Snapshot of values taken when checkpoint mode was entered (for revert).
    internal var checkpointValues: Array<Int>
        get() = _checkpointValues.wrappedValue.sref({ this.checkpointValues = it })
        set(newValue) {
            _checkpointValues.wrappedValue = newValue.sref()
        }
    internal var _checkpointValues: skip.model.Observed<Array<Int>> = skip.model.Observed(Array(repeating = 0, count = 81))
    internal var checkpointNotes: Array<Int>
        get() = _checkpointNotes.wrappedValue.sref({ this.checkpointNotes = it })
        set(newValue) {
            _checkpointNotes.wrappedValue = newValue.sref()
        }
    internal var _checkpointNotes: skip.model.Observed<Array<Int>> = skip.model.Observed(Array(repeating = 0, count = 81))

    // Progress
    internal var difficulty: SudokuDifficulty
        get() = _difficulty.wrappedValue
        set(newValue) {
            _difficulty.wrappedValue = newValue
        }
    internal var _difficulty: skip.model.Observed<SudokuDifficulty> = skip.model.Observed(SudokuDifficulty.medium)
    internal var hintsRemaining: Int
        get() = _hintsRemaining.wrappedValue
        set(newValue) {
            _hintsRemaining.wrappedValue = newValue
        }
    internal var _hintsRemaining: skip.model.Observed<Int> = skip.model.Observed(3)
    internal var elapsedSeconds: Int
        get() = _elapsedSeconds.wrappedValue
        set(newValue) {
            _elapsedSeconds.wrappedValue = newValue
        }
    internal var _elapsedSeconds: skip.model.Observed<Int> = skip.model.Observed(0)
    internal var isPaused: Boolean
        get() = _isPaused.wrappedValue
        set(newValue) {
            _isPaused.wrappedValue = newValue
        }
    internal var _isPaused: skip.model.Observed<Boolean> = skip.model.Observed(false)
    internal var isComplete: Boolean
        get() = _isComplete.wrappedValue
        set(newValue) {
            _isComplete.wrappedValue = newValue
        }
    internal var _isComplete: skip.model.Observed<Boolean> = skip.model.Observed(false)
    internal var hasGivenUp: Boolean
        get() = _hasGivenUp.wrappedValue
        set(newValue) {
            _hasGivenUp.wrappedValue = newValue
        }
    internal var _hasGivenUp: skip.model.Observed<Boolean> = skip.model.Observed(false)

    /// True when the game is finished and the board is locked (give-up only — no
    /// mistakes-based loss state exists). Kept as a computed alias of `hasGivenUp`
    /// so view conditionals read naturally.
    internal val isGameOver: Boolean
        get() = hasGivenUp

    // Records
    internal var bestEasy: Int
        get() = _bestEasy.wrappedValue
        set(newValue) {
            _bestEasy.wrappedValue = newValue
        }
    internal var _bestEasy: skip.model.Observed<Int> = skip.model.Observed(UserDefaults.standard.integer(forKey = "sudoku_best_easy"))
    internal var bestMedium: Int
        get() = _bestMedium.wrappedValue
        set(newValue) {
            _bestMedium.wrappedValue = newValue
        }
    internal var _bestMedium: skip.model.Observed<Int> = skip.model.Observed(UserDefaults.standard.integer(forKey = "sudoku_best_medium"))
    internal var bestHard: Int
        get() = _bestHard.wrappedValue
        set(newValue) {
            _bestHard.wrappedValue = newValue
        }
    internal var _bestHard: skip.model.Observed<Int> = skip.model.Observed(UserDefaults.standard.integer(forKey = "sudoku_best_hard"))
    internal var bestExpert: Int
        get() = _bestExpert.wrappedValue
        set(newValue) {
            _bestExpert.wrappedValue = newValue
        }
    internal var _bestExpert: skip.model.Observed<Int> = skip.model.Observed(UserDefaults.standard.integer(forKey = "sudoku_best_expert"))
    internal var puzzlesSolved: Int
        get() = _puzzlesSolved.wrappedValue
        set(newValue) {
            _puzzlesSolved.wrappedValue = newValue
        }
    internal var _puzzlesSolved: skip.model.Observed<Int> = skip.model.Observed(UserDefaults.standard.integer(forKey = "sudoku_puzzles_solved"))

    // Cursor-based history (unlimited undo/redo).
    // history[0..<cursor] are undoable; history[cursor..<count] are redoable.
    internal var historyIndices: Array<Int>
        get() = _historyIndices.wrappedValue.sref({ this.historyIndices = it })
        set(newValue) {
            _historyIndices.wrappedValue = newValue.sref()
        }
    internal var _historyIndices: skip.model.Observed<Array<Int>> = skip.model.Observed(arrayOf())
    internal var historyOldValues: Array<Int>
        get() = _historyOldValues.wrappedValue.sref({ this.historyOldValues = it })
        set(newValue) {
            _historyOldValues.wrappedValue = newValue.sref()
        }
    internal var _historyOldValues: skip.model.Observed<Array<Int>> = skip.model.Observed(arrayOf())
    internal var historyOldNotes: Array<Int>
        get() = _historyOldNotes.wrappedValue.sref({ this.historyOldNotes = it })
        set(newValue) {
            _historyOldNotes.wrappedValue = newValue.sref()
        }
    internal var _historyOldNotes: skip.model.Observed<Array<Int>> = skip.model.Observed(arrayOf())
    internal var historyOldProvisional: Array<Boolean>
        get() = _historyOldProvisional.wrappedValue.sref({ this.historyOldProvisional = it })
        set(newValue) {
            _historyOldProvisional.wrappedValue = newValue.sref()
        }
    internal var _historyOldProvisional: skip.model.Observed<Array<Boolean>> = skip.model.Observed(arrayOf())
    internal var historyNewValues: Array<Int>
        get() = _historyNewValues.wrappedValue.sref({ this.historyNewValues = it })
        set(newValue) {
            _historyNewValues.wrappedValue = newValue.sref()
        }
    internal var _historyNewValues: skip.model.Observed<Array<Int>> = skip.model.Observed(arrayOf())
    internal var historyNewNotes: Array<Int>
        get() = _historyNewNotes.wrappedValue.sref({ this.historyNewNotes = it })
        set(newValue) {
            _historyNewNotes.wrappedValue = newValue.sref()
        }
    internal var _historyNewNotes: skip.model.Observed<Array<Int>> = skip.model.Observed(arrayOf())
    internal var historyNewProvisional: Array<Boolean>
        get() = _historyNewProvisional.wrappedValue.sref({ this.historyNewProvisional = it })
        set(newValue) {
            _historyNewProvisional.wrappedValue = newValue.sref()
        }
    internal var _historyNewProvisional: skip.model.Observed<Array<Boolean>> = skip.model.Observed(arrayOf())
    internal var historyCursor: Int
        get() = _historyCursor.wrappedValue
        set(newValue) {
            _historyCursor.wrappedValue = newValue
        }
    internal var _historyCursor: skip.model.Observed<Int> = skip.model.Observed(0)

    internal val canUndo: Boolean
        get() = historyCursor > 0
    internal val canRedo: Boolean
        get() = historyCursor < historyIndices.count

    internal fun bestTime(for_: SudokuDifficulty): Int {
        val difficulty = for_
        when (difficulty) {
            SudokuDifficulty.easy -> return bestEasy
            SudokuDifficulty.medium -> return bestMedium
            SudokuDifficulty.hard -> return bestHard
            SudokuDifficulty.expert -> return bestExpert
        }
    }

    internal fun updateBestTime(seconds: Int, for_: SudokuDifficulty): Boolean {
        val difficulty = for_
        val current = bestTime(for_ = difficulty)
        if (current != 0 && seconds >= current) {
            return false
        }
        when (difficulty) {
            SudokuDifficulty.easy -> bestEasy = seconds
            SudokuDifficulty.medium -> bestMedium = seconds
            SudokuDifficulty.hard -> bestHard = seconds
            SudokuDifficulty.expert -> bestExpert = seconds
        }
        UserDefaults.standard.set(seconds, forKey = difficulty.bestTimeKey)
        return true
    }

    internal fun newGame(difficulty: SudokuDifficulty) {
        this.difficulty = difficulty
        val (puzzle, sol) = generatePuzzle(difficulty = difficulty)
        values = puzzle
        solution = sol
        isOriginal = puzzle.map { it -> it != 0 }
        notes = Array(repeating = 0, count = 81)
        isProvisional = Array(repeating = false, count = 81)
        isFilledByGiveUp = Array(repeating = false, count = 81)
        selectedIndex = null
        notesMode = false
        checkpointActive = false
        checkpointValues = Array(repeating = 0, count = 81)
        checkpointNotes = Array(repeating = 0, count = 81)
        hintsRemaining = difficulty.initialHints
        elapsedSeconds = 0
        isPaused = false
        isComplete = false
        hasGivenUp = false
        clearHistory()
    }

    private fun clearHistory() {
        historyIndices.removeAll()
        historyOldValues.removeAll()
        historyOldNotes.removeAll()
        historyOldProvisional.removeAll()
        historyNewValues.removeAll()
        historyNewNotes.removeAll()
        historyNewProvisional.removeAll()
        historyCursor = 0
    }

    // MARK: Cell queries

    /// Count of digit `d` placed anywhere on the board.
    internal fun placedCount(of: Int): Int {
        val d = of
        var count = 0
        for (v in values.sref()) {
            if (v == d) {
                count += 1
            }
        }
        return count
    }

    internal fun isPeer(a: Int, b: Int): Boolean {
        if (a == b) {
            return false
        }
        val ra = a / 9
        val ca = a % 9
        val rb = b / 9
        val cb = b % 9
        if (ra == rb) {
            return true
        }
        if (ca == cb) {
            return true
        }
        if (ra / 3 == rb / 3 && ca / 3 == cb / 3) {
            return true
        }
        return false
    }

    /// True when this cell holds a value that duplicates another value in the same
    /// row, column, or 3×3 box. Used by Easy mode to flag obviously-wrong placements.
    internal fun hasConflict(at: Int): Boolean {
        val index = at
        val v = values[index]
        if (v == 0) {
            return false
        }
        val row = index / 9
        val col = index % 9
        for (c in 0..<9) {
            if (c != col && values[idx(row, c)] == v) {
                return true
            }
        }
        for (r in 0..<9) {
            if (r != row && values[idx(r, col)] == v) {
                return true
            }
        }
        val boxRow = (row / 3) * 3
        val boxCol = (col / 3) * 3
        for (r in boxRow..<(boxRow + 3)) {
            for (c in boxCol..<(boxCol + 3)) {
                if ((r != row || c != col) && values[idx(r, c)] == v) {
                    return true
                }
            }
        }
        return false
    }

    // MARK: Notes bitmask helpers

    internal fun hasNote(cellIndex: Int, digit: Int): Boolean = (notes[cellIndex] and (1 shl digit)) != 0

    internal fun toggleNote(cellIndex: Int, digit: Int) {
        notes[cellIndex] = notes[cellIndex] xor (1 shl digit)
    }

    internal fun clearNotes(cellIndex: Int) {
        notes[cellIndex] = 0
    }

    /// Remove `digit` from the notes of all peers of `cellIndex`.
    internal fun clearPeerNotes(of: Int, digit: Int) {
        val cellIndex = of
        val row = cellIndex / 9
        val col = cellIndex % 9
        val mask = (1 shl digit).inv()
        for (c in 0..<9) {
            val ri = idx(row, c)
            notes[ri] = notes[ri] and mask
        }
        for (r in 0..<9) {
            val ci = idx(r, col)
            notes[ci] = notes[ci] and mask
        }
        val boxRow = (row / 3) * 3
        val boxCol = (col / 3) * 3
        for (r in boxRow..<(boxRow + 3)) {
            for (c in boxCol..<(boxCol + 3)) {
                val bi = idx(r, c)
                notes[bi] = notes[bi] and mask
            }
        }
    }

    // MARK: Actions

    /// Record a single-cell edit in the history, dropping any pending redo entries.
    /// Caller passes the pre-edit and post-edit (value, notes, provisional) state.
    private fun recordHistory(cellIndex: Int, oldValue: Int, oldNotes: Int, oldProvisional: Boolean, newValue: Int, newNotes: Int, newProvisional: Boolean) {
        // Drop any redo entries past the cursor — they're invalidated by the new edit.
        // Skip Lite's Array doesn't expose removeSubrange, so we use a removeLast loop.
        while (historyIndices.count > historyCursor) {
            historyIndices.removeLast()
            historyOldValues.removeLast()
            historyOldNotes.removeLast()
            historyOldProvisional.removeLast()
            historyNewValues.removeLast()
            historyNewNotes.removeLast()
            historyNewProvisional.removeLast()
        }
        historyIndices.append(cellIndex)
        historyOldValues.append(oldValue)
        historyOldNotes.append(oldNotes)
        historyOldProvisional.append(oldProvisional)
        historyNewValues.append(newValue)
        historyNewNotes.append(newNotes)
        historyNewProvisional.append(newProvisional)
        historyCursor = historyIndices.count
    }

    /// Attempt to place `digit` into the selected cell. Returns true if a value change occurred.
    internal fun placeDigit(digit: Int): Boolean {
        val i_0 = selectedIndex
        if ((i_0 == null) || isPaused || isComplete || isGameOver) {
            return false
        }
        if (isOriginal[i_0]) {
            return false
        }
        val oldValue = values[i_0]
        val oldNotes = notes[i_0]
        val oldProvisional = isProvisional[i_0]
        if (notesMode) {
            toggleNote(i_0, digit)
            // A note edit is provisional iff we are in checkpoint mode and the
            // edit happens on a fresh (not previously-touched) cell, or the cell
            // is already provisional.
            if (checkpointActive) {
                isProvisional[i_0] = true
            }
            recordHistory(i_0, oldValue = oldValue, oldNotes = oldNotes, oldProvisional = oldProvisional, newValue = values[i_0], newNotes = notes[i_0], newProvisional = isProvisional[i_0])
            return true
        }
        // If the cell already has this digit, treat as clear.
        if (values[i_0] == digit) {
            values[i_0] = 0
            // Clearing a value also clears the provisional flag (cell is now empty).
            isProvisional[i_0] = false
            recordHistory(i_0, oldValue = oldValue, oldNotes = oldNotes, oldProvisional = oldProvisional, newValue = values[i_0], newNotes = notes[i_0], newProvisional = isProvisional[i_0])
            return true
        }
        values[i_0] = digit
        clearNotes(i_0)
        isProvisional[i_0] = checkpointActive
        if (digit == solution[i_0]) {
            // Correct placement: clear this digit from peer notes for convenience.
            clearPeerNotes(of = i_0, digit = digit)
        }
        recordHistory(i_0, oldValue = oldValue, oldNotes = oldNotes, oldProvisional = oldProvisional, newValue = values[i_0], newNotes = notes[i_0], newProvisional = isProvisional[i_0])
        checkCompletion()
        return true
    }

    internal fun undo() {
        if (!canUndo) {
            return
        }
        historyCursor -= 1
        val i = historyIndices[historyCursor]
        values[i] = historyOldValues[historyCursor]
        notes[i] = historyOldNotes[historyCursor]
        isProvisional[i] = historyOldProvisional[historyCursor]
    }

    internal fun redo() {
        if (!canRedo) {
            return
        }
        val i = historyIndices[historyCursor]
        values[i] = historyNewValues[historyCursor]
        notes[i] = historyNewNotes[historyCursor]
        isProvisional[i] = historyNewProvisional[historyCursor]
        historyCursor += 1
    }

    /// Whether a hint can be used right now — true when the difficulty grants
    /// unlimited hints (Easy) or there is at least one hint remaining.
    internal val canUseHint: Boolean
        get() {
            if (!difficulty.hintsEnabled) {
                return false
            }
            if (difficulty.hasUnlimitedHints) {
                return true
            }
            return hintsRemaining > 0
        }

    /// Use a hint to auto-fill the correct value into the selected cell.
    /// If no cell is selected, picks the first empty cell.
    internal fun useHint() {
        if (!canUseHint || isPaused || isComplete || isGameOver) {
            return
        }
        var target = selectedIndex
        if (target == null || (target != null && (isOriginal[target!!] || values[target!!] == solution[target!!]))) {
            // Pick first empty or incorrect cell
            for (i in 0..<81) {
                if (!isOriginal[i] && values[i] != solution[i]) {
                    target = i
                    break
                }
            }
        }
        val i_1 = target
        if (i_1 == null) {
            return
        }
        if (isOriginal[i_1]) {
            return
        }
        val oldValue = values[i_1]
        val oldNotes = notes[i_1]
        val oldProvisional = isProvisional[i_1]
        values[i_1] = solution[i_1]
        notes[i_1] = 0
        // A hint placement is treated as committed even in checkpoint mode — the
        // user explicitly asked for the correct answer.
        isProvisional[i_1] = false
        if (!difficulty.hasUnlimitedHints) {
            hintsRemaining -= 1
        }
        selectedIndex = i_1
        clearPeerNotes(of = i_1, digit = solution[i_1])
        recordHistory(i_1, oldValue = oldValue, oldNotes = oldNotes, oldProvisional = oldProvisional, newValue = values[i_1], newNotes = notes[i_1], newProvisional = isProvisional[i_1])
        checkCompletion()
    }

    // MARK: Checkpoint

    /// Begin checkpoint mode: snapshot current values/notes so a revert can restore them.
    /// Any pending redo entries are dropped — moves you abandoned before opening the
    /// checkpoint shouldn't reappear once you commit or revert it.
    internal fun enterCheckpoint() {
        if (isPaused || isComplete || isGameOver || checkpointActive) {
            return
        }
        while (historyIndices.count > historyCursor) {
            historyIndices.removeLast()
            historyOldValues.removeLast()
            historyOldNotes.removeLast()
            historyOldProvisional.removeLast()
            historyNewValues.removeLast()
            historyNewNotes.removeLast()
            historyNewProvisional.removeLast()
        }
        checkpointValues = values
        checkpointNotes = notes
        checkpointActive = true
    }

    /// Commit the checkpoint: keep all values, clear provisional flags. Clears history
    /// because committed values are now indistinguishable from any earlier ones.
    internal fun commitCheckpoint() {
        if (!checkpointActive) {
            return
        }
        isProvisional = Array(repeating = false, count = 81)
        checkpointActive = false
        clearHistory()
    }

    /// Revert the checkpoint: restore the snapshot taken when checkpoint mode began,
    /// removing every value/note placed during the session. Clears history.
    internal fun revertCheckpoint() {
        if (!checkpointActive) {
            return
        }
        values = checkpointValues
        notes = checkpointNotes
        isProvisional = Array(repeating = false, count = 81)
        checkpointActive = false
        clearHistory()
    }

    /// True when the board is fully filled and satisfies the Sudoku constraints:
    /// every row, column, and 3×3 box contains the digits 1–9 exactly once.
    ///
    /// A puzzle is allowed to have multiple valid completions (e.g., when a "deadly
    /// pair" of cells in two adjacent blocks could be swapped), so we evaluate the
    /// board purely on its own merits rather than comparing against the canonical
    /// `solution`. Anything passing the Sudoku rules is a win.
    internal fun isBoardValid(): Boolean {
        // All cells must hold a digit in 1...9.
        for (v in values.sref()) {
            if (v < 1 || v > 9) {
                return false
            }
        }
        // Each row must contain every digit exactly once.
        for (r in 0..<9) {
            var seen = 0
            for (c in 0..<9) {
                val bit = 1 shl values[idx(r, c)]
                if ((seen and bit) != 0) {
                    return false
                }
                seen = seen or bit
            }
        }
        // Each column must contain every digit exactly once.
        for (c in 0..<9) {
            var seen = 0
            for (r in 0..<9) {
                val bit = 1 shl values[idx(r, c)]
                if ((seen and bit) != 0) {
                    return false
                }
                seen = seen or bit
            }
        }
        // Each 3×3 box must contain every digit exactly once.
        for (br in 0..<3) {
            for (bc in 0..<3) {
                var seen = 0
                for (r in (br * 3)..<(br * 3 + 3)) {
                    for (c in (bc * 3)..<(bc * 3 + 3)) {
                        val bit = 1 shl values[idx(r, c)]
                        if ((seen and bit) != 0) {
                            return false
                        }
                        seen = seen or bit
                    }
                }
            }
        }
        return true
    }

    internal fun checkCompletion() {
        if (!isBoardValid()) {
            return
        }
        isComplete = true
        puzzlesSolved += 1
        UserDefaults.standard.set(puzzlesSolved, forKey = "sudoku_puzzles_solved")
        updateBestTime(elapsedSeconds, for_ = difficulty)
    }

    internal fun giveUp() {
        // Fill all empty cells with the solution and mark them so the post-mortem
        // view can tint them distinctly from user-placed cells.
        for (i in 0..<81) {
            if (!isOriginal[i] && values[i] == 0) {
                values[i] = solution[i]
                isFilledByGiveUp[i] = true
            }
        }
        // Drop any checkpoint UI state — there's nothing left to revert to.
        checkpointActive = false
        isProvisional = Array(repeating = false, count = 81)
        hasGivenUp = true
        isPaused = false
    }

    internal fun tick() {
        if (isPaused || isComplete || isGameOver) {
            return
        }
        elapsedSeconds += 1
    }

    // MARK: Persistence

    internal fun makeSavedState(): SudokuSavedState = SudokuSavedState(values = values, isOriginal = isOriginal, solution = solution, notes = notes, isProvisional = isProvisional, isFilledByGiveUp = isFilledByGiveUp, difficultyRaw = difficulty.rawValue, hintsRemaining = hintsRemaining, elapsedSeconds = elapsedSeconds, isComplete = isComplete, hasGivenUp = hasGivenUp, checkpointActive = checkpointActive, checkpointValues = checkpointValues, checkpointNotes = checkpointNotes, historyIndices = historyIndices, historyOldValues = historyOldValues, historyOldNotes = historyOldNotes, historyOldProvisional = historyOldProvisional, historyNewValues = historyNewValues, historyNewNotes = historyNewNotes, historyNewProvisional = historyNewProvisional, historyCursor = historyCursor)

    internal fun restoreState(state: SudokuSavedState) {
        values = state.values
        isOriginal = state.isOriginal
        solution = state.solution
        notes = state.notes
        isProvisional = state.isProvisional
        isFilledByGiveUp = state.isFilledByGiveUp
        difficulty = SudokuDifficulty(rawValue = state.difficultyRaw) ?: SudokuDifficulty.medium
        hintsRemaining = state.hintsRemaining
        elapsedSeconds = state.elapsedSeconds
        isComplete = state.isComplete
        hasGivenUp = state.hasGivenUp
        checkpointActive = state.checkpointActive
        checkpointValues = state.checkpointValues
        checkpointNotes = state.checkpointNotes
        historyIndices = state.historyIndices
        historyOldValues = state.historyOldValues
        historyOldNotes = state.historyOldNotes
        historyOldProvisional = state.historyOldProvisional
        historyNewValues = state.historyNewValues
        historyNewNotes = state.historyNewNotes
        historyNewProvisional = state.historyNewProvisional
        historyCursor = state.historyCursor
        selectedIndex = null
        notesMode = false
        isPaused = false
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
        UserDefaults.standard.set(json_0, forKey = "sudoku_saved_state")
    }

    @androidx.annotation.Keep
    companion object {

        internal fun loadSavedState(): SudokuSavedState? {
            val json_1 = UserDefaults.standard.string(forKey = "sudoku_saved_state")
            if (json_1 == null) {
                return null
            }
            val data_1 = json_1.data(using = StringEncoding.utf8)
            if (data_1 == null) {
                return null
            }
            val state_0 = try { JSONDecoder().decode(SudokuSavedState::class, from = data_1) } catch (_: Throwable) { null }
            if (state_0 == null) {
                clearSavedState()
                return null
            }
            // Validate the saved state before restoring. If the user's previous session
            // somehow ended up with an inconsistent puzzle (for example, after an older
            // build of the app whose generator could produce invalid clues), we discard
            // the saved state rather than restoring an unsolvable board.
            if (!isSavedStateConsistent(state_0)) {
                clearSavedState()
                return null
            }
            return state_0.sref()
        }

        /// True when the persisted state passes basic Sudoku invariants: the canonical
        /// solution is a complete valid Sudoku, every immutable clue matches the
        /// solution at the same index, and the clues considered on their own contain no
        /// duplicate digit in any row, column, or 3×3 box.
        private fun isSavedStateConsistent(state: SudokuSavedState): Boolean {
            if (state.values.count != 81) {
                return false
            }
            if (state.isOriginal.count != 81) {
                return false
            }
            if (state.solution.count != 81) {
                return false
            }
            if (!isFullSudokuSolution(state.solution)) {
                return false
            }
            var clueGrid = Array(repeating = 0, count = 81)
            for (i in 0..<81) {
                if (!state.isOriginal[i]) {
                    continue
                }
                // An immutable clue must match the solution at that position.
                if (state.values[i] != state.solution[i]) {
                    return false
                }
                clueGrid[i] = state.values[i]
            }
            return isPuzzleConsistent(clueGrid)
        }

        internal fun clearSavedState(): Unit = UserDefaults.standard.removeObject(forKey = "sudoku_saved_state")
    }
}

// MARK: - Game View

internal class SudokuGameView: View {
    internal var showInstructions: Boolean
        get() = _showInstructions.wrappedValue
        set(newValue) {
            _showInstructions.wrappedValue = newValue
        }
    internal var _showInstructions: Binding<Boolean>
    private var game: SudokuModel
        get() = _game.wrappedValue
        set(newValue) {
            _game.wrappedValue = newValue
        }
    private var _game: skip.ui.State<SudokuModel>
    private var timerTask: Task<Unit>?
        get() = _timerTask.wrappedValue
        set(newValue) {
            _timerTask.wrappedValue = newValue
        }
    private var _timerTask: skip.ui.State<Task<Unit>?> = skip.ui.State(null)
    private var showPauseMenu: Boolean
        get() = _showPauseMenu.wrappedValue
        set(newValue) {
            _showPauseMenu.wrappedValue = newValue
        }
    private var _showPauseMenu: skip.ui.State<Boolean>
    private var showSettings: Boolean
        get() = _showSettings.wrappedValue
        set(newValue) {
            _showSettings.wrappedValue = newValue
        }
    private var _showSettings: skip.ui.State<Boolean>
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
    internal lateinit var dismiss: DismissAction
    internal lateinit var scenePhase: ScenePhase
    internal var settings: SudokuSettings
        get() = _settings.wrappedValue
        set(newValue) {
            _settings.wrappedValue = newValue
        }
    internal var _settings = skip.ui.Environment<SudokuSettings>()

    internal fun playHaptic(pattern: HapticPattern) {
        if (settings.vibrations) {
            HapticFeedback.play(pattern)
        }
    }

    // MARK: Sudoku-specific haptic patterns

    /// Heavy press for placing a digit into a cell — the keycap moves from raised to
    /// lowered, so a thud reinforces the physical "click in."
    internal val pushInHaptic: HapticPattern
        get() = HapticPattern(arrayOf(HapticEvent(HapticEventType.thud, intensity = 0.55)))

    /// Light release for clearing the same digit out of a cell — the keycap pops
    /// back up, so a low-tick + tick reads as a release.
    internal val depressHaptic: HapticPattern
        get() = HapticPattern(arrayOf(
            HapticEvent(HapticEventType.lowTick, intensity = 0.5),
            HapticEvent(HapticEventType.tick, intensity = 0.3, delay = 0.04)
        ))

    /// Undo — feels like reversing direction, so the intensity falls.
    internal val undoHaptic: HapticPattern
        get() = HapticPattern(arrayOf(HapticEvent(HapticEventType.fall, intensity = 0.6)))

    /// Redo — re-applying a move, so the intensity rises.
    internal val redoHaptic: HapticPattern
        get() = HapticPattern(arrayOf(HapticEvent(HapticEventType.rise, intensity = 0.6)))

    /// Commit — celebratory confirmation when the player locks in a checkpoint.
    internal val commitHaptic: HapticPattern
        get() = HapticPattern.success

    /// Revert — sad descending tones when the player throws their experiment away.
    internal val revertHaptic: HapticPattern
        get() = HapticPattern.error

    override fun body(): View {
        return ComposeBuilder { composectx: ComposeContext ->
            GeometryReader { geo ->
                ComposeBuilder { composectx: ComposeContext ->
                    VStack(spacing = 0.0) { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            hudView
                                .frame(height = 44.0)
                                .padding(Edge.Set.horizontal, 12.0).Compose(composectx)

                            statusBar
                                .padding(Edge.Set.horizontal, 16.0)
                                .padding(Edge.Set.top, 6.0).Compose(composectx)

                            Spacer(minLength = 8.0).Compose(composectx)

                            // Board
                            ZStack { ->
                                ComposeBuilder { composectx: ComposeContext ->
                                    boardView(size = min(geo.size.width - 20.0, geo.size.height * 0.60))
                                        .frame(width = min(geo.size.width - 20.0, geo.size.height * 0.60), height = min(geo.size.width - 20.0, geo.size.height * 0.60)).Compose(composectx)

                                    if (game.isPaused && !game.isComplete && !game.isGameOver) {
                                        pauseBoardCover(size = min(geo.size.width - 20.0, geo.size.height * 0.60)).Compose(composectx)
                                    }
                                    ComposeResult.ok
                                }
                            }.Compose(composectx)

                            Spacer(minLength = 8.0).Compose(composectx)

                            controlPad
                                .padding(Edge.Set.horizontal, 12.0)
                                .padding(Edge.Set.top, 8.0)
                                .padding(Edge.Set.bottom, 12.0).Compose(composectx)
                            ComposeResult.ok
                        }
                    }
                    .frame(maxWidth = Double.infinity, maxHeight = Double.infinity)
                    .background(LinearGradient(colors = arrayOf(Color(red = 0.06, green = 0.07, blue = 0.14), Color(red = 0.04, green = 0.04, blue = 0.10)), startPoint = UnitPoint.top, endPoint = UnitPoint.bottom)
                        .ignoresSafeArea())
                    .overlay { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            if (game.isComplete) {
                                completeOverlay.Compose(composectx)
                            } else if (showPauseMenu) {
                                pauseMenuOverlay.Compose(composectx)
                            }
                            // Give-up state intentionally has no overlay — the board reveals the
                            // solution in-place, with red/green tints showing what went wrong and
                            // what was auto-filled. The pause button remains available so the
                            // player can start a new game or quit.
                            ComposeResult.ok
                        }
                    }.Compose(composectx)
                    ComposeResult.ok
                }
            }
            .navigationBarBackButtonHidden()
            .onAppear { ->
                if (!hasInitialized) {
                    hasInitialized = true
                    val matchtarget_0 = SudokuModel.loadSavedState()
                    if (matchtarget_0 != null) {
                        val savedState = matchtarget_0
                        game.restoreState(savedState)
                    } else {
                        game.newGame(difficulty = settings.lastDifficulty)
                    }
                }
                startTimer()
            }
            .onDisappear { -> stopTimer() }
            .onChange(of = scenePhase) { _, newPhase ->
                if (newPhase != ScenePhase.active) {
                    pauseGame()
                    game.saveState()
                }
            }
            .sheet(isPresented = Binding({ _showSettings.wrappedValue }, { it -> _showSettings.wrappedValue = it })) { ->
                ComposeBuilder { composectx: ComposeContext ->
                    SudokuSettingsView(settings = settings)
                        .presentationDetents(setOf(PresentationDetent.medium, PresentationDetent.large)).Compose(composectx)
                    ComposeResult.ok
                }
            }
            .sheet(isPresented = Binding({ _showDifficultyPicker.wrappedValue }, { it -> _showDifficultyPicker.wrappedValue = it })) { ->
                ComposeBuilder { composectx: ComposeContext ->
                    DifficultyPickerView(currentDifficulty = game.difficulty) { newDifficulty ->
                        settings.lastDifficulty = newDifficulty
                        SudokuModel.clearSavedState()
                        game.newGame(difficulty = newDifficulty)
                        game.saveState()
                        startTimer()
                        showPauseMenu = false
                        showDifficultyPicker = false
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
        val rememberedgame by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<SudokuModel>, Any>) { mutableStateOf(_game) }
        _game = rememberedgame

        val rememberedtimerTask by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Task<Unit>?>, Any>) { mutableStateOf(_timerTask) }
        _timerTask = rememberedtimerTask

        val rememberedshowPauseMenu by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Boolean>, Any>) { mutableStateOf(_showPauseMenu) }
        _showPauseMenu = rememberedshowPauseMenu

        val rememberedshowSettings by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Boolean>, Any>) { mutableStateOf(_showSettings) }
        _showSettings = rememberedshowSettings

        val rememberedshowDifficultyPicker by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Boolean>, Any>) { mutableStateOf(_showDifficultyPicker) }
        _showDifficultyPicker = rememberedshowDifficultyPicker

        val rememberedhasInitialized by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Boolean>, Any>) { mutableStateOf(_hasInitialized) }
        _hasInitialized = rememberedhasInitialized

        this.dismiss = EnvironmentValues.shared.dismiss
        this.scenePhase = EnvironmentValues.shared.scenePhase
        _settings.wrappedValue = EnvironmentValues.shared.environmentObject(type = SudokuSettings::class)!!

        return super.Evaluate(context, options)
    }

    // MARK: HUD

    internal val hudView: View
        get() {
            return HStack(spacing = 12.0) { ->
                ComposeBuilder { composectx: ComposeContext ->
                    Button(action = { -> dismiss() }) { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            Image("cancel", bundle = Bundle.module)
                                .font(Font.title2)
                                .foregroundStyle(Color.white.opacity(0.7)).Compose(composectx)
                            ComposeResult.ok
                        }
                    }.Compose(composectx)

                    Spacer().Compose(composectx)

                    Text(LocalizedStringKey(stringLiteral = "SUDOKU"), bundle = Bundle.module)
                        .font(Font.headline)
                        .fontWeight(Font.Weight.heavy)
                        .tracking(3.0)
                        .foregroundStyle(Color.white.opacity(0.85)).Compose(composectx)

                    Spacer().Compose(composectx)

                    Button(action = { -> pauseGame() }) { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            Image("pause_circle", bundle = Bundle.module)
                                .font(Font.title2)
                                .foregroundStyle(Color.white.opacity(0.7)).Compose(composectx)
                            ComposeResult.ok
                        }
                    }.Compose(composectx)
                    ComposeResult.ok
                }
            }
        }

    internal val statusBar: View
        get() {
            // After Give Up the timer freezes and the pill simply reads "Game Over" in
            // place of the running MM:SS time.
            val timeValue: Text = if (game.hasGivenUp) Text(LocalizedStringKey(stringLiteral = "Game Over"), bundle = Bundle.module) else Text(verbatim = formatTime(game.elapsedSeconds))
            val timeTint: Color = if (game.hasGivenUp) Color(red = 1.0, green = 0.55, blue = 0.55) else Color(red = 0.60, green = 0.75, blue = 0.95)
            return HStack(spacing = 0.0) { ->
                ComposeBuilder { composectx: ComposeContext ->
                    statusPill(title = Text(LocalizedStringKey(stringLiteral = "Difficulty"), bundle = Bundle.module), value = Text(verbatim = game.difficulty.label), tint = game.difficulty.accentColor).Compose(composectx)
                    Spacer(minLength = 8.0).Compose(composectx)
                    statusPill(title = Text(LocalizedStringKey(stringLiteral = "Time"), bundle = Bundle.module), value = timeValue, tint = timeTint).Compose(composectx)
                    ComposeResult.ok
                }
            }
        }

    internal fun statusPill(title: Text, value: Text, tint: Color): View {
        return VStack(spacing = 2.0) { ->
            ComposeBuilder { composectx: ComposeContext ->
                title
                    .font(Font.caption2)
                    .foregroundStyle(Color.white.opacity(0.55)).Compose(composectx)
                value
                    .font(Font.callout)
                    .fontWeight(Font.Weight.bold)
                    .foregroundStyle(tint)
                    .monospaced().Compose(composectx)
                ComposeResult.ok
            }
        }
        .frame(maxWidth = Double.infinity)
        .padding(Edge.Set.vertical, 6.0)
        .background(RoundedRectangle(cornerRadius = 10.0)
            .fill(Color.white.opacity(0.05)))
        .overlay { ->
            ComposeBuilder { composectx: ComposeContext ->
                RoundedRectangle(cornerRadius = 10.0)
                    .stroke(tint.opacity(0.35), lineWidth = 1.0).Compose(composectx)
                ComposeResult.ok
            }
        }
    }

    // MARK: Board

    internal fun boardView(size: Double): View {
        val cellSize = size / 9.0
        val thinLine: Double = 0.5
        val thickLine: Double = 2.0
        return ZStack { ->
            ComposeBuilder { composectx: ComposeContext ->
                // Background
                RoundedRectangle(cornerRadius = 10.0)
                    .fill(Color(red = 0.10, green = 0.12, blue = 0.22)).Compose(composectx)

                // Cells laid out in a standard 9x9 grid (no absolute positioning)
                // so that each cell has its own natural hit-testing area. Using
                // `.position()` here worked on iOS but broke tap detection on
                // Android, since Compose's tap handler covered the entire board
                // for every cell rather than just the cell's visible frame.
                VStack(spacing = 0.0) { ->
                    ComposeBuilder { composectx: ComposeContext ->
                        ForEach(0..<9, id = { it }) { row ->
                            ComposeBuilder { composectx: ComposeContext ->
                                HStack(spacing = 0.0) { ->
                                    ComposeBuilder { composectx: ComposeContext ->
                                        ForEach(0..<9, id = { it }) { col ->
                                            ComposeBuilder { composectx: ComposeContext ->
                                                val i = row * 9 + col
                                                cellView(index = i, size = cellSize)
                                                    .onTapGesture l@{ it ->
                                                        if (game.isPaused || game.isComplete || game.isGameOver) {
                                                            return@l
                                                        }
                                                        game.selectedIndex = i
                                                        playHaptic(HapticPattern.pick)
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

                // Grid lines overlay (drawn on top to not be occluded by cells)
                gridLinesOverlay(cellSize = cellSize, thin = thinLine, thick = thickLine)
                    .allowsHitTesting(false).Compose(composectx)
                ComposeResult.ok
            }
        }
        .frame(width = size, height = size)
        .clipShape(RoundedRectangle(cornerRadius = 10.0))
        .overlay { ->
            ComposeBuilder { composectx: ComposeContext ->
                RoundedRectangle(cornerRadius = 10.0)
                    .stroke(Color.white.opacity(0.35), lineWidth = 2.0).Compose(composectx)
                ComposeResult.ok
            }
        }
    }

    internal fun gridLinesOverlay(cellSize: Double, thin: Double, thick: Double): View {
        return ZStack(alignment = Alignment.topLeading) { ->
            ComposeBuilder { composectx: ComposeContext ->
                // Thin horizontal lines
                ForEach(1..<9) { i ->
                    ComposeBuilder { composectx: ComposeContext ->
                        val isThick = (i % 3 == 0)
                        Rectangle()
                            .fill(if (isThick) Color.white.opacity(0.55) else Color.white.opacity(0.12))
                            .frame(height = if (isThick) thick else thin)
                            .offset(y = Double(i) * cellSize - (if (isThick) thick else thin) / 2.0).Compose(composectx)
                        ComposeResult.ok
                    }
                }.Compose(composectx)
                // Thin vertical lines
                ForEach(1..<9) { i ->
                    ComposeBuilder { composectx: ComposeContext ->
                        val isThick = (i % 3 == 0)
                        Rectangle()
                            .fill(if (isThick) Color.white.opacity(0.55) else Color.white.opacity(0.12))
                            .frame(width = if (isThick) thick else thin)
                            .offset(x = Double(i) * cellSize - (if (isThick) thick else thin) / 2.0).Compose(composectx)
                        ComposeResult.ok
                    }
                }.Compose(composectx)
                ComposeResult.ok
            }
        }
    }

    internal fun cellView(index: Int, size: Double): View {
        val value = game.values[index]
        val isSelected = game.selectedIndex == index
        val isOriginal = game.isOriginal[index]
        val isProvisional = game.isProvisional[index]
        val highlightLevel = computeHighlight(for_ = index)
        // After Give Up: tint auto-filled cells green, user's incorrect placements red.
        val isFilledByGiveUp = game.hasGivenUp && game.isFilledByGiveUp[index]
        val isUserWrong = game.hasGivenUp && !isOriginal && !isFilledByGiveUp && value != 0 && value != game.solution[index]
        // Easy mode flags obvious mistakes — a user-placed value that duplicates
        // a peer in its row, column, or box renders red even before Give Up.
        val isObviousMistake = !isOriginal && value != 0 && game.difficulty == SudokuDifficulty.easy && game.hasConflict(at = index)
        return ZStack { ->
            ComposeBuilder { composectx: ComposeContext ->
                // Background
                Rectangle()
                    .fill(cellBackground(isSelected = isSelected, highlight = highlightLevel)).Compose(composectx)

                if (value != 0) {
                    Text({
                        val str = LocalizedStringKey.StringInterpolation(literalCapacity = 0, interpolationCount = 0)
                        str.appendInterpolation(value)
                        LocalizedStringKey(stringInterpolation = str)
                    }())
                        .font(Font.system(size = size * 0.55, weight = if (isOriginal) Font.Weight.black else Font.Weight.semibold, design = Font.Design.rounded))
                        .foregroundStyle(cellTextColor(isOriginal = isOriginal, isProvisional = isProvisional, isFilledByGiveUp = isFilledByGiveUp, isUserWrong = isUserWrong, isObviousMistake = isObviousMistake))
                        .monospaced().Compose(composectx)
                } else {
                    notesGridView(index = index, cellSize = size, isProvisional = isProvisional).Compose(composectx)
                }
                ComposeResult.ok
            }
        }
        .frame(width = size, height = size)
    }

    internal fun notesGridView(index: Int, cellSize: Double, isProvisional: Boolean): View {
        val noteFont = cellSize * 0.22
        val noteColor = if (isProvisional) Color(red = 1.0, green = 0.78, blue = 0.40).opacity(0.85) else Color.white.opacity(0.55)
        return GeometryReader { _ ->
            ComposeBuilder { composectx: ComposeContext ->
                ZStack { ->
                    ComposeBuilder { composectx: ComposeContext ->
                        ForEach(1..<10) { d ->
                            ComposeBuilder { composectx: ComposeContext ->
                                val row = (d - 1) / 3
                                val col = (d - 1) % 3
                                if (game.hasNote(index, d)) {
                                    Text({
                                        val str = LocalizedStringKey.StringInterpolation(literalCapacity = 0, interpolationCount = 0)
                                        str.appendInterpolation(d)
                                        LocalizedStringKey(stringInterpolation = str)
                                    }())
                                        .font(Font.system(size = noteFont, weight = Font.Weight.medium, design = Font.Design.rounded))
                                        .foregroundStyle(noteColor)
                                        .monospaced()
                                        .position(x = (Double(col) + 0.5) * (cellSize / 3.0), y = (Double(row) + 0.5) * (cellSize / 3.0)).Compose(composectx)
                                }
                                ComposeResult.ok
                            }
                        }.Compose(composectx)
                        ComposeResult.ok
                    }
                }.Compose(composectx)
                ComposeResult.ok
            }
        }
        .frame(width = cellSize, height = cellSize)
    }

    /// Highlight levels: 0 = none, 1 = peer (row/col/box), 2 = same digit, 3 = selected.
    /// Expert mode suppresses the same-digit highlight to make the puzzle harder.
    internal fun computeHighlight(for_: Int): Int {
        val index = for_
        val sel_0 = game.selectedIndex
        if (sel_0 == null) {
            return 0
        }
        if (sel_0 == index) {
            return 3
        }
        val selValue = game.values[sel_0]
        if (game.difficulty != SudokuDifficulty.expert && selValue != 0 && game.values[index] == selValue) {
            return 2
        }
        if (game.isPeer(sel_0, index)) {
            return 1
        }
        return 0
    }

    internal fun cellBackground(isSelected: Boolean, highlight: Int): Color {
        when (highlight) {
            3 -> return Color(red = 0.25, green = 0.45, blue = 0.85).opacity(0.65)
            2 -> return Color(red = 0.20, green = 0.40, blue = 0.75).opacity(0.40)
            1 -> return Color(red = 0.14, green = 0.18, blue = 0.32).opacity(0.70)
            else -> return Color(red = 0.08, green = 0.10, blue = 0.18)
        }
    }

    internal fun cellTextColor(isOriginal: Boolean, isProvisional: Boolean, isFilledByGiveUp: Boolean, isUserWrong: Boolean, isObviousMistake: Boolean): Color {
        if (isFilledByGiveUp) {
            // Auto-filled by Give Up — green so the player can spot what they hadn't found.
            return Color(red = 0.45, green = 0.92, blue = 0.55)
        }
        if (isUserWrong || isObviousMistake) {
            // Red flags wrong placements: revealed at the end by Give Up, or in
            // Easy mode whenever the value duplicates a peer.
            return Color(red = 1.0, green = 0.45, blue = 0.45)
        }
        if (isOriginal) {
            // Slightly dimmed so original clues read as immutable.
            return Color.white.opacity(0.62)
        }
        if (isProvisional) {
            // Distinctive amber/orange for values placed after a checkpoint.
            return Color(red = 1.0, green = 0.78, blue = 0.40)
        }
        return Color(red = 0.65, green = 0.85, blue = 1.0)
    }

    // MARK: Board Pause Cover

    internal fun pauseBoardCover(size: Double): View {
        return RoundedRectangle(cornerRadius = 10.0)
            .fill(Color.black.opacity(0.82))
            .frame(width = size, height = size)
            .overlay { ->
                ComposeBuilder { composectx: ComposeContext ->
                    VStack(spacing = 12.0) { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            Image("pause_circle", bundle = Bundle.module)
                                .font(Font.system(size = 54.0))
                                .foregroundStyle(Color.white.opacity(0.75)).Compose(composectx)
                            Text(LocalizedStringKey(stringLiteral = "PAUSED"), bundle = Bundle.module)
                                .font(Font.title2)
                                .fontWeight(Font.Weight.heavy)
                                .tracking(4.0)
                                .foregroundStyle(Color.white.opacity(0.75)).Compose(composectx)
                            ComposeResult.ok
                        }
                    }.Compose(composectx)
                    ComposeResult.ok
                }
            }
    }

    // MARK: Control Pad (number grid + action buttons)

    internal val controlPad: View
        get() {
            val busy = game.isPaused || game.isGameOver || game.isComplete
            // Pre-compute the Notes label as a Text view so the ternary is between Text
            // values (not String literals) — Skip Lite doesn't infer LocalizedStringKey
            // from a String? : String ternary.
            val notesLabel: Text = if (game.notesMode) Text(LocalizedStringKey(stringLiteral = "Notes ✓"), bundle = Bundle.module) else Text(LocalizedStringKey(stringLiteral = "Notes"), bundle = Bundle.module)
            // Hint label varies: ∞ for unlimited (Easy), a count for Medium, just "Hint"
            // for difficulties with no hints (Hard/Expert).
            val hintLabel: Text
            if (game.difficulty.hasUnlimitedHints) {
                hintLabel = Text(LocalizedStringKey(stringLiteral = "Hint (∞)"), bundle = Bundle.module)
            } else if (!game.difficulty.hintsEnabled) {
                hintLabel = Text(LocalizedStringKey(stringLiteral = "Hint"), bundle = Bundle.module)
            } else {
                hintLabel = Text({
                    val str = LocalizedStringKey.StringInterpolation(literalCapacity = 0, interpolationCount = 0)
                    str.appendLiteral("Hint (")
                    str.appendInterpolation(game.hintsRemaining)
                    str.appendLiteral(")")
                    LocalizedStringKey(stringInterpolation = str)
                }(), bundle = Bundle.module)
            }
            return HStack(spacing = 8.0) { ->
                ComposeBuilder { composectx: ComposeContext ->
                    // Left column: Notes (top), Hint (bottom)
                    VStack(spacing = 8.0) { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            actionButton(label = notesLabel, iconName = "edit", highlighted = game.notesMode, disabled = busy, action = { ->
                                game.notesMode = !game.notesMode
                                playHaptic(HapticPattern.pick)
                            }).Compose(composectx)
                            actionButton(label = hintLabel, iconName = "lightbulb", disabled = !game.canUseHint || busy, action = { ->
                                game.useHint()
                                game.saveState()
                                if (game.isComplete) {
                                    playHaptic(HapticPattern.bigCelebrate)
                                } else {
                                    playHaptic(HapticPattern.snap)
                                }
                            }).Compose(composectx)
                            ComposeResult.ok
                        }
                    }
                    .frame(width = 64.0).Compose(composectx)

                    // Center: 3x3 number grid
                    numberPad.Compose(composectx)

                    // Right column: Undo/Redo (top), Checkpoint/Commit-Revert (bottom)
                    VStack(spacing = 8.0) { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            undoOrUndoRedoButton(busy = busy).Compose(composectx)
                            checkpointOrCommitRevertButton(busy = busy).Compose(composectx)
                            ComposeResult.ok
                        }
                    }
                    .frame(width = 64.0).Compose(composectx)
                    ComposeResult.ok
                }
            }
        }

    internal fun undoOrUndoRedoButton(busy: Boolean): View {
        return ComposeBuilder { composectx: ComposeContext ->
            if (game.canRedo) {
                splitActionButton(topLabel = Text(LocalizedStringKey(stringLiteral = "Undo"), bundle = Bundle.module), topIcon = "undo", topDisabled = !game.canUndo || busy, topAction = { ->
                    game.undo()
                    game.saveState()
                    playHaptic(undoHaptic)
                }, bottomLabel = Text(LocalizedStringKey(stringLiteral = "Redo"), bundle = Bundle.module), bottomIcon = "redo", bottomDisabled = busy, bottomAction = { ->
                    game.redo()
                    game.saveState()
                    if (game.isComplete) {
                        playHaptic(HapticPattern.bigCelebrate)
                    } else {
                        playHaptic(redoHaptic)
                    }
                }).Compose(composectx)
            } else {
                actionButton(label = Text(LocalizedStringKey(stringLiteral = "Undo"), bundle = Bundle.module), iconName = "undo", disabled = !game.canUndo || busy, action = { ->
                    game.undo()
                    game.saveState()
                    playHaptic(undoHaptic)
                }).Compose(composectx)
            }
            ComposeResult.ok
        }
    }

    internal fun checkpointOrCommitRevertButton(busy: Boolean): View {
        return ComposeBuilder { composectx: ComposeContext ->
            if (game.checkpointActive) {
                splitActionButton(topLabel = Text(LocalizedStringKey(stringLiteral = "Commit"), bundle = Bundle.module), topIcon = "check", topDisabled = busy, topAction = { ->
                    game.commitCheckpoint()
                    game.saveState()
                    playHaptic(commitHaptic)
                }, bottomLabel = Text(LocalizedStringKey(stringLiteral = "Revert"), bundle = Bundle.module), bottomIcon = "close", bottomDisabled = busy, bottomAction = { ->
                    game.revertCheckpoint()
                    game.saveState()
                    playHaptic(revertHaptic)
                }).Compose(composectx)
            } else {
                actionButton(label = Text(LocalizedStringKey(stringLiteral = "Checkpoint"), bundle = Bundle.module), iconName = "flag", disabled = busy, action = { ->
                    game.enterCheckpoint()
                    game.saveState()
                    playHaptic(HapticPattern.pick)
                }).Compose(composectx)
            }
            ComposeResult.ok
        }
    }

    internal fun actionButton(label: Text, iconName: String, highlighted: Boolean = false, disabled: Boolean, action: () -> Unit): View {
        return Button(action = action) { ->
            ComposeBuilder { composectx: ComposeContext ->
                VStack(spacing = 3.0) { ->
                    ComposeBuilder { composectx: ComposeContext ->
                        Image(iconName, bundle = Bundle.module)
                            .renderingMode(Image.TemplateRenderingMode.template)
                            .resizable()
                            .aspectRatio(contentMode = ContentMode.fit)
                            .frame(width = 18.0, height = 18.0).Compose(composectx)
                        label
                            .font(Font.system(size = 10.0, weight = Font.Weight.semibold))
                            .lineLimit(1)
                            .minimumScaleFactor(0.6).Compose(composectx)
                        ComposeResult.ok
                    }
                }
                .frame(maxWidth = Double.infinity, maxHeight = Double.infinity)
                .foregroundStyle(if (highlighted) Color.white else Color.white.opacity(if (disabled) 0.35 else 0.80))
                .background(RoundedRectangle(cornerRadius = 10.0)
                    .fill(if (highlighted) Color(red = 0.30, green = 0.55, blue = 0.95).opacity(0.6) else Color.white.opacity(0.06))).Compose(composectx)
                ComposeResult.ok
            }
        }
        .buttonStyle(ButtonStyle.plain)
        .disabled(disabled)
    }

    /// Two stacked buttons sharing the same footprint as a single action button,
    /// separated by a thin horizontal divider.
    internal fun splitActionButton(topLabel: Text, topIcon: String, topDisabled: Boolean, topAction: () -> Unit, bottomLabel: Text, bottomIcon: String, bottomDisabled: Boolean, bottomAction: () -> Unit): View {
        return VStack(spacing = 0.0) { ->
            ComposeBuilder { composectx: ComposeContext ->
                splitHalf(label = topLabel, iconName = topIcon, disabled = topDisabled, action = topAction).Compose(composectx)
                Rectangle()
                    .fill(Color.white.opacity(0.18))
                    .frame(height = 1.0).Compose(composectx)
                splitHalf(label = bottomLabel, iconName = bottomIcon, disabled = bottomDisabled, action = bottomAction).Compose(composectx)
                ComposeResult.ok
            }
        }
        .background(RoundedRectangle(cornerRadius = 10.0)
            .fill(Color.white.opacity(0.06)))
        .clipShape(RoundedRectangle(cornerRadius = 10.0))
    }

    /// One half of a vertically-split action button. Lays out the icon and label
    /// side-by-side to use the wider horizontal space in the half-height slot.
    private fun splitHalf(label: Text, iconName: String, disabled: Boolean, action: () -> Unit): View {
        return Button(action = action) { ->
            ComposeBuilder { composectx: ComposeContext ->
                HStack(spacing = 4.0) { ->
                    ComposeBuilder { composectx: ComposeContext ->
                        Image(iconName, bundle = Bundle.module)
                            .renderingMode(Image.TemplateRenderingMode.template)
                            .resizable()
                            .aspectRatio(contentMode = ContentMode.fit)
                            .frame(width = 13.0, height = 13.0).Compose(composectx)
                        label
                            .font(Font.system(size = 10.0, weight = Font.Weight.semibold))
                            .lineLimit(1)
                            .minimumScaleFactor(0.5).Compose(composectx)
                        ComposeResult.ok
                    }
                }
                .frame(maxWidth = Double.infinity, maxHeight = Double.infinity)
                .foregroundStyle(Color.white.opacity(if (disabled) 0.35 else 0.80)).Compose(composectx)
                ComposeResult.ok
            }
        }
        .buttonStyle(ButtonStyle.plain)
        .disabled(disabled)
    }

    // MARK: Number Pad

    internal val numberPad: View
        get() {
            return VStack(spacing = 6.0) { ->
                ComposeBuilder { composectx: ComposeContext ->
                    ForEach(0..<3) { row ->
                        ComposeBuilder { composectx: ComposeContext ->
                            HStack(spacing = 6.0) { ->
                                ComposeBuilder { composectx: ComposeContext ->
                                    ForEach(1..<4) { col ->
                                        ComposeBuilder { composectx: ComposeContext ->
                                            numberButton(digit = row * 3 + col).Compose(composectx)
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

    internal fun numberButton(digit: Int): View {
        val placedCount = game.placedCount(of = digit)
        // Clamp the remaining count so non-Easy difficulties (which allow more than
        // 9 of a digit) don't display negative numbers once a duplicate sneaks in.
        val remainingDisplay = max(0, 9 - placedCount)
        val easyMode = game.difficulty == SudokuDifficulty.easy
        // Exhaustion only locks the button in Easy mode — other difficulties let the
        // player place as many copies of a digit as they want.
        val exhausted = easyMode && placedCount >= 9
        // When the selected cell already holds this digit (and isn't a clue), tapping
        // it clears the cell. The button visually lowers (pushed in) to signal that.
        val clearAffordance: Boolean = linvoke l@{ ->
            val sel_1 = game.selectedIndex
            if (sel_1 == null) {
                return@l false
            }
            if (game.isOriginal[sel_1]) {
                return@l false
            }
            return@l game.values[sel_1] == digit
        }
        // When the selected cell is an immutable clue, every number button is dead —
        // gray them all out to make that obvious.
        val immutableCellSelected: Boolean = linvoke l@{ ->
            val sel_2 = game.selectedIndex
            if (sel_2 == null) {
                return@l false
            }
            return@l game.isOriginal[sel_2]
        }
        // Three visual states for the keycap: lowered (selected digit), flat (disabled
        // because the cell is locked or the digit is exhausted), and raised (default).
        val lowered = clearAffordance && !immutableCellSelected
        val flat = immutableCellSelected || (exhausted && !clearAffordance)
        val raised = !lowered && !flat
        val digitColor: Color = linvoke l@{ ->
            if (immutableCellSelected) {
                return@l Color.white.opacity(0.18)
            }
            if (exhausted) {
                return@l Color.white.opacity(0.25)
            }
            if (game.notesMode) {
                return@l Color(red = 0.75, green = 0.85, blue = 1.0)
            }
            return@l Color.white
        }
        val countColor: Color = if (immutableCellSelected) Color.white.opacity(0.18) else Color.white.opacity(0.45)
        // Primitive keycap look: a thick rounded outline (the "well") that stays put,
        // with an inner face that shifts up when de-pressed and down when pressed in.
        val outlineColor: Color = if (flat) Color.white.opacity(0.18) else Color.white.opacity(0.42)
        val faceColor: Color = linvoke l@{ ->
            if (flat) {
                return@l Color.white.opacity(0.04)
            }
            if (lowered) {
                return@l Color.white.opacity(0.06)
            }
            if (game.notesMode) {
                return@l Color(red = 0.20, green = 0.32, blue = 0.65).opacity(0.55)
            }
            return@l Color.white.opacity(0.22)
        }
        // How far the face (and its content) shift inside the outline.
        val faceOffset: Double = if (lowered) 2.0 else (if (raised) -2.0 else 0.0)
        val disabled = flat || game.isPaused || game.isComplete || game.isGameOver
        return Button(action = { ->
            // Capture whether this tap is a push-in (placing a new value) or a
            // de-press (clearing the existing same-digit) before placeDigit mutates
            // state, so the haptic matches the visual transition.
            val wasClearAffordance = clearAffordance
            game.placeDigit(digit)
            game.saveState()
            if (game.isComplete) {
                playHaptic(HapticPattern.bigCelebrate)
            } else if (wasClearAffordance) {
                playHaptic(depressHaptic)
            } else {
                playHaptic(pushInHaptic)
            }
        }) { ->
            ComposeBuilder { composectx: ComposeContext ->
                ZStack { ->
                    ComposeBuilder { composectx: ComposeContext ->
                        // 1. Outline "well" — fixed in place, never moves with press state.
                        RoundedRectangle(cornerRadius = 10.0)
                            .strokeBorder(outlineColor, lineWidth = 2.5).Compose(composectx)
                        // 2. Inner face — sits inside the outline with a small inset, and
                        //    shifts vertically to read as raised (default) or pressed in.
                        RoundedRectangle(cornerRadius = 6.0)
                            .fill(faceColor)
                            .padding(4.0)
                            .offset(y = faceOffset).Compose(composectx)
                        // 3. Content (digit + remaining count) — rides with the face so the
                        //    label appears physically attached to the moving keycap.
                        VStack(spacing = 1.0) { ->
                            ComposeBuilder { composectx: ComposeContext ->
                                Text({
                                    val str = LocalizedStringKey.StringInterpolation(literalCapacity = 0, interpolationCount = 0)
                                    str.appendInterpolation(digit)
                                    LocalizedStringKey(stringInterpolation = str)
                                }())
                                    .font(Font.system(size = 28.0, weight = Font.Weight.heavy, design = Font.Design.rounded))
                                    .monospaced()
                                    .foregroundStyle(digitColor).Compose(composectx)
                                Text({
                                    val str = LocalizedStringKey.StringInterpolation(literalCapacity = 0, interpolationCount = 0)
                                    str.appendInterpolation(remainingDisplay)
                                    LocalizedStringKey(stringInterpolation = str)
                                }())
                                    .font(Font.system(size = 9.0, weight = Font.Weight.medium))
                                    .monospaced()
                                    .foregroundStyle(countColor).Compose(composectx)
                                ComposeResult.ok
                            }
                        }
                        .offset(y = faceOffset).Compose(composectx)
                        ComposeResult.ok
                    }
                }
                .frame(maxWidth = Double.infinity, maxHeight = Double.infinity).Compose(composectx)
                ComposeResult.ok
            }
        }
        .buttonStyle(ButtonStyle.plain)
        .disabled(disabled)
    }

    // MARK: Pause Menu Overlay

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

                            if (!game.isGameOver && !game.isComplete) {
                                Button(action = { -> resumeGame() }) { ->
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
                            }

                            Button(action = { -> showDifficultyPicker = true }) { ->
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

                            if (!game.isGameOver && !game.isComplete) {
                                Button(action = { ->
                                    game.giveUp()
                                    showPauseMenu = false
                                }) { ->
                                    ComposeBuilder { composectx: ComposeContext ->
                                        Text(LocalizedStringKey(stringLiteral = "Give Up"), bundle = Bundle.module)
                                            .font(Font.headline)
                                            .fontWeight(Font.Weight.bold)
                                            .foregroundStyle(Color.white)
                                            .frame(width = 160.0).Compose(composectx)
                                        ComposeResult.ok
                                    }
                                }
                                .buttonStyle(ButtonStyle.borderedProminent)
                                .tint(Color(red = 0.7, green = 0.4, blue = 0.1)).Compose(composectx)
                            }

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

    // MARK: Complete Overlay

    internal val completeOverlay: View
        get() {
            return ZStack { ->
                ComposeBuilder { composectx: ComposeContext ->
                    Color.black.opacity(0.75).ignoresSafeArea().Compose(composectx)

                    VStack(spacing = 16.0) { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            Text(LocalizedStringKey(stringLiteral = "\u2B50\u2B50\u2B50"), bundle = Bundle.module)
                                .font(Font.system(size = 36.0)).Compose(composectx)
                            Text(LocalizedStringKey(stringLiteral = "Puzzle Solved!"), bundle = Bundle.module)
                                .font(Font.largeTitle)
                                .fontWeight(Font.Weight.black)
                                .foregroundStyle(LinearGradient(colors = arrayOf(Color.yellow, Color.orange), startPoint = UnitPoint.top, endPoint = UnitPoint.bottom)).Compose(composectx)

                            VStack(spacing = 6.0) { ->
                                ComposeBuilder { composectx: ComposeContext ->
                                    statLine(title = "Time", value = formatTime(game.elapsedSeconds), color = Color(red = 0.60, green = 0.85, blue = 1.0)).Compose(composectx)

                                    val best = game.bestTime(for_ = game.difficulty)
                                    if (best == game.elapsedSeconds && best > 0) {
                                        Text(LocalizedStringKey(stringLiteral = "New Best Time!"), bundle = Bundle.module)
                                            .font(Font.title3)
                                            .fontWeight(Font.Weight.bold)
                                            .foregroundStyle(Color.yellow).Compose(composectx)
                                    } else if (best > 0) {
                                        Text({
                                            val str = LocalizedStringKey.StringInterpolation(literalCapacity = 0, interpolationCount = 0)
                                            str.appendLiteral("Best: ")
                                            str.appendInterpolation(formatTime(best))
                                            LocalizedStringKey(stringInterpolation = str)
                                        }())
                                            .font(Font.subheadline)
                                            .foregroundStyle(Color.white.opacity(0.65)).Compose(composectx)
                                    }
                                    ComposeResult.ok
                                }
                            }.Compose(composectx)

                            Button(action = { -> showDifficultyPicker = true }) { ->
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

                            ShareLink(item = "I solved a ${game.difficulty.label} Sudoku in ${formatTime(game.elapsedSeconds)} on Faire Games! Can you beat it?\nhttps://appfair.net", subject = Text(LocalizedStringKey(stringLiteral = "Sudoku Time"), bundle = Bundle.module), message = Text({
                                val str = LocalizedStringKey.StringInterpolation(literalCapacity = 0, interpolationCount = 0)
                                str.appendLiteral("I solved Sudoku in ")
                                str.appendInterpolation(formatTime(game.elapsedSeconds))
                                str.appendLiteral("!")
                                LocalizedStringKey(stringInterpolation = str)
                            }())) { ->
                                ComposeBuilder { composectx: ComposeContext ->
                                    HStack(spacing = 6.0) { ->
                                        ComposeBuilder { composectx: ComposeContext ->
                                            Image("ios_share", bundle = Bundle.module)
                                                .renderingMode(Image.TemplateRenderingMode.template)
                                                .resizable()
                                                .aspectRatio(contentMode = ContentMode.fit)
                                                .frame(width = 16.0, height = 16.0).Compose(composectx)
                                            Text(LocalizedStringKey(stringLiteral = "Share"), bundle = Bundle.module)
                                                .font(Font.subheadline).Compose(composectx)
                                            ComposeResult.ok
                                        }
                                    }
                                    .foregroundStyle(Color.white.opacity(0.7)).Compose(composectx)
                                    ComposeResult.ok
                                }
                            }.Compose(composectx)
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

    internal fun statLine(title: String, value: String, color: Color): View {
        return VStack(spacing = 2.0) { ->
            ComposeBuilder { composectx: ComposeContext ->
                Text(title)
                    .font(Font.caption)
                    .foregroundStyle(Color.white.opacity(0.6)).Compose(composectx)
                Text(value)
                    .font(Font.title3)
                    .fontWeight(Font.Weight.bold)
                    .foregroundStyle(color)
                    .monospaced().Compose(composectx)
                ComposeResult.ok
            }
        }
    }

    // MARK: Pause / Resume / Timer

    internal fun pauseGame() {
        if (showPauseMenu) {
            return
        }
        // After Give Up (or completion) the game is already stopped — just surface
        // the menu so the player can start a new game or quit.
        if (!game.isComplete && !game.isGameOver) {
            game.isPaused = true
        }
        showPauseMenu = true
    }

    internal fun resumeGame() {
        showPauseMenu = false
        game.isPaused = false
    }

    internal fun startTimer() {
        stopTimer()
        timerTask = Task(isMainActor = true) { ->
            while (!Task.isCancelled) {
                try { Task.sleep(nanoseconds = 1_000_000_000) } catch (_: Throwable) { null }
                game.tick()
            }
        }
    }

    internal fun stopTimer() {
        timerTask?.cancel()
        timerTask = null
    }

    private constructor(showInstructions: Binding<Boolean>, game: SudokuModel = SudokuModel(), timerTask: Task<Unit>? = null, showPauseMenu: Boolean = false, showSettings: Boolean = false, showDifficultyPicker: Boolean = false, hasInitialized: Boolean = false, privatep: Nothing? = null) {
        this._showInstructions = showInstructions
        this._game = skip.ui.State(game)
        this._timerTask = skip.ui.State(timerTask)
        this._showPauseMenu = skip.ui.State(showPauseMenu)
        this._showSettings = skip.ui.State(showSettings)
        this._showDifficultyPicker = skip.ui.State(showDifficultyPicker)
        this._hasInitialized = skip.ui.State(hasInitialized)
    }

    constructor(showInstructions: Binding<Boolean>): this(showInstructions = showInstructions, privatep = null) {
    }
}

// MARK: - Difficulty Picker

internal class DifficultyPickerView: View {
    internal val currentDifficulty: SudokuDifficulty
    internal val onSelect: (SudokuDifficulty) -> Unit
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

                                    ForEach(SudokuDifficulty.allCases) { d ->
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
                                                                    Text(d.detail)
                                                                        .font(Font.caption)
                                                                        .foregroundStyle(Color.white.opacity(0.6)).Compose(composectx)
                                                                    ComposeResult.ok
                                                                }
                                                            }.Compose(composectx)
                                                            Spacer().Compose(composectx)
                                                            if (d == currentDifficulty) {
                                                                Image("check_circle", bundle = Bundle.module)
                                                                    .renderingMode(Image.TemplateRenderingMode.template)
                                                                    .resizable()
                                                                    .aspectRatio(contentMode = ContentMode.fit)
                                                                    .frame(width = 22.0, height = 22.0)
                                                                    .foregroundStyle(d.accentColor).Compose(composectx)
                                                            }
                                                            ComposeResult.ok
                                                        }
                                                    }
                                                    .padding(16.0)
                                                    .background(RoundedRectangle(cornerRadius = 14.0)
                                                        .fill(d.accentColor.opacity(0.18)))
                                                    .overlay { ->
                                                        ComposeBuilder { composectx: ComposeContext ->
                                                            RoundedRectangle(cornerRadius = 14.0)
                                                                .stroke(d.accentColor.opacity(0.45), lineWidth = 1.5).Compose(composectx)
                                                            ComposeResult.ok
                                                        }
                                                    }.Compose(composectx)
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
                    .background(Color(red = 0.05, green = 0.06, blue = 0.14).ignoresSafeArea())
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
                                    }
                                    .foregroundStyle(Color.white).Compose(composectx)
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

    constructor(currentDifficulty: SudokuDifficulty, onSelect: (SudokuDifficulty) -> Unit) {
        this.currentDifficulty = currentDifficulty
        this.onSelect = onSelect
    }
}

// MARK: - Helpers

internal fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    val mStr = if (m < 10) "0${m}" else "${m}"
    val sStr = if (s < 10) "0${s}" else "${s}"
    return "${mStr}:${sStr}"
}

// MARK: - Preview Icon

class SudokuPreviewIcon: View {
    constructor() {
    }

    // A small 4x4 mini representation (clean enough to be identifiable at icon size)
    override fun body(): View {
        return ComposeBuilder { composectx: ComposeContext ->
            ZStack { ->
                ComposeBuilder { composectx: ComposeContext ->
                    LinearGradient(colors = arrayOf(Color(red = 0.10, green = 0.15, blue = 0.30), Color(red = 0.05, green = 0.08, blue = 0.18)), startPoint = UnitPoint.topLeading, endPoint = UnitPoint.bottomTrailing).Compose(composectx)

                    // 3x3 grid of 3x3 sub-grids
                    VStack(spacing = 2.0) { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            ForEach(0..<3) { br ->
                                ComposeBuilder { composectx: ComposeContext ->
                                    HStack(spacing = 2.0) { ->
                                        ComposeBuilder { composectx: ComposeContext ->
                                            ForEach(0..<3) { bc ->
                                                ComposeBuilder { composectx: ComposeContext ->
                                                    miniBox(bandRow = br, bandCol = bc).Compose(composectx)
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
                    .padding(6.0).Compose(composectx)
                    ComposeResult.ok
                }
            }
            .clipShape(RoundedRectangle(cornerRadius = 12.0)).Compose(composectx)
        }
    }

    private fun miniBox(bandRow: Int, bandCol: Int): View {
        // Use canonical digits for a pleasing icon look
        val pattern = arrayOf(
            arrayOf("5", ".", ".", "6", "7", "8", ".", "1", "2"),
            arrayOf("6", "7", ".", ".", "9", "5", "3", ".", "8"),
            arrayOf(".", "9", "8", "3", ".", "2", "5", "6", "."),
            arrayOf(".", "5", "9", "7", ".", "1", ".", "2", "3"),
            arrayOf("4", ".", "6", ".", "5", ".", "7", ".", "."),
            arrayOf("7", "1", ".", ".", "2", "4", "8", "5", "."),
            arrayOf(".", "6", "1", ".", "3", ".", "2", "8", "4"),
            arrayOf(".", "8", ".", "4", "1", ".", "6", ".", "."),
            arrayOf("3", ".", "5", ".", "8", "6", ".", "7", "9")
        )
        val boxIndex = bandRow * 3 + bandCol
        val digits = pattern[boxIndex].sref()
        return VStack(spacing = 1.0) { ->
            ComposeBuilder { composectx: ComposeContext ->
                ForEach(0..<3) { r ->
                    ComposeBuilder { composectx: ComposeContext ->
                        HStack(spacing = 1.0) { ->
                            ComposeBuilder { composectx: ComposeContext ->
                                ForEach(0..<3) { c ->
                                    ComposeBuilder { composectx: ComposeContext ->
                                        val v = digits[r * 3 + c]
                                        Text(v)
                                            .font(Font.system(size = 10.0, weight = Font.Weight.heavy, design = Font.Design.rounded))
                                            .foregroundStyle(if (v == ".") Color.clear else colorFor(digit = v, box = boxIndex))
                                            .monospaced()
                                            .frame(width = 10.0, height = 10.0)
                                            .background(Color.white.opacity(0.05))
                                            .cornerRadius(1.0).Compose(composectx)
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
        .padding(2.0)
        .background(RoundedRectangle(cornerRadius = 3.0)
            .fill(Color.white.opacity(0.04)))
        .overlay { ->
            ComposeBuilder { composectx: ComposeContext ->
                RoundedRectangle(cornerRadius = 3.0)
                    .stroke(Color.white.opacity(0.35), lineWidth = 0.5).Compose(composectx)
                ComposeResult.ok
            }
        }
    }

    private fun colorFor(digit: String, box: Int): Color {
        // Color "given" digits with slight variation for visual interest
        val palette: Array<Color> = arrayOf(
            Color.white,
            Color(red = 0.65, green = 0.85, blue = 1.0),
            Color(red = 1.0, green = 0.75, blue = 0.55)
        )
        return palette[(box + (Int(digit) ?: 0)) % palette.count]
    }

    @androidx.annotation.Keep
    companion object {
    }
}

// MARK: - Settings View

internal class SudokuSettingsView: View {
    internal var settings: SudokuSettings
        get() = _settings.wrappedValue
        set(newValue) {
            _settings.wrappedValue = newValue
        }
    internal var _settings: skip.ui.Bindable<SudokuSettings>
    internal lateinit var dismiss: DismissAction
    private var confirmReset: Boolean
        get() = _confirmReset.wrappedValue
        set(newValue) {
            _confirmReset.wrappedValue = newValue
        }
    private var _confirmReset: skip.ui.State<Boolean>

    override fun body(): View {
        return ComposeBuilder { composectx: ComposeContext ->
            NavigationStack { ->
                ComposeBuilder { composectx: ComposeContext ->
                    Form { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            Section(header = Text(LocalizedStringKey(stringLiteral = "Sudoku"), bundle = Bundle.module)) { ->
                                ComposeBuilder { composectx: ComposeContext ->
                                    Toggle(isOn = Binding({ _settings.wrappedValue.vibrations }, { it -> _settings.wrappedValue.vibrations = it })) { ->
                                        ComposeBuilder { composectx: ComposeContext ->
                                            Text(LocalizedStringKey(stringLiteral = "Vibrations"), bundle = Bundle.module).Compose(composectx)
                                            ComposeResult.ok
                                        }
                                    }.Compose(composectx)
                                    Picker(selection = Binding({ _settings.wrappedValue.lastDifficulty }, { it -> _settings.wrappedValue.lastDifficulty = it }), content = { ->
                                        ComposeBuilder { composectx: ComposeContext ->
                                            ForEach(SudokuDifficulty.allCases) { d ->
                                                ComposeBuilder { composectx: ComposeContext ->
                                                    Text(d.label).tag(d).Compose(composectx)
                                                    ComposeResult.ok
                                                }
                                            }.Compose(composectx)
                                            ComposeResult.ok
                                        }
                                    }, label = { ->
                                        ComposeBuilder { composectx: ComposeContext ->
                                            Text(LocalizedStringKey(stringLiteral = "Default Difficulty"), bundle = Bundle.module).Compose(composectx)
                                            ComposeResult.ok
                                        }
                                    }).Compose(composectx)
                                    ComposeResult.ok
                                }
                            }.Compose(composectx)
                            Section(header = Text(LocalizedStringKey(stringLiteral = "Records"), bundle = Bundle.module)) { ->
                                ComposeBuilder { composectx: ComposeContext ->
                                    recordRow(for_ = SudokuDifficulty.easy).Compose(composectx)
                                    recordRow(for_ = SudokuDifficulty.medium).Compose(composectx)
                                    recordRow(for_ = SudokuDifficulty.hard).Compose(composectx)
                                    recordRow(for_ = SudokuDifficulty.expert).Compose(composectx)
                                    HStack { ->
                                        ComposeBuilder { composectx: ComposeContext ->
                                            Text(LocalizedStringKey(stringLiteral = "Puzzles Solved"), bundle = Bundle.module).Compose(composectx)
                                            Spacer().Compose(composectx)
                                            Text({
                                                val str = LocalizedStringKey.StringInterpolation(literalCapacity = 0, interpolationCount = 0)
                                                str.appendInterpolation(UserDefaults.standard.integer(forKey = "sudoku_puzzles_solved"))
                                                LocalizedStringKey(stringInterpolation = str)
                                            }())
                                                .foregroundStyle(Color.secondary)
                                                .monospaced().Compose(composectx)
                                            ComposeResult.ok
                                        }
                                    }.Compose(composectx)
                                    ComposeResult.ok
                                }
                            }.Compose(composectx)
                            Section(header = Text(LocalizedStringKey(stringLiteral = "Data"), bundle = Bundle.module)) { ->
                                ComposeBuilder { composectx: ComposeContext ->
                                    Button(role = ButtonRole.destructive, action = { -> confirmReset = true }) { ->
                                        ComposeBuilder { composectx: ComposeContext ->
                                            Text(LocalizedStringKey(stringLiteral = "Reset Sudoku Records"), bundle = Bundle.module).Compose(composectx)
                                            ComposeResult.ok
                                        }
                                    }
                                    .confirmationDialog(Text(LocalizedStringKey(stringLiteral = "Reset Sudoku Records?"), bundle = Bundle.module), isPresented = Binding({ _confirmReset.wrappedValue }, { it -> _confirmReset.wrappedValue = it }), titleVisibility = Visibility.visible, actions = { ->
                                        ComposeBuilder { composectx: ComposeContext ->
                                            Button(role = ButtonRole.destructive, action = { -> resetSudokuRecords() }) { ->
                                                ComposeBuilder { composectx: ComposeContext ->
                                                    Text(LocalizedStringKey(stringLiteral = "Reset"), bundle = Bundle.module).Compose(composectx)
                                                    ComposeResult.ok
                                                }
                                            }.Compose(composectx)
                                            ComposeResult.ok
                                        }
                                    }, message = { ->
                                        ComposeBuilder { composectx: ComposeContext ->
                                            Text(LocalizedStringKey(stringLiteral = "This will permanently reset all Sudoku best times and puzzle counts."), bundle = Bundle.module).Compose(composectx)
                                            ComposeResult.ok
                                        }
                                    }).Compose(composectx)
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
    @Suppress("UNCHECKED_CAST")
    override fun Evaluate(context: ComposeContext, options: Int): kotlin.collections.List<Renderable> {
        val rememberedconfirmReset by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Boolean>, Any>) { mutableStateOf(_confirmReset) }
        _confirmReset = rememberedconfirmReset

        this.dismiss = EnvironmentValues.shared.dismiss

        return super.Evaluate(context, options)
    }

    internal fun recordRow(for_: SudokuDifficulty): View {
        val difficulty = for_
        val best = UserDefaults.standard.integer(forKey = difficulty.bestTimeKey)
        return HStack { ->
            ComposeBuilder { composectx: ComposeContext ->
                Text(difficulty.label).Compose(composectx)
                Spacer().Compose(composectx)
                Text(if (best > 0) formatTime(best) else "—")
                    .foregroundStyle(Color.secondary)
                    .monospaced().Compose(composectx)
                ComposeResult.ok
            }
        }
    }

    private constructor(settings: SudokuSettings, confirmReset: Boolean = false, privatep: Nothing? = null) {
        this._settings = skip.ui.Bindable(settings)
        this._confirmReset = skip.ui.State(confirmReset)
    }

    constructor(settings: SudokuSettings): this(settings = settings, privatep = null) {
    }
}

@Stable
open class SudokuSettings: Observable {
    open var vibrations: Boolean
        get() = _vibrations.wrappedValue
        set(newValue) {
            _vibrations.wrappedValue = newValue
            defaults.set(vibrations, forKey = "sudokuVibrations")
        }
    var _vibrations: skip.model.Observed<Boolean> = skip.model.Observed(defaults.value(forKey = "sudokuVibrations", default = true))

    open var lastDifficulty: SudokuDifficulty
        get() = _lastDifficulty.wrappedValue
        set(newValue) {
            _lastDifficulty.wrappedValue = newValue
            defaults.set(lastDifficulty.rawValue, forKey = "sudokuLastDifficulty")
        }
    var _lastDifficulty: skip.model.Observed<SudokuDifficulty> = skip.model.Observed(SudokuDifficulty(rawValue = defaults.value(forKey = "sudokuLastDifficulty", default = 1)) ?: SudokuDifficulty.medium)

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
