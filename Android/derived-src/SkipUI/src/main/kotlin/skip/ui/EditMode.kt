package skip.ui

import skip.lib.*

// Copyright 2023–2026 Skip
// SPDX-License-Identifier: MPL-2.0

enum class EditMode {
    inactive,
    transient,
    active;

    val isEditing: Boolean
        get() = this != EditMode.inactive

    @androidx.annotation.Keep
    companion object {
    }
}

