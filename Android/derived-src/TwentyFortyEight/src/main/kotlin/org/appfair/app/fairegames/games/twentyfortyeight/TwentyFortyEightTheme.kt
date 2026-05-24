// Licensed under the GNU General Public License v2.0 or later
// SPDX-License-Identifier: GPL-2.0-or-later

package org.appfair.app.fairegames.games.twentyfortyeight

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import skip.lib.*
import skip.lib.Array

import skip.ui.*
import skip.foundation.*
import skip.model.*

/// A color palette for the 2048 game. Defines every color the game's view layer
/// paints: window background, board frame, empty cells, score boxes, HUD text,
/// the per-value tile colors, and the tile text foregrounds. Six themes ship —
/// three light, three dark — and the user picks one from the Settings sheet.
class TwentyFortyEightTheme: Identifiable<String> {
    override val id: String
    val isDark: Boolean

    val background: Color
    val boardBackground: Color
    val emptyCellBackground: Color
    val emptyCellOpacity: Double
    val hudForeground: Color
    val hudBackground: Color
    val scoreBoxBackground: Color
    val scoreBoxLabel: Color
    val scoreBoxValue: Color
    val lowTileForeground: Color
    val highTileForeground: Color
    val tileBeyondColor: Color

    val tile2: Color
    val tile4: Color
    val tile8: Color
    val tile16: Color
    val tile32: Color
    val tile64: Color
    val tile128: Color
    val tile256: Color
    val tile512: Color
    val tile1024: Color
    val tile2048: Color

    fun tileColor(for_: Int): Color {
        val value = for_
        when (value) {
            2 -> return tile2
            4 -> return tile4
            8 -> return tile8
            16 -> return tile16
            32 -> return tile32
            64 -> return tile64
            128 -> return tile128
            256 -> return tile256
            512 -> return tile512
            1024 -> return tile1024
            2048 -> return tile2048
            0 -> return emptyCellBackground
            else -> return tileBeyondColor
        }
    }

    fun tileForeground(for_: Int): Color {
        val value = for_
        return if (value <= 4) lowTileForeground else highTileForeground
    }

    /// A condensed palette used by the theme picker preview (5 swatches).
    val previewSwatches: Array<Color>
        get() = arrayOf(tile2, tile8, tile32, tile128, tile2048)

    /// Localized display name of this theme. Uses literal `Text` calls so the
    /// xcstrings extractor can see each name as a translatable key.
    fun nameText(): Text {
        when (id) {
            "classic" -> return Text(LocalizedStringKey(stringLiteral = "Classic"), bundle = Bundle.module, comment = "2048 theme name — the original 2048 palette (beige board, warm orange/yellow tiles)")
            "sakura" -> return Text(LocalizedStringKey(stringLiteral = "Sakura"), bundle = Bundle.module, comment = "2048 theme name — pink cherry-blossom palette (light theme)")
            "lagoon" -> return Text(LocalizedStringKey(stringLiteral = "Lagoon"), bundle = Bundle.module, comment = "2048 theme name — bright tropical lagoon blue/teal palette (light theme)")
            "midnight" -> return Text(LocalizedStringKey(stringLiteral = "Midnight"), bundle = Bundle.module, comment = "2048 theme name — dark night-sky palette with neon accents")
            "forest" -> return Text(LocalizedStringKey(stringLiteral = "Forest"), bundle = Bundle.module, comment = "2048 theme name — dark deep-forest palette with autumn highlights")
            "ember" -> return Text(LocalizedStringKey(stringLiteral = "Ember"), bundle = Bundle.module, comment = "2048 theme name — dark warm palette evoking glowing embers and firelight")
            else -> return Text(LocalizedStringKey(stringLiteral = "Classic"), bundle = Bundle.module, comment = "2048 theme name — the original 2048 palette (beige board, warm orange/yellow tiles)")
        }
    }

    constructor(id: String, isDark: Boolean, background: Color, boardBackground: Color, emptyCellBackground: Color, emptyCellOpacity: Double, hudForeground: Color, hudBackground: Color, scoreBoxBackground: Color, scoreBoxLabel: Color, scoreBoxValue: Color, lowTileForeground: Color, highTileForeground: Color, tileBeyondColor: Color, tile2: Color, tile4: Color, tile8: Color, tile16: Color, tile32: Color, tile64: Color, tile128: Color, tile256: Color, tile512: Color, tile1024: Color, tile2048: Color) {
        this.id = id
        this.isDark = isDark
        this.background = background
        this.boardBackground = boardBackground
        this.emptyCellBackground = emptyCellBackground
        this.emptyCellOpacity = emptyCellOpacity
        this.hudForeground = hudForeground
        this.hudBackground = hudBackground
        this.scoreBoxBackground = scoreBoxBackground
        this.scoreBoxLabel = scoreBoxLabel
        this.scoreBoxValue = scoreBoxValue
        this.lowTileForeground = lowTileForeground
        this.highTileForeground = highTileForeground
        this.tileBeyondColor = tileBeyondColor
        this.tile2 = tile2
        this.tile4 = tile4
        this.tile8 = tile8
        this.tile16 = tile16
        this.tile32 = tile32
        this.tile64 = tile64
        this.tile128 = tile128
        this.tile256 = tile256
        this.tile512 = tile512
        this.tile1024 = tile1024
        this.tile2048 = tile2048
    }

