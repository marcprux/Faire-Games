package skip.foundation

import skip.lib.*

// Copyright 2026 Skip
// SPDX-License-Identifier: MPL-2.0

open class UnitFuelEfficiency: Dimension {

    constructor(symbol: String, converter: UnitConverter): super(symbol, converter) {
    }

    @androidx.annotation.Keep
    companion object: CompanionClass() {
        override val litersPer100Kilometers = UnitFuelEfficiency(symbol = "L/100km", converter = UnitConverterLinear(coefficient = 1.0))
        override val milesPerImperialGallon = UnitFuelEfficiency(symbol = "mpg", converter = UnitConverterReciprocal(reciprocal = 282.481))
        override val milesPerGallon = UnitFuelEfficiency(symbol = "mpg", converter = UnitConverterReciprocal(reciprocal = 235.215))

        override fun baseUnit(): Dimension = litersPer100Kilometers
    }
    open class CompanionClass: Dimension.CompanionClass() {
        open val litersPer100Kilometers
            get() = UnitFuelEfficiency.litersPer100Kilometers
        open val milesPerImperialGallon
            get() = UnitFuelEfficiency.milesPerImperialGallon
        open val milesPerGallon
            get() = UnitFuelEfficiency.milesPerGallon
        override fun baseUnit(): Dimension = UnitFuelEfficiency.baseUnit()
    }
}

