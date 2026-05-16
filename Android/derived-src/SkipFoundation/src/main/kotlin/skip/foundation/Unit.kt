package skip.foundation

import skip.lib.*

// Copyright 2026 Skip
// SPDX-License-Identifier: MPL-2.0

// "Unit" is a reserved name in Kotlin (kotlin.Unit). On iOS, Foundation.Unit
// is used directly. In Skip/Kotlin, FoundationUnit is the base class; consumers
// use Dimension subclasses (UnitMass, UnitLength, etc.) — not Unit directly.
typealias NSUnit = FoundationUnit
typealias NSDimension = Dimension

// MARK: - UnitConverter

open class UnitConverter {
    constructor() {
    }

    open fun baseUnitValue(fromValue: Double): Double {
        val value = fromValue
        return value
    }

    open fun value(fromBaseUnitValue: Double): Double {
        val baseUnitValue = fromBaseUnitValue
        return baseUnitValue
    }

    @androidx.annotation.Keep
    companion object: CompanionClass() {
    }
    open class CompanionClass {
    }
}

// MARK: - UnitConverterLinear

class UnitConverterLinear: UnitConverter {
    val coefficient: Double
    val constant: Double

    constructor(coefficient: Double, constant: Double = 0.0): super() {
        this.coefficient = coefficient
        this.constant = constant
    }

    override fun baseUnitValue(fromValue: Double): Double {
        val value = fromValue
        return value * coefficient + constant
    }

    override fun value(fromBaseUnitValue: Double): Double {
        val baseUnitValue = fromBaseUnitValue
        return (baseUnitValue - constant) / coefficient
    }

    override fun equals(other: Any?): Boolean {
        if (other !is UnitConverterLinear) {
            return false
        }
        val lhs = this
        val rhs = other
        return lhs.coefficient == rhs.coefficient && lhs.constant == rhs.constant
    }

    override fun hashCode(): Int {
        var hasher = Hasher()
        hash(into = InOut<Hasher>({ hasher }, { hasher = it }))
        return hasher.finalize()
    }
    fun hash(into: InOut<Hasher>) {
        val hasher = into
        hasher.value.combine(coefficient)
        hasher.value.combine(constant)
    }

    @androidx.annotation.Keep
    companion object: UnitConverter.CompanionClass() {
    }
}

// MARK: - UnitConverterReciprocal

class UnitConverterReciprocal: UnitConverter {
    val reciprocal: Double

    constructor(reciprocal: Double): super() {
        this.reciprocal = reciprocal
    }

    override fun baseUnitValue(fromValue: Double): Double {
        val value = fromValue
        return reciprocal / value
    }

    override fun value(fromBaseUnitValue: Double): Double {
        val baseUnitValue = fromBaseUnitValue
        return reciprocal / baseUnitValue
    }

    override fun equals(other: Any?): Boolean {
        if (other !is UnitConverterReciprocal) {
            return false
        }
        val lhs = this
        val rhs = other
        return lhs.reciprocal == rhs.reciprocal
    }

    override fun hashCode(): Int {
        var hasher = Hasher()
        hash(into = InOut<Hasher>({ hasher }, { hasher = it }))
        return hasher.finalize()
    }
    fun hash(into: InOut<Hasher>) {
        val hasher = into
        hasher.value.combine(reciprocal)
    }

    @androidx.annotation.Keep
    companion object: UnitConverter.CompanionClass() {
    }
}

// MARK: - FoundationUnit (named to avoid Kotlin's kotlin.Unit conflict)

open class FoundationUnit {
    val symbol: String

    constructor(symbol: String) {
        this.symbol = symbol
    }

    open val description: String
        get() = symbol

    override fun equals(other: Any?): Boolean {
        if (other !is FoundationUnit) {
            return false
        }
        val lhs = this
        val rhs = other
        if (lhs === rhs) {
            return true
        }
        if (type(of = lhs) != type(of = rhs)) {
            return false
        }
        return lhs.symbol == rhs.symbol
    }

    override fun hashCode(): Int {
        var hasher = Hasher()
        hash(into = InOut<Hasher>({ hasher }, { hasher = it }))
        return hasher.finalize()
    }
    open fun hash(into: InOut<Hasher>) {
        val hasher = into
        hasher.value.combine(symbol)
    }

    override fun toString(): String = description

    @androidx.annotation.Keep
    companion object: CompanionClass() {
    }
    open class CompanionClass {
    }
}

// MARK: - Dimension

open class Dimension: FoundationUnit {
    val converter: UnitConverter

    constructor(symbol: String, converter: UnitConverter): super(symbol = symbol) {
        this.converter = converter
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Dimension) {
            return false
        }
        val lhs = this
        val rhs = other
        if (lhs === rhs) {
            return true
        }
        if (type(of = lhs) != type(of = rhs)) {
            return false
        }
        if (lhs.symbol != rhs.symbol) {
            return false
        }
        // Compare converters by type and value
        (lhs.converter as? UnitConverterLinear)?.let { lc ->
            (rhs.converter as? UnitConverterLinear)?.let { rc ->
                return lc == rc
            }
        }
        (lhs.converter as? UnitConverterReciprocal)?.let { lr ->
            (rhs.converter as? UnitConverterReciprocal)?.let { rr ->
                return lr == rr
            }
        }
        return false
    }

    override fun hashCode(): Int {
        var hasher = Hasher()
        hash(into = InOut<Hasher>({ hasher }, { hasher = it }))
        return hasher.finalize()
    }
    override fun hash(into: InOut<Hasher>) {
        val hasher = into
        hasher.value.combine(symbol)
        val matchtarget_0 = converter as? UnitConverterLinear
        if (matchtarget_0 != null) {
            val lc = matchtarget_0
            hasher.value.combine(lc)
        } else {
            (converter as? UnitConverterReciprocal)?.let { lr ->
                hasher.value.combine(lr)
            }
        }
    }

    @androidx.annotation.Keep
    companion object: CompanionClass() {

        override fun baseUnit(): Dimension {
            return fatalError("Subclass must override baseUnit()")
        }
    }
    open class CompanionClass: FoundationUnit.CompanionClass() {
        open fun baseUnit(): Dimension = Dimension.baseUnit()
    }
}

