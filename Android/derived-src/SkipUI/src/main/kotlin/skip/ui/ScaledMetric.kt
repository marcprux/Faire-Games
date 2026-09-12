package skip.ui

import skip.lib.*

// Copyright 2023–2026 Skip
// SPDX-License-Identifier: MPL-2.0
import skip.model.*

import android.content.res.Resources
import androidx.compose.ui.unit.fontscaling.FontScaleConverterFactory
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

// SwiftUI ScaledMetric accetps a `BinaryFloatingPoint`, implemented by Double, Float, and Float80.
// We're only supporting Double for now.
class ScaledMetric<Value>: StateTracker where Value: Double {
    private var _wrappedValue: Value
    private var wrappedValueState: MutableState<Value>? = null
        get() = field.sref({ this.wrappedValueState = it })
        set(newValue) {
            field = newValue.sref()
        }

    constructor(wrappedValue: Value) {
        _wrappedValue = wrappedValue
        StateTracking.register(this)
    }

    constructor(wrappedValue: Value, relativeTo: Font.TextStyle) {
        val textStyle = relativeTo
        _wrappedValue = wrappedValue
        // Compose doesn't scale differently based on text style, so we ignore it.
        textStyle
        StateTracking.register(this)
    }

    val wrappedValue: Value
        get() {
            val value = wrappedValueState?.value ?: _wrappedValue
            return ScaledMetricBridge.scaledValue(for_ = value) as Value
        }

    override fun trackState() {
        wrappedValueState = mutableStateOf(_wrappedValue)
    }

    @androidx.annotation.Keep
    companion object {
    }
}

enum class ScaledMetricBridge {
    ;

    @androidx.annotation.Keep
    companion object {

        fun scaledValue(for_: Double): Double {
            val value = for_
            val scale: Float = Resources.getSystem().configuration.fontScale
            val scaled: Double
            if (FontScaleConverterFactory.isNonLinearFontScalingActive(fontScale = scale)) {
                val matchtarget_0 = FontScaleConverterFactory.forScale(fontScale = scale)
                if (matchtarget_0 != null) {
                    val converter = matchtarget_0
                    scaled = Double(converter.convertSpToDp(sp = Float(value)))
                } else {
                    scaled = value * scale
                }
            } else {
                scaled = value * scale
            }
            return scaled
        }
    }
}
