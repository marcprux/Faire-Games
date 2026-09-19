package skip.foundation

import skip.lib.*

// Copyright 2026 Skip
// SPDX-License-Identifier: MPL-2.0

open class UnitEnergy: Dimension {

    constructor(symbol: String, converter: UnitConverter): super(symbol, converter) {
    }

    @androidx.annotation.Keep
    companion object: CompanionClass() {
        override val kilojoules = UnitEnergy(symbol = "kJ", converter = UnitConverterLinear(coefficient = 1000.0))
        override val joules = UnitEnergy(symbol = "J", converter = UnitConverterLinear(coefficient = 1.0))
        override val kilocalories = UnitEnergy(symbol = "kCal", converter = UnitConverterLinear(coefficient = 4184.0))
        override val calories = UnitEnergy(symbol = "cal", converter = UnitConverterLinear(coefficient = 4.184))
        override val kilowattHours = UnitEnergy(symbol = "kWh", converter = UnitConverterLinear(coefficient = 3600000.0))

        override fun baseUnit(): Dimension = joules
    }
    open class CompanionClass: Dimension.CompanionClass() {
        open val kilojoules
            get() = UnitEnergy.kilojoules
        open val joules
            get() = UnitEnergy.joules
        open val kilocalories
            get() = UnitEnergy.kilocalories
        open val calories
            get() = UnitEnergy.calories
        open val kilowattHours
            get() = UnitEnergy.kilowattHours
        override fun baseUnit(): Dimension = UnitEnergy.baseUnit()
    }
}

