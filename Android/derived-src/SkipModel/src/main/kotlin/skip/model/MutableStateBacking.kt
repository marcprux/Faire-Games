package skip.model

import skip.lib.*

// Copyright 2024–2026 Skip
// SPDX-License-Identifier: MPL-2.0
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

class MutableStateBacking: StateTracker {
    private var state: MutableList<MutableState<Int>> = mutableListOf()
        get() = field.sref({ this.state = it })
        set(newValue) {
            field = newValue.sref()
        }
    // Lazily allocated: nil until the first non-nil write transaction is observed. Apps that
    // never use `withAnimation` / `withTransaction` pay zero memory per backing.
    private var lastWriteTransactions: MutableList<StateMutationTransaction?>? = null
        get() = field.sref({ this.lastWriteTransactions = it })
        set(newValue) {
            field = newValue.sref()
        }
    private var isTracking = false

    constructor() {
        StateTracking.register(this)
    }

    fun access(stateAt: Int) {
        val index = stateAt
        synchronized(this) { ->
            initialize(stateAt = index)
            // Only consult the read cursor when this slot actually has a recorded transaction —
            // saves a ThreadLocal access on every plain state read in apps without animation.
            lastWriteTransactions.sref()?.let { ledger ->
                if (index < ledger.size) {
                    ledger[index].sref()?.let { tx ->
                        StateTracking.recordRead(tx)
                    }
                }
            }
            state[index].value.sref()
        }
    }

    fun update(stateAt: Int) {
        val index = stateAt
        synchronized(this) { ->
            initialize(stateAt = index)
            val tx = StateTracking.currentTransaction
            if (tx != null || lastWriteTransactions != null) {
                // Allocate the ledger lazily; once allocated, we must keep it in sync (writing
                // nil overwrites a stale tx from a prior write inside `withAnimation`).
                val ledger = ensureLedger()
                while (ledger.size <= index) {
                    ledger.add(null)
                }
                ledger[index] = tx
            }
            // Only update state when tracking. We do, however, read state even when tracking has not begun.
            // Otherwise post-tracking updates may not cause recomposition
            if (isTracking) {
                state[index].value += 1
            }
        }
    }

    private fun initialize(stateAt: Int) {
        val index = stateAt
        while (state.size <= index) {
            state.add(mutableStateOf(0))
        }
    }

    private fun ensureLedger(): MutableList<StateMutationTransaction?> {
        lastWriteTransactions.sref()?.let { existing ->
            return existing.sref()
        }
        val created: MutableList<StateMutationTransaction?> = mutableListOf()
        lastWriteTransactions = created
        return created.sref()
    }

    override fun trackState() {
        synchronized(this) { -> isTracking = true }
    }

    @androidx.annotation.Keep
    companion object {
    }
}
