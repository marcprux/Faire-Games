package skip.foundation

import skip.lib.*

// Copyright 2026 Skip
// SPDX-License-Identifier: MPL-2.0

open class UnitTemperature: Dimension {

    constructor(symbol: String, converter: UnitConverter): super(symbol, converter) {
    }

    @androidx.annotation.Keep
    companion object: CompanionClass() {
        override val kelvin = UnitTemperature(symbol = "K", converter = UnitConverterLinear(coefficient = 1.0, constant = 0.0))
        override val celsius = UnitTemperature(symbol = "°C", converter = UnitConverterLinear(coefficient = 1.0, constant = 273.15))
        override val fahrenheit = UnitTemperature(symbol = "°F", converter = UnitConverterLinear(coefficient = 5.0 / 9.0, constant = 255.37222222222428))

        override fun baseUnit(): Dimension = kelvin
    }
    open class CompanionClass: Dimension.CompanionClass() {
        open val kelvin
            get() = UnitTemperature.kelvin
        open val celsius
            get() = UnitTemperature.celsius
        open val fahrenheit
            get() = UnitTemperature.fahrenheit
        override fun baseUnit(): Dimension = UnitTemperature.baseUnit()
    }
}

