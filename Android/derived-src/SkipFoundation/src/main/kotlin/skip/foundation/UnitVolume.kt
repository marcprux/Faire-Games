package skip.foundation

import skip.lib.*

// Copyright 2026 Skip
// SPDX-License-Identifier: MPL-2.0

open class UnitVolume: Dimension {

    constructor(symbol: String, converter: UnitConverter): super(symbol, converter) {
    }

    @androidx.annotation.Keep
    companion object: CompanionClass() {
        // Metric
        override val megaliters = UnitVolume(symbol = "ML", converter = UnitConverterLinear(coefficient = 1e6))
        override val kiloliters = UnitVolume(symbol = "kL", converter = UnitConverterLinear(coefficient = 1000.0))
        override val liters = UnitVolume(symbol = "L", converter = UnitConverterLinear(coefficient = 1.0))
        override val deciliters = UnitVolume(symbol = "dL", converter = UnitConverterLinear(coefficient = 0.1))
        override val centiliters = UnitVolume(symbol = "cL", converter = UnitConverterLinear(coefficient = 0.01))
        override val milliliters = UnitVolume(symbol = "mL", converter = UnitConverterLinear(coefficient = 0.001))

        // Cubic metric
        override val cubicKilometers = UnitVolume(symbol = "km³", converter = UnitConverterLinear(coefficient = 1e12))
        override val cubicMeters = UnitVolume(symbol = "m³", converter = UnitConverterLinear(coefficient = 1000.0))
        override val cubicDecimeters = UnitVolume(symbol = "dm³", converter = UnitConverterLinear(coefficient = 1.0))
        override val cubicCentimeters = UnitVolume(symbol = "cm³", converter = UnitConverterLinear(coefficient = 0.001))
        override val cubicMillimeters = UnitVolume(symbol = "mm³", converter = UnitConverterLinear(coefficient = 0.000001))

        // Cubic imperial
        override val cubicInches = UnitVolume(symbol = "in³", converter = UnitConverterLinear(coefficient = 0.0163871))
        override val cubicFeet = UnitVolume(symbol = "ft³", converter = UnitConverterLinear(coefficient = 28.3168))
        override val cubicYards = UnitVolume(symbol = "yd³", converter = UnitConverterLinear(coefficient = 764.555))
        override val cubicMiles = UnitVolume(symbol = "mi³", converter = UnitConverterLinear(coefficient = 4.168e12))

        // US customary
        override val acreFeet = UnitVolume(symbol = "af", converter = UnitConverterLinear(coefficient = 1.233e6))
        override val bushels = UnitVolume(symbol = "bsh", converter = UnitConverterLinear(coefficient = 35.2391))
        override val teaspoons = UnitVolume(symbol = "tsp", converter = UnitConverterLinear(coefficient = 0.00492892))
        override val tablespoons = UnitVolume(symbol = "tbsp", converter = UnitConverterLinear(coefficient = 0.0147868))
        override val fluidOunces = UnitVolume(symbol = "fl oz", converter = UnitConverterLinear(coefficient = 0.0295735))
        override val cups = UnitVolume(symbol = "cup", converter = UnitConverterLinear(coefficient = 0.24))
        override val pints = UnitVolume(symbol = "pt", converter = UnitConverterLinear(coefficient = 0.473176))
        override val quarts = UnitVolume(symbol = "qt", converter = UnitConverterLinear(coefficient = 0.946353))
        override val gallons = UnitVolume(symbol = "gal", converter = UnitConverterLinear(coefficient = 3.78541))

        // Imperial
        override val imperialTeaspoons = UnitVolume(symbol = "tsp", converter = UnitConverterLinear(coefficient = 0.00591939))
        override val imperialTablespoons = UnitVolume(symbol = "tbsp", converter = UnitConverterLinear(coefficient = 0.0177582))
        override val imperialFluidOunces = UnitVolume(symbol = "fl oz", converter = UnitConverterLinear(coefficient = 0.0284131))
        override val imperialPints = UnitVolume(symbol = "pt", converter = UnitConverterLinear(coefficient = 0.568261))
        override val imperialQuarts = UnitVolume(symbol = "qt", converter = UnitConverterLinear(coefficient = 1.13652))
        override val imperialGallons = UnitVolume(symbol = "gal", converter = UnitConverterLinear(coefficient = 4.54609))
        override val metricCups = UnitVolume(symbol = "metric cup", converter = UnitConverterLinear(coefficient = 0.25))

        override fun baseUnit(): Dimension = liters
    }
    open class CompanionClass: Dimension.CompanionClass() {
        open val megaliters
            get() = UnitVolume.megaliters
        open val kiloliters
            get() = UnitVolume.kiloliters
        open val liters
            get() = UnitVolume.liters
        open val deciliters
            get() = UnitVolume.deciliters
        open val centiliters
            get() = UnitVolume.centiliters
        open val milliliters
            get() = UnitVolume.milliliters
        open val cubicKilometers
            get() = UnitVolume.cubicKilometers
        open val cubicMeters
            get() = UnitVolume.cubicMeters
        open val cubicDecimeters
            get() = UnitVolume.cubicDecimeters
        open val cubicCentimeters
            get() = UnitVolume.cubicCentimeters
        open val cubicMillimeters
            get() = UnitVolume.cubicMillimeters
        open val cubicInches
            get() = UnitVolume.cubicInches
        open val cubicFeet
            get() = UnitVolume.cubicFeet
        open val cubicYards
            get() = UnitVolume.cubicYards
        open val cubicMiles
            get() = UnitVolume.cubicMiles
        open val acreFeet
            get() = UnitVolume.acreFeet
        open val bushels
            get() = UnitVolume.bushels
        open val teaspoons
            get() = UnitVolume.teaspoons
        open val tablespoons
            get() = UnitVolume.tablespoons
        open val fluidOunces
            get() = UnitVolume.fluidOunces
        open val cups
            get() = UnitVolume.cups
        open val pints
            get() = UnitVolume.pints
        open val quarts
            get() = UnitVolume.quarts
        open val gallons
            get() = UnitVolume.gallons
        open val imperialTeaspoons
            get() = UnitVolume.imperialTeaspoons
        open val imperialTablespoons
            get() = UnitVolume.imperialTablespoons
        open val imperialFluidOunces
            get() = UnitVolume.imperialFluidOunces
        open val imperialPints
            get() = UnitVolume.imperialPints
        open val imperialQuarts
            get() = UnitVolume.imperialQuarts
        open val imperialGallons
            get() = UnitVolume.imperialGallons
        open val metricCups
            get() = UnitVolume.metricCups
        override fun baseUnit(): Dimension = UnitVolume.baseUnit()
    }
}

