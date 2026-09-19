package skip.foundation

import skip.lib.*

// Copyright 2026 Skip
// SPDX-License-Identifier: MPL-2.0

open class UnitElectricPotentialDifference: Dimension {

    constructor(symbol: String, converter: UnitConverter): super(symbol, converter) {
    }

    @androidx.annotation.Keep
    companion object: CompanionClass() {
        override val megavolts = UnitElectricPotentialDifference(symbol = "MV", converter = UnitConverterLinear(coefficient = 1e6))
        override val kilovolts = UnitElectricPotentialDifference(symbol = "kV", converter = UnitConverterLinear(coefficient = 1000.0))
        override val volts = UnitElectricPotentialDifference(symbol = "V", converter = UnitConverterLinear(coefficient = 1.0))
        override val millivolts = UnitElectricPotentialDifference(symbol = "mV", converter = UnitConverterLinear(coefficient = 0.001))
        override val microvolts = UnitElectricPotentialDifference(symbol = "µV", converter = UnitConverterLinear(coefficient = 0.000001))

        override fun baseUnit(): Dimension = volts
    }
    open class CompanionClass: Dimension.CompanionClass() {
        open val megavolts
            get() = UnitElectricPotentialDifference.megavolts
        open val kilovolts
            get() = UnitElectricPotentialDifference.kilovolts
        open val volts
            get() = UnitElectricPotentialDifference.volts
        open val millivolts
            get() = UnitElectricPotentialDifference.millivolts
        open val microvolts
            get() = UnitElectricPotentialDifference.microvolts
        override fun baseUnit(): Dimension = UnitElectricPotentialDifference.baseUnit()
    }
}

