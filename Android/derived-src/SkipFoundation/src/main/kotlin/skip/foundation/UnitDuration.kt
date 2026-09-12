package skip.foundation

import skip.lib.*

// Copyright 2026 Skip
// SPDX-License-Identifier: MPL-2.0

open class UnitDuration: Dimension {

    constructor(symbol: String, converter: UnitConverter): super(symbol, converter) {
    }

    @androidx.annotation.Keep
    companion object: CompanionClass() {
        override val hours = UnitDuration(symbol = "hr", converter = UnitConverterLinear(coefficient = 3600.0))
        override val minutes = UnitDuration(symbol = "min", converter = UnitConverterLinear(coefficient = 60.0))
        override val seconds = UnitDuration(symbol = "s", converter = UnitConverterLinear(coefficient = 1.0))
        override val milliseconds = UnitDuration(symbol = "ms", converter = UnitConverterLinear(coefficient = 0.001))
        override val microseconds = UnitDuration(symbol = "µs", converter = UnitConverterLinear(coefficient = 0.000001))
        override val nanoseconds = UnitDuration(symbol = "ns", converter = UnitConverterLinear(coefficient = 1e-9))
        override val picoseconds = UnitDuration(symbol = "ps", converter = UnitConverterLinear(coefficient = 1e-12))

        override fun baseUnit(): Dimension = seconds
    }
    open class CompanionClass: Dimension.CompanionClass() {
        open val hours
            get() = UnitDuration.hours
        open val minutes
            get() = UnitDuration.minutes
        open val seconds
            get() = UnitDuration.seconds
        open val milliseconds
            get() = UnitDuration.milliseconds
        open val microseconds
            get() = UnitDuration.microseconds
        open val nanoseconds
            get() = UnitDuration.nanoseconds
        open val picoseconds
            get() = UnitDuration.picoseconds
        override fun baseUnit(): Dimension = UnitDuration.baseUnit()
    }
}

