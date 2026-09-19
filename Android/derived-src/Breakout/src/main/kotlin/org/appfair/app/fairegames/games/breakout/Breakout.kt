// Licensed under the GNU General Public License v2.0 or later
// SPDX-License-Identifier: GPL-2.0-or-later

package org.appfair.app.fairegames.games.breakout

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

class BreakoutContainerView: View {
    private var settings: BreakoutSettings
        get() = _settings.wrappedValue
        set(newValue) {
            _settings.wrappedValue = newValue
        }
    private var _settings: skip.ui.State<BreakoutSettings> = skip.ui.State(BreakoutSettings())
    private var showInstructions: Boolean
        get() = _showInstructions.wrappedValue
        set(newValue) {
            _showInstructions.wrappedValue = newValue
        }
    private var _showInstructions: skip.ui.State<Boolean> = skip.ui.State(false)
    private val instructionsConfig = GameInstructionsConfig(key = "Breakout.instructions", bundle = Bundle.module, firstLaunchKey = "instructionsShown_Breakout", title = "Breakout")

    constructor() {
    }

    override fun body(): View {
        return ComposeBuilder { composectx: ComposeContext ->
            BreakoutGameView(showInstructions = Binding({ _showInstructions.wrappedValue }, { it -> _showInstructions.wrappedValue = it }))
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
        val rememberedsettings by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<BreakoutSettings>, Any>) { mutableStateOf(_settings) }
        _settings = rememberedsettings

        val rememberedshowInstructions by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Boolean>, Any>) { mutableStateOf(_showInstructions) }
        _showInstructions = rememberedshowInstructions

        return super.Evaluate(context, options)
    }

    @androidx.annotation.Keep
    companion object {
    }
}

fun resetBreakoutHighScore(): Unit = UserDefaults.standard.set(0, forKey = "breakout_highscore")

// MARK: - Constants

private val paddleHeight: Double = 14.0
private val paddleBottomFraction: Double = 0.25 // paddle sits 1/4 from bottom
private val ballRadius: Double = 7.0
private val brickRows: Int = 8
private val brickCols: Int = 10
private val brickHeight: Double = 16.0
private val brickSpacing: Double = 2.0
private val brickTopMargin: Double = 80.0
private val initialBallSpeed: Double = 320.0

// Row colors — classic rainbow from top to bottom
private val rowColors: Array<Tuple3<Double, Double, Double>> = arrayOf(
    Tuple3(0.90, 0.20, 0.20),
    Tuple3(0.95, 0.40, 0.15),
    Tuple3(0.95, 0.60, 0.10),
    Tuple3(0.95, 0.80, 0.15),
    Tuple3(0.40, 0.80, 0.25),
    Tuple3(0.20, 0.70, 0.55),
    Tuple3(0.30, 0.50, 0.90),
    Tuple3(0.55, 0.35, 0.85)
)

// Points per row (top rows are worth more)
private val rowPoints: Array<Int> = arrayOf(7, 7, 5, 5, 3, 3, 1, 1)

// MARK: - Brick Model

internal class BrickData {
    internal var alive: Boolean
    internal val row: Int
    internal val col: Int

    internal constructor(row: Int, col: Int) {
        this.alive = true
        this.row = row
        this.col = col
    }
}

// MARK: - Saved State

@androidx.annotation.Keep
@Suppress("MUST_BE_INITIALIZED")
internal class BreakoutSavedState: Codable, MutableStruct {
    internal var paddleX: Double
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    internal var ballX: Double
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    internal var ballY: Double
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    internal var ballDX: Double
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    internal var ballDY: Double
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    internal var brickAlive: Array<Boolean>
        get() = field.sref({ this.brickAlive = it })
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
    internal var lives: Int
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
    internal var isLevelComplete: Boolean
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    internal var isLaunched: Boolean
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }

    constructor(paddleX: Double, ballX: Double, ballY: Double, ballDX: Double, ballDY: Double, brickAlive: Array<Boolean>, score: Int, lives: Int, level: Int, isGameOver: Boolean, isLevelComplete: Boolean, isLaunched: Boolean) {
        this.paddleX = paddleX
        this.ballX = ballX
        this.ballY = ballY
        this.ballDX = ballDX
        this.ballDY = ballDY
        this.brickAlive = brickAlive
        this.score = score
        this.lives = lives
        this.level = level
        this.isGameOver = isGameOver
        this.isLevelComplete = isLevelComplete
        this.isLaunched = isLaunched
    }

    override var supdate: ((Any) -> Unit)? = null
    override var smutatingcount = 0
    override fun scopy(): MutableStruct = BreakoutSavedState(paddleX, ballX, ballY, ballDX, ballDY, brickAlive, score, lives, level, isGameOver, isLevelComplete, isLaunched)

    private enum class CodingKeys(override val rawValue: String, @Suppress("UNUSED_PARAMETER") unusedp: Nothing? = null): CodingKey, RawRepresentable<String> {
        paddleX("paddleX"),
        ballX("ballX"),
        ballY("ballY"),
        ballDX("ballDX"),
        ballDY("ballDY"),
        brickAlive("brickAlive"),
        score("score"),
        lives("lives"),
        level("level"),
        isGameOver("isGameOver"),
        isLevelComplete("isLevelComplete"),
        isLaunched("isLaunched");

        @androidx.annotation.Keep
        companion object {
            fun init(rawValue: String): CodingKeys? {
                return when (rawValue) {
                    "paddleX" -> CodingKeys.paddleX
                    "ballX" -> CodingKeys.ballX
                    "ballY" -> CodingKeys.ballY
                    "ballDX" -> CodingKeys.ballDX
                    "ballDY" -> CodingKeys.ballDY
                    "brickAlive" -> CodingKeys.brickAlive
                    "score" -> CodingKeys.score
                    "lives" -> CodingKeys.lives
                    "level" -> CodingKeys.level
                    "isGameOver" -> CodingKeys.isGameOver
                    "isLevelComplete" -> CodingKeys.isLevelComplete
                    "isLaunched" -> CodingKeys.isLaunched
                    else -> null
                }
            }
        }
    }

    override fun encode(to: Encoder) {
        val container = to.container(keyedBy = CodingKeys::class)
        container.encode(paddleX, forKey = CodingKeys.paddleX)
        container.encode(ballX, forKey = CodingKeys.ballX)
        container.encode(ballY, forKey = CodingKeys.ballY)
        container.encode(ballDX, forKey = CodingKeys.ballDX)
        container.encode(ballDY, forKey = CodingKeys.ballDY)
        container.encode(brickAlive, forKey = CodingKeys.brickAlive)
        container.encode(score, forKey = CodingKeys.score)
        container.encode(lives, forKey = CodingKeys.lives)
        container.encode(level, forKey = CodingKeys.level)
        container.encode(isGameOver, forKey = CodingKeys.isGameOver)
        container.encode(isLevelComplete, forKey = CodingKeys.isLevelComplete)
        container.encode(isLaunched, forKey = CodingKeys.isLaunched)
    }

    constructor(from: Decoder) {
        val container = from.container(keyedBy = CodingKeys::class)
        this.paddleX = container.decode(Double::class, forKey = CodingKeys.paddleX)
        this.ballX = container.decode(Double::class, forKey = CodingKeys.ballX)
        this.ballY = container.decode(Double::class, forKey = CodingKeys.ballY)
        this.ballDX = container.decode(Double::class, forKey = CodingKeys.ballDX)
        this.ballDY = container.decode(Double::class, forKey = CodingKeys.ballDY)
        this.brickAlive = container.decode(Array::class, elementType = Boolean::class, forKey = CodingKeys.brickAlive)
        this.score = container.decode(Int::class, forKey = CodingKeys.score)
        this.lives = container.decode(Int::class, forKey = CodingKeys.lives)
        this.level = container.decode(Int::class, forKey = CodingKeys.level)
        this.isGameOver = container.decode(Boolean::class, forKey = CodingKeys.isGameOver)
        this.isLevelComplete = container.decode(Boolean::class, forKey = CodingKeys.isLevelComplete)
        this.isLaunched = container.decode(Boolean::class, forKey = CodingKeys.isLaunched)
    }

    @androidx.annotation.Keep
    companion object: DecodableCompanion<BreakoutSavedState> {
        override fun init(from: Decoder): BreakoutSavedState = BreakoutSavedState(from = from)

        private fun CodingKeys(rawValue: String): CodingKeys? = CodingKeys.init(rawValue = rawValue)
    }
}

