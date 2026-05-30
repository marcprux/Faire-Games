package skip.foundation

import skip.lib.*

// Copyright 2026 Skip
// SPDX-License-Identifier: MPL-2.0

open class UnitFrequency: Dimension {

    constructor(symbol: String, converter: UnitConverter): super(symbol, converter) {
    }

    @androidx.annotation.Keep
    companion object: CompanionClass() {
        override val terahertz = UnitFrequency(symbol = "THz", converter = UnitConverterLinear(coefficient = 1e12))
        override val gigahertz = UnitFrequency(symbol = "GHz", converter = UnitConverterLinear(coefficient = 1e9))
        override val megahertz = UnitFrequency(symbol = "MHz", converter = UnitConverterLinear(coefficient = 1e6))
        override val kilohertz = UnitFrequency(symbol = "kHz", converter = UnitConverterLinear(coefficient = 1000.0))
        override val hertz = UnitFrequency(symbol = "Hz", converter = UnitConverterLinear(coefficient = 1.0))
        override val millihertz = UnitFrequency(symbol = "mHz", converter = UnitConverterLinear(coefficient = 0.001))
        override val microhertz = UnitFrequency(symbol = "µHz", converter = UnitConverterLinear(coefficient = 0.000001))
        override val nanohertz = UnitFrequency(symbol = "nHz", converter = UnitConverterLinear(coefficient = 1e-9))
        override val framesPerSecond = UnitFrequency(symbol = "fps", converter = UnitConverterLinear(coefficient = 1.0))

        override fun baseUnit(): Dimension = hertz
    }
    open class CompanionClass: Dimension.CompanionClass() {
        open val terahertz
            get() = UnitFrequency.terahertz
        open val gigahertz
            get() = UnitFrequency.gigahertz
        open val megahertz
            get() = UnitFrequency.megahertz
        open val kilohertz
            get() = UnitFrequency.kilohertz
        open val hertz
            get() = UnitFrequency.hertz
        open val millihertz
            get() = UnitFrequency.millihertz
        open val microhertz
            get() = UnitFrequency.microhertz
        open val nanohertz
            get() = UnitFrequency.nanohertz
        open val framesPerSecond
            get() = UnitFrequency.framesPerSecond
        override fun baseUnit(): Dimension = UnitFrequency.baseUnit()
    }
}

