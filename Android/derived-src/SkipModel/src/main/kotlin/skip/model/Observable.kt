package skip.model

import skip.lib.*

// Copyright 2023–2026 Skip
// SPDX-License-Identifier: MPL-2.0

/// Kotlin representation of `Observation.Observable`.
interface Observable {
}

/// Kotlin representation of `Combine.ObservableObject`.
interface ObservableObject {
    val objectWillChange: ObservableObjectPublisher
}