// MARK: - Game Model

@Stable
internal class BreakoutModel: Observable {
    // Field
    internal var fieldWidth: Double
        get() = _fieldWidth.wrappedValue
        set(newValue) {
            _fieldWidth.wrappedValue = newValue
        }
    internal var _fieldWidth: skip.model.Observed<Double> = skip.model.Observed(400.0)
    internal var fieldHeight: Double
        get() = _fieldHeight.wrappedValue
        set(newValue) {
            _fieldHeight.wrappedValue = newValue
        }
    internal var _fieldHeight: skip.model.Observed<Double> = skip.model.Observed(700.0)

    // Paddle
    internal var paddleX: Double
        get() = _paddleX.wrappedValue
        set(newValue) {
            _paddleX.wrappedValue = newValue
        }
    internal var _paddleX: skip.model.Observed<Double> = skip.model.Observed(200.0) // center
    internal var paddleWidth: Double
        get() = _paddleWidth.wrappedValue
        set(newValue) {
            _paddleWidth.wrappedValue = newValue
        }
    internal var _paddleWidth: skip.model.Observed<Double> = skip.model.Observed(72.0)

    // Ball
    internal var ballX: Double
        get() = _ballX.wrappedValue
        set(newValue) {
            _ballX.wrappedValue = newValue
        }
    internal var _ballX: skip.model.Observed<Double> = skip.model.Observed(200.0)
    internal var ballY: Double
        get() = _ballY.wrappedValue
        set(newValue) {
            _ballY.wrappedValue = newValue
        }
    internal var _ballY: skip.model.Observed<Double> = skip.model.Observed(500.0)
    internal var ballDX: Double
        get() = _ballDX.wrappedValue
        set(newValue) {
            _ballDX.wrappedValue = newValue
        }
    internal var _ballDX: skip.model.Observed<Double> = skip.model.Observed(0.0)
    internal var ballDY: Double
        get() = _ballDY.wrappedValue
        set(newValue) {
            _ballDY.wrappedValue = newValue
        }
    internal var _ballDY: skip.model.Observed<Double> = skip.model.Observed(0.0)

    // Paddle hit feedback: -1 = no hit this frame, 0..1 = deflection amount
    // (0 = mirror reflection, 1 = maximum angle change)
    internal var lastPaddleDeflection: Double
        get() = _lastPaddleDeflection.wrappedValue
        set(newValue) {
            _lastPaddleDeflection.wrappedValue = newValue
        }
    internal var _lastPaddleDeflection: skip.model.Observed<Double> = skip.model.Observed(-1.0)

    // Bricks
    internal var bricks: Array<Array<BrickData>>
        get() = _bricks.wrappedValue.sref({ this.bricks = it })
        set(newValue) {
            _bricks.wrappedValue = newValue.sref()
        }
    internal var _bricks: skip.model.Observed<Array<Array<BrickData>>> = skip.model.Observed(arrayOf())

    // State
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
    internal var _highScore: skip.model.Observed<Int> = skip.model.Observed(UserDefaults.standard.integer(forKey = "breakout_highscore"))
    internal var lives: Int
        get() = _lives.wrappedValue
        set(newValue) {
            _lives.wrappedValue = newValue
        }
    internal var _lives: skip.model.Observed<Int> = skip.model.Observed(3)
    internal var level: Int
        get() = _level.wrappedValue
        set(newValue) {
            _level.wrappedValue = newValue
        }
    internal var _level: skip.model.Observed<Int> = skip.model.Observed(1)
    internal var isGameOver: Boolean
        get() = _isGameOver.wrappedValue
        set(newValue) {
            _isGameOver.wrappedValue = newValue
        }
    internal var _isGameOver: skip.model.Observed<Boolean> = skip.model.Observed(false)
    internal var isLevelComplete: Boolean
        get() = _isLevelComplete.wrappedValue
        set(newValue) {
            _isLevelComplete.wrappedValue = newValue
        }
    internal var _isLevelComplete: skip.model.Observed<Boolean> = skip.model.Observed(false)
    internal var isLaunched: Boolean
        get() = _isLaunched.wrappedValue
        set(newValue) {
            _isLaunched.wrappedValue = newValue
        }
    internal var _isLaunched: skip.model.Observed<Boolean> = skip.model.Observed(false) // ball is sitting on paddle until launched

