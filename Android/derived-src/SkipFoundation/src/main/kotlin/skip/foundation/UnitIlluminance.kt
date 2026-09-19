package skip.foundation

import skip.lib.*

// Copyright 2026 Skip
// SPDX-License-Identifier: MPL-2.0

open class UnitIlluminance: Dimension {

    constructor(symbol: String, converter: UnitConverter): super(symbol, converter) {
    }

    @androidx.annotation.Keep
    companion object: CompanionClass() {
        override val lux = UnitIlluminance(symbol = "lx", converter = UnitConverterLinear(coefficient = 1.0))

        override fun baseUnit(): Dimension = lux
    }
    open class CompanionClass: Dimension.CompanionClass() {
        open val lux
            get() = UnitIlluminance.lux
        override fun baseUnit(): Dimension = UnitIlluminance.baseUnit()
    }
}

