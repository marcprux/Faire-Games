package skip.model

import skip.lib.*
import skip.lib.Set

// Copyright 2023–2026 Skip
// SPDX-License-Identifier: MPL-2.0

interface Cancellable {
    fun cancel()
}

class AnyCancellable: Cancellable {
    internal var cancellable: Cancellable? = null
        get() = field.sref({ this.cancellable = it })
        set(newValue) {
            field = newValue.sref()
        }

    internal constructor() {
    }

    constructor(cancellable: Cancellable): this() {
        this.cancellable = cancellable
    }

    constructor(cancel: () -> Unit): this(CancelClosure(onCancel = cancel)) {
    }

    override fun cancel() {
        cancellable.sref()?.let { cancellable ->
            cancellable.cancel()
        }
    }

    fun store(in_: InOut<Set<AnyCancellable>>) {
        val set = in_
        set.value.insert(this)
    }

    override fun hashCode(): Int {
        var hasher = Hasher()
        hash(into = InOut<Hasher>({ hasher }, { hasher = it }))
        return hasher.finalize()
    }
    fun hash(into: InOut<Hasher>) {
        val hasher = into
        hasher.value.combine(cancellable)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is AnyCancellable) {
            return false
        }
        val lhs = this
        val rhs = other
        return lhs.cancellable == rhs.cancellable
    }

    internal class CancelClosure: Cancellable {
        internal val onCancel: () -> Unit

        override fun cancel(): Unit = onCancel()

        constructor(onCancel: () -> Unit) {
            this.onCancel = onCancel
        }
    }

    @androidx.annotation.Keep
    companion object {
    }
}

