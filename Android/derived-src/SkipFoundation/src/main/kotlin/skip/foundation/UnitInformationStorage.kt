package skip.foundation

import skip.lib.*

// Copyright 2026 Skip
// SPDX-License-Identifier: MPL-2.0

open class UnitInformationStorage: Dimension {

    constructor(symbol: String, converter: UnitConverter): super(symbol, converter) {
    }

    @androidx.annotation.Keep
    companion object: CompanionClass() {
        // Base unit
        override val bytes = UnitInformationStorage(symbol = "B", converter = UnitConverterLinear(coefficient = 1.0))

        // Sub-byte
        override val bits = UnitInformationStorage(symbol = "bit", converter = UnitConverterLinear(coefficient = 0.125))
        override val nibbles = UnitInformationStorage(symbol = "nibble", converter = UnitConverterLinear(coefficient = 0.5))

        // Decimal bytes
        override val yottabytes = UnitInformationStorage(symbol = "YB", converter = UnitConverterLinear(coefficient = 1e24))
        override val zettabytes = UnitInformationStorage(symbol = "ZB", converter = UnitConverterLinear(coefficient = 1e21))
        override val exabytes = UnitInformationStorage(symbol = "EB", converter = UnitConverterLinear(coefficient = 1e18))
        override val petabytes = UnitInformationStorage(symbol = "PB", converter = UnitConverterLinear(coefficient = 1e15))
        override val terabytes = UnitInformationStorage(symbol = "TB", converter = UnitConverterLinear(coefficient = 1e12))
        override val gigabytes = UnitInformationStorage(symbol = "GB", converter = UnitConverterLinear(coefficient = 1e9))
        override val megabytes = UnitInformationStorage(symbol = "MB", converter = UnitConverterLinear(coefficient = 1e6))
        override val kilobytes = UnitInformationStorage(symbol = "kB", converter = UnitConverterLinear(coefficient = 1000.0))

        // Decimal bits
        override val yottabits = UnitInformationStorage(symbol = "Yb", converter = UnitConverterLinear(coefficient = 1.25e23))
        override val zettabits = UnitInformationStorage(symbol = "Zb", converter = UnitConverterLinear(coefficient = 1.25e20))
        override val exabits = UnitInformationStorage(symbol = "Eb", converter = UnitConverterLinear(coefficient = 1.25e17))
        override val petabits = UnitInformationStorage(symbol = "Pb", converter = UnitConverterLinear(coefficient = 1.25e14))
        override val terabits = UnitInformationStorage(symbol = "Tb", converter = UnitConverterLinear(coefficient = 1.25e11))
        override val gigabits = UnitInformationStorage(symbol = "Gb", converter = UnitConverterLinear(coefficient = 1.25e8))
        override val megabits = UnitInformationStorage(symbol = "Mb", converter = UnitConverterLinear(coefficient = 125000.0))
        override val kilobits = UnitInformationStorage(symbol = "kb", converter = UnitConverterLinear(coefficient = 125.0))

        // Binary bytes (1024-based)
        override val yobibytes = UnitInformationStorage(symbol = "YiB", converter = UnitConverterLinear(coefficient = 1208925819614629174706176.0))
        override val zebibytes = UnitInformationStorage(symbol = "ZiB", converter = UnitConverterLinear(coefficient = 1180591620717411303424.0))
        override val exbibytes = UnitInformationStorage(symbol = "EiB", converter = UnitConverterLinear(coefficient = 1152921504606846976.0))
        override val pebibytes = UnitInformationStorage(symbol = "PiB", converter = UnitConverterLinear(coefficient = 1125899906842624.0))
        override val tebibytes = UnitInformationStorage(symbol = "TiB", converter = UnitConverterLinear(coefficient = 1099511627776.0))
        override val gibibytes = UnitInformationStorage(symbol = "GiB", converter = UnitConverterLinear(coefficient = 1073741824.0))
        override val mebibytes = UnitInformationStorage(symbol = "MiB", converter = UnitConverterLinear(coefficient = 1048576.0))
        override val kibibytes = UnitInformationStorage(symbol = "KiB", converter = UnitConverterLinear(coefficient = 1024.0))

        // Binary bits (1024-based)
        override val yobibits = UnitInformationStorage(symbol = "Yib", converter = UnitConverterLinear(coefficient = 151115727451828646838272.0))
        override val zebibits = UnitInformationStorage(symbol = "Zib", converter = UnitConverterLinear(coefficient = 147573952589676412928.0))
        override val exbibits = UnitInformationStorage(symbol = "Eib", converter = UnitConverterLinear(coefficient = 144115188075855872.0))
        override val pebibits = UnitInformationStorage(symbol = "Pib", converter = UnitConverterLinear(coefficient = 140737488355328.0))
        override val tebibits = UnitInformationStorage(symbol = "Tib", converter = UnitConverterLinear(coefficient = 137438953472.0))
        override val gibibits = UnitInformationStorage(symbol = "Gib", converter = UnitConverterLinear(coefficient = 134217728.0))
        override val mebibits = UnitInformationStorage(symbol = "Mib", converter = UnitConverterLinear(coefficient = 131072.0))
        override val kibibits = UnitInformationStorage(symbol = "Kib", converter = UnitConverterLinear(coefficient = 128.0))

        override fun baseUnit(): Dimension = bytes
    }
    open class CompanionClass: Dimension.CompanionClass() {
        open val bytes
            get() = UnitInformationStorage.bytes
        open val bits
            get() = UnitInformationStorage.bits
        open val nibbles
            get() = UnitInformationStorage.nibbles
        open val yottabytes
            get() = UnitInformationStorage.yottabytes
        open val zettabytes
            get() = UnitInformationStorage.zettabytes
        open val exabytes
            get() = UnitInformationStorage.exabytes
        open val petabytes
            get() = UnitInformationStorage.petabytes
        open val terabytes
            get() = UnitInformationStorage.terabytes
        open val gigabytes
            get() = UnitInformationStorage.gigabytes
        open val megabytes
            get() = UnitInformationStorage.megabytes
        open val kilobytes
            get() = UnitInformationStorage.kilobytes
        open val yottabits
            get() = UnitInformationStorage.yottabits
        open val zettabits
            get() = UnitInformationStorage.zettabits
        open val exabits
            get() = UnitInformationStorage.exabits
        open val petabits
            get() = UnitInformationStorage.petabits
        open val terabits
            get() = UnitInformationStorage.terabits
        open val gigabits
            get() = UnitInformationStorage.gigabits
        open val megabits
            get() = UnitInformationStorage.megabits
        open val kilobits
            get() = UnitInformationStorage.kilobits
        open val yobibytes
            get() = UnitInformationStorage.yobibytes
        open val zebibytes
            get() = UnitInformationStorage.zebibytes
        open val exbibytes
            get() = UnitInformationStorage.exbibytes
        open val pebibytes
            get() = UnitInformationStorage.pebibytes
        open val tebibytes
            get() = UnitInformationStorage.tebibytes
        open val gibibytes
            get() = UnitInformationStorage.gibibytes
        open val mebibytes
            get() = UnitInformationStorage.mebibytes
        open val kibibytes
            get() = UnitInformationStorage.kibibytes
        open val yobibits
            get() = UnitInformationStorage.yobibits
        open val zebibits
            get() = UnitInformationStorage.zebibits
        open val exbibits
            get() = UnitInformationStorage.exbibits
        open val pebibits
            get() = UnitInformationStorage.pebibits
        open val tebibits
            get() = UnitInformationStorage.tebibits
        open val gibibits
            get() = UnitInformationStorage.gibibits
        open val mebibits
            get() = UnitInformationStorage.mebibits
        open val kibibits
            get() = UnitInformationStorage.kibibits
        override fun baseUnit(): Dimension = UnitInformationStorage.baseUnit()
    }
}

