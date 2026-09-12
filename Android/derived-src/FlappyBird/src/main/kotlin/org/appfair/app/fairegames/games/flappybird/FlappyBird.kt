// Licensed under the GNU General Public License v2.0 or later
// SPDX-License-Identifier: GPL-2.0-or-later

package org.appfair.app.fairegames.games.flappybird

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

class FlappyBirdContainerView: View {
    private var settings: FlappyBirdSettings
        get() = _settings.wrappedValue
        set(newValue) {
            _settings.wrappedValue = newValue
        }
    private var _settings: skip.ui.State<FlappyBirdSettings> = skip.ui.State(FlappyBirdSettings())
    private var showInstructions: Boolean
        get() = _showInstructions.wrappedValue
        set(newValue) {
            _showInstructions.wrappedValue = newValue
        }
    private var _showInstructions: skip.ui.State<Boolean> = skip.ui.State(false)
    private val instructionsConfig = GameInstructionsConfig(key = "FlappyBird.instructions", bundle = Bundle.module, firstLaunchKey = "instructionsShown_FlappyBird", title = "Flappy Bird")

    constructor() {
    }

    override fun body(): View {
        return ComposeBuilder { composectx: ComposeContext ->
            FlappyBirdGameView(showInstructions = Binding({ _showInstructions.wrappedValue }, { it -> _showInstructions.wrappedValue = it }))
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
        val rememberedsettings by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<FlappyBirdSettings>, Any>) { mutableStateOf(_settings) }
        _settings = rememberedsettings

        val rememberedshowInstructions by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Boolean>, Any>) { mutableStateOf(_showInstructions) }
        _showInstructions = rememberedshowInstructions

        return super.Evaluate(context, options)
    }

    @androidx.annotation.Keep
    companion object {
    }
}

fun resetFlappyBirdHighScore(): Unit = UserDefaults.standard.set(0, forKey = "flappybird_highscore")

// MARK: - Constants

private val birdSize: Double = 30.0
private val groundHeight: Double = 80.0
private val birdX: Double = 80.0
private val pipeWidth: Double = 52.0

/// Difficulty-dependent parameters. Difficulty ranges from 1 (easiest) to 10 (hardest).
/// Level 5 matches the original game feel.
private fun effectiveGravity(difficulty: Int): Double {
    // 1 → 750, 5 → 950, 10 → 1200
    return 750.0 + Double(difficulty - 1) * 50.0
}

private fun effectiveFlapVelocity(difficulty: Int): Double {
    // 1 → -290, 5 → -330, 10 → -380
    return -290.0 - Double(difficulty - 1) * 10.0
}

private fun effectivePipeSpeed(difficulty: Int): Double {
    // 1 → 90, 5 → 130, 10 → 180
    return 90.0 + Double(difficulty - 1) * 10.0
}

private fun effectivePipeGap(difficulty: Int): Double {
    // 1 → 210, 5 → 160, 10 → 115
    return 210.0 - Double(difficulty - 1) * 10.5
}

private fun effectivePipeSpacing(difficulty: Int): Double {
    // 1 → 280, 5 → 210, 10 → 155
    return 280.0 - Double(difficulty - 1) * 14.0
}

// MARK: - Pipe Model

internal class PipeData: Identifiable<Int> {
    override val id: Int
    internal var x: Double
    internal val gapY: Double // center of the gap
    internal var scored: Boolean

    internal constructor(id: Int, x: Double, gapY: Double) {
        this.id = id
        this.x = x
        this.gapY = gapY
        this.scored = false
    }
}

// MARK: - Saved State

