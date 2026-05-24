package skip.foundation

import skip.lib.*

// Copyright 2026 Skip
// SPDX-License-Identifier: MPL-2.0

open class UnitElectricResistance: Dimension {

    constructor(symbol: String, converter: UnitConverter): super(symbol, converter) {
    }

    @androidx.annotation.Keep
    companion object: CompanionClass() {
        override val megaohms = UnitElectricResistance(symbol = "MΩ", converter = UnitConverterLinear(coefficient = 1e6))
        override val kiloohms = UnitElectricResistance(symbol = "kΩ", converter = UnitConverterLinear(coefficient = 1000.0))
        override val ohms = UnitElectricResistance(symbol = "Ω", converter = UnitConverterLinear(coefficient = 1.0))
        override val milliohms = UnitElectricResistance(symbol = "mΩ", converter = UnitConverterLinear(coefficient = 0.001))
        override val microohms = UnitElectricResistance(symbol = "µΩ", converter = UnitConverterLinear(coefficient = 0.000001))

        override fun baseUnit(): Dimension = ohms
    }
    open class CompanionClass: Dimension.CompanionClass() {
        open val megaohms
            get() = UnitElectricResistance.megaohms
        open val kiloohms
            get() = UnitElectricResistance.kiloohms
        open val ohms
            get() = UnitElectricResistance.ohms
        open val milliohms
            get() = UnitElectricResistance.milliohms
        open val microohms
            get() = UnitElectricResistance.microohms
        override fun baseUnit(): Dimension = UnitElectricResistance.baseUnit()
    }
}

