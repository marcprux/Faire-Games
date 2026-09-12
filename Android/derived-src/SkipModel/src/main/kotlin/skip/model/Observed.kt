package skip.model

import skip.lib.*

// Copyright 2023–2026 Skip
// SPDX-License-Identifier: MPL-2.0

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.referentialEqualityPolicy

/// We model properties of `@Observable` types as if they had this synthetic `@Observed` property wrapper.
/// Like `Published`, it uses `MutableState` to tie into Compose's observation system.
@Suppress("MUST_BE_INITIALIZED")
class Observed<Value>: StateTracker {
    constructor(wrappedValue: Value) {
        _wrappedValue = wrappedValue
        StateTracking.register(this)
    }

    var wrappedValue: Value
        get() {
            val matchtarget_0 = projectedValue
            if (matchtarget_0 != null) {
                val projectedValue = matchtarget_0
                // Skip the recordRead call entirely when no `withAnimation` ever wrote to us —
                // the common case for non-animated state. Inside withAnimation the stamp is
                // non-nil and we report it so animatable modifiers can pick it up.
                lastWriteTransaction?.let { tx ->
                    StateTracking.recordRead(tx)
                }
                return projectedValue.value.sref({ this.wrappedValue = it })
            } else {
                return _wrappedValue.sref({ this.wrappedValue = it })
            }
        }
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
            val tx = StateTracking.currentTransaction
            // Only stamp when we have a tx to record OR we previously recorded one (so a plain
            // write clears the stale tx). Avoids a ThreadLocal hit on every plain write.
            if (tx != null || lastWriteTransaction != null) {
                lastWriteTransaction = tx
            }
            projectedValue.sref()?.let { projectedValue ->
                projectedValue.value = newValue
            }
            _wrappedValue = newValue
        }
    private var _wrappedValue: Value
        get() = field.sref({ this._wrappedValue = it })
        set(newValue) {
            field = newValue.sref()
        }
    private var lastWriteTransaction: StateMutationTransaction? = null

    var projectedValue: MutableState<Value>? = null
        get() = field.sref({ this.projectedValue = it })
        set(newValue) {
            field = newValue.sref()
        }

    override fun trackState() {
        // Once we create our internal MutableState, reads and writes will be tracked by Compose
        if (projectedValue == null) {
            projectedValue = mutableStateOf(_wrappedValue, referentialEqualityPolicy())
        }
    }

    @androidx.annotation.Keep
    companion object {
    }
}