@androidx.annotation.Keep
@Suppress("MUST_BE_INITIALIZED")
internal class FlappyBirdSavedState: Codable, MutableStruct {
    internal var birdY: Double
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    internal var birdVelocity: Double
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    internal var birdRotation: Double
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    internal var pipeIds: Array<Int>
        get() = field.sref({ this.pipeIds = it })
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
            willmutate()
            field = newValue
            didmutate()
        }
    internal var pipeXs: Array<Double>
        get() = field.sref({ this.pipeXs = it })
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
            willmutate()
            field = newValue
            didmutate()
        }
    internal var pipeGapYs: Array<Double>
        get() = field.sref({ this.pipeGapYs = it })
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
            willmutate()
            field = newValue
            didmutate()
        }
    internal var pipeScored: Array<Boolean>
        get() = field.sref({ this.pipeScored = it })
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
    internal var hasStarted: Boolean
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    internal var difficulty: Int
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }

    constructor(birdY: Double, birdVelocity: Double, birdRotation: Double, pipeIds: Array<Int>, pipeXs: Array<Double>, pipeGapYs: Array<Double>, pipeScored: Array<Boolean>, score: Int, isGameOver: Boolean, hasStarted: Boolean, difficulty: Int) {
        this.birdY = birdY
        this.birdVelocity = birdVelocity
        this.birdRotation = birdRotation
        this.pipeIds = pipeIds
        this.pipeXs = pipeXs
        this.pipeGapYs = pipeGapYs
        this.pipeScored = pipeScored
        this.score = score
        this.isGameOver = isGameOver
        this.hasStarted = hasStarted
        this.difficulty = difficulty
    }

    override var supdate: ((Any) -> Unit)? = null
    override var smutatingcount = 0
    override fun scopy(): MutableStruct = FlappyBirdSavedState(birdY, birdVelocity, birdRotation, pipeIds, pipeXs, pipeGapYs, pipeScored, score, isGameOver, hasStarted, difficulty)

    private enum class CodingKeys(override val rawValue: String, @Suppress("UNUSED_PARAMETER") unusedp: Nothing? = null): CodingKey, RawRepresentable<String> {
        birdY("birdY"),
        birdVelocity("birdVelocity"),
        birdRotation("birdRotation"),
        pipeIds("pipeIds"),
        pipeXs("pipeXs"),
        pipeGapYs("pipeGapYs"),
        pipeScored("pipeScored"),
        score("score"),
        isGameOver("isGameOver"),
        hasStarted("hasStarted"),
        difficulty("difficulty");

        @androidx.annotation.Keep
        companion object {
            fun init(rawValue: String): CodingKeys? {
                return when (rawValue) {
                    "birdY" -> CodingKeys.birdY
                    "birdVelocity" -> CodingKeys.birdVelocity
                    "birdRotation" -> CodingKeys.birdRotation
                    "pipeIds" -> CodingKeys.pipeIds
                    "pipeXs" -> CodingKeys.pipeXs
                    "pipeGapYs" -> CodingKeys.pipeGapYs
                    "pipeScored" -> CodingKeys.pipeScored
                    "score" -> CodingKeys.score
                    "isGameOver" -> CodingKeys.isGameOver
                    "hasStarted" -> CodingKeys.hasStarted
                    "difficulty" -> CodingKeys.difficulty
                    else -> null
                }
            }
        }
    }

    override fun encode(to: Encoder) {
        val container = to.container(keyedBy = CodingKeys::class)
        container.encode(birdY, forKey = CodingKeys.birdY)
        container.encode(birdVelocity, forKey = CodingKeys.birdVelocity)
        container.encode(birdRotation, forKey = CodingKeys.birdRotation)
        container.encode(pipeIds, forKey = CodingKeys.pipeIds)
        container.encode(pipeXs, forKey = CodingKeys.pipeXs)
        container.encode(pipeGapYs, forKey = CodingKeys.pipeGapYs)
        container.encode(pipeScored, forKey = CodingKeys.pipeScored)
        container.encode(score, forKey = CodingKeys.score)
        container.encode(isGameOver, forKey = CodingKeys.isGameOver)
        container.encode(hasStarted, forKey = CodingKeys.hasStarted)
        container.encode(difficulty, forKey = CodingKeys.difficulty)
    }

    constructor(from: Decoder) {
        val container = from.container(keyedBy = CodingKeys::class)
        this.birdY = container.decode(Double::class, forKey = CodingKeys.birdY)
        this.birdVelocity = container.decode(Double::class, forKey = CodingKeys.birdVelocity)
        this.birdRotation = container.decode(Double::class, forKey = CodingKeys.birdRotation)
        this.pipeIds = container.decode(Array::class, elementType = Int::class, forKey = CodingKeys.pipeIds)
        this.pipeXs = container.decode(Array::class, elementType = Double::class, forKey = CodingKeys.pipeXs)
        this.pipeGapYs = container.decode(Array::class, elementType = Double::class, forKey = CodingKeys.pipeGapYs)
        this.pipeScored = container.decode(Array::class, elementType = Boolean::class, forKey = CodingKeys.pipeScored)
        this.score = container.decode(Int::class, forKey = CodingKeys.score)
        this.isGameOver = container.decode(Boolean::class, forKey = CodingKeys.isGameOver)
        this.hasStarted = container.decode(Boolean::class, forKey = CodingKeys.hasStarted)
        this.difficulty = container.decode(Int::class, forKey = CodingKeys.difficulty)
    }

    @androidx.annotation.Keep
    companion object: DecodableCompanion<FlappyBirdSavedState> {
        override fun init(from: Decoder): FlappyBirdSavedState = FlappyBirdSavedState(from = from)

        private fun CodingKeys(rawValue: String): CodingKeys? = CodingKeys.init(rawValue = rawValue)
    }
}

// MARK: - Game Model

/// Radius for the rounded pipe corners (visual and collision)
private val pipeCornerRadius: Double = 8.0

/// How many pixels to forgive at pipe opening corners for collision
private val pipeCornerInset: Double = 6.0

@Stable
internal class FlappyBirdModel: Observable {
    internal var birdY: Double
        get() = _birdY.wrappedValue
        set(newValue) {
            _birdY.wrappedValue = newValue
        }
    internal var _birdY: skip.model.Observed<Double> = skip.model.Observed(0.0)
    internal var birdVelocity: Double
        get() = _birdVelocity.wrappedValue
        set(newValue) {
            _birdVelocity.wrappedValue = newValue
        }
    internal var _birdVelocity: skip.model.Observed<Double> = skip.model.Observed(0.0)
    internal var birdRotation: Double
        get() = _birdRotation.wrappedValue
        set(newValue) {
            _birdRotation.wrappedValue = newValue
        }
    internal var _birdRotation: skip.model.Observed<Double> = skip.model.Observed(0.0)
    internal var wingAngle: Double
        get() = _wingAngle.wrappedValue
        set(newValue) {
            _wingAngle.wrappedValue = newValue
        }
    internal var _wingAngle: skip.model.Observed<Double> = skip.model.Observed(0.0) // -1 = up, 0 = mid, 1 = down
    internal var wingTimer: Double
        get() = _wingTimer.wrappedValue
        set(newValue) {
            _wingTimer.wrappedValue = newValue
        }
    internal var _wingTimer: skip.model.Observed<Double> = skip.model.Observed(0.0)
    internal var pipes: Array<PipeData>
        get() = _pipes.wrappedValue.sref({ this.pipes = it })
        set(newValue) {
            _pipes.wrappedValue = newValue.sref()
        }
    internal var _pipes: skip.model.Observed<Array<PipeData>> = skip.model.Observed(arrayOf())
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
    internal var _highScore: skip.model.Observed<Int> = skip.model.Observed(UserDefaults.standard.integer(forKey = "flappybird_highscore"))
    internal var isGameOver: Boolean
        get() = _isGameOver.wrappedValue
        set(newValue) {
            _isGameOver.wrappedValue = newValue
        }
    internal var _isGameOver: skip.model.Observed<Boolean> = skip.model.Observed(false)
    internal var hasStarted: Boolean
        get() = _hasStarted.wrappedValue
        set(newValue) {
            _hasStarted.wrappedValue = newValue
        }
    internal var _hasStarted: skip.model.Observed<Boolean> = skip.model.Observed(false)
    /// What the bird crashed into: "pipe", "ground", "ceiling", or "" if alive
    internal var crashType: String
        get() = _crashType.wrappedValue
        set(newValue) {
            _crashType.wrappedValue = newValue
        }
    internal var _crashType: skip.model.Observed<String> = skip.model.Observed("")
    internal var fieldHeight: Double
        get() = _fieldHeight.wrappedValue
        set(newValue) {
            _fieldHeight.wrappedValue = newValue
        }
    internal var _fieldHeight: skip.model.Observed<Double> = skip.model.Observed(600.0)
    internal var fieldWidth: Double
        get() = _fieldWidth.wrappedValue
        set(newValue) {
            _fieldWidth.wrappedValue = newValue
        }
    internal var _fieldWidth: skip.model.Observed<Double> = skip.model.Observed(400.0)
    internal var difficulty: Int
        get() = _difficulty.wrappedValue
        set(newValue) {
            _difficulty.wrappedValue = newValue
        }
    internal var _difficulty: skip.model.Observed<Int> = skip.model.Observed(5)

