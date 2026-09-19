package skip.foundation

import skip.lib.*

// Copyright 2026 Skip
// SPDX-License-Identifier: MPL-2.0

open class UnitSpeed: Dimension {

    constructor(symbol: String, converter: UnitConverter): super(symbol, converter) {
    }

    @androidx.annotation.Keep
    companion object: CompanionClass() {
        override val metersPerSecond = UnitSpeed(symbol = "m/s", converter = UnitConverterLinear(coefficient = 1.0))
        override val kilometersPerHour = UnitSpeed(symbol = "km/h", converter = UnitConverterLinear(coefficient = 0.277778))
        override val milesPerHour = UnitSpeed(symbol = "mph", converter = UnitConverterLinear(coefficient = 0.44704))
        override val knots = UnitSpeed(symbol = "kn", converter = UnitConverterLinear(coefficient = 0.514444))

        override fun baseUnit(): Dimension = metersPerSecond
    }
    open class CompanionClass: Dimension.CompanionClass() {
        open val metersPerSecond
            get() = UnitSpeed.metersPerSecond
        open val kilometersPerHour
            get() = UnitSpeed.kilometersPerHour
        open val milesPerHour
            get() = UnitSpeed.milesPerHour
        open val knots
            get() = UnitSpeed.knots
        override fun baseUnit(): Dimension = UnitSpeed.baseUnit()
    }
}

