package skip.ui

import skip.lib.*

// Copyright 2023–2026 Skip
// SPDX-License-Identifier: MPL-2.0
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp

enum class ContentMarginPlacement(override val rawValue: Int, @Suppress("UNUSED_PARAMETER") unusedp: Nothing? = null): RawRepresentable<Int> {
    automatic(0),
    scrollContent(1),
    scrollIndicators(2);

    @androidx.annotation.Keep
    companion object {
        fun init(rawValue: Int): ContentMarginPlacement? {
            return when (rawValue) {
                0 -> ContentMarginPlacement.automatic
                1 -> ContentMarginPlacement.scrollContent
                2 -> ContentMarginPlacement.scrollIndicators
                else -> null
            }
        }
    }
}

fun ContentMarginPlacement(rawValue: Int): ContentMarginPlacement? = ContentMarginPlacement.init(rawValue = rawValue)

/// Holds the content margin values for each placement type.
class ContentMargins: MutableStruct {
    var automatic: EdgeInsets? = null
        get() = field.sref({ this.automatic = it })
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
            willmutate()
            field = newValue
            didmutate()
        }
    var scrollContent: EdgeInsets? = null
        get() = field.sref({ this.scrollContent = it })
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
            willmutate()
            field = newValue
            didmutate()
        }
    var scrollIndicators: EdgeInsets? = null
        get() = field.sref({ this.scrollIndicators = it })
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
            willmutate()
            field = newValue
            didmutate()
        }

    constructor(automatic: EdgeInsets? = null, scrollContent: EdgeInsets? = null, scrollIndicators: EdgeInsets? = null) {
        this.automatic = automatic
        this.scrollContent = scrollContent
        this.scrollIndicators = scrollIndicators
    }

    /// Returns the effective content margin for the given placement.
    /// For `.automatic`, returns `scrollContent` if set, otherwise `automatic`.
    fun effectiveContentMargin(for_: ContentMarginPlacement): EdgeInsets? {
        val placement = for_
        when (placement) {
            ContentMarginPlacement.automatic -> {
                // For automatic, prefer scrollContent if set, then fall back to automatic
                return (scrollContent ?: automatic).sref()
            }
            ContentMarginPlacement.scrollContent -> return (scrollContent ?: automatic).sref()
            ContentMarginPlacement.scrollIndicators -> return (scrollIndicators ?: automatic).sref()
        }
    }

    /// Converts the content margins to Compose PaddingValues for the given placement.
    internal fun asComposePaddingValues(for_: ContentMarginPlacement): PaddingValues? {
        val placement = for_
        val insets_0 = effectiveContentMargin(for_ = placement)
        if (insets_0 == null) {
            return null
        }
        return androidx.compose.foundation.layout.PaddingValues(start = Double(insets_0.leading).dp, top = Double(insets_0.top).dp, end = Double(insets_0.trailing).dp, bottom = Double(insets_0.bottom).dp)
    }

    private constructor(copy: MutableStruct) {
        @Suppress("NAME_SHADOWING", "UNCHECKED_CAST") val copy = copy as ContentMargins
        this.automatic = copy.automatic
        this.scrollContent = copy.scrollContent
        this.scrollIndicators = copy.scrollIndicators
    }

    override var supdate: ((Any) -> Unit)? = null
    override var smutatingcount = 0
    override fun scopy(): MutableStruct = ContentMargins(this as MutableStruct)

    @androidx.annotation.Keep
    companion object {
    }
}

