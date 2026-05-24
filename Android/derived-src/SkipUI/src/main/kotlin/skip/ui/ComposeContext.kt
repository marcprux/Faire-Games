package skip.ui

import skip.lib.*

// Copyright 2023–2026 Skip
// SPDX-License-Identifier: MPL-2.0
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.ui.Modifier

/// Context to provide modifiers, etc to composables.
///
/// This type is often used as an argument to internal `@Composable` functions and is not mutated by reference, so mark `@Stable`
/// to avoid excessive recomposition.
@Stable
@Suppress("MUST_BE_INITIALIZED")
class ComposeContext: MutableStruct {
    /// Modifiers to apply.
    var modifier: Modifier
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }

    /// Mechanism for a parent view to change how a child view is composed.
    var composer: Composer? = null
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }

    /// Use in conjunction with `rememberSaveable` to store view state.
    var stateSaver: Saver<Any?, Any>
        get() = field.sref({ this.stateSaver = it })
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
            willmutate()
            field = newValue
            didmutate()
        }

    /// The scope of the current composition (so users can call scoped modifiers).
    var scope: Any? = null
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }

    /// The context to pass to child content of a container view.
    ///
    /// By default, modifiers and the `composer` are reset for child content.
    fun content(modifier: Modifier = Modifier, composer: Composer? = null, stateSaver: Saver<Any?, Any>? = null, scope: Any? = null): ComposeContext {
        var context = this.sref()
        context.modifier = modifier
        context.composer = composer
        context.stateSaver = stateSaver ?: this.stateSaver
        context.scope = scope
        return context.sref()
    }

    constructor(modifier: Modifier = Modifier, composer: Composer? = null, stateSaver: Saver<Any?, Any> = ComposeStateSaver(), scope: Any? = null) {
        this.modifier = modifier
        this.composer = composer
        this.stateSaver = stateSaver
        this.scope = scope
    }

    override var supdate: ((Any) -> Unit)? = null
    override var smutatingcount = 0
    override fun scopy(): MutableStruct = ComposeContext(modifier, composer, stateSaver, scope)

    override fun equals(other: Any?): Boolean {
        if (other !is ComposeContext) return false
        return modifier == other.modifier && composer == other.composer && stateSaver == other.stateSaver && scope == other.scope
    }

    @androidx.annotation.Keep
    companion object {
    }
}

