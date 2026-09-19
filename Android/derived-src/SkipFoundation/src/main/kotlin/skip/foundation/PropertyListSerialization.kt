package skip.foundation

import skip.lib.*
import skip.lib.Array

// Copyright 2023–2026 Skip
// SPDX-License-Identifier: MPL-2.0
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.ByteArrayInputStream

open class PropertyListSerialization {

    enum class PropertyListFormat(override val rawValue: UInt, @Suppress("UNUSED_PARAMETER") unusedp: Nothing? = null): RawRepresentable<UInt> {
        openStep(UInt(1)),
        xml(UInt(100)),
        binary(UInt(200));

        @androidx.annotation.Keep
        companion object {
            fun init(rawValue: UInt): PropertyListSerialization.PropertyListFormat? {
                return when (rawValue) {
                    UInt(1) -> PropertyListFormat.openStep
                    UInt(100) -> PropertyListFormat.xml
                    UInt(200) -> PropertyListFormat.binary
                    else -> null
                }
            }
        }
    }

    class ReadOptions: RawRepresentable<UInt>, OptionSet<PropertyListSerialization.ReadOptions, UInt>, MutableStruct {
        override var rawValue: UInt

        constructor(rawValue: UInt) {
            this.rawValue = rawValue
        }

        override val rawvaluelong: ULong
            get() = ULong(rawValue)
        override fun makeoptionset(rawvaluelong: ULong): PropertyListSerialization.ReadOptions = ReadOptions(rawValue = UInt(rawvaluelong))
        override fun assignoptionset(target: PropertyListSerialization.ReadOptions) {
            willmutate()
            try {
                assignfrom(target)
            } finally {
                didmutate()
            }
        }

        private constructor(copy: MutableStruct) {
            @Suppress("NAME_SHADOWING", "UNCHECKED_CAST") val copy = copy as PropertyListSerialization.ReadOptions
            this.rawValue = copy.rawValue
        }

        override var supdate: ((Any) -> Unit)? = null
        override var smutatingcount = 0
        override fun scopy(): MutableStruct = PropertyListSerialization.ReadOptions(this as MutableStruct)

        private fun assignfrom(target: PropertyListSerialization.ReadOptions) {
            this.rawValue = target.rawValue
        }

        @androidx.annotation.Keep
        companion object {

            val mutableContainers = ReadOptions(rawValue = 1U)
            val mutableContainersAndLeaves = ReadOptions(rawValue = 2U)

            fun of(vararg options: PropertyListSerialization.ReadOptions): PropertyListSerialization.ReadOptions {
                val value = options.fold(UInt(0)) { result, option -> result or option.rawValue }
                return ReadOptions(rawValue = value)
            }
        }
    }

