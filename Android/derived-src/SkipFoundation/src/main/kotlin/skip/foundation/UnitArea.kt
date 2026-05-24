package skip.foundation

import skip.lib.*

// Copyright 2026 Skip
// SPDX-License-Identifier: MPL-2.0

open class UnitArea: Dimension {

    constructor(symbol: String, converter: UnitConverter): super(symbol, converter) {
    }

    @androidx.annotation.Keep
    companion object: CompanionClass() {
        override val squareMegameters = UnitArea(symbol = "Mm²", converter = UnitConverterLinear(coefficient = 1e12))
        override val squareKilometers = UnitArea(symbol = "km²", converter = UnitConverterLinear(coefficient = 1e6))
        override val squareMeters = UnitArea(symbol = "m²", converter = UnitConverterLinear(coefficient = 1.0))
        override val squareCentimeters = UnitArea(symbol = "cm²", converter = UnitConverterLinear(coefficient = 0.0001))
        override val squareMillimeters = UnitArea(symbol = "mm²", converter = UnitConverterLinear(coefficient = 0.000001))
        override val squareMicrometers = UnitArea(symbol = "µm²", converter = UnitConverterLinear(coefficient = 1e-12))
        override val squareNanometers = UnitArea(symbol = "nm²", converter = UnitConverterLinear(coefficient = 1e-18))
        override val squareInches = UnitArea(symbol = "in²", converter = UnitConverterLinear(coefficient = 0.00064516))
        override val squareFeet = UnitArea(symbol = "ft²", converter = UnitConverterLinear(coefficient = 0.092903))
        override val squareYards = UnitArea(symbol = "yd²", converter = UnitConverterLinear(coefficient = 0.836127))
        override val squareMiles = UnitArea(symbol = "mi²", converter = UnitConverterLinear(coefficient = 2.59e6))
        override val acres = UnitArea(symbol = "ac", converter = UnitConverterLinear(coefficient = 4046.86))
        override val ares = UnitArea(symbol = "a", converter = UnitConverterLinear(coefficient = 100.0))
        override val hectares = UnitArea(symbol = "ha", converter = UnitConverterLinear(coefficient = 10000.0))

        override fun baseUnit(): Dimension = squareMeters
    }
    open class CompanionClass: Dimension.CompanionClass() {
        open val squareMegameters
            get() = UnitArea.squareMegameters
        open val squareKilometers
            get() = UnitArea.squareKilometers
        open val squareMeters
            get() = UnitArea.squareMeters
        open val squareCentimeters
            get() = UnitArea.squareCentimeters
        open val squareMillimeters
            get() = UnitArea.squareMillimeters
        open val squareMicrometers
            get() = UnitArea.squareMicrometers
        open val squareNanometers
            get() = UnitArea.squareNanometers
        open val squareInches
            get() = UnitArea.squareInches
        open val squareFeet
            get() = UnitArea.squareFeet
        open val squareYards
            get() = UnitArea.squareYards
        open val squareMiles
            get() = UnitArea.squareMiles
        open val acres
            get() = UnitArea.acres
        open val ares
            get() = UnitArea.ares
        open val hectares
            get() = UnitArea.hectares
        override fun baseUnit(): Dimension = UnitArea.baseUnit()
    }
}