    internal var nextPipeID: Int
        get() = _nextPipeID.wrappedValue
        set(newValue) {
            _nextPipeID.wrappedValue = newValue
        }
    internal var _nextPipeID: skip.model.Observed<Int> = skip.model.Observed(0)

    private val gravity: Double
        get() = effectiveGravity(difficulty)
    private val flapVel: Double
        get() = effectiveFlapVelocity(difficulty)
    private val speed: Double
        get() = effectivePipeSpeed(difficulty)
    private val gap: Double
        get() = effectivePipeGap(difficulty)
    private val spacing: Double
        get() = effectivePipeSpacing(difficulty)

    internal fun setup(width: Double, height: Double) {
        fieldWidth = width
        fieldHeight = height
    }

    internal fun newGame() {
        birdY = fieldHeight * 0.4
        birdVelocity = 0.0
        birdRotation = 0.0
        wingAngle = 0.0
        wingTimer = 0.0
        pipes = arrayOf()
        score = 0
        isGameOver = false
        hasStarted = false
        crashType = ""
        nextPipeID = 0
    }

    internal fun flap() {
        if (isGameOver) {
            return
        }
        if (!hasStarted) {
            hasStarted = true
            spawnInitialPipes()
        }
        birdVelocity = flapVel
        wingTimer = 0.3 // start a flap cycle lasting 0.3s
        wingAngle = -1.0 // wing up
    }

    internal fun update(dt: Double) {
        if (!hasStarted || isGameOver) {
            return
        }

        // Physics
        birdVelocity += gravity * dt
        birdY += birdVelocity * dt

        // Bird rotation: nose up at -25 when flapping, rotate down to +90 when falling
        val clampedVel = min(max(birdVelocity, flapVel), 400.0)
        birdRotation = ((clampedVel - flapVel) / (400.0 - flapVel)) * 115.0 - 25.0

        // Wing animation: flap cycle over 0.3s
        // -1 (up) → 0 (mid) → 1 (down) → 0 (mid, rest)
        if (wingTimer > 0.0) {
            wingTimer -= dt
            if (wingTimer <= 0.0) {
                wingTimer = 0.0
                wingAngle = 0.0
            } else {
                val t = 1.0 - wingTimer / 0.3 // 0→1 over the cycle
                if (t < 0.33) {
                    wingAngle = -1.0 + t * 3.0 // -1 → 0
                } else if (t < 0.66) {
                    wingAngle = (t - 0.33) * 3.0 // 0 → 1
                } else {
                    wingAngle = 1.0 - (t - 0.66) * 3.0 // 1 → 0
                }
            }
        }

        // Move pipes
        val dx = speed * dt
        for (pipe in pipes.sref()) {
            pipe.x -= dx
        }

        // Score — bird passes the trailing edge of a pipe
        for (pipe in pipes.sref()) {
            if (!pipe.scored && pipe.x + pipeWidth < birdX) {
                pipe.scored = true
                score += 1
            }
        }

        // Remove off-screen pipes
        pipes = pipes.filter { it -> it.x + pipeWidth > -10.0 }

        // Spawn new pipes
        val matchtarget_0 = pipes.last
        if (matchtarget_0 != null) {
            val last = matchtarget_0
            if (last.x < fieldWidth - spacing) {
                spawnPipe(atX = fieldWidth + 20.0)
            }
        } else {
            spawnPipe(atX = fieldWidth + 20.0)
        }

        // Collision detection
        val playableHeight = fieldHeight - groundHeight

        // Ceiling
        if (birdY - birdSize / 2.0 < 0.0) {
            crashType = "ceiling"
            gameOver()
            return
        }

        // Ground
        if (birdY + birdSize / 2.0 > playableHeight) {
            crashType = "ground"
            gameOver()
            return
        }

        // Pipes — with forgiving rounded corners
        val birdLeft = birdX - birdSize / 2.0
        val birdRight = birdX + birdSize / 2.0
        val birdTop = birdY - birdSize / 2.0
        val birdBottom = birdY + birdSize / 2.0

        for (pipe in pipes.sref()) {
            if (birdRight > pipe.x && birdLeft < pipe.x + pipeWidth) {
                val topPipeBottom = pipe.gapY - gap / 2.0
                val bottomPipeTop = pipe.gapY + gap / 2.0

                // Check if bird is in the gap — no collision
                if (birdTop >= topPipeBottom && birdBottom <= bottomPipeTop) {
                    continue
                }

                // Near the pipe opening corners, give extra forgiveness
                // by shrinking the collision zone horizontally
                val nearTopOpening = abs(birdBottom - topPipeBottom) < pipeCornerInset
                val nearBottomOpening = abs(birdTop - bottomPipeTop) < pipeCornerInset
                if (nearTopOpening || nearBottomOpening) {
                    val insetLeft = pipe.x + pipeCornerInset
                    val insetRight = pipe.x + pipeWidth - pipeCornerInset
                    if (birdRight <= insetLeft || birdLeft >= insetRight) {
                        continue // bird clips only the rounded corner — forgive it
                    }
                }

                // Solid collision
                if (birdTop < topPipeBottom || birdBottom > bottomPipeTop) {
                    crashType = "pipe"
                    gameOver()
                    return
                }
            }
        }
    }

    private fun spawnInitialPipes() {
        var x = fieldWidth + 60.0
        for (unusedbinding in 0..<3) {
            spawnPipe(atX = x)
            x += spacing
        }
    }

