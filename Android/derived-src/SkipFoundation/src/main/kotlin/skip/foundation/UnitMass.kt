package skip.foundation

import skip.lib.*

// Copyright 2026 Skip
// SPDX-License-Identifier: MPL-2.0

open class UnitMass: Dimension {

    constructor(symbol: String, converter: UnitConverter): super(symbol, converter) {
    }

    @androidx.annotation.Keep
    companion object: CompanionClass() {
        override val kilograms = UnitMass(symbol = "kg", converter = UnitConverterLinear(coefficient = 1.0))
        override val grams = UnitMass(symbol = "g", converter = UnitConverterLinear(coefficient = 0.001))
        override val decigrams = UnitMass(symbol = "dg", converter = UnitConverterLinear(coefficient = 0.0001))
        override val centigrams = UnitMass(symbol = "cg", converter = UnitConverterLinear(coefficient = 0.00001))
        override val milligrams = UnitMass(symbol = "mg", converter = UnitConverterLinear(coefficient = 0.000001))
        override val micrograms = UnitMass(symbol = "µg", converter = UnitConverterLinear(coefficient = 1e-9))
        override val nanograms = UnitMass(symbol = "ng", converter = UnitConverterLinear(coefficient = 1e-12))
        override val picograms = UnitMass(symbol = "pg", converter = UnitConverterLinear(coefficient = 1e-15))
        override val ounces = UnitMass(symbol = "oz", converter = UnitConverterLinear(coefficient = 0.0283495))
        override val pounds = UnitMass(symbol = "lb", converter = UnitConverterLinear(coefficient = 0.453592))
        override val stones = UnitMass(symbol = "st", converter = UnitConverterLinear(coefficient = 6.35029))
        override val metricTons = UnitMass(symbol = "t", converter = UnitConverterLinear(coefficient = 1000.0))
        override val shortTons = UnitMass(symbol = "ton", converter = UnitConverterLinear(coefficient = 907.185))
        override val carats = UnitMass(symbol = "ct", converter = UnitConverterLinear(coefficient = 0.0002))
        override val ouncesTroy = UnitMass(symbol = "oz t", converter = UnitConverterLinear(coefficient = 0.0311035))
        override val slugs = UnitMass(symbol = "slug", converter = UnitConverterLinear(coefficient = 14.5939))

        override fun baseUnit(): Dimension = kilograms
    }
    open class CompanionClass: Dimension.CompanionClass() {
        open val kilograms
            get() = UnitMass.kilograms
        open val grams
            get() = UnitMass.grams
        open val decigrams
            get() = UnitMass.decigrams
        open val centigrams
            get() = UnitMass.centigrams
        open val milligrams
            get() = UnitMass.milligrams
        open val micrograms
            get() = UnitMass.micrograms
        open val nanograms
            get() = UnitMass.nanograms
        open val picograms
            get() = UnitMass.picograms
        open val ounces
            get() = UnitMass.ounces
        open val pounds
            get() = UnitMass.pounds
        open val stones
            get() = UnitMass.stones
        open val metricTons
            get() = UnitMass.metricTons
        open val shortTons
            get() = UnitMass.shortTons
        open val carats
            get() = UnitMass.carats
        open val ouncesTroy
            get() = UnitMass.ouncesTroy
        open val slugs
            get() = UnitMass.slugs
        override fun baseUnit(): Dimension = UnitMass.baseUnit()
    }
}

