package skip.foundation

import skip.lib.*

// Copyright 2026 Skip
// SPDX-License-Identifier: MPL-2.0

open class UnitElectricCurrent: Dimension {

    constructor(symbol: String, converter: UnitConverter): super(symbol, converter) {
    }

    @androidx.annotation.Keep
    companion object: CompanionClass() {
        override val megaamperes = UnitElectricCurrent(symbol = "MA", converter = UnitConverterLinear(coefficient = 1e6))
        override val kiloamperes = UnitElectricCurrent(symbol = "kA", converter = UnitConverterLinear(coefficient = 1000.0))
        override val amperes = UnitElectricCurrent(symbol = "A", converter = UnitConverterLinear(coefficient = 1.0))
        override val milliamperes = UnitElectricCurrent(symbol = "mA", converter = UnitConverterLinear(coefficient = 0.001))
        override val microamperes = UnitElectricCurrent(symbol = "µA", converter = UnitConverterLinear(coefficient = 0.000001))

        override fun baseUnit(): Dimension = amperes
    }
    open class CompanionClass: Dimension.CompanionClass() {
        open val megaamperes
            get() = UnitElectricCurrent.megaamperes
        open val kiloamperes
            get() = UnitElectricCurrent.kiloamperes
        open val amperes
            get() = UnitElectricCurrent.amperes
        open val milliamperes
            get() = UnitElectricCurrent.milliamperes
        open val microamperes
            get() = UnitElectricCurrent.microamperes
        override fun baseUnit(): Dimension = UnitElectricCurrent.baseUnit()
    }
}

