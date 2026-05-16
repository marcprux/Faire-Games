package skip.foundation

import skip.lib.*

// Copyright 2026 Skip
// SPDX-License-Identifier: MPL-2.0

typealias NSMeasurement = Measurement<*>

@Suppress("MUST_BE_INITIALIZED")
class Measurement<UnitType>: Comparable<Measurement<UnitType>>, MutableStruct where UnitType: FoundationUnit {
    var value: Double
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    var unit: UnitType
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }

    constructor(value: Double, unit: UnitType) {
        this.value = value
        this.unit = unit
    }

    // MARK: Conversion

    fun converted(to: UnitType): Measurement<UnitType> {
        val otherUnit = to
        if (unit === otherUnit || unit == otherUnit) {
            return Measurement(value = value, unit = otherUnit)
        }
        val fromDim_0 = unit as? Dimension
        if (fromDim_0 == null) {
            return Measurement(value = value, unit = otherUnit)
        }
        val toDim_0 = otherUnit as? Dimension
        if (toDim_0 == null) {
            return Measurement(value = value, unit = otherUnit)
        }
        val baseValue = fromDim_0.converter.baseUnitValue(fromValue = value)
        val result = toDim_0.converter.value(fromBaseUnitValue = baseValue)
        return Measurement(value = result, unit = otherUnit)
    }

    // MARK: Equatable (exact comparison, matching Apple Foundation)

    override fun equals(other: Any?): Boolean {
        if (other !is Measurement<*>) {
            return false
        }
        val lhs = this
        val rhs = other
        if (lhs.unit == rhs.unit) {
            return lhs.value == rhs.value
        }
        val lhsDim_0 = lhs.unit as? Dimension
        if (lhsDim_0 == null) {
            return false
        }
        val rhsDim_0 = rhs.unit as? Dimension
        if (rhsDim_0 == null) {
            return false
        }
        val lhsBase = lhsDim_0.converter.baseUnitValue(fromValue = lhs.value)
        val rhsBase = rhsDim_0.converter.baseUnitValue(fromValue = rhs.value)
        return lhsBase == rhsBase
    }

    // MARK: Comparable

    override fun compareTo(other: Measurement<UnitType>): Int {
        if (this == other) return 0
        fun islessthan(lhs: Measurement<UnitType>, rhs: Measurement<UnitType>): Boolean {
            val lhsDim_1 = lhs.unit as? Dimension
            if (lhsDim_1 == null) {
                return lhs.value < rhs.value
            }
            val rhsDim_1 = rhs.unit as? Dimension
            if (rhsDim_1 == null) {
                return lhs.value < rhs.value
            }
            return lhsDim_1.converter.baseUnitValue(fromValue = lhs.value) < rhsDim_1.converter.baseUnitValue(fromValue = rhs.value)
        }
        return if (islessthan(this, other)) -1 else 1
    }

    // MARK: Hashable

    override fun hashCode(): Int {
        var hasher = Hasher()
        hash(into = InOut<Hasher>({ hasher }, { hasher = it }))
        return hasher.finalize()
    }
    fun hash(into: InOut<Hasher>) {
        val hasher = into
        val matchtarget_0 = unit as? Dimension
        if (matchtarget_0 != null) {
            val dim = matchtarget_0
            hasher.value.combine(dim.converter.baseUnitValue(fromValue = value))
        } else {
            hasher.value.combine(value)
            hasher.value.combine(unit)
        }
    }

    // MARK: CustomStringConvertible

    val description: String
        get() = "${value} ${unit.symbol}"

    // MARK: Arithmetic (named methods — Skip does not support custom operators)

    fun adding(other: Measurement<UnitType>): Measurement<UnitType> {
        if (unit == other.unit) {
            return Measurement(value = value + other.value, unit = unit)
        }
        val otherConverted = other.converted(to = unit)
        return Measurement(value = value + otherConverted.value, unit = unit)
    }

    fun subtracting(other: Measurement<UnitType>): Measurement<UnitType> {
        if (unit == other.unit) {
            return Measurement(value = value - other.value, unit = unit)
        }
        val otherConverted = other.converted(to = unit)
        return Measurement(value = value - otherConverted.value, unit = unit)
    }

    fun negate() {
        willmutate()
        try {
            value = -value
        } finally {
            didmutate()
        }
    }

    fun multiplied(by: Double): Measurement<UnitType> {
        val scalar = by
        return Measurement(value = value * scalar, unit = unit)
    }

    fun divided(by: Double): Measurement<UnitType> {
        val scalar = by
        return Measurement(value = value / scalar, unit = unit)
    }

    // NOTE: Codable is not conformable here — Kotlin type erasure prevents the
    // companion object from referencing the generic UnitType. All Codable
    // encode/decode happens on the native Swift side (Foundation /
    // swift-corelibs-foundation).

    private constructor(copy: MutableStruct) {
        @Suppress("NAME_SHADOWING", "UNCHECKED_CAST") val copy = copy as Measurement<UnitType>
        this.value = copy.value
        this.unit = copy.unit
    }

    override var supdate: ((Any) -> Unit)? = null
    override var smutatingcount = 0
    override fun scopy(): MutableStruct = Measurement<UnitType>(this as MutableStruct)

    override fun toString(): String = description

    @androidx.annotation.Keep
    companion object {
    }
}