    override fun equals(other: Any?): Boolean {
        if (other !is TwentyFortyEightTheme) return false
        return id == other.id && isDark == other.isDark && background == other.background && boardBackground == other.boardBackground && emptyCellBackground == other.emptyCellBackground && emptyCellOpacity == other.emptyCellOpacity && hudForeground == other.hudForeground && hudBackground == other.hudBackground && scoreBoxBackground == other.scoreBoxBackground && scoreBoxLabel == other.scoreBoxLabel && scoreBoxValue == other.scoreBoxValue && lowTileForeground == other.lowTileForeground && highTileForeground == other.highTileForeground && tileBeyondColor == other.tileBeyondColor && tile2 == other.tile2 && tile4 == other.tile4 && tile8 == other.tile8 && tile16 == other.tile16 && tile32 == other.tile32 && tile64 == other.tile64 && tile128 == other.tile128 && tile256 == other.tile256 && tile512 == other.tile512 && tile1024 == other.tile1024 && tile2048 == other.tile2048
    }

    override fun hashCode(): Int {
        var result = 1
        result = Hasher.combine(result, id)
        result = Hasher.combine(result, isDark)
        result = Hasher.combine(result, background)
        result = Hasher.combine(result, boardBackground)
        result = Hasher.combine(result, emptyCellBackground)
        result = Hasher.combine(result, emptyCellOpacity)
        result = Hasher.combine(result, hudForeground)
        result = Hasher.combine(result, hudBackground)
        result = Hasher.combine(result, scoreBoxBackground)
        result = Hasher.combine(result, scoreBoxLabel)
        result = Hasher.combine(result, scoreBoxValue)
        result = Hasher.combine(result, lowTileForeground)
        result = Hasher.combine(result, highTileForeground)
        result = Hasher.combine(result, tileBeyondColor)
        result = Hasher.combine(result, tile2)
        result = Hasher.combine(result, tile4)
        result = Hasher.combine(result, tile8)
        result = Hasher.combine(result, tile16)
        result = Hasher.combine(result, tile32)
        result = Hasher.combine(result, tile64)
        result = Hasher.combine(result, tile128)
        result = Hasher.combine(result, tile256)
        result = Hasher.combine(result, tile512)
        result = Hasher.combine(result, tile1024)
        result = Hasher.combine(result, tile2048)
        return result
    }