    // Layout cache
    internal var brickWidth: Double
        get() = _brickWidth.wrappedValue
        set(newValue) {
            _brickWidth.wrappedValue = newValue
        }
    internal var _brickWidth: skip.model.Observed<Double> = skip.model.Observed(36.0)
    internal var brickAreaLeft: Double
        get() = _brickAreaLeft.wrappedValue
        set(newValue) {
            _brickAreaLeft.wrappedValue = newValue
        }
    internal var _brickAreaLeft: skip.model.Observed<Double> = skip.model.Observed(4.0)

    private var ballSpeed: Double
        get() = _ballSpeed.wrappedValue
        set(newValue) {
            _ballSpeed.wrappedValue = newValue
        }
    private var _ballSpeed: skip.model.Observed<Double> = skip.model.Observed(initialBallSpeed)

    internal fun setup(width: Double, height: Double) {
        if (fieldWidth == width && fieldHeight == height) {
            return
        }
        fieldWidth = width
        fieldHeight = height
        paddleX = width / 2.0

        // Calculate brick layout
        val totalSpacing = brickSpacing * Double(brickCols + 1)
        brickWidth = (width - totalSpacing) / Double(brickCols)
        brickAreaLeft = brickSpacing
    }

    internal fun newGame() {
        score = 0
        lives = 3
        level = 1
        isGameOver = false
        isLevelComplete = false
        ballSpeed = initialBallSpeed
        buildLevel()
        resetBall()
    }

    internal fun startLevel(lvl: Int) {
        level = lvl
        isLevelComplete = false
        // Speed increases each level
        ballSpeed = initialBallSpeed + Double(level - 1) * 25.0
        buildLevel()
        resetBall()
    }

    private fun buildLevel() {
        bricks = arrayOf()
        for (r in 0..<brickRows) {
            var row: Array<BrickData> = arrayOf()
            for (c in 0..<brickCols) {
                row.append(BrickData(row = r, col = c))
            }
            bricks.append(row)
        }
    }

    internal fun resetBall() {
        isLaunched = false
        ballX = paddleX
        ballY = fieldHeight * (1.0 - paddleBottomFraction) - paddleHeight - ballRadius - 2.0
        ballDX = 0.0
        ballDY = 0.0
    }

    internal fun launch() {
        if (isLaunched) {
            return
        }
        isLaunched = true
        // Slight random angle so it's not always straight up
        val angle = Double.random(in_ = -0.4..0.4)
        ballDX = ballSpeed * sin(angle)
        ballDY = -ballSpeed * cos(angle)
    }

    internal fun update(dt: Double) {
        if (!isLaunched || isGameOver || isLevelComplete) {
            return
        }

        lastPaddleDeflection = -1.0

        ballX += ballDX * dt
        ballY += ballDY * dt

        // Wall collisions (left/right)
        if (ballX - ballRadius < 0.0) {
            ballX = ballRadius
            ballDX = abs(ballDX)
        } else if (ballX + ballRadius > fieldWidth) {
            ballX = fieldWidth - ballRadius
            ballDX = -abs(ballDX)
        }

        // Ceiling
        if (ballY - ballRadius < 0.0) {
            ballY = ballRadius
            ballDY = abs(ballDY)
        }

        // Paddle collision
        val paddleTop = fieldHeight * (1.0 - paddleBottomFraction) - paddleHeight
        val paddleLeft = paddleX - paddleWidth / 2.0
        val paddleRight = paddleX + paddleWidth / 2.0

        if (ballDY > 0.0 && ballY + ballRadius >= paddleTop && ballY + ballRadius <= paddleTop + paddleHeight + 4.0) {
            if (ballX >= paddleLeft - ballRadius && ballX <= paddleRight + ballRadius) {
                // Compute incoming angle (relative to vertical)
                val incomingAngle = atan2(ballDX, ballDY)

                ballY = paddleTop - ballRadius
                // Reflect with angle based on where ball hit the paddle
                val hitPos = (ballX - paddleX) / (paddleWidth / 2.0) // -1 to 1
                val clampedHit = min(max(hitPos, -0.95), 0.95)
                val maxAngle = 1.15 // ~66 degrees max
                val outAngle = clampedHit * maxAngle
                val speed = currentSpeed()
                ballDX = speed * sin(outAngle)
                ballDY = -speed * cos(outAngle)

                // Mirror reflection would negate DX and DY, so the mirror
                // outgoing angle (relative to vertical, going up) is -incomingAngle.
                val mirrorAngle = -incomingAngle
                // Deflection = how far the actual outgoing angle is from the mirror angle,
                // normalized to 0..1 where 0 = perfect mirror, 1 = max deviation
                val angleDiff = abs(outAngle - mirrorAngle)
                val maxPossibleDiff = 2.0 * maxAngle // theoretical max
                lastPaddleDeflection = min(angleDiff / maxPossibleDiff, 1.0)
            }
        }

        // Ball lost (below paddle)
        if (ballY - ballRadius > fieldHeight) {
            lives -= 1
            if (lives <= 0) {
                isGameOver = true
                saveHighScore()
            } else {
                resetBall()
            }
            return
        }

        // Brick collisions
        checkBrickCollisions()

        // Check level complete
        var anyAlive = false
        for (row in bricks.sref()) {
            for (brick in row.sref()) {
                if (brick.alive) {
                    anyAlive = true
                    break
                }
            }
            if (anyAlive) {
                break
            }
        }
        if (!anyAlive) {
            isLevelComplete = true
            saveHighScore()
        }
    }

