package skip.lib

import skip.lib.Array

// Copyright 2023–2026 Skip
// SPDX-License-Identifier: MPL-2.0
interface RegexComponent {
    //associatedtype RegexOutput // in Swift but not Kotlin

    /// The regular expression represented by this component.
    val regex: Regex
}

/// Kotlin representation of `Swift.Regex`.
class Regex: RegexComponent {
    private val _regex: kotlin.text.Regex

    constructor(string: String) {
        _regex = kotlin.text.Regex(string, RegexOption.MULTILINE)
    }

    override val regex: Regex
        get() = this

    /// The result of matching a regular expression against a string.
    class Match {
        /// The range of the overall match.
        // public let range: Range<String.Index>

        internal val match: kotlin.text.MatchResult

        val count: Int
            get() = match.groups.size

        operator fun get(index: Int): Regex.Match.MatchGroup = MatchGroup(group = match.groups.get(index))

        class MatchGroup {
            internal val group: kotlin.text.MatchGroup?

            // val range: IntRange

            val substring: Substring?
                get() {
                    val matchtarget_0 = group
                    if (matchtarget_0 != null) {
                        val group = matchtarget_0
                        return Substring(group.value, 0)
                    } else {
                        return null
                    }
                }

            constructor(group: kotlin.text.MatchGroup? = null) {
                this.group = group.sref()
            }

            @androidx.annotation.Keep
            companion object {
            }
        }

        constructor(match: kotlin.text.MatchResult) {
            this.match = match.sref()
        }

        @androidx.annotation.Keep
        companion object {
        }
    }

    fun matches(string: String): Array<Regex.Match> {
        var matches: Array<Regex.Match> = arrayOf()
        for (match in _regex.findAll(string)) {
            matches.append(Match(match = match))
        }
        return matches.sref()
    }

    fun replace(string: String, with: String): String {
        val replacement = with
        val safeReplacement = java.util.regex.Matcher.quoteReplacement(replacement)
        return _regex.replace(string, safeReplacement)
    }

    fun replace(string: String, maxReplacements: Int = Int.max, with: (Regex.Match) -> String): String {
        val replacement = with
        if (maxReplacements <= 0) {
            return string
        }

        var replacementCount = 0
        return _regex.replace(string) l@{ match ->
            val replacementValue: String
            if (replacementCount < maxReplacements) {
                replacementCount += 1
                replacementValue = replacement(Match(match = match))
            } else {
                replacementValue = match.value
            }
            return@l java.util.regex.Matcher.quoteReplacement(replacementValue)
        }
    }

    @androidx.annotation.Keep
    companion object {
    }
}