    private fun spawnPipe(atX: Double) {
        val x = atX
        val playable = fieldHeight - groundHeight
        val margin = gap / 2.0 + 40.0
        val maxGap = playable - gap / 2.0 - 40.0
        val gapY = Double.random(in_ = margin..max(margin, maxGap))
        val pipe = PipeData(id = nextPipeID, x = x, gapY = gapY)
        nextPipeID += 1
        pipes.append(pipe)
    }

    private fun gameOver() {
        isGameOver = true
        if (score > highScore) {
            highScore = score
            UserDefaults.standard.set(highScore, forKey = "flappybird_highscore")
        }
        saveState()
    }

    // MARK: - State Persistence

    internal fun makeSavedState(): FlappyBirdSavedState {
        var pipeIds: Array<Int> = arrayOf()
        var pipeXs: Array<Double> = arrayOf()
        var pipeGapYs: Array<Double> = arrayOf()
        var pipeScored: Array<Boolean> = arrayOf()
        for (pipe in pipes.sref()) {
            pipeIds.append(pipe.id)
            pipeXs.append(pipe.x)
            pipeGapYs.append(pipe.gapY)
            pipeScored.append(pipe.scored)
        }
        return FlappyBirdSavedState(birdY = birdY, birdVelocity = birdVelocity, birdRotation = birdRotation, pipeIds = pipeIds, pipeXs = pipeXs, pipeGapYs = pipeGapYs, pipeScored = pipeScored, score = score, isGameOver = isGameOver, hasStarted = hasStarted, difficulty = difficulty)
    }

    internal fun restoreState(state: FlappyBirdSavedState) {
        birdY = state.birdY
        birdVelocity = state.birdVelocity
        birdRotation = state.birdRotation
        score = state.score
        isGameOver = state.isGameOver
        hasStarted = state.hasStarted
        difficulty = state.difficulty
        highScore = UserDefaults.standard.integer(forKey = "flappybird_highscore")

        var restoredPipes: Array<PipeData> = arrayOf()
        for (i in 0..<state.pipeIds.count) {
            val pipe = PipeData(id = state.pipeIds[i], x = state.pipeXs[i], gapY = state.pipeGapYs[i])
            pipe.scored = state.pipeScored[i]
            restoredPipes.append(pipe)
        }
        pipes = restoredPipes

        var maxId = 0
        for (pipe in pipes.sref()) {
            if (pipe.id > maxId) {
                maxId = pipe.id
            }
        }
        nextPipeID = maxId + 1
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
        UserDefaults.standard.set(json_0, forKey = "flappybird_saved_state")
    }

    @androidx.annotation.Keep
    companion object {

        internal fun loadSavedState(): FlappyBirdSavedState? {
            val json_1 = UserDefaults.standard.string(forKey = "flappybird_saved_state")
            if (json_1 == null) {
                return null
            }
            val data_1 = json_1.data(using = StringEncoding.utf8)
            if (data_1 == null) {
                return null
            }
            return try { JSONDecoder().decode(FlappyBirdSavedState::class, from = data_1) } catch (_: Throwable) { null }
        }

        internal fun clearSavedState(): Unit = UserDefaults.standard.removeObject(forKey = "flappybird_saved_state")
    }
}

// MARK: - Game View

internal class FlappyBirdGameView: View {
    internal var showInstructions: Boolean
        get() = _showInstructions.wrappedValue
        set(newValue) {
            _showInstructions.wrappedValue = newValue
        }
    internal var _showInstructions: Binding<Boolean>
    private var game: FlappyBirdModel
        get() = _game.wrappedValue
        set(newValue) {
            _game.wrappedValue = newValue
        }
    private var _game: skip.ui.State<FlappyBirdModel>
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
    internal lateinit var dismiss: DismissAction
    internal lateinit var scenePhase: ScenePhase
    internal var settings: FlappyBirdSettings
        get() = _settings.wrappedValue
        set(newValue) {
            _settings.wrappedValue = newValue
        }
    internal var _settings = skip.ui.Environment<FlappyBirdSettings>()

    internal fun playHaptic(pattern: HapticPattern) {
        if (settings.vibrations) {
            HapticFeedback.play(pattern)
        }
    }

    internal fun playFlapHaptic() {
        if (!settings.vibrations) {
            return
        }
        // Strong, satisfying wing-flap thud
        HapticFeedback.play(HapticPattern(arrayOf(
            HapticEvent(HapticEventType.thud, intensity = 0.7),
            HapticEvent(HapticEventType.tap, intensity = 0.5, delay = 0.03)
        )))
    }

    internal fun playCrashHaptic(type: String) {
        if (!settings.vibrations) {
            return
        }
        if (type == "pipe") {
            // Dramatic pipe crash: sharp impact + rattling aftershock
            HapticFeedback.play(HapticPattern(arrayOf(
                HapticEvent(HapticEventType.thud, intensity = 1.0),
                HapticEvent(HapticEventType.thud, intensity = 1.0, delay = 0.04),
                HapticEvent(HapticEventType.tap, intensity = 0.9, delay = 0.05),
                HapticEvent(HapticEventType.thud, intensity = 0.7, delay = 0.06),
                HapticEvent(HapticEventType.tick, intensity = 0.5, delay = 0.06),
                HapticEvent(HapticEventType.tick, intensity = 0.3, delay = 0.05)
            )))
        } else {
            // Ground/ceiling crash: single heavy slam + bounce
            HapticFeedback.play(HapticPattern(arrayOf(
                HapticEvent(HapticEventType.thud, intensity = 1.0),
                HapticEvent(HapticEventType.rise, intensity = 0.6, delay = 0.08),
                HapticEvent(HapticEventType.thud, intensity = 0.5, delay = 0.1),
                HapticEvent(HapticEventType.tick, intensity = 0.3, delay = 0.08)
            )))
        }
    }

