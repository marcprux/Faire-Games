package skip.ui

import skip.lib.*

// Copyright 2023–2026 Skip
// SPDX-License-Identifier: MPL-2.0
import skip.model.*

/// The context of the current state-processing update.
///
/// On Android, `Transaction` is a plain value carrier — its `animation` field is the source
/// of truth for the per-slot mutation tag. `withTransaction(_:_:)` pushes `self` onto the
/// `StateTracking` thread-local stack so writes inside the body get tagged with this
/// transaction, then on exit pops the stack AND publishes the animation through
/// `Animation.markRecentWithAnimation(_:)` as a fallback for render-time-resolved values.
class Transaction: StateMutationTransaction {
    var animation: Animation? = null
        get() = field.sref({ this.animation = it })
        set(newValue) {
            field = newValue.sref()
        }
    var disablesAnimations: Boolean
    var isContinuous: Boolean
    var tracksVelocity: Boolean

    /// Storage for custom values written via `withTransaction(_:_:_:)` keypath form. The key is
    /// the fully-qualified `TransactionKey` type name.
    private var customValues: Dictionary<String, Any?>? = null
        get() = field.sref({ this.customValues = it })
        set(newValue) {
            field = newValue.sref()
        }

    constructor() {
        this.animation = null
        this.disablesAnimations = false
        this.isContinuous = false
        this.tracksVelocity = false
    }

    constructor(animation: Animation?) {
        this.animation = animation
        this.disablesAnimations = false
        this.isContinuous = false
        this.tracksVelocity = false
    }

    /// Read a custom value by key type name. Returns `nil` if no value was set.
    ///
    /// Skip Lite cannot access a static member of a generic type from a companion object, so the
    /// Swift-side subscript `Transaction[K.Type]` (which would call `K.defaultValue`) is not
    /// available on Android. Custom-key consumers manage their own defaults via these helpers.
    fun getCustomValue(forKeyTypeName: String): Any? {
        val name = forKeyTypeName
        val values_0 = customValues.sref()
        if (values_0 == null) {
            return null
        }
        return (values_0[name] ?: null).sref()
    }

    /// Set a custom value by key type name.
    fun setCustomValue(forKeyTypeName: String, value: Any?) {
        val name = forKeyTypeName
        if (customValues == null) {
            customValues = dictionaryOf()
        }
        var values = customValues!!.sref()
        values[name] = value.sref()
        customValues = values
    }

    /// Produce a shallow copy of this transaction. Used by `withTransaction(_:_:_:)` so a
    /// keypath-mutated copy doesn't poison the outer transaction.
    internal fun copy(): Transaction {
        val result = Transaction()
        result.animation = animation
        result.disablesAnimations = disablesAnimations
        result.isContinuous = isContinuous
        result.tracksVelocity = tracksVelocity
        customValues.sref()?.let { customValues ->
            result.customValues = customValues
        }
        return result
    }

    @androidx.annotation.Keep
    companion object {
    }
}


/// Executes a closure with the specified transaction and returns the result. The transaction
/// is pushed onto the per-thread `StateTracking` stack for the body's duration so observable
/// writes inside get the per-slot tag; on exit, the transaction is popped AND (if it carries
/// an animation and doesn't disable animations) the animation is published through one
/// Compose frame as a fallback for animatable values that resolve later.
fun <Result> withTransaction(transaction: Transaction, body: () -> Result): Result {
    var deferaction_0: (() -> Unit)? = null
    try {
        val token = StateTracking.pushTransaction(transaction)
        deferaction_0 = {
            StateTracking.popTransaction(token)
            transaction.animation.sref()?.let { animation ->
                if (!transaction.disablesAnimations) {
                    Animation.markRecentWithAnimation(animation)
                }
            }
        }
        return body()
    } finally {
        deferaction_0?.invoke()
    }
}


// Skip Lite cannot model `WritableKeyPath<Transaction, V>` at the call site, so the keypath
// variant of `withTransaction` is iOS-only for now. Use the explicit-Transaction overload to set
// individual properties on the Skip side.

