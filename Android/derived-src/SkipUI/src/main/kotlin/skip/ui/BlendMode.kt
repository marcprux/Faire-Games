package skip.ui

import skip.lib.*
import skip.lib.Array

// Copyright 2023–2026 Skip
// SPDX-License-Identifier: MPL-2.0

@androidx.annotation.Keep
enum class BlendMode(override val rawValue: Int, @Suppress("UNUSED_PARAMETER") unusedp: Nothing? = null): CaseIterable, RawRepresentable<Int> {
    normal(0),
    multiply(1),
    screen(2),
    overlay(3),
    darken(4),
    lighten(5),
    colorDodge(6),
    colorBurn(7),
    softLight(8),
    hardLight(9),
    difference(10),
    exclusion(11),
    hue(12),
    saturation(13),
    color(14),
    luminosity(15),
    sourceAtop(16),
    destinationOver(17),
    destinationOut(18),
    plusDarker(19),
    plusLighter(20);

    internal fun asComposeBlendMode(): androidx.compose.ui.graphics.BlendMode {
        when (this) {
            BlendMode.normal -> return androidx.compose.ui.graphics.BlendMode.SrcOver.sref()
            BlendMode.multiply -> return androidx.compose.ui.graphics.BlendMode.Multiply.sref()
            BlendMode.screen -> return androidx.compose.ui.graphics.BlendMode.Screen.sref()
            BlendMode.overlay -> return androidx.compose.ui.graphics.BlendMode.Overlay.sref()
            BlendMode.darken -> return androidx.compose.ui.graphics.BlendMode.Darken.sref()
            BlendMode.lighten -> return androidx.compose.ui.graphics.BlendMode.Lighten.sref()
            BlendMode.colorDodge -> return androidx.compose.ui.graphics.BlendMode.ColorDodge.sref()
            BlendMode.colorBurn -> return androidx.compose.ui.graphics.BlendMode.ColorBurn.sref()
            BlendMode.softLight -> return androidx.compose.ui.graphics.BlendMode.Softlight.sref()
            BlendMode.hardLight -> return androidx.compose.ui.graphics.BlendMode.Hardlight.sref()
            BlendMode.difference -> return androidx.compose.ui.graphics.BlendMode.Difference.sref()
            BlendMode.exclusion -> return androidx.compose.ui.graphics.BlendMode.Exclusion.sref()
            BlendMode.hue -> return androidx.compose.ui.graphics.BlendMode.Hue.sref()
            BlendMode.saturation -> return androidx.compose.ui.graphics.BlendMode.Saturation.sref()
            BlendMode.color -> return androidx.compose.ui.graphics.BlendMode.Color.sref()
            BlendMode.luminosity -> return androidx.compose.ui.graphics.BlendMode.Luminosity.sref()
            BlendMode.sourceAtop -> return androidx.compose.ui.graphics.BlendMode.SrcAtop.sref()
            BlendMode.destinationOver -> return androidx.compose.ui.graphics.BlendMode.DstOver.sref()
            BlendMode.destinationOut -> return androidx.compose.ui.graphics.BlendMode.DstOut.sref()
            BlendMode.plusDarker, BlendMode.plusLighter -> return androidx.compose.ui.graphics.BlendMode.Plus.sref()
        }
    }

    @androidx.annotation.Keep
    companion object: CaseIterableCompanion<BlendMode> {
        fun init(rawValue: Int): BlendMode? {
            return when (rawValue) {
                0 -> BlendMode.normal
                1 -> BlendMode.multiply
                2 -> BlendMode.screen
                3 -> BlendMode.overlay
                4 -> BlendMode.darken
                5 -> BlendMode.lighten
                6 -> BlendMode.colorDodge
                7 -> BlendMode.colorBurn
                8 -> BlendMode.softLight
                9 -> BlendMode.hardLight
                10 -> BlendMode.difference
                11 -> BlendMode.exclusion
                12 -> BlendMode.hue
                13 -> BlendMode.saturation
                14 -> BlendMode.color
                15 -> BlendMode.luminosity
                16 -> BlendMode.sourceAtop
                17 -> BlendMode.destinationOver
                18 -> BlendMode.destinationOut
                19 -> BlendMode.plusDarker
                20 -> BlendMode.plusLighter
                else -> null
            }
        }

        override val allCases: Array<BlendMode>
            get() = arrayOf(normal, multiply, screen, overlay, darken, lighten, colorDodge, colorBurn, softLight, hardLight, difference, exclusion, hue, saturation, color, luminosity, sourceAtop, destinationOver, destinationOut, plusDarker, plusLighter)
    }
}

fun BlendMode(rawValue: Int): BlendMode? = BlendMode.init(rawValue = rawValue)