    override fun body(): View {
        return ComposeBuilder { composectx: ComposeContext ->
            GeometryReader { geo ->
                ComposeBuilder { composectx: ComposeContext ->
                    initField(geo = geo)
                    ZStack { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            // Sky background
                            LinearGradient(colors = arrayOf(
                                Color(red = 0.30, green = 0.75, blue = 0.93),
                                Color(red = 0.55, green = 0.85, blue = 0.95)
                            ), startPoint = UnitPoint.top, endPoint = UnitPoint.bottom)
                                .ignoresSafeArea().Compose(composectx)

                            // Game field
                            gameField(width = geo.size.width, height = geo.size.height).Compose(composectx)

                            // HUD overlay
                            headerView
                                .frame(maxWidth = Double.infinity, maxHeight = Double.infinity, alignment = Alignment.top)
                                .padding(Edge.Set.top, 8.0).Compose(composectx)

                            // Tap to start
                            if (!game.hasStarted && !game.isGameOver) {
                                startPrompt.Compose(composectx)
                            }

                            // Game over
                            if (game.isGameOver) {
                                gameOverOverlay.Compose(composectx)
                            }

                            if (showPauseMenu && !game.isGameOver) {
                                pauseMenuOverlay.Compose(composectx)
                            }
                            ComposeResult.ok
                        }
                    }
                    .onTapGesture l@{ it ->
                        if (game.isGameOver || showPauseMenu) {
                            return@l
                        }
                        game.flap()
                        playFlapHaptic()
                    }.Compose(composectx)
                    ComposeResult.ok
                }
            }
            .navigationBarBackButtonHidden()
            .onAppear { ->
                game.difficulty = settings.difficulty
                val matchtarget_1 = FlappyBirdModel.loadSavedState()
                if (matchtarget_1 != null) {
                    val saved = matchtarget_1
                    game.restoreState(saved)
                    if (saved.isGameOver) {
                        // Show game over screen
                    } else if (saved.hasStarted) {
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
                    pauseGame()
                    game.saveState()
                }
            }
            .sheet(isPresented = Binding({ _showSettings.wrappedValue }, { it -> _showSettings.wrappedValue = it })) { ->
                ComposeBuilder { composectx: ComposeContext ->
                    FlappyBirdSettingsView(settings = settings)
                        .presentationDetents(setOf(PresentationDetent.medium, PresentationDetent.large)).Compose(composectx)
                    ComposeResult.ok
                }
            }
            .onChange(of = settings.difficulty) { _, newVal -> game.difficulty = newVal }.Compose(composectx)
        }
    }

    @Composable
    @Suppress("UNCHECKED_CAST")
    override fun Evaluate(context: ComposeContext, options: Int): kotlin.collections.List<Renderable> {
        val rememberedgame by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<FlappyBirdModel>, Any>) { mutableStateOf(_game) }
        _game = rememberedgame