    private fun checkBrickCollisions() {
        for (r in 0..<brickRows) {
            for (c in 0..<brickCols) {
                val brick = bricks[r][c]
                if (!brick.alive) {
                    continue
                }

                val bx = brickAreaLeft + Double(c) * (brickWidth + brickSpacing)
                val by = brickTopMargin + Double(r) * (brickHeight + brickSpacing)

                // AABB vs circle collision
                val closestX = min(max(ballX, bx), bx + brickWidth)
                val closestY = min(max(ballY, by), by + brickHeight)
                val dx = ballX - closestX
                val dy = ballY - closestY
                val distSq = dx * dx + dy * dy

                if (distSq < ballRadius * ballRadius) {
                    brick.alive = false
                    score += rowPoints[r]

                    // Determine reflection axis — which face was hit?
                    val overlapLeft = (ballX + ballRadius) - bx
                    val overlapRight = (bx + brickWidth) - (ballX - ballRadius)
                    val overlapTop = (ballY + ballRadius) - by
                    val overlapBottom = (by + brickHeight) - (ballY - ballRadius)

                    val minOverlapX = min(overlapLeft, overlapRight)
                    val minOverlapY = min(overlapTop, overlapBottom)

                    if (minOverlapX < minOverlapY) {
                        ballDX = -ballDX
                        // Push out
                        if (overlapLeft < overlapRight) {
                            ballX = bx - ballRadius
                        } else {
                            ballX = bx + brickWidth + ballRadius
                        }
                    } else {
                        ballDY = -ballDY
                        if (overlapTop < overlapBottom) {
                            ballY = by - ballRadius
                        } else {
                            ballY = by + brickHeight + ballRadius
                        }
                    }
                    return // one brick per frame for cleaner physics
                }
            }
        }
    }

    private fun currentSpeed(): Double {
        // Slight speed increase as bricks are cleared
        val totalBricks = brickRows * brickCols
        var alive = 0
        for (row in bricks.sref()) {
            for (brick in row.sref()) {
                if (brick.alive) {
                    alive += 1
                }
            }
        }
        val cleared = totalBricks - alive
        return ballSpeed + Double(cleared) * 1.5
    }

    private fun saveHighScore() {
        if (score > highScore) {
            highScore = score
            UserDefaults.standard.set(highScore, forKey = "breakout_highscore")
        }
    }

    // MARK: - State Persistence

    internal fun makeSavedState(): BreakoutSavedState {
        var brickAlive: Array<Boolean> = arrayOf()
        for (r in 0..<bricks.count) {
            for (c in 0..<bricks[r].count) {
                brickAlive.append(bricks[r][c].alive)
            }
        }
        return BreakoutSavedState(paddleX = paddleX, ballX = ballX, ballY = ballY, ballDX = ballDX, ballDY = ballDY, brickAlive = brickAlive, score = score, lives = lives, level = level, isGameOver = isGameOver, isLevelComplete = isLevelComplete, isLaunched = isLaunched)
    }

    internal fun restoreState(state: BreakoutSavedState) {
        paddleX = state.paddleX
        ballX = state.ballX
        ballY = state.ballY
        ballDX = state.ballDX
        ballDY = state.ballDY
        score = state.score
        lives = state.lives
        level = state.level
        isGameOver = state.isGameOver
        isLevelComplete = state.isLevelComplete
        isLaunched = state.isLaunched
        highScore = UserDefaults.standard.integer(forKey = "breakout_highscore")
        ballSpeed = initialBallSpeed + Double(level - 1) * 25.0

        // Rebuild bricks and apply saved alive states
        buildLevel()
        var idx = 0
        for (r in 0..<bricks.count) {
            for (c in 0..<bricks[r].count) {
                if (idx < state.brickAlive.count) {
                    bricks[r][c].alive = state.brickAlive[idx]
                }
                idx += 1
            }
        }
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
        UserDefaults.standard.set(json_0, forKey = "breakout_saved_state")
    }

    @androidx.annotation.Keep
    companion object {

        internal fun loadSavedState(): BreakoutSavedState? {
            val json_1 = UserDefaults.standard.string(forKey = "breakout_saved_state")
            if (json_1 == null) {
                return null
            }
            val data_1 = json_1.data(using = StringEncoding.utf8)
            if (data_1 == null) {
                return null
            }
            return try { JSONDecoder().decode(BreakoutSavedState::class, from = data_1) } catch (_: Throwable) { null }
        }

        internal fun clearSavedState(): Unit = UserDefaults.standard.removeObject(forKey = "breakout_saved_state")
    }
}

// MARK: - Game View

internal class BreakoutGameView: View {
    internal var showInstructions: Boolean
        get() = _showInstructions.wrappedValue
        set(newValue) {
            _showInstructions.wrappedValue = newValue
        }
    internal var _showInstructions: Binding<Boolean>
    private var game: BreakoutModel
        get() = _game.wrappedValue
        set(newValue) {
            _game.wrappedValue = newValue
        }
    private var _game: skip.ui.State<BreakoutModel>
    private var tickTimer: Timer?
        get() = _tickTimer.wrappedValue
        set(newValue) {
            _tickTimer.wrappedValue = newValue
        }
    private var _tickTimer: skip.ui.State<Timer?> = skip.ui.State(null)
    private var lastTick: Double
        get() = _lastTick.wrappedValue
        set(newValue) {
            _lastTick.wrappedValue = newValue
        }
    private var _lastTick: skip.ui.State<Double>
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
    private var debugText: String
        get() = _debugText.wrappedValue
        set(newValue) {
            _debugText.wrappedValue = newValue
        }
    private var _debugText: skip.ui.State<String>
    private var debugTouchCount: Int
        get() = _debugTouchCount.wrappedValue
        set(newValue) {
            _debugTouchCount.wrappedValue = newValue
        }
    private var _debugTouchCount: skip.ui.State<Int>
    private var dragAnchorX: Double?
        get() = _dragAnchorX.wrappedValue
        set(newValue) {
            _dragAnchorX.wrappedValue = newValue
        }
    private var _dragAnchorX: skip.ui.State<Double?> = skip.ui.State(null) // paddle X at drag start
    internal lateinit var dismiss: DismissAction
    internal lateinit var scenePhase: ScenePhase
    internal var settings: BreakoutSettings
        get() = _settings.wrappedValue
        set(newValue) {
            _settings.wrappedValue = newValue
        }
    internal var _settings = skip.ui.Environment<BreakoutSettings>()

    internal fun playHaptic(pattern: HapticPattern) {
        if (settings.vibrations) {
            HapticFeedback.play(pattern)
        }
    }

