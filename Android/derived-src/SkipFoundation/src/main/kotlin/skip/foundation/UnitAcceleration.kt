package skip.foundation

import skip.lib.*

// Copyright 2026 Skip
// SPDX-License-Identifier: MPL-2.0

open class UnitAcceleration: Dimension {

    constructor(symbol: String, converter: UnitConverter): super(symbol, converter) {
    }

    @androidx.annotation.Keep
    companion object: CompanionClass() {
        override val metersPerSecondSquared = UnitAcceleration(symbol = "m/s²", converter = UnitConverterLinear(coefficient = 1.0))
        override val gravity = UnitAcceleration(symbol = "g", converter = UnitConverterLinear(coefficient = 9.81))

        override fun baseUnit(): Dimension = metersPerSecondSquared
    }
    open class CompanionClass: Dimension.CompanionClass() {
        open val metersPerSecondSquared
            get() = UnitAcceleration.metersPerSecondSquared
        open val gravity
            get() = UnitAcceleration.gravity
        override fun baseUnit(): Dimension = UnitAcceleration.baseUnit()
    }
}