        val rememberedtickTimer by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Timer?>, Any>) { mutableStateOf(_tickTimer) }
        _tickTimer = rememberedtickTimer

        val rememberedlastTick by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Double>, Any>) { mutableStateOf(_lastTick) }
        _lastTick = rememberedlastTick

        val rememberedshowPauseMenu by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Boolean>, Any>) { mutableStateOf(_showPauseMenu) }
        _showPauseMenu = rememberedshowPauseMenu

        val rememberedshowSettings by rememberSaveable(stateSaver = context.stateSaver as Saver<skip.ui.State<Boolean>, Any>) { mutableStateOf(_showSettings) }
        _showSettings = rememberedshowSettings

        this.dismiss = EnvironmentValues.shared.dismiss
        this.scenePhase = EnvironmentValues.shared.scenePhase
        _settings.wrappedValue = EnvironmentValues.shared.environmentObject(type = FlappyBirdSettings::class)!!

        return super.Evaluate(context, options)
    }

    private fun initField(geo: GeometryProxy): Boolean {
        game.setup(width = geo.size.width, height = geo.size.height)
        return true
    }

    // MARK: - Game Field

    internal fun gameField(width: Double, height: Double): View {
        val playableHeight = height - groundHeight

        return ZStack(alignment = Alignment.topLeading) { ->
            ComposeBuilder { composectx: ComposeContext ->
                // Pipes
                ForEach(game.pipes) { pipe ->
                    ComposeBuilder { composectx: ComposeContext ->
                        pipeView(pipe = pipe, playableHeight = playableHeight).Compose(composectx)
                        ComposeResult.ok
                    }
                }.Compose(composectx)

                // Bird
                birdView
                    .position(x = birdX, y = game.birdY).Compose(composectx)

                // Ground
                groundView(width = width, height = height).Compose(composectx)
                ComposeResult.ok
            }
        }
    }

    // MARK: - Bird
    //
    // Chunky, slightly wider-than-tall yellow bird viewed from the side.
    // Body is an egg-shaped ellipse with a dark outline. A small wing
    // protrudes from the back of the body and flaps when the player taps.
    // wingAngle drives the wing position: -1 = raised, 0 = mid, 1 = lowered.

    internal val birdView: View
        get() {
            val s = birdSize
            val w = s * 1.18 // slightly wider than tall, egg-shaped
            // Wing vertical offset driven by wingAngle (-1 up, 0 mid, 1 down)
            val wingY = game.wingAngle * s * 0.22

            return ZStack { ->
                ComposeBuilder { composectx: ComposeContext ->
                    // Dark outline — slightly larger ellipse behind the body
                    Ellipse()
                        .fill(Color(red = 0.20, green = 0.15, blue = 0.05))
                        .frame(width = w + 4, height = s + 4).Compose(composectx)

                    // Main body — warm yellow, egg-shaped
                    Ellipse()
                        .fill(Color(red = 0.98, green = 0.82, blue = 0.15))
                        .frame(width = w, height = s).Compose(composectx)

                    // Belly highlight — lighter yellow on the lower half
                    Ellipse()
                        .fill(Color(red = 1.0, green = 0.93, blue = 0.50))
                        .frame(width = w * 0.50, height = s * 0.30)
                        .offset(y = s * 0.18).Compose(composectx)

                    // Wing — small rounded shape that moves up/down with the flap
                    // Wing outline
                    Ellipse()
                        .fill(Color(red = 0.20, green = 0.15, blue = 0.05))
                        .frame(width = s * 0.42, height = s * 0.28)
                        .offset(x = -w * 0.22, y = s * 0.02 + wingY).Compose(composectx)
                    // Wing fill
                    Ellipse()
                        .fill(Color(red = 0.90, green = 0.72, blue = 0.12))
                        .frame(width = s * 0.38, height = s * 0.24)
                        .offset(x = -w * 0.22, y = s * 0.02 + wingY).Compose(composectx)
                    // Wing inner highlight
                    Ellipse()
                        .fill(Color(red = 1.0, green = 0.88, blue = 0.35))
                        .frame(width = s * 0.22, height = s * 0.13)
                        .offset(x = -w * 0.22, y = s * 0.00 + wingY).Compose(composectx)

                    // Tail feathers — tiny dark mark at the back
                    Ellipse()
                        .fill(Color(red = 0.20, green = 0.15, blue = 0.05))
                        .frame(width = s * 0.14, height = s * 0.22)
                        .offset(x = -w * 0.52, y = -s * 0.02).Compose(composectx)
                    Ellipse()
                        .fill(Color(red = 0.75, green = 0.58, blue = 0.08))
                        .frame(width = s * 0.10, height = s * 0.18)
                        .offset(x = -w * 0.52, y = -s * 0.02).Compose(composectx)

                    // Eye — large white circle with black pupil, positioned upper-front
                    // Eye outline
                    Circle()
                        .fill(Color(red = 0.20, green = 0.15, blue = 0.05))
                        .frame(width = s * 0.40, height = s * 0.40)
                        .offset(x = w * 0.16, y = -s * 0.14).Compose(composectx)
                    Circle()
                        .fill(Color.white)
                        .frame(width = s * 0.36, height = s * 0.36)
                        .offset(x = w * 0.16, y = -s * 0.14).Compose(composectx)
                    // Pupil — pushed toward the front
                    Circle()
                        .fill(Color.black)
                        .frame(width = s * 0.17, height = s * 0.17)
                        .offset(x = w * 0.24, y = -s * 0.13).Compose(composectx)

                    // Beak — two-part, protruding from the front
                    // Upper beak (orange-yellow)
                    Ellipse()
                        .fill(Color(red = 0.20, green = 0.15, blue = 0.05))
                        .frame(width = s * 0.38, height = s * 0.20)
                        .offset(x = w * 0.44, y = s * 0.04).Compose(composectx)
                    Ellipse()
                        .fill(Color(red = 0.96, green = 0.58, blue = 0.12))
                        .frame(width = s * 0.34, height = s * 0.16)
                        .offset(x = w * 0.44, y = s * 0.04).Compose(composectx)

                    // Lower beak (darker red-orange)
                    Ellipse()
                        .fill(Color(red = 0.20, green = 0.15, blue = 0.05))
                        .frame(width = s * 0.34, height = s * 0.16)
                        .offset(x = w * 0.42, y = s * 0.15).Compose(composectx)
                    Ellipse()
                        .fill(Color(red = 0.88, green = 0.30, blue = 0.12))
                        .frame(width = s * 0.30, height = s * 0.12)
                        .offset(x = w * 0.42, y = s * 0.15).Compose(composectx)
                    ComposeResult.ok
                }
            }
            .rotationEffect(Angle.degrees(min(max(game.birdRotation, -25.0), 90.0)))
        }

    // MARK: - Pipes

    internal fun pipeView(pipe: PipeData, playableHeight: Double): View {
        val currentGap = effectivePipeGap(game.difficulty)
        val topHeight = pipe.gapY - currentGap / 2.0
        val bottomY = pipe.gapY + currentGap / 2.0
        val bottomHeight = playableHeight - bottomY

        return ZStack(alignment = Alignment.topLeading) { ->
            ComposeBuilder { composectx: ComposeContext ->
                // Top pipe
                if (topHeight > 0.0) {
                    pipeRect(width = pipeWidth, height = topHeight)
                        .position(x = pipe.x + pipeWidth / 2.0, y = topHeight / 2.0).Compose(composectx)

                    // Top pipe cap (lip at the opening)
                    pipeCap(width = pipeWidth + 8.0)
                        .position(x = pipe.x + pipeWidth / 2.0, y = topHeight - 12.0).Compose(composectx)
                }

                // Bottom pipe
                if (bottomHeight > 0.0) {
                    pipeRect(width = pipeWidth, height = bottomHeight)
                        .position(x = pipe.x + pipeWidth / 2.0, y = bottomY + bottomHeight / 2.0).Compose(composectx)

                    // Bottom pipe cap
                    pipeCap(width = pipeWidth + 8.0)
                        .position(x = pipe.x + pipeWidth / 2.0, y = bottomY + 12.0).Compose(composectx)
                }
                ComposeResult.ok
            }
        }
    }

    internal fun pipeRect(width: Double, height: Double): View {
        return ZStack { ->
            ComposeBuilder { composectx: ComposeContext ->
                RoundedRectangle(cornerRadius = pipeCornerRadius)
                    .fill(Color(red = 0.32, green = 0.68, blue = 0.22))
                    .frame(width = width, height = height).Compose(composectx)
                // Highlight stripe
                RoundedRectangle(cornerRadius = pipeCornerRadius - 2.0)
                    .fill(Color(red = 0.42, green = 0.78, blue = 0.30))
                    .frame(width = width * 0.3, height = height)
                    .offset(x = -width * 0.15).Compose(composectx)
                // Shadow stripe
                RoundedRectangle(cornerRadius = pipeCornerRadius - 2.0)
                    .fill(Color(red = 0.22, green = 0.55, blue = 0.15))
                    .frame(width = width * 0.15, height = height)
                    .offset(x = width * 0.35).Compose(composectx)
                ComposeResult.ok
            }
        }
    }

    internal fun pipeCap(width: Double): View {
        return ZStack { ->
            ComposeBuilder { composectx: ComposeContext ->
                RoundedRectangle(cornerRadius = pipeCornerRadius)
                    .fill(Color(red = 0.32, green = 0.68, blue = 0.22))
                    .frame(width = width, height = 24.0).Compose(composectx)
                RoundedRectangle(cornerRadius = pipeCornerRadius - 2.0)
                    .fill(Color(red = 0.42, green = 0.78, blue = 0.30))
                    .frame(width = width * 0.3, height = 24.0)
                    .offset(x = -width * 0.15).Compose(composectx)
                ComposeResult.ok
            }
        }
    }

    // MARK: - Ground

    internal fun groundView(width: Double, height: Double): View {
        return VStack(spacing = 0.0) { ->
            ComposeBuilder { composectx: ComposeContext ->
                Spacer().Compose(composectx)
                // Grass edge
                Rectangle()
                    .fill(Color(red = 0.55, green = 0.78, blue = 0.22))
                    .frame(height = 8.0).Compose(composectx)
                // Dirt
                Rectangle()
                    .fill(Color(red = 0.84, green = 0.72, blue = 0.48))
                    .frame(height = groundHeight - 8.0).Compose(composectx)
                ComposeResult.ok
            }
        }
        .frame(width = width, height = height)
    }

    // MARK: - HUD

    internal val headerView: View
        get() {
            return HStack(spacing = 12.0) { ->
                ComposeBuilder { composectx: ComposeContext ->
                    Button(action = { -> dismiss() }) { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            Image("cancel", bundle = Bundle.module)
                                .font(Font.title2)
                                .foregroundStyle(Color.white.opacity(0.8)).Compose(composectx)
                            ComposeResult.ok
                        }
                    }.Compose(composectx)
                    Spacer().Compose(composectx)

                    // Score display
                    VStack(spacing = 0.0) { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            Text({
                                val str = LocalizedStringKey.StringInterpolation(literalCapacity = 0, interpolationCount = 0)
                                str.appendInterpolation(game.score)
                                LocalizedStringKey(stringInterpolation = str)
                            }())
                                .font(Font.system(size = 40.0))
                                .fontWeight(Font.Weight.black)
                                .foregroundStyle(Color.white)
                                .shadow(color = Color.black.opacity(0.3), radius = 2.0, x = 1.0, y = 1.0).Compose(composectx)
                            ComposeResult.ok
                        }
                    }.Compose(composectx)

                    Spacer().Compose(composectx)
                    Button(action = { -> pauseGame() }) { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            Image("pause_circle", bundle = Bundle.module)
                                .font(Font.title2)
                                .foregroundStyle(Color.white.opacity(0.8)).Compose(composectx)
                            ComposeResult.ok
                        }
                    }.Compose(composectx)
                    ComposeResult.ok
                }
            }
            .padding(Edge.Set.horizontal, 16.0)
        }

    // MARK: - Start Prompt

    internal val startPrompt: View
        get() {
            return VStack(spacing = 16.0) { ->
                ComposeBuilder { composectx: ComposeContext ->
                    Text(LocalizedStringKey(stringLiteral = "TAP TO FLY"), bundle = Bundle.module)
                        .font(Font.title)
                        .fontWeight(Font.Weight.black)
                        .foregroundStyle(Color.white)
                        .shadow(color = Color.black.opacity(0.3), radius = 2.0, x = 1.0, y = 1.0).Compose(composectx)

                    // Bouncing arrow hint
                    Text(LocalizedStringKey(stringLiteral = "\u25B2"), bundle = Bundle.module)
                        .font(Font.largeTitle)
                        .foregroundStyle(Color.white.opacity(0.7)).Compose(composectx)
                    ComposeResult.ok
                }
            }
        }

    // MARK: - Game Over

    internal val gameOverOverlay: View
        get() {
            return ZStack { ->
                ComposeBuilder { composectx: ComposeContext ->
                    Color.black.opacity(0.5)
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

                            VStack(spacing = 2.0) { ->
                                ComposeBuilder { composectx: ComposeContext ->
                                    Text(LocalizedStringKey(stringLiteral = "Difficulty"), bundle = Bundle.module)
                                        .font(Font.caption)
                                        .foregroundStyle(Color.white.opacity(0.6)).Compose(composectx)
                                    Text({
                                        val str = LocalizedStringKey.StringInterpolation(literalCapacity = 0, interpolationCount = 0)
                                        str.appendInterpolation(game.difficulty)
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
                                FlappyBirdModel.clearSavedState()
                                game.difficulty = settings.difficulty
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

                            ShareLink(item = "I scored ${game.score} in Flappy Bird (difficulty ${game.difficulty}) on Faire Games! Can you beat it?\nhttps://appfair.net", subject = Text(LocalizedStringKey(stringLiteral = "Flappy Bird Score"), bundle = Bundle.module), message = Text({
                                val str = LocalizedStringKey.StringInterpolation(literalCapacity = 0, interpolationCount = 0)
                                str.appendLiteral("I scored ")
                                str.appendInterpolation(game.score)
                                str.appendLiteral(" in Flappy Bird!")
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

                            Button(action = { ->
                                FlappyBirdModel.clearSavedState()
                                game.newGame()
                                showPauseMenu = false
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
                        .fill(Color(red = 0.1, green = 0.1, blue = 0.2))).Compose(composectx)
                    ComposeResult.ok
                }
            }
        }

    internal fun pauseGame() {
        if (showPauseMenu) {
            return
        }
        stopTimer()
        showPauseMenu = true
    }

    internal fun resumeGame() {
        showPauseMenu = false
        startTimer()
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

        // Clamp to avoid huge jumps after backgrounding
        if (dt > 0.1) {
            dt = 0.016
        }

        val wasAlive = !game.isGameOver
        game.update(dt = dt)

        if (game.isGameOver && wasAlive) {
            playCrashHaptic(type = game.crashType)
            stopTimer()
        }
    }

    internal fun currentTime(): Double = Date().timeIntervalSince1970

    private constructor(showInstructions: Binding<Boolean>, game: FlappyBirdModel = FlappyBirdModel(), tickTimer: Timer? = null, lastTick: Double = 0.0, showPauseMenu: Boolean = false, showSettings: Boolean = false, privatep: Nothing? = null) {
        this._showInstructions = showInstructions
        this._game = skip.ui.State(game)
        this._tickTimer = skip.ui.State(tickTimer)
        this._lastTick = skip.ui.State(lastTick)
        this._showPauseMenu = skip.ui.State(showPauseMenu)
        this._showSettings = skip.ui.State(showSettings)
    }

    constructor(showInstructions: Binding<Boolean>): this(showInstructions = showInstructions, privatep = null) {
    }
}

// MARK: - Preview Icon

class FlappyBirdPreviewIcon: View {
    constructor() {
    }

    override fun body(): View {
        return ComposeBuilder { composectx: ComposeContext ->
            ZStack { ->
                ComposeBuilder { composectx: ComposeContext ->
                    // Sky
                    LinearGradient(colors = arrayOf(
                        Color(red = 0.30, green = 0.75, blue = 0.93),
                        Color(red = 0.55, green = 0.85, blue = 0.95)
                    ), startPoint = UnitPoint.top, endPoint = UnitPoint.bottom).Compose(composectx)

                    // Mini pipes
                    HStack(spacing = 20.0) { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            miniPipe(gapOffset = -10.0).Compose(composectx)
                            miniPipe(gapOffset = 8.0).Compose(composectx)
                            ComposeResult.ok
                        }
                    }.Compose(composectx)

                    // Mini bird — egg-shaped yellow body, wing, white eye, orange beak
                    ZStack { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            // Outline
                            Ellipse()
                                .fill(Color(red = 0.20, green = 0.15, blue = 0.05))
                                .frame(width = 18.0, height = 16.0).Compose(composectx)
                            // Body
                            Ellipse()
                                .fill(Color(red = 0.98, green = 0.82, blue = 0.15))
                                .frame(width = 16.0, height = 14.0).Compose(composectx)
                            // Wing
                            Ellipse()
                                .fill(Color(red = 0.90, green = 0.72, blue = 0.12))
                                .frame(width = 6.0, height = 4.0)
                                .offset(x = -4.0, y = 1.0).Compose(composectx)
                            // Eye
                            Circle()
                                .fill(Color.white)
                                .frame(width = 5.0, height = 5.0)
                                .offset(x = 4.0, y = -2.0).Compose(composectx)
                            Circle()
                                .fill(Color.black)
                                .frame(width = 2.5, height = 2.5)
                                .offset(x = 5.0, y = -1.5).Compose(composectx)
                            // Beak
                            Ellipse()
                                .fill(Color(red = 0.96, green = 0.58, blue = 0.12))
                                .frame(width = 6.0, height = 3.0)
                                .offset(x = 9.0, y = 1.0).Compose(composectx)
                            ComposeResult.ok
                        }
                    }
                    .offset(x = -8.0, y = -4.0).Compose(composectx)

                    // Ground
                    VStack { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            Spacer().Compose(composectx)
                            Rectangle()
                                .fill(Color(red = 0.55, green = 0.78, blue = 0.22))
                                .frame(height = 3.0).Compose(composectx)
                            Rectangle()
                                .fill(Color(red = 0.84, green = 0.72, blue = 0.48))
                                .frame(height = 16.0).Compose(composectx)
                            ComposeResult.ok
                        }
                    }.Compose(composectx)
                    ComposeResult.ok
                }
            }
            .clipShape(RoundedRectangle(cornerRadius = 12.0)).Compose(composectx)
        }
    }

    internal fun miniPipe(gapOffset: Double): View {
        return VStack(spacing = 28.0) { ->
            ComposeBuilder { composectx: ComposeContext ->
                RoundedRectangle(cornerRadius = 2.0)
                    .fill(Color(red = 0.32, green = 0.68, blue = 0.22))
                    .frame(width = 14.0, height = 30.0).Compose(composectx)
                RoundedRectangle(cornerRadius = 2.0)
                    .fill(Color(red = 0.32, green = 0.68, blue = 0.22))
                    .frame(width = 14.0, height = 30.0).Compose(composectx)
                ComposeResult.ok
            }
        }
        .offset(y = gapOffset)
    }

    @androidx.annotation.Keep
    companion object {
    }
}

// MARK: - Settings

internal class FlappyBirdSettingsView: View {
    internal var settings: FlappyBirdSettings
        get() = _settings.wrappedValue
        set(newValue) {
            _settings.wrappedValue = newValue
        }
    internal var _settings: skip.ui.Bindable<FlappyBirdSettings>
    internal lateinit var dismiss: DismissAction

    override fun body(): View {
        return ComposeBuilder { composectx: ComposeContext ->
            NavigationStack { ->
                ComposeBuilder { composectx: ComposeContext ->
                    Form { ->
                        ComposeBuilder { composectx: ComposeContext ->
                            Section(header = Text(LocalizedStringKey(stringLiteral = "Flappy Bird"), bundle = Bundle.module)) { ->
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
                                            Slider(value = Binding(get = { -> Double(settings.difficulty) }, set = { it -> settings.difficulty = Int(it.rounded()) }), in_ = 1.0..10.0, step = 1.0).Compose(composectx)
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
                            }
                            .textCase(null).Compose(composectx)
                            Section(header = Text(LocalizedStringKey(stringLiteral = "Data"), bundle = Bundle.module)) { ->
                                ComposeBuilder { composectx: ComposeContext ->
                                    Button(role = ButtonRole.destructive, action = { -> resetFlappyBirdHighScore() }) { ->
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

    constructor(settings: FlappyBirdSettings) {
        this._settings = skip.ui.Bindable(settings)
    }
}

@Stable
open class FlappyBirdSettings: Observable {
    open var vibrations: Boolean
        get() = _vibrations.wrappedValue
        set(newValue) {
            _vibrations.wrappedValue = newValue
            defaults.set(vibrations, forKey = "flappyBirdVibrations")
        }
    var _vibrations: skip.model.Observed<Boolean> = skip.model.Observed(defaults.value(forKey = "flappyBirdVibrations", default = true))

    open var difficulty: Int
        get() = _difficulty.wrappedValue
        set(newValue) {
            _difficulty.wrappedValue = newValue
            defaults.set(difficulty, forKey = "flappyBirdDifficulty")
        }
    var _difficulty: skip.model.Observed<Int> = skip.model.Observed(defaults.value(forKey = "flappyBirdDifficulty", default = 5))

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
