package skip.lib

// Copyright 2023–2026 Skip
// SPDX-License-Identifier: MPL-2.0

class ObjectIdentifier {
    internal val object_: Any

    override fun equals(other: Any?): Boolean {
        if (other !is ObjectIdentifier) {
            return false
        }
        val lhs = this
        val rhs = other
        return lhs.object_ === rhs.object_
    }

    internal constructor(object_: Any) {
        this.object_ = object_.sref()
    }

    override fun hashCode(): Int {
        var result = 1
        result = Hasher.combine(result, object_)
        return result
    }

    @androidx.annotation.Keep
    companion object {
    }
}

