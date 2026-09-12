package skip.foundation

import skip.lib.*

// Copyright 2026 Skip
// SPDX-License-Identifier: MPL-2.0

open class UnitPressure: Dimension {

    constructor(symbol: String, converter: UnitConverter): super(symbol, converter) {
    }

    @androidx.annotation.Keep
    companion object: CompanionClass() {
        override val newtonsPerMetersSquared = UnitPressure(symbol = "N/m²", converter = UnitConverterLinear(coefficient = 1.0))
        override val gigapascals = UnitPressure(symbol = "GPa", converter = UnitConverterLinear(coefficient = 1e9))
        override val megapascals = UnitPressure(symbol = "MPa", converter = UnitConverterLinear(coefficient = 1e6))
        override val kilopascals = UnitPressure(symbol = "kPa", converter = UnitConverterLinear(coefficient = 1000.0))
        override val hectopascals = UnitPressure(symbol = "hPa", converter = UnitConverterLinear(coefficient = 100.0))
        override val inchesOfMercury = UnitPressure(symbol = "inHg", converter = UnitConverterLinear(coefficient = 3386.39))
        override val bars = UnitPressure(symbol = "bar", converter = UnitConverterLinear(coefficient = 100000.0))
        override val millibars = UnitPressure(symbol = "mbar", converter = UnitConverterLinear(coefficient = 100.0))
        override val millimetersOfMercury = UnitPressure(symbol = "mmHg", converter = UnitConverterLinear(coefficient = 133.322))
        override val poundsForcePerSquareInch = UnitPressure(symbol = "psi", converter = UnitConverterLinear(coefficient = 6894.76))

        override fun baseUnit(): Dimension = newtonsPerMetersSquared
    }
    open class CompanionClass: Dimension.CompanionClass() {
        open val newtonsPerMetersSquared
            get() = UnitPressure.newtonsPerMetersSquared
        open val gigapascals
            get() = UnitPressure.gigapascals
        open val megapascals
            get() = UnitPressure.megapascals
        open val kilopascals
            get() = UnitPressure.kilopascals
        open val hectopascals
            get() = UnitPressure.hectopascals
        open val inchesOfMercury
            get() = UnitPressure.inchesOfMercury
        open val bars
            get() = UnitPressure.bars
        open val millibars
            get() = UnitPressure.millibars
        open val millimetersOfMercury
            get() = UnitPressure.millimetersOfMercury
        open val poundsForcePerSquareInch
            get() = UnitPressure.poundsForcePerSquareInch
        override fun baseUnit(): Dimension = UnitPressure.baseUnit()
    }
}