    override fun body(): View {
        return ComposeBuilder { composectx: ComposeContext ->
            GeometryReader { geo ->
                ComposeBuilder { composectx: ComposeContext ->
                    initField(geo = geo)

                    VStack(spacing = 0.0) { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            // Fixed HUD bar — buttons live here, outside the drag area
                            hudView
                                .frame(height = 44.0).Compose(composectx)

                            // Playfield — the drag gesture target
                            ZStack { ->
                                ComposeBuilder { composectx: ComposeContext ->
                                    // Background — fills the ZStack so it is hittable
                                    Color(red = 0.04, green = 0.04, blue = 0.12).Compose(composectx)

                                    gameFieldView(paddleX = game.paddleX, ballX = game.ballX, ballY = game.ballY).Compose(composectx)

                                    if (!game.isLaunched && !game.isGameOver && !game.isLevelComplete) {
                                        launchPrompt.Compose(composectx)
                                    }

                                    if (game.isLevelComplete) {
                                        levelCompleteOverlay.Compose(composectx)
                                    }

                                    if (game.isGameOver) {
                                        gameOverOverlay.Compose(composectx)
                                    }

                                    if (showPauseMenu && !game.isGameOver && !game.isLevelComplete) {
                                        pauseMenuOverlay.Compose(composectx)
                                    }

                                    // Debug overlay
                                    if (settings.debugInfo) {
                                        VStack { ->
                                            ComposeBuilder { composectx: ComposeContext ->
                                                Spacer().Compose(composectx)
                                                Text(debugText)
                                                    .font(Font.system(size = 11.0, design = Font.Design.monospaced))
                                                    .foregroundStyle(Color.green)
                                                    .padding(6.0)
                                                    .background(Color.black.opacity(0.7))
                                                    .padding(Edge.Set.bottom, 100.0).Compose(composectx)
                                                ComposeResult.ok
                                            }
                                        }
                                        .allowsHitTesting(false).Compose(composectx)
                                    }
                                    ComposeResult.ok
                                }
                            }
                            .gesture(DragGesture(minimumDistance = 0.0, coordinateSpace = LocalCoordinateSpace.local)
                                .onChanged l@{ value ->
                                    debugTouchCount += 1
                                    debugText = "drag #${debugTouchCount} loc=(${Int(value.location.x)},${Int(value.location.y)}) start=(${Int(value.startLocation.x)},${Int(value.startLocation.y)}) paddleX=${Int(game.paddleX)} launched=${game.isLaunched} over=${game.isGameOver}"

                                    if (game.isGameOver || game.isLevelComplete || showPauseMenu) {
                                        return@l
                                    }

                                    // Launch ball on first touch
                                    if (!game.isLaunched) {
                                        game.launch()
                                        playHaptic(HapticPattern.pick)
                                    }

                                    // On first touch of a drag, record the anchor
                                    if (dragAnchorX == null) {
                                        dragAnchorX = game.paddleX - value.startLocation.x
                                    }

                                    // Move paddle relative to the anchor so it never jumps
                                    val x = min(max((dragAnchorX ?: 0.0) + value.location.x, game.paddleWidth / 2.0), game.fieldWidth - game.paddleWidth / 2.0)
                                    game.paddleX = x
                                }
                                .onEnded { _ -> dragAnchorX = null }).Compose(composectx)
                            ComposeResult.ok
                        }
                    }
                    .background(Color(red = 0.04, green = 0.04, blue = 0.12).ignoresSafeArea()).Compose(composectx)
                    ComposeResult.ok
                }
            }
            .navigationBarBackButtonHidden()
            .onAppear { ->
                val matchtarget_0 = BreakoutModel.loadSavedState()
                if (matchtarget_0 != null) {
                    val state = matchtarget_0
                    game.restoreState(state)
                    if (!game.isGameOver && !game.isLevelComplete) {
                        showPauseMenu = true
                    }
                } else {
                    game.newGame()
                }
                startTimer()
            }
            .onDisappear { -> stopTimer() }
            .onChange(of = scenePhase) { _, newPhase ->
                if (newPhase != ScenePhase.active) {
                    game.saveState()
                    stopTimer()
                    if (game.isLaunched && !game.isGameOver && !game.isLevelComplete) {
                        showPauseMenu = true
                    }
                } else if (!showPauseMenu) {
                    startTimer()
                }
            }
            .sheet(isPresented = Binding({ _showSettings.wrappedValue }, { it -> _showSettings.wrappedValue = it })) { ->
                ComposeBuilder { composectx: ComposeContext ->
                    BreakoutSettingsView(settings = settings)
                        .presentationDetents(setOf(PresentationDetent.medium, PresentationDetent.large)).Compose(composectx)
                    ComposeResult.ok
                }
            }.Compose(composectx)
        }
    }

    @Composable
    @Suppress("UNCHECKED_CAST")
    override fun Evaluate(context: ComposeContext, options: Int): kotlin.collections.List<Renderable> {
        val rememberedgame by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<BreakoutModel>, Any>) { mutableStateOf(_game) }
        _game = rememberedgame

        val rememberedtickTimer by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Timer?>, Any>) { mutableStateOf(_tickTimer) }
        _tickTimer = rememberedtickTimer

        val rememberedlastTick by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Double>, Any>) { mutableStateOf(_lastTick) }
        _lastTick = rememberedlastTick

        val rememberedshowSettings by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Boolean>, Any>) { mutableStateOf(_showSettings) }
        _showSettings = rememberedshowSettings

        val rememberedshowPauseMenu by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Boolean>, Any>) { mutableStateOf(_showPauseMenu) }
        _showPauseMenu = rememberedshowPauseMenu

        val remembereddebugText by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<String>, Any>) { mutableStateOf(_debugText) }
        _debugText = remembereddebugText

        val remembereddebugTouchCount by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Int>, Any>) { mutableStateOf(_debugTouchCount) }
        _debugTouchCount = remembereddebugTouchCount

        val remembereddragAnchorX by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Double?>, Any>) { mutableStateOf(_dragAnchorX) }
        _dragAnchorX = remembereddragAnchorX

        this.dismiss = EnvironmentValues.shared.dismiss
        this.scenePhase = EnvironmentValues.shared.scenePhase
        _settings.wrappedValue = EnvironmentValues.shared.environmentObject(type = BreakoutSettings::class)!!

        return super.Evaluate(context, options)
    }

    private fun initField(geo: GeometryProxy): Boolean {
        game.setup(width = geo.size.width, height = geo.size.height)
        return true
    }

    // MARK: - Game Field

    internal fun gameFieldView(paddleX: Double, ballX: Double, ballY: Double): View {
        return ZStack(alignment = Alignment.topLeading) { ->
            ComposeBuilder { composectx: ComposeContext ->
                // Bricks
                ForEach(0..<brickRows, id = { it }) { r ->
                    ComposeBuilder { composectx: ComposeContext ->
                        ForEach(0..<brickCols, id = { it }) { c ->
                            ComposeBuilder { composectx: ComposeContext ->
                                if (game.bricks.count > r && game.bricks[r].count > c && game.bricks[r][c].alive) {
                                    brickView(row = r, col = c).Compose(composectx)
                                }
                                ComposeResult.ok
                            }
                        }.Compose(composectx)
                        ComposeResult.ok
                    }
                }.Compose(composectx)

                // Ball
                Circle()
                    .fill(Color.white)
                    .frame(width = ballRadius * 2.0, height = ballRadius * 2.0)
                    .position(x = ballX, y = ballY).Compose(composectx)

                // Paddle
                paddleShape(atX = paddleX).Compose(composectx)
                ComposeResult.ok
            }
        }
    }

    // MARK: - Brick

    internal fun brickView(row: Int, col: Int): View {
        val x = game.brickAreaLeft + Double(col) * (game.brickWidth + brickSpacing)
        val y = brickTopMargin + Double(row) * (brickHeight + brickSpacing)
        val ci = row % rowColors.count
        val base = rowColors[ci]
        val baseColor = Color(red = base.element0, green = base.element1, blue = base.element2)
        val lightColor = Color(red = min(base.element0 + 0.18, 1.0), green = min(base.element1 + 0.18, 1.0), blue = min(base.element2 + 0.18, 1.0))
        val darkColor = Color(red = max(base.element0 - 0.15, 0.0), green = max(base.element1 - 0.15, 0.0), blue = max(base.element2 - 0.15, 0.0))

        return ZStack { ->
            ComposeBuilder { composectx: ComposeContext ->
                // Base
                RoundedRectangle(cornerRadius = 3.0)
                    .fill(baseColor)
                    .frame(width = game.brickWidth, height = brickHeight).Compose(composectx)
                // Top highlight
                RoundedRectangle(cornerRadius = 3.0)
                    .fill(lightColor)
                    .frame(width = game.brickWidth - 2, height = brickHeight * 0.45)
                    .offset(y = -brickHeight * 0.2).Compose(composectx)
                // Bottom shadow
                RoundedRectangle(cornerRadius = 3.0)
                    .fill(darkColor)
                    .frame(width = game.brickWidth - 2, height = brickHeight * 0.2)
                    .offset(y = brickHeight * 0.35).Compose(composectx)
                ComposeResult.ok
            }
        }
        .position(x = x + game.brickWidth / 2.0, y = y + brickHeight / 2.0)
    }

    // MARK: - Paddle

    internal fun paddleShape(atX: Double): View {
        val px = atX
        val y = game.fieldHeight * (1.0 - paddleBottomFraction) - paddleHeight / 2.0
        return ZStack { ->
            ComposeBuilder { composectx: ComposeContext ->
                RoundedRectangle(cornerRadius = 6.0)
                    .fill(LinearGradient(colors = arrayOf(
                        Color(red = 0.70, green = 0.75, blue = 0.85),
                        Color(red = 0.45, green = 0.50, blue = 0.65)
                    ), startPoint = UnitPoint.top, endPoint = UnitPoint.bottom))
                    .frame(width = game.paddleWidth, height = paddleHeight).Compose(composectx)
                // Top shine
                RoundedRectangle(cornerRadius = 4.0)
                    .fill(Color.white.opacity(0.35))
                    .frame(width = game.paddleWidth - 6, height = paddleHeight * 0.35)
                    .offset(y = -paddleHeight * 0.2).Compose(composectx)
                ComposeResult.ok
            }
        }
        .position(x = px, y = y)
    }

    // MARK: - HUD

    internal val hudView: View
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

                    Text({
                        val str = LocalizedStringKey.StringInterpolation(literalCapacity = 0, interpolationCount = 0)
                        str.appendLiteral("SCORE: ")
                        str.appendInterpolation(game.score)
                        LocalizedStringKey(stringInterpolation = str)
                    }())
                        .font(Font.caption)
                        .fontWeight(Font.Weight.bold)
                        .foregroundStyle(Color.white)
                        .monospaced().Compose(composectx)

                    Spacer().Compose(composectx)

                    // Lives as dots
                    HStack(spacing = 4.0) { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            ForEach(0..<game.lives, id = { it }) { _ ->
                                ComposeBuilder { composectx: ComposeContext ->
                                    Circle()
                                        .fill(Color(red = 0.9, green = 0.3, blue = 0.3))
                                        .frame(width = 10.0, height = 10.0).Compose(composectx)
                                    ComposeResult.ok
                                }
                            }.Compose(composectx)
                            ComposeResult.ok
                        }
                    }.Compose(composectx)

                    Spacer().Compose(composectx)

                    Text({
                        val str = LocalizedStringKey.StringInterpolation(literalCapacity = 0, interpolationCount = 0)
                        str.appendLiteral("LV ")
                        str.appendInterpolation(game.level)
                        LocalizedStringKey(stringInterpolation = str)
                    }())
                        .font(Font.caption)
                        .fontWeight(Font.Weight.bold)
                        .foregroundStyle(Color.white.opacity(0.7))
                        .monospaced().Compose(composectx)

                    Button(action = { ->
                        showPauseMenu = true
                        stopTimer()
                    }) { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            Image("pause_circle", bundle = Bundle.module)
                                .font(Font.title2)
                                .foregroundStyle(Color.white.opacity(0.6)).Compose(composectx)
                            ComposeResult.ok
                        }
                    }.Compose(composectx)
                    ComposeResult.ok
                }
            }
            .padding(Edge.Set.horizontal, 12.0)
            .padding(Edge.Set.vertical, 6.0)
            .background(Color(red = 0.04, green = 0.04, blue = 0.12))
        }

    // MARK: - Launch Prompt

    internal val launchPrompt: View
        get() {
            return VStack(spacing = 8.0) { ->
                ComposeBuilder { composectx: ComposeContext ->
                    Text(LocalizedStringKey(stringLiteral = "TAP TO LAUNCH"), bundle = Bundle.module)
                        .font(Font.headline)
                        .fontWeight(Font.Weight.black)
                        .foregroundStyle(Color.white)
                        .shadow(color = Color.black.opacity(0.4), radius = 2.0, x = 1.0, y = 1.0).Compose(composectx)
                    Text(LocalizedStringKey(stringLiteral = "DRAG TO MOVE PADDLE"), bundle = Bundle.module)
                        .font(Font.caption)
                        .fontWeight(Font.Weight.bold)
                        .foregroundStyle(Color.white.opacity(0.6)).Compose(composectx)
                    ComposeResult.ok
                }
            }
        }

    // MARK: - Pause Menu

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

                            Button(action = { ->
                                showPauseMenu = false
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
                                showPauseMenu = false
                                BreakoutModel.clearSavedState()
                                game.newGame()
                                startTimer()
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
                    }
                    .padding(28.0)
                    .background(RoundedRectangle(cornerRadius = 20.0)
                        .fill(Color(red = 0.08, green = 0.08, blue = 0.18))).Compose(composectx)
                    ComposeResult.ok
                }
            }
        }

    // MARK: - Level Complete

    internal val levelCompleteOverlay: View
        get() {
            return ZStack { ->
                ComposeBuilder { composectx: ComposeContext ->
                    Color.black.opacity(0.6)
                        .ignoresSafeArea().Compose(composectx)

                    VStack(spacing = 16.0) { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            Text({
                                val str = LocalizedStringKey.StringInterpolation(literalCapacity = 0, interpolationCount = 0)
                                str.appendLiteral("LEVEL ")
                                str.appendInterpolation(game.level)
                                str.appendLiteral(" CLEAR!")
                                LocalizedStringKey(stringInterpolation = str)
                            }())
                                .font(Font.largeTitle)
                                .fontWeight(Font.Weight.black)
                                .foregroundStyle(Color.yellow).Compose(composectx)

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
                                        .font(Font.system(size = 40.0))
                                        .fontWeight(Font.Weight.bold)
                                        .foregroundStyle(Color.white)
                                        .monospaced().Compose(composectx)
                                    ComposeResult.ok
                                }
                            }.Compose(composectx)

                            Button(action = { ->
                                BreakoutModel.clearSavedState()
                                game.startLevel(lvl = game.level + 1)
                                startTimer()
                                playHaptic(HapticPattern.snap)
                            }) { ->
                                ComposeBuilder { composectx: ComposeContext ->
                                    Text(LocalizedStringKey(stringLiteral = "Next Level"), bundle = Bundle.module)
                                        .font(Font.headline)
                                        .fontWeight(Font.Weight.bold)
                                        .foregroundStyle(Color.white)
                                        .frame(width = 180.0, height = 48.0)
                                        .background(RoundedRectangle(cornerRadius = 12.0)
                                            .fill(Color(red = 0.2, green = 0.6, blue = 0.3))).Compose(composectx)
                                    ComposeResult.ok
                                }
                            }
                            .buttonStyle(ButtonStyle.plain)
                            .padding(Edge.Set.top, 4.0).Compose(composectx)
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

    // MARK: - Game Over

    internal val gameOverOverlay: View
        get() {
            return ZStack { ->
                ComposeBuilder { composectx: ComposeContext ->
                    Color.black.opacity(0.7)
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
                                            Text(LocalizedStringKey(stringLiteral = "Best"), bundle = Bundle.module)
                                                .font(Font.caption)
                                                .foregroundStyle(Color.white.opacity(0.6)).Compose(composectx)
                                            Text({
                                                val str = LocalizedStringKey.StringInterpolation(literalCapacity = 0, interpolationCount = 0)
                                                str.appendInterpolation(game.highScore)
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
                                BreakoutModel.clearSavedState()
                                game.newGame()
                                startTimer()
                                playHaptic(HapticPattern.snap)
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

                            ShareLink(item = "I scored ${game.score} (level ${game.level}) in Breakout on Faire Games! Can you beat it?\nhttps://appfair.net", subject = Text(LocalizedStringKey(stringLiteral = "Breakout Score"), bundle = Bundle.module), message = Text({
                                val str = LocalizedStringKey.StringInterpolation(literalCapacity = 0, interpolationCount = 0)
                                str.appendLiteral("I scored ")
                                str.appendInterpolation(game.score)
                                str.appendLiteral(" in Breakout!")
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
                        .fill(Color(red = 0.08, green = 0.08, blue = 0.18))).Compose(composectx)
                    ComposeResult.ok
                }
            }
        }

    // MARK: - Timer

    internal fun startTimer() {
        stopTimer()
        lastTick = currentTime()
        tickTimer = Timer.scheduledTimer(withTimeInterval = 1.0 / 60.0, repeats = true) { _ -> tick() }
    }

    internal fun stopTimer() {
        tickTimer?.invalidate()
        tickTimer = null
    }

    internal fun tick() {
        val now = currentTime()
        var dt = now - lastTick
        lastTick = now
        if (dt > 0.1) {
            dt = 0.016
        }

        if (showPauseMenu) {
            return
        }

        val wasBrickCount = aliveBrickCount()
        game.update(dt = dt)
        val nowBrickCount = aliveBrickCount()

        if (nowBrickCount < wasBrickCount) {
            playHaptic(HapticPattern.snap)
        }

        // Paddle hit haptic — intensity varies with deflection
        if (game.lastPaddleDeflection >= 0.0) {
            val deflection = game.lastPaddleDeflection
            // 0 = mirror reflection (hardest hit), 1 = max deflection (lightest)
            val intensity = 1.0 - deflection * 0.7 // range: 1.0 (mirror) to 0.3 (max deflection)
            if (deflection < 0.15) {
                // Near-mirror: heavy thud + tap (satisfying direct return)
                HapticFeedback.play(HapticPattern(arrayOf(
                    HapticEvent(HapticEventType.thud, intensity = intensity),
                    HapticEvent(HapticEventType.tap, intensity = intensity * 0.7, delay = 0.04)
                )))
            } else if (deflection < 0.5) {
                // Moderate deflection: medium tap
                HapticFeedback.play(HapticPattern(arrayOf(
                    HapticEvent(HapticEventType.tap, intensity = intensity)
                )))
            } else {
                // Large deflection: light tick
                HapticFeedback.play(HapticPattern(arrayOf(
                    HapticEvent(HapticEventType.tick, intensity = intensity)
                )))
            }
        }

        if (game.isGameOver) {
            playHaptic(HapticPattern.impact)
            stopTimer()
        }
    }

    internal fun aliveBrickCount(): Int {
        var count = 0
        for (row in game.bricks.sref()) {
            for (brick in row.sref()) {
                if (brick.alive) {
                    count += 1
                }
            }
        }
        return count
    }

    internal fun currentTime(): Double = Date().timeIntervalSince1970

    private constructor(showInstructions: Binding<Boolean>, game: BreakoutModel = BreakoutModel(), tickTimer: Timer? = null, lastTick: Double = 0.0, showSettings: Boolean = false, showPauseMenu: Boolean = false, debugText: String = "waiting for touch", debugTouchCount: Int = 0, dragAnchorX: Double? = null, privatep: Nothing? = null) {
        this._showInstructions = showInstructions
        this._game = skip.ui.State(game)
        this._tickTimer = skip.ui.State(tickTimer)
        this._lastTick = skip.ui.State(lastTick)
        this._showSettings = skip.ui.State(showSettings)
        this._showPauseMenu = skip.ui.State(showPauseMenu)
        this._debugText = skip.ui.State(debugText)
        this._debugTouchCount = skip.ui.State(debugTouchCount)
        this._dragAnchorX = skip.ui.State(dragAnchorX)
    }

    constructor(showInstructions: Binding<Boolean>): this(showInstructions = showInstructions, privatep = null) {
    }
}

// MARK: - Preview Icon

class BreakoutPreviewIcon: View {
    constructor() {
    }

    override fun body(): View {
        return ComposeBuilder { composectx: ComposeContext ->
            ZStack { ->
                ComposeBuilder { composectx: ComposeContext ->
                    // Dark background
                    Color(red = 0.04, green = 0.04, blue = 0.12).Compose(composectx)

                    // Mini brick rows
                    VStack(spacing = 2.0) { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            ForEach(0..<5, id = { it }) { r ->
                                ComposeBuilder { composectx: ComposeContext ->
                                    HStack(spacing = 2.0) { ->
                                        ComposeBuilder { composectx: ComposeContext ->
                                            ForEach(0..<6, id = { it }) { c ->
                                                ComposeBuilder { composectx: ComposeContext ->
                                                    val ci = r % rowColors.count
                                                    val col = rowColors[ci]
                                                    RoundedRectangle(cornerRadius = 1.0)
                                                        .fill(Color(red = col.element0, green = col.element1, blue = col.element2))
                                                        .frame(height = 6.0).Compose(composectx)
                                                    ComposeResult.ok
                                                }
                                            }.Compose(composectx)
                                            ComposeResult.ok
                                        }
                                    }
                                    .padding(Edge.Set.horizontal, 6.0).Compose(composectx)
                                    ComposeResult.ok
                                }
                            }.Compose(composectx)
                            ComposeResult.ok
                        }
                    }
                    .padding(Edge.Set.top, 18.0).Compose(composectx)

                    // Mini ball
                    Circle()
                        .fill(Color.white)
                        .frame(width = 6.0, height = 6.0)
                        .offset(y = 16.0).Compose(composectx)

                    // Mini paddle
                    VStack { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            Spacer().Compose(composectx)
                            RoundedRectangle(cornerRadius = 3.0)
                                .fill(Color(red = 0.60, green = 0.65, blue = 0.75))
                                .frame(width = 30.0, height = 6.0)
                                .padding(Edge.Set.bottom, 14.0).Compose(composectx)
                            ComposeResult.ok
                        }
                    }.Compose(composectx)
                    ComposeResult.ok
                }
            }
            .clipShape(RoundedRectangle(cornerRadius = 12.0)).Compose(composectx)
        }
    }

    @androidx.annotation.Keep
    companion object {
    }
}

// MARK: - Settings

internal class BreakoutSettingsView: View {
    internal var settings: BreakoutSettings
        get() = _settings.wrappedValue
        set(newValue) {
            _settings.wrappedValue = newValue
        }
    internal var _settings: skip.ui.Bindable<BreakoutSettings>
    internal lateinit var dismiss: DismissAction

    override fun body(): View {
        return ComposeBuilder { composectx: ComposeContext ->
            NavigationStack { ->
                ComposeBuilder { composectx: ComposeContext ->
                    Form { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            Section(header = Text(LocalizedStringKey(stringLiteral = "Breakout"), bundle = Bundle.module)) { ->
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
                            Section(header = Text(LocalizedStringKey(stringLiteral = "Debug"), bundle = Bundle.module)) { ->
                                ComposeBuilder { composectx: ComposeContext ->
                                    Toggle(isOn = Binding({ _settings.wrappedValue.debugInfo }, { it -> _settings.wrappedValue.debugInfo = it })) { ->
                                        ComposeBuilder { composectx: ComposeContext ->
                                            Text(LocalizedStringKey(stringLiteral = "Debug Information"), bundle = Bundle.module).Compose(composectx)
                                            ComposeResult.ok
                                        }
                                    }.Compose(composectx)
                                    ComposeResult.ok
                                }
                            }.Compose(composectx)
                            Section(header = Text(LocalizedStringKey(stringLiteral = "Data"), bundle = Bundle.module)) { ->
                                ComposeBuilder { composectx: ComposeContext ->
                                    Button(role = ButtonRole.destructive, action = { -> resetBreakoutHighScore() }) { ->
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

    constructor(settings: BreakoutSettings) {
        this._settings = skip.ui.Bindable(settings)
    }
}

@Stable
open class BreakoutSettings: Observable {
    open var vibrations: Boolean
        get() = _vibrations.wrappedValue
        set(newValue) {
            _vibrations.wrappedValue = newValue
            defaults.set(vibrations, forKey = "breakoutVibrations")
        }
    var _vibrations: skip.model.Observed<Boolean> = skip.model.Observed(defaults.value(forKey = "breakoutVibrations", default = true))

    open var debugInfo: Boolean
        get() = _debugInfo.wrappedValue
        set(newValue) {
            _debugInfo.wrappedValue = newValue
            defaults.set(debugInfo, forKey = "breakoutDebugInfo")
        }
    var _debugInfo: skip.model.Observed<Boolean> = skip.model.Observed(defaults.value(forKey = "breakoutDebugInfo", default = false))

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