    @androidx.annotation.Keep
    companion object: CompanionClass() {

        @Deprecated("This API is not yet available in Skip. Consider placing it within a #if !SKIP block. You can file an issue against the owning library at https://github.com/skiptools, or see the library README for information on adding support", level = DeprecationLevel.ERROR)
        fun propertyList(propertyList: Any, isValidFor: PropertyListSerialization.PropertyListFormat): Boolean {
            fatalError()
        }

        override fun propertyList(from: Data, options: PropertyListSerialization.ReadOptions, format: Any?): Dictionary<String, String>? {
            val data = from
            val text = data.utf8String
            if (text == null) {
                // should this throw an error?
                return null
            }

            // TODO: auto-detect format from data content if the format argument is unset
            val trimmed = text.trimmingCharacters(in_ = CharacterSet.whitespacesAndNewlines)
            if ((format as? PropertyListSerialization.PropertyListFormat) == PropertyListSerialization.PropertyListFormat.xml || trimmed.hasPrefix("<?xml") || trimmed.hasPrefix("<plist")) {
                return convertStringsDictToICUDict(from = data)
            } else {
                return openStepPropertyList(from = data, options = options)
            }
        }

        private fun convertStringsDictToICUDict(from: Data): Dictionary<String, String>? {
            val data = from
            val parser = Xml.newPullParser()
            parser.setInput(ByteArrayInputStream(data.platformValue), "UTF-8")

            var result: Dictionary<String, String> = dictionaryOf()
            var dictStack: Array<Dictionary<String, Any>> = arrayOf()
            var keyStack: Array<String> = arrayOf()
            var textAccumulator = ""

            var eventType = parser.getEventType()
            while (eventType != XmlPullParser.END_DOCUMENT) {

                val tagName = parser.getName()
                for (unusedi in 0..0) {
                    when (eventType) {
                        XmlPullParser.START_TAG -> {
                            textAccumulator = ""
                            if (tagName == "dict") {
                                dictStack.append(dictionaryOf())
                            }
                        }
                        XmlPullParser.TEXT -> textAccumulator += parser.getText() ?: ""
                        XmlPullParser.END_TAG -> {
                            val content = textAccumulator.trimmingCharacters(in_ = CharacterSet.whitespacesAndNewlines)
                            for (unusedi in 0..0) {
                                when (tagName) {
                                    "key" -> keyStack.append(content)
                                    "string" -> {
                                        keyStack.popLast()?.let { key ->
                                            if (!dictStack.isEmpty) {
                                                dictStack[dictStack.count - 1][key] = content
                                            }
                                        }
                                    }
                                    "dict" -> {
                                        dictStack.popLast()?.let { finishedDict ->
                                            val matchtarget_0 = keyStack.popLast()
                                            if (matchtarget_0 != null) {
                                                val parentKey = matchtarget_0
                                                if (!dictStack.isEmpty) {
                                                    dictStack[dictStack.count - 1][parentKey] = finishedDict.sref()
                                                } else {
                                                    for ((key, value) in finishedDict.sref()) {
                                                        (value as? Dictionary<String, Any>).sref()?.let { stringsDict ->
                                                            convertStringsDictToICUString(stringsDict)?.let { icuString ->
                                                                result[key] = icuString
                                                            }
                                                        }
                                                    }
                                                }
                                            } else {
                                                for ((key, value) in finishedDict.sref()) {
                                                    (value as? Dictionary<String, Any>).sref()?.let { stringsDict ->
                                                        convertStringsDictToICUString(stringsDict)?.let { icuString ->
                                                            result[key] = icuString
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    else -> break
                                }
                            }

                            textAccumulator = ""
                        }
                        else -> break
                    }
                }

                eventType = parser.next()
            }

            return result.sref()
        }

        private fun convertStringsDictToICUString(stringsDict: Dictionary<String, Any>): String? {
            val formatKey_0 = stringsDict["NSStringLocalizedFormatKey"] as? String
            if (formatKey_0 == null) {
                return null
            }

            var result: String = formatKey_0

            for ((key, varDict) in stringsDict.compactMapValues({ it -> it as? Dictionary<String, Any> })) {
                val specType_0 = varDict["NSStringFormatSpecTypeKey"] as? String
                if ((specType_0 == null) || (specType_0 != "NSStringPluralRuleType")) {
                    continue
                }

                val categories = arrayOf("zero", "one", "two", "few", "many", "other")
                val rules = categories.compactMap(fun(category: String): String? {
                    val pluralString_0 = varDict[category] as? String
                    if (pluralString_0 == null) {
                        return null
                    }
                    var cleaned = pluralString_0!!
                    for (index in 1..10) {
                        cleaned = cleaned
                            .replacingOccurrences(of = "%${index}\$@", with = "{${index - 1}}")
                            .replacingOccurrences(of = "%${index}\$d", with = "{${index - 1}}")
                    }
                    val icuCategory = if (category == "zero") "=0" else category
                    return "${icuCategory}{${cleaned}}"
                }).joined(separator = " ")

                if (!rules.isEmpty) {
                    val pattern = "%#@${key}@"
                    val replacement = "{0, plural, ${rules}}"

                    if (result.contains(pattern)) {
                        result = result.replacingOccurrences(of = pattern, with = replacement)
                    } else if (result.contains("%#@")) {
                        val parts = result.components(separatedBy = "%#@")
                        if (parts.count > 1) {
                            val prefix = parts[0]
                            val remaining = parts[1]
                            val subParts = remaining.components(separatedBy = "@")
                            if (subParts.count > 1) {
                                val suffix = subParts[1..Int.max].joined(separator = "@")
                                result = prefix + replacement + suffix
                            }
                        }
                    }
                }
            }

            return result
        }

        private fun openStepPropertyList(from: Data, options: PropertyListSerialization.ReadOptions = PropertyListSerialization.ReadOptions.of()): Dictionary<String, String>? {
            val data = from
            var dict: Dictionary<String, String> = dictionaryOf()

            val text = data.utf8String
            if (text == null) {
                // should this throw an error?
                return null
            }

            val lines = text.components(separatedBy = "\n")

            for (line in lines.sref()) {
                if (!line.hasPrefix("\"")) {
                    continue // maybe a comment? (note: we do no support multi-line /* */ comments
                }
                var key: String? = null
                var value: String? = null
                var isParsingKey = true
                var currentToken = ""
                var isEscaped = false
                var isInsideString = false

                for (char in line) {
                    if (isEscaped) {
                        if (char == 'n') {
                            currentToken += "\n"
                        } else if (char == 'r') {
                            currentToken += "\r"
                        } else if (char == 't') {
                            currentToken += "\t"
                            //} else if char == "u" { // TODO: handle unicode escapes like \uXXXX
                        } else {
                            // otherwise, just add the literal characters (like " or \)
                            currentToken += char
                        }
                        isEscaped = false
                        continue
                    }

                    when (char) {
                        '\\' -> isEscaped = true
                        '\"' -> {
                            isInsideString = !isInsideString
                            if (!isInsideString) {
                                if (isParsingKey) {
                                    key = currentToken
                                    isParsingKey = false
                                } else {
                                    value = currentToken
                                }
                                currentToken = ""
                            }
                        }
                        '=' -> {
                            if (isInsideString) {
                                currentToken += char
                            } else {
                                isParsingKey = false
                            }
                        }
                        ';' -> {
                            if (isInsideString) {
                                currentToken += char
                            } else {
                                key?.let { k ->
                                    value?.let { v ->
                                        dict[k] = v
                                    }
                                }
                            }
                        }
                        else -> {
                            if (isInsideString) {
                                currentToken += char
                            }
                        }
                    }
                }
            }

            return dict.sref()
        }

        @Deprecated("This API is not yet available in Skip. Consider placing it within a #if !SKIP block. You can file an issue against the owning library at https://github.com/skiptools, or see the library README for information on adding support", level = DeprecationLevel.ERROR)
        fun data(fromPropertyList: Any, format: PropertyListSerialization.PropertyListFormat, options: Int): Data {
            fatalError()
        }

        @Deprecated("This API is not yet available in Skip. Consider placing it within a #if !SKIP block. You can file an issue against the owning library at https://github.com/skiptools, or see the library README for information on adding support", level = DeprecationLevel.ERROR)
        fun writePropertyList(propertyList: Any, to: Any, format: PropertyListSerialization.PropertyListFormat, options: Int, error: Any): Int {
            fatalError()
        }

        @Deprecated("This API is not yet available in Skip. Consider placing it within a #if !SKIP block. You can file an issue against the owning library at https://github.com/skiptools, or see the library README for information on adding support", level = DeprecationLevel.ERROR)
        fun propertyList(with: Any, options: PropertyListSerialization.ReadOptions = PropertyListSerialization.ReadOptions.of(), format: Any?): Any {
            fatalError()
        }

        override fun PropertyListFormat(rawValue: UInt): PropertyListSerialization.PropertyListFormat? = PropertyListFormat.init(rawValue = rawValue)
    }
    open class CompanionClass {
        open fun propertyList(from: Data, options: PropertyListSerialization.ReadOptions = PropertyListSerialization.ReadOptions.of(), format: Any?): Dictionary<String, String>? = PropertyListSerialization.propertyList(from = from, options = options, format = format)
        open fun PropertyListFormat(rawValue: UInt): PropertyListSerialization.PropertyListFormat? = PropertyListSerialization.PropertyListFormat(rawValue = rawValue)
    }
}

