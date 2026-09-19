package skip.foundation

import skip.lib.*

// Copyright 2026 Skip
// SPDX-License-Identifier: MPL-2.0

open class UnitElectricCharge: Dimension {

    constructor(symbol: String, converter: UnitConverter): super(symbol, converter) {
    }

    @androidx.annotation.Keep
    companion object: CompanionClass() {
        override val coulombs = UnitElectricCharge(symbol = "C", converter = UnitConverterLinear(coefficient = 1.0))
        override val megaampereHours = UnitElectricCharge(symbol = "MAh", converter = UnitConverterLinear(coefficient = 3.6e9))
        override val kiloampereHours = UnitElectricCharge(symbol = "kAh", converter = UnitConverterLinear(coefficient = 3.6e6))
        override val ampereHours = UnitElectricCharge(symbol = "Ah", converter = UnitConverterLinear(coefficient = 3600.0))
        override val milliampereHours = UnitElectricCharge(symbol = "mAh", converter = UnitConverterLinear(coefficient = 3.6))
        override val microampereHours = UnitElectricCharge(symbol = "µAh", converter = UnitConverterLinear(coefficient = 0.0036))

        override fun baseUnit(): Dimension = coulombs
    }
    open class CompanionClass: Dimension.CompanionClass() {
        open val coulombs
            get() = UnitElectricCharge.coulombs
        open val megaampereHours
            get() = UnitElectricCharge.megaampereHours
        open val kiloampereHours
            get() = UnitElectricCharge.kiloampereHours
        open val ampereHours
            get() = UnitElectricCharge.ampereHours
        open val milliampereHours
            get() = UnitElectricCharge.milliampereHours
        open val microampereHours
            get() = UnitElectricCharge.microampereHours
        override fun baseUnit(): Dimension = UnitElectricCharge.baseUnit()
    }
}

