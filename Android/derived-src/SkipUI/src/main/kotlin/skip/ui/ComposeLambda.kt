package skip.ui

import skip.lib.*

// Copyright 2023–2026 Skip
// SPDX-License-Identifier: MPL-2.0
import androidx.compose.runtime.Composable

/// Used by the transpiler in place of SkipLib's standard `linvoke` when dealing with Composable code.
@Composable
fun <R> linvokeComposable(l: @Composable () -> R): R = l()
