package skip.foundation

import skip.lib.*

// Copyright 2026 Skip
// SPDX-License-Identifier: MPL-2.0

open class UnitAngle: Dimension {

    constructor(symbol: String, converter: UnitConverter): super(symbol, converter) {
    }

    @androidx.annotation.Keep
    companion object: CompanionClass() {
        override val degrees = UnitAngle(symbol = "°", converter = UnitConverterLinear(coefficient = 1.0))
        override val arcMinutes = UnitAngle(symbol = "ʹ", converter = UnitConverterLinear(coefficient = 1.0 / 60.0))
        override val arcSeconds = UnitAngle(symbol = "ʺ", converter = UnitConverterLinear(coefficient = 1.0 / 3600.0))
        override val radians = UnitAngle(symbol = "rad", converter = UnitConverterLinear(coefficient = 180.0 / Double.pi))
        override val gradians = UnitAngle(symbol = "grad", converter = UnitConverterLinear(coefficient = 0.9))
        override val revolutions = UnitAngle(symbol = "rev", converter = UnitConverterLinear(coefficient = 360.0))

        override fun baseUnit(): Dimension = degrees
    }
    open class CompanionClass: Dimension.CompanionClass() {
        open val degrees
            get() = UnitAngle.degrees
        open val arcMinutes
            get() = UnitAngle.arcMinutes
        open val arcSeconds
            get() = UnitAngle.arcSeconds
        open val radians
            get() = UnitAngle.radians
        open val gradians
            get() = UnitAngle.gradians
        open val revolutions
            get() = UnitAngle.revolutions
        override fun baseUnit(): Dimension = UnitAngle.baseUnit()
    }
}

