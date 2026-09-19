package skip.ui

import skip.lib.*

// Copyright 2023–2026 Skip
// SPDX-License-Identifier: MPL-2.0

// Model Environment as a class rather than struct to mutate by reference and avoid copy overhead
class Environment<Value> where Value: Any {
    constructor() {
    }

    constructor(wrappedValue: Value) {
        this.wrappedValue = wrappedValue
    }

    var wrappedValue: Value
        get() = wrappedValuestorage.sref({ this.wrappedValue = it })
        set(newValue) {
            wrappedValuestorage = newValue.sref()
        }
    private lateinit var wrappedValuestorage: Value

    val projectedValue: Binding<Value>
        get() {
            return Binding(get = { -> this.wrappedValue }, set = { it -> this.wrappedValue = it })
        }

    @androidx.annotation.Keep
    companion object {
    }
}

