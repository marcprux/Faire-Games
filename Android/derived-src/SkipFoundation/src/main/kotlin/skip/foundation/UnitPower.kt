package skip.foundation

import skip.lib.*

// Copyright 2026 Skip
// SPDX-License-Identifier: MPL-2.0

open class UnitPower: Dimension {

    constructor(symbol: String, converter: UnitConverter): super(symbol, converter) {
    }

    @androidx.annotation.Keep
    companion object: CompanionClass() {
        override val terawatts = UnitPower(symbol = "TW", converter = UnitConverterLinear(coefficient = 1e12))
        override val gigawatts = UnitPower(symbol = "GW", converter = UnitConverterLinear(coefficient = 1e9))
        override val megawatts = UnitPower(symbol = "MW", converter = UnitConverterLinear(coefficient = 1e6))
        override val kilowatts = UnitPower(symbol = "kW", converter = UnitConverterLinear(coefficient = 1000.0))
        override val watts = UnitPower(symbol = "W", converter = UnitConverterLinear(coefficient = 1.0))
        override val milliwatts = UnitPower(symbol = "mW", converter = UnitConverterLinear(coefficient = 0.001))
        override val microwatts = UnitPower(symbol = "µW", converter = UnitConverterLinear(coefficient = 0.000001))
        override val nanowatts = UnitPower(symbol = "nW", converter = UnitConverterLinear(coefficient = 1e-9))
        override val picowatts = UnitPower(symbol = "pW", converter = UnitConverterLinear(coefficient = 1e-12))
        override val femtowatts = UnitPower(symbol = "fW", converter = UnitConverterLinear(coefficient = 1e-15))
        override val horsepower = UnitPower(symbol = "hp", converter = UnitConverterLinear(coefficient = 745.7))

        override fun baseUnit(): Dimension = watts
    }
    open class CompanionClass: Dimension.CompanionClass() {
        open val terawatts
            get() = UnitPower.terawatts
        open val gigawatts
            get() = UnitPower.gigawatts
        open val megawatts
            get() = UnitPower.megawatts
        open val kilowatts
            get() = UnitPower.kilowatts
        open val watts
            get() = UnitPower.watts
        open val milliwatts
            get() = UnitPower.milliwatts
        open val microwatts
            get() = UnitPower.microwatts
        open val nanowatts
            get() = UnitPower.nanowatts
        open val picowatts
            get() = UnitPower.picowatts
        open val femtowatts
            get() = UnitPower.femtowatts
        open val horsepower
            get() = UnitPower.horsepower
        override fun baseUnit(): Dimension = UnitPower.baseUnit()
    }
}