    @androidx.annotation.Keep
    companion object {

        val classic = TwentyFortyEightTheme(id = "classic", isDark = false, background = Color(red = 0.98, green = 0.97, blue = 0.94), boardBackground = Color(red = 0.47, green = 0.43, blue = 0.40), emptyCellBackground = Color(red = 0.80, green = 0.76, blue = 0.71), emptyCellOpacity = 0.35, hudForeground = Color(red = 0.47, green = 0.43, blue = 0.40), hudBackground = Color(red = 0.98, green = 0.97, blue = 0.94), scoreBoxBackground = Color(red = 0.47, green = 0.43, blue = 0.40), scoreBoxLabel = Color(red = 0.93, green = 0.89, blue = 0.85), scoreBoxValue = Color.white, lowTileForeground = Color(red = 0.47, green = 0.43, blue = 0.40), highTileForeground = Color.white, tileBeyondColor = Color(red = 0.24, green = 0.23, blue = 0.20), tile2 = Color(red = 0.93, green = 0.89, blue = 0.85), tile4 = Color(red = 0.93, green = 0.88, blue = 0.78), tile8 = Color(red = 0.95, green = 0.69, blue = 0.47), tile16 = Color(red = 0.96, green = 0.58, blue = 0.39), tile32 = Color(red = 0.96, green = 0.49, blue = 0.37), tile64 = Color(red = 0.96, green = 0.37, blue = 0.23), tile128 = Color(red = 0.93, green = 0.81, blue = 0.45), tile256 = Color(red = 0.93, green = 0.80, blue = 0.38), tile512 = Color(red = 0.93, green = 0.78, blue = 0.31), tile1024 = Color(red = 0.93, green = 0.77, blue = 0.25), tile2048 = Color(red = 0.93, green = 0.76, blue = 0.18))

        val sakura = TwentyFortyEightTheme(id = "sakura", isDark = false, background = Color(red = 0.99, green = 0.95, blue = 0.97), boardBackground = Color(red = 0.73, green = 0.45, blue = 0.55), emptyCellBackground = Color(red = 0.96, green = 0.85, blue = 0.88), emptyCellOpacity = 0.55, hudForeground = Color(red = 0.55, green = 0.25, blue = 0.40), hudBackground = Color(red = 0.99, green = 0.95, blue = 0.97), scoreBoxBackground = Color(red = 0.73, green = 0.45, blue = 0.55), scoreBoxLabel = Color(red = 0.99, green = 0.90, blue = 0.93), scoreBoxValue = Color.white, lowTileForeground = Color(red = 0.55, green = 0.25, blue = 0.40), highTileForeground = Color.white, tileBeyondColor = Color(red = 0.35, green = 0.10, blue = 0.30), tile2 = Color(red = 0.99, green = 0.92, blue = 0.94), tile4 = Color(red = 0.99, green = 0.84, blue = 0.89), tile8 = Color(red = 0.98, green = 0.70, blue = 0.80), tile16 = Color(red = 0.97, green = 0.55, blue = 0.72), tile32 = Color(red = 0.94, green = 0.42, blue = 0.62), tile64 = Color(red = 0.88, green = 0.30, blue = 0.52), tile128 = Color(red = 0.84, green = 0.62, blue = 0.82), tile256 = Color(red = 0.75, green = 0.48, blue = 0.78), tile512 = Color(red = 0.65, green = 0.36, blue = 0.70), tile1024 = Color(red = 0.55, green = 0.26, blue = 0.62), tile2048 = Color(red = 0.46, green = 0.18, blue = 0.52))

        val lagoon = TwentyFortyEightTheme(id = "lagoon", isDark = false, background = Color(red = 0.93, green = 0.97, blue = 0.99), boardBackground = Color(red = 0.20, green = 0.45, blue = 0.60), emptyCellBackground = Color(red = 0.82, green = 0.92, blue = 0.96), emptyCellOpacity = 0.55, hudForeground = Color(red = 0.10, green = 0.35, blue = 0.50), hudBackground = Color(red = 0.93, green = 0.97, blue = 0.99), scoreBoxBackground = Color(red = 0.20, green = 0.45, blue = 0.60), scoreBoxLabel = Color(red = 0.85, green = 0.95, blue = 0.99), scoreBoxValue = Color.white, lowTileForeground = Color(red = 0.18, green = 0.38, blue = 0.50), highTileForeground = Color.white, tileBeyondColor = Color(red = 0.05, green = 0.25, blue = 0.35), tile2 = Color(red = 0.92, green = 0.97, blue = 0.99), tile4 = Color(red = 0.80, green = 0.92, blue = 0.98), tile8 = Color(red = 0.55, green = 0.82, blue = 0.95), tile16 = Color(red = 0.35, green = 0.72, blue = 0.93), tile32 = Color(red = 0.22, green = 0.62, blue = 0.88), tile64 = Color(red = 0.15, green = 0.48, blue = 0.80), tile128 = Color(red = 0.40, green = 0.85, blue = 0.85), tile256 = Color(red = 0.25, green = 0.75, blue = 0.78), tile512 = Color(red = 0.18, green = 0.65, blue = 0.70), tile1024 = Color(red = 0.12, green = 0.55, blue = 0.62), tile2048 = Color(red = 0.08, green = 0.45, blue = 0.55))

        val midnight = TwentyFortyEightTheme(id = "midnight", isDark = true, background = Color(red = 0.05, green = 0.06, blue = 0.12), boardBackground = Color(red = 0.10, green = 0.12, blue = 0.20), emptyCellBackground = Color(red = 0.18, green = 0.20, blue = 0.30), emptyCellOpacity = 0.60, hudForeground = Color(red = 0.85, green = 0.88, blue = 0.96), hudBackground = Color(red = 0.05, green = 0.06, blue = 0.12), scoreBoxBackground = Color(red = 0.18, green = 0.20, blue = 0.32), scoreBoxLabel = Color(red = 0.70, green = 0.75, blue = 0.92), scoreBoxValue = Color.white, lowTileForeground = Color.white, highTileForeground = Color.white, tileBeyondColor = Color(red = 1.00, green = 0.97, blue = 0.80), tile2 = Color(red = 0.28, green = 0.30, blue = 0.45), tile4 = Color(red = 0.34, green = 0.38, blue = 0.62), tile8 = Color(red = 0.30, green = 0.50, blue = 0.85), tile16 = Color(red = 0.28, green = 0.58, blue = 0.92), tile32 = Color(red = 0.50, green = 0.32, blue = 0.88), tile64 = Color(red = 0.65, green = 0.30, blue = 0.92), tile128 = Color(red = 0.85, green = 0.40, blue = 0.85), tile256 = Color(red = 0.95, green = 0.42, blue = 0.72), tile512 = Color(red = 1.00, green = 0.55, blue = 0.55), tile1024 = Color(red = 1.00, green = 0.68, blue = 0.38), tile2048 = Color(red = 1.00, green = 0.82, blue = 0.25))

        val forest = TwentyFortyEightTheme(id = "forest", isDark = true, background = Color(red = 0.05, green = 0.10, blue = 0.07), boardBackground = Color(red = 0.10, green = 0.18, blue = 0.13), emptyCellBackground = Color(red = 0.18, green = 0.28, blue = 0.20), emptyCellOpacity = 0.55, hudForeground = Color(red = 0.80, green = 0.92, blue = 0.80), hudBackground = Color(red = 0.05, green = 0.10, blue = 0.07), scoreBoxBackground = Color(red = 0.16, green = 0.26, blue = 0.18), scoreBoxLabel = Color(red = 0.70, green = 0.88, blue = 0.70), scoreBoxValue = Color.white, lowTileForeground = Color.white, highTileForeground = Color.white, tileBeyondColor = Color(red = 1.00, green = 0.95, blue = 0.70), tile2 = Color(red = 0.22, green = 0.38, blue = 0.28), tile4 = Color(red = 0.30, green = 0.50, blue = 0.35), tile8 = Color(red = 0.36, green = 0.62, blue = 0.40), tile16 = Color(red = 0.45, green = 0.72, blue = 0.42), tile32 = Color(red = 0.58, green = 0.80, blue = 0.40), tile64 = Color(red = 0.72, green = 0.85, blue = 0.36), tile128 = Color(red = 0.88, green = 0.82, blue = 0.32), tile256 = Color(red = 0.95, green = 0.72, blue = 0.28), tile512 = Color(red = 0.96, green = 0.58, blue = 0.22), tile1024 = Color(red = 0.97, green = 0.45, blue = 0.18), tile2048 = Color(red = 0.98, green = 0.32, blue = 0.14))

        val ember = TwentyFortyEightTheme(id = "ember", isDark = true, background = Color(red = 0.10, green = 0.05, blue = 0.10), boardBackground = Color(red = 0.22, green = 0.10, blue = 0.20), emptyCellBackground = Color(red = 0.32, green = 0.18, blue = 0.28), emptyCellOpacity = 0.55, hudForeground = Color(red = 0.99, green = 0.85, blue = 0.78), hudBackground = Color(red = 0.10, green = 0.05, blue = 0.10), scoreBoxBackground = Color(red = 0.28, green = 0.14, blue = 0.24), scoreBoxLabel = Color(red = 0.99, green = 0.78, blue = 0.72), scoreBoxValue = Color.white, lowTileForeground = Color.white, highTileForeground = Color(red = 0.20, green = 0.08, blue = 0.05), tileBeyondColor = Color(red = 1.00, green = 1.00, blue = 0.90), tile2 = Color(red = 0.42, green = 0.20, blue = 0.36), tile4 = Color(red = 0.58, green = 0.24, blue = 0.40), tile8 = Color(red = 0.75, green = 0.30, blue = 0.40), tile16 = Color(red = 0.88, green = 0.38, blue = 0.35), tile32 = Color(red = 0.95, green = 0.50, blue = 0.30), tile64 = Color(red = 0.98, green = 0.62, blue = 0.25), tile128 = Color(red = 0.99, green = 0.72, blue = 0.25), tile256 = Color(red = 0.99, green = 0.82, blue = 0.32), tile512 = Color(red = 1.00, green = 0.90, blue = 0.45), tile1024 = Color(red = 1.00, green = 0.94, blue = 0.62), tile2048 = Color(red = 1.00, green = 0.97, blue = 0.78))

        val all: Array<TwentyFortyEightTheme> = arrayOf(
            TwentyFortyEightTheme.classic,
            TwentyFortyEightTheme.sakura,
            TwentyFortyEightTheme.lagoon,
            TwentyFortyEightTheme.midnight,
            TwentyFortyEightTheme.forest,
            TwentyFortyEightTheme.ember
        )

        fun theme(forID: String): TwentyFortyEightTheme {
            val id = forID
            for (t in all.sref()) {
                if (t.id == id) {
                    return t
                }
            }
            return TwentyFortyEightTheme.classic
        }
    }
}
