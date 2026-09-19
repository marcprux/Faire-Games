package skip.foundation

import skip.lib.*

// Copyright 2026 Skip
// SPDX-License-Identifier: MPL-2.0

open class UnitConcentrationMass: Dimension {

    constructor(symbol: String, converter: UnitConverter): super(symbol, converter) {
    }

    @androidx.annotation.Keep
    companion object: CompanionClass() {
        override val gramsPerLiter = UnitConcentrationMass(symbol = "g/L", converter = UnitConverterLinear(coefficient = 1.0))
        override val milligramsPerDeciliter = UnitConcentrationMass(symbol = "mg/dL", converter = UnitConverterLinear(coefficient = 0.01))

        override fun millimolesPerLiter(withGramsPerMole: Double): UnitConcentrationMass {
            val gramsPerMole = withGramsPerMole
            return UnitConcentrationMass(symbol = "mmol/L", converter = UnitConverterLinear(coefficient = gramsPerMole / 1000.0))
        }

        override fun baseUnit(): Dimension = gramsPerLiter
    }
    open class CompanionClass: Dimension.CompanionClass() {
        open val gramsPerLiter
            get() = UnitConcentrationMass.gramsPerLiter
        open val milligramsPerDeciliter
            get() = UnitConcentrationMass.milligramsPerDeciliter
        open fun millimolesPerLiter(withGramsPerMole: Double): UnitConcentrationMass = UnitConcentrationMass.millimolesPerLiter(withGramsPerMole = withGramsPerMole)
        override fun baseUnit(): Dimension = UnitConcentrationMass.baseUnit()
    }
}

