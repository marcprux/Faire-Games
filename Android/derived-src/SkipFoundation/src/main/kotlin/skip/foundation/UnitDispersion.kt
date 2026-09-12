package skip.foundation

import skip.lib.*

// Copyright 2026 Skip
// SPDX-License-Identifier: MPL-2.0

open class UnitDispersion: Dimension {

    constructor(symbol: String, converter: UnitConverter): super(symbol, converter) {
    }

    @androidx.annotation.Keep
    companion object: CompanionClass() {
        override val partsPerMillion = UnitDispersion(symbol = "ppm", converter = UnitConverterLinear(coefficient = 1.0))

        override fun baseUnit(): Dimension = partsPerMillion
    }
    open class CompanionClass: Dimension.CompanionClass() {
        open val partsPerMillion
            get() = UnitDispersion.partsPerMillion
        override fun baseUnit(): Dimension = UnitDispersion.baseUnit()
    }
}

