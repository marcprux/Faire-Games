// Copyright 2023–2026 Skip
// SPDX-License-Identifier: MPL-2.0

package skip.ui

import skip.lib.*

/// The result of composing content.
///
/// Reserved for future use. Having a return value also expands recomposition scope. See `ComposeBuilder` for details.
class ComposeResult {

    @androidx.annotation.Keep
    companion object {
        val ok = ComposeResult()
    }
}
