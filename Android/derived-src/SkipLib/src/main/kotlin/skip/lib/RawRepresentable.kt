package skip.lib

// Copyright 2023–2026 Skip
// SPDX-License-Identifier: MPL-2.0

/// Kotlin representation of `Swift.RawRepresentable`.
interface RawRepresentable<RawType> {
    val rawValue: RawType
}

