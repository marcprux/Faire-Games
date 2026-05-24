package skip.foundation

import skip.lib.*

// Copyright 2026 Skip
// SPDX-License-Identifier: MPL-2.0

open class UnitLength: Dimension {

    constructor(symbol: String, converter: UnitConverter): super(symbol, converter) {
    }

    @androidx.annotation.Keep
    companion object: CompanionClass() {
        override val megameters = UnitLength(symbol = "Mm", converter = UnitConverterLinear(coefficient = 1000000.0))
        override val kilometers = UnitLength(symbol = "km", converter = UnitConverterLinear(coefficient = 1000.0))
        override val hectometers = UnitLength(symbol = "hm", converter = UnitConverterLinear(coefficient = 100.0))
        override val decameters = UnitLength(symbol = "dam", converter = UnitConverterLinear(coefficient = 10.0))
        override val meters = UnitLength(symbol = "m", converter = UnitConverterLinear(coefficient = 1.0))
        override val decimeters = UnitLength(symbol = "dm", converter = UnitConverterLinear(coefficient = 0.1))
        override val centimeters = UnitLength(symbol = "cm", converter = UnitConverterLinear(coefficient = 0.01))
        override val millimeters = UnitLength(symbol = "mm", converter = UnitConverterLinear(coefficient = 0.001))
        override val micrometers = UnitLength(symbol = "µm", converter = UnitConverterLinear(coefficient = 0.000001))
        override val nanometers = UnitLength(symbol = "nm", converter = UnitConverterLinear(coefficient = 1e-9))
        override val picometers = UnitLength(symbol = "pm", converter = UnitConverterLinear(coefficient = 1e-12))
        override val inches = UnitLength(symbol = "in", converter = UnitConverterLinear(coefficient = 0.0254))
        override val feet = UnitLength(symbol = "ft", converter = UnitConverterLinear(coefficient = 0.3048))
        override val yards = UnitLength(symbol = "yd", converter = UnitConverterLinear(coefficient = 0.9144))
        override val miles = UnitLength(symbol = "mi", converter = UnitConverterLinear(coefficient = 1609.344))
        override val scandinavianMiles = UnitLength(symbol = "smi", converter = UnitConverterLinear(coefficient = 10000.0))
        override val lightyears = UnitLength(symbol = "ly", converter = UnitConverterLinear(coefficient = 9.4607304725808e15))
        override val nauticalMiles = UnitLength(symbol = "NM", converter = UnitConverterLinear(coefficient = 1852.0))
        override val fathoms = UnitLength(symbol = "ftm", converter = UnitConverterLinear(coefficient = 1.8288))
        override val furlongs = UnitLength(symbol = "fur", converter = UnitConverterLinear(coefficient = 201.168))
        override val astronomicalUnits = UnitLength(symbol = "ua", converter = UnitConverterLinear(coefficient = 1.495978707e11))
        override val parsecs = UnitLength(symbol = "pc", converter = UnitConverterLinear(coefficient = 3.0856775814913673e16))

        override fun baseUnit(): Dimension = meters
    }
    open class CompanionClass: Dimension.CompanionClass() {
        open val megameters
            get() = UnitLength.megameters
        open val kilometers
            get() = UnitLength.kilometers
        open val hectometers
            get() = UnitLength.hectometers
        open val decameters
            get() = UnitLength.decameters
        open val meters
            get() = UnitLength.meters
        open val decimeters
            get() = UnitLength.decimeters
        open val centimeters
            get() = UnitLength.centimeters
        open val millimeters
            get() = UnitLength.millimeters
        open val micrometers
            get() = UnitLength.micrometers
        open val nanometers
            get() = UnitLength.nanometers
        open val picometers
            get() = UnitLength.picometers
        open val inches
            get() = UnitLength.inches
        open val feet
            get() = UnitLength.feet
        open val yards
            get() = UnitLength.yards
        open val miles
            get() = UnitLength.miles
        open val scandinavianMiles
            get() = UnitLength.scandinavianMiles
        open val lightyears
            get() = UnitLength.lightyears
        open val nauticalMiles
            get() = UnitLength.nauticalMiles
        open val fathoms
            get() = UnitLength.fathoms
        open val furlongs
            get() = UnitLength.furlongs
        open val astronomicalUnits
            get() = UnitLength.astronomicalUnits
        open val parsecs
            get() = UnitLength.parsecs
        override fun baseUnit(): Dimension = UnitLength.baseUnit()
    }
}

