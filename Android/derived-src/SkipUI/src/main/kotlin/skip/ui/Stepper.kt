package skip.ui

import skip.lib.*

// Copyright 2023–2026 Skip
// SPDX-License-Identifier: MPL-2.0
import skip.foundation.*
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@androidx.annotation.Keep
class Stepper: View, Renderable, skip.lib.SwiftProjecting {
    internal val value: Binding<Double>?
    internal val label: ComposeBuilder
    internal val step: Double
    internal val minValue: Double?
    internal val maxValue: Double?
    internal val onIncrement: (() -> Unit)?
    internal val onDecrement: (() -> Unit)?
    internal val onEditingChanged: ((Boolean) -> Unit)?

    // MARK: - Custom increment/decrement initializers

    constructor(label: () -> View, onIncrement: (() -> Unit)?, onDecrement: (() -> Unit)?, onEditingChanged: (Boolean) -> Unit = { _ ->  }) {
        this.value = null
        this.label = ComposeBuilder.from(label)
        this.step = 1.0
        this.minValue = null
        this.maxValue = null
        this.onIncrement = onIncrement
        this.onDecrement = onDecrement
        this.onEditingChanged = onEditingChanged
    }

    constructor(titleKey: LocalizedStringKey, onIncrement: (() -> Unit)?, onDecrement: (() -> Unit)?, onEditingChanged: (Boolean) -> Unit = { _ ->  }): this(label = { ->
        ComposeBuilder { composectx: ComposeContext ->
            Text(titleKey).Compose(composectx)
            ComposeResult.ok
        }
    }, onIncrement = onIncrement, onDecrement = onDecrement, onEditingChanged = onEditingChanged) {
    }

    constructor(titleResource: LocalizedStringResource, onIncrement: (() -> Unit)?, onDecrement: (() -> Unit)?, onEditingChanged: (Boolean) -> Unit = { _ ->  }): this(label = { ->
        ComposeBuilder { composectx: ComposeContext ->
            Text(titleResource).Compose(composectx)
            ComposeResult.ok
        }
    }, onIncrement = onIncrement, onDecrement = onDecrement, onEditingChanged = onEditingChanged) {
    }

    constructor(title: String, onIncrement: (() -> Unit)?, onDecrement: (() -> Unit)?, onEditingChanged: (Boolean) -> Unit = { _ ->  }): this(label = { ->
        ComposeBuilder { composectx: ComposeContext ->
            Text(verbatim = title).Compose(composectx)
            ComposeResult.ok
        }
    }, onIncrement = onIncrement, onDecrement = onDecrement, onEditingChanged = onEditingChanged) {
    }

    // MARK: - Int value initializers (no bounds)

    constructor(value: Binding<Int>, step: Int = 1, label: () -> View, onEditingChanged: (Boolean) -> Unit = { _ ->  }) {
        val intValue = value
        var capturedBinding = intValue.sref()
        this.value = Binding(get = { -> Double(capturedBinding.wrappedValue) }, set = { it -> capturedBinding.wrappedValue = Int(it) })
        this.label = ComposeBuilder.from(label)
        this.step = Double(step)
        this.minValue = null
        this.maxValue = null
        this.onIncrement = null
        this.onDecrement = null
        this.onEditingChanged = onEditingChanged
    }

    constructor(titleKey: LocalizedStringKey, value: Binding<Int>, step: Int = 1, onEditingChanged: (Boolean) -> Unit = { _ ->  }): this(value = value, step = step, label = { ->
        ComposeBuilder { composectx: ComposeContext ->
            Text(titleKey).Compose(composectx)
            ComposeResult.ok
        }
    }, onEditingChanged = onEditingChanged) {
    }

    constructor(titleResource: LocalizedStringResource, value: Binding<Int>, step: Int = 1, onEditingChanged: (Boolean) -> Unit = { _ ->  }): this(value = value, step = step, label = { ->
        ComposeBuilder { composectx: ComposeContext ->
            Text(titleResource).Compose(composectx)
            ComposeResult.ok
        }
    }, onEditingChanged = onEditingChanged) {
    }

    constructor(title: String, value: Binding<Int>, step: Int = 1, onEditingChanged: (Boolean) -> Unit = { _ ->  }): this(value = value, step = step, label = { ->
        ComposeBuilder { composectx: ComposeContext ->
            Text(verbatim = title).Compose(composectx)
            ComposeResult.ok
        }
    }, onEditingChanged = onEditingChanged) {
    }

    // MARK: - Double value initializers (no bounds)

    constructor(value: Binding<Double>, step: Double = 1.0, label: () -> View, onEditingChanged: (Boolean) -> Unit = { _ ->  }) {
        this.value = value.sref()
        this.label = ComposeBuilder.from(label)
        this.step = step
        this.minValue = null
        this.maxValue = null
        this.onIncrement = null
        this.onDecrement = null
        this.onEditingChanged = onEditingChanged
    }

    constructor(titleKey: LocalizedStringKey, value: Binding<Double>, step: Double = 1.0, onEditingChanged: (Boolean) -> Unit = { _ ->  }): this(value = value, step = step, label = { ->
        ComposeBuilder { composectx: ComposeContext ->
            Text(titleKey).Compose(composectx)
            ComposeResult.ok
        }
    }, onEditingChanged = onEditingChanged) {
    }

    constructor(titleResource: LocalizedStringResource, value: Binding<Double>, step: Double = 1.0, onEditingChanged: (Boolean) -> Unit = { _ ->  }): this(value = value, step = step, label = { ->
        ComposeBuilder { composectx: ComposeContext ->
            Text(titleResource).Compose(composectx)
            ComposeResult.ok
        }
    }, onEditingChanged = onEditingChanged) {
    }

    constructor(title: String, value: Binding<Double>, step: Double = 1.0, onEditingChanged: (Boolean) -> Unit = { _ ->  }): this(value = value, step = step, label = { ->
        ComposeBuilder { composectx: ComposeContext ->
            Text(verbatim = title).Compose(composectx)
            ComposeResult.ok
        }
    }, onEditingChanged = onEditingChanged) {
    }

    // MARK: - Int value initializers (with bounds) - Available via Fuse bridging

    @Deprecated("This API is not yet available in Skip. Consider placing it within a #if !SKIP block. You can file an issue against the owning library at https://github.com/skiptools, or see the library README for information on adding support", level = DeprecationLevel.ERROR)
    constructor(value: Binding<Int>, in_: ClosedRange<Int>, step: Int = 1, label: () -> View, onEditingChanged: (Boolean) -> Unit = { _ ->  }): this(value = value, step = step, label = label, onEditingChanged = onEditingChanged) {
    }

    @Deprecated("This API is not yet available in Skip. Consider placing it within a #if !SKIP block. You can file an issue against the owning library at https://github.com/skiptools, or see the library README for information on adding support", level = DeprecationLevel.ERROR)
    constructor(titleKey: LocalizedStringKey, value: Binding<Int>, in_: ClosedRange<Int>, step: Int = 1, onEditingChanged: (Boolean) -> Unit = { _ ->  }): this(value = value, step = step, label = { ->
        ComposeBuilder { composectx: ComposeContext ->
            Text(titleKey).Compose(composectx)
            ComposeResult.ok
        }
    }, onEditingChanged = onEditingChanged) {
    }

    @Deprecated("This API is not yet available in Skip. Consider placing it within a #if !SKIP block. You can file an issue against the owning library at https://github.com/skiptools, or see the library README for information on adding support", level = DeprecationLevel.ERROR)
    constructor(titleResource: LocalizedStringResource, value: Binding<Int>, in_: ClosedRange<Int>, step: Int = 1, onEditingChanged: (Boolean) -> Unit = { _ ->  }): this(value = value, step = step, label = { ->
        ComposeBuilder { composectx: ComposeContext ->
            Text(titleResource).Compose(composectx)
            ComposeResult.ok
        }
    }, onEditingChanged = onEditingChanged) {
    }

    @Deprecated("This API is not yet available in Skip. Consider placing it within a #if !SKIP block. You can file an issue against the owning library at https://github.com/skiptools, or see the library README for information on adding support", level = DeprecationLevel.ERROR)
    constructor(title: String, value: Binding<Int>, in_: ClosedRange<Int>, step: Int = 1, onEditingChanged: (Boolean) -> Unit = { _ ->  }): this(value = value, step = step, label = { ->
        ComposeBuilder { composectx: ComposeContext ->
            Text(verbatim = title).Compose(composectx)
            ComposeResult.ok
        }
    }, onEditingChanged = onEditingChanged) {
    }

    // MARK: - Double value initializers (with bounds) - Available via Fuse bridging

    @Deprecated("This API is not yet available in Skip. Consider placing it within a #if !SKIP block. You can file an issue against the owning library at https://github.com/skiptools, or see the library README for information on adding support", level = DeprecationLevel.ERROR)
    constructor(value: Binding<Double>, in_: ClosedRange<Double>, step: Double = 1.0, label: () -> View, onEditingChanged: (Boolean) -> Unit = { _ ->  }): this(value = value, step = step, label = label, onEditingChanged = onEditingChanged) {
    }

    @Deprecated("This API is not yet available in Skip. Consider placing it within a #if !SKIP block. You can file an issue against the owning library at https://github.com/skiptools, or see the library README for information on adding support", level = DeprecationLevel.ERROR)
    constructor(titleKey: LocalizedStringKey, value: Binding<Double>, in_: ClosedRange<Double>, step: Double = 1.0, onEditingChanged: (Boolean) -> Unit = { _ ->  }): this(value = value, step = step, label = { ->
        ComposeBuilder { composectx: ComposeContext ->
            Text(titleKey).Compose(composectx)
            ComposeResult.ok
        }
    }, onEditingChanged = onEditingChanged) {
    }

    @Deprecated("This API is not yet available in Skip. Consider placing it within a #if !SKIP block. You can file an issue against the owning library at https://github.com/skiptools, or see the library README for information on adding support", level = DeprecationLevel.ERROR)
    constructor(titleResource: LocalizedStringResource, value: Binding<Double>, in_: ClosedRange<Double>, step: Double = 1.0, onEditingChanged: (Boolean) -> Unit = { _ ->  }): this(value = value, step = step, label = { ->
        ComposeBuilder { composectx: ComposeContext ->
            Text(titleResource).Compose(composectx)
            ComposeResult.ok
        }
    }, onEditingChanged = onEditingChanged) {
    }

    @Deprecated("This API is not yet available in Skip. Consider placing it within a #if !SKIP block. You can file an issue against the owning library at https://github.com/skiptools, or see the library README for information on adding support", level = DeprecationLevel.ERROR)
    constructor(title: String, value: Binding<Double>, in_: ClosedRange<Double>, step: Double = 1.0, onEditingChanged: (Boolean) -> Unit = { _ ->  }): this(value = value, step = step, label = { ->
        ComposeBuilder { composectx: ComposeContext ->
            Text(verbatim = title).Compose(composectx)
            ComposeResult.ok
        }
    }, onEditingChanged = onEditingChanged) {
    }

    constructor(getValue: () -> Double, setValue: (Double) -> Unit, step: Double, minValue: Double?, maxValue: Double?, bridgedOnEditingChanged: ((Boolean) -> Unit)?, bridgedLabel: View) {
        this.value = Binding(get = getValue, set = setValue)
        this.label = ComposeBuilder.from { -> bridgedLabel }
        this.step = step
        this.minValue = minValue
        this.maxValue = maxValue
        this.onIncrement = null
        this.onDecrement = null
        this.onEditingChanged = bridgedOnEditingChanged
    }

    constructor(bridgedOnIncrement: (() -> Unit)?, bridgedOnDecrement: (() -> Unit)?, bridgedOnEditingChanged: ((Boolean) -> Unit)?, bridgedLabel: View) {
        this.value = null
        this.label = ComposeBuilder.from { -> bridgedLabel }
        this.step = 1.0
        this.minValue = null
        this.maxValue = null
        this.onIncrement = bridgedOnIncrement
        this.onDecrement = bridgedOnDecrement
        this.onEditingChanged = bridgedOnEditingChanged
    }

    @Composable
    override fun Render(context: ComposeContext) {
        val isEnabled = EnvironmentValues.shared.isEnabled
        val tint = (EnvironmentValues.shared._tint ?: Color.accentColor).sref()
        val tintColor = tint.colorImpl()
        val disabledColor = tintColor.copy(alpha = ContentAlpha.disabled)

        val currentValue = value?.wrappedValue ?: 0.0
        val canDecrement = isEnabled && (onDecrement != null || (minValue == null || currentValue > minValue!!))
        val canIncrement = isEnabled && (onIncrement != null || (maxValue == null || currentValue + step <= maxValue!!))

        val contentContext = context.content()

        if (EnvironmentValues.shared._labelsHidden) {
            Row(modifier = context.modifier, horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) { -> RenderButtons(canDecrement = canDecrement, canIncrement = canIncrement, tintColor = tintColor, disabledColor = disabledColor) }
        } else {
            ComposeContainer(modifier = context.modifier, fillWidth = true) { modifier ->
                Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) { ->
                    Box(modifier = Modifier.weight(1.0f)) { -> label.Compose(context = contentContext) }
                    RenderButtons(canDecrement = canDecrement, canIncrement = canIncrement, tintColor = tintColor, disabledColor = disabledColor)
                }
            }
        }
    }

    @Composable
    private fun RenderButtons(canDecrement: Boolean, canIncrement: Boolean, tintColor: androidx.compose.ui.graphics.Color, disabledColor: androidx.compose.ui.graphics.Color) {
        val borderColor = tintColor.copy(alpha = 0.3f)
        val shape = RoundedCornerShape(8.dp)

        Row(modifier = Modifier
            .height(36.dp)
            .border(width = 1.dp, color = borderColor, shape = shape)
            .clip(shape), verticalAlignment = Alignment.CenterVertically) { ->
            IconButton(onClick = { -> performDecrement() }, enabled = canDecrement, modifier = Modifier.size(36.dp)) { -> Icon(imageVector = Icons.Filled.Remove, contentDescription = "Decrement", tint = if (canDecrement) tintColor else disabledColor) }
            HorizontalDivider(modifier = Modifier.width(1.dp).height(20.dp), color = borderColor)
            IconButton(onClick = { -> performIncrement() }, enabled = canIncrement, modifier = Modifier.size(36.dp)) { -> Icon(imageVector = Icons.Filled.Add, contentDescription = "Increment", tint = if (canIncrement) tintColor else disabledColor) }
        }
    }

    private fun performIncrement() {
        onEditingChanged?.invoke(true)
        val matchtarget_0 = onIncrement
        if (matchtarget_0 != null) {
            val onIncrement = matchtarget_0
            onIncrement()
        } else if (value != null) {
            var newValue = value.wrappedValue + step
            if ((maxValue != null) && (newValue > maxValue)) {
                newValue = maxValue
            }
            value.wrappedValue = newValue
        }
        onEditingChanged?.invoke(false)
    }

    private fun performDecrement() {
        onEditingChanged?.invoke(true)
        val matchtarget_1 = onDecrement
        if (matchtarget_1 != null) {
            val onDecrement = matchtarget_1
            onDecrement()
        } else if (value != null) {
            var newValue = value.wrappedValue - step
            if ((minValue != null) && (newValue < minValue)) {
                newValue = minValue
            }
            value.wrappedValue = newValue
        }
        onEditingChanged?.invoke(false)
    }

    override fun Swift_projection(options: Int): () -> Any = Swift_projectionImpl(options)
    private external fun Swift_projectionImpl(options: Int): () -> Any

    @androidx.annotation.Keep
    companion object {
    }
}

/*
import class Foundation.Formatter
import protocol Foundation.ParseableFormatStyle

@available(iOS 16.0, macOS 13.0, watchOS 9.0, *)
@available(tvOS, unavailable)
extension Stepper {

/// Creates a stepper configured to increment or decrement a binding to a
/// value using a step value you provide, displaying its value with an
/// applied format style.
public init<F>(value: Binding<F.FormatInput>, step: F.FormatInput.Stride = 1, format: F, @ViewBuilder label: () -> any View, onEditingChanged: @escaping (Bool) -> Void = { _ in }) where F : ParseableFormatStyle, F.FormatInput : BinaryFloatingPoint, F.FormatOutput == String { fatalError() }

/// Creates a stepper configured to increment or decrement a binding to a
/// value using a step value and within a range of values you provide,
/// displaying its value with an applied format style.
public init<F>(value: Binding<F.FormatInput>, in bounds: ClosedRange<F.FormatInput>, step: F.FormatInput.Stride = 1, format: F, @ViewBuilder label: () -> any View, onEditingChanged: @escaping (Bool) -> Void = { _ in }) where F : ParseableFormatStyle, F.FormatInput : BinaryFloatingPoint, F.FormatOutput == String { fatalError() }
}

@available(iOS 16.0, macOS 13.0, watchOS 9.0, *)
@available(tvOS, unavailable)
extension Stepper {

/// Creates a stepper with a title key and configured to increment and
/// decrement a binding to a value and step amount you provide,
/// displaying its value with an applied format style.
public init<F>(_ titleKey: LocalizedStringKey, value: Binding<F.FormatInput>, step: F.FormatInput.Stride = 1, format: F, onEditingChanged: @escaping (Bool) -> Void = { _ in }) where F : ParseableFormatStyle, F.FormatInput : BinaryFloatingPoint, F.FormatOutput == String { fatalError() }

/// Creates a stepper with a title and configured to increment and
/// decrement a binding to a value and step amount you provide,
/// displaying its value with an applied format style.
public init<S, F>(_ title: S, value: Binding<F.FormatInput>, step: F.FormatInput.Stride = 1, format: F, onEditingChanged: @escaping (Bool) -> Void = { _ in }) where S : StringProtocol, F : ParseableFormatStyle, F.FormatInput : BinaryFloatingPoint, F.FormatOutput == String { fatalError() }

/// Creates a stepper instance that increments and decrements a binding to
/// a value, by a step size and within a closed range that you provide,
/// displaying its value with an applied format style.
public init<F>(_ titleKey: LocalizedStringKey, value: Binding<F.FormatInput>, in bounds: ClosedRange<F.FormatInput>, step: F.FormatInput.Stride = 1, format: F, onEditingChanged: @escaping (Bool) -> Void = { _ in }) where F : ParseableFormatStyle, F.FormatInput : BinaryFloatingPoint, F.FormatOutput == String { fatalError() }

/// Creates a stepper instance that increments and decrements a binding to
/// a value, by a step size and within a closed range that you provide,
/// displaying its value with an applied format style.
public init<S, F>(_ title: S, value: Binding<F.FormatInput>, in bounds: ClosedRange<F.FormatInput>, step: F.FormatInput.Stride = 1, format: F, onEditingChanged: @escaping (Bool) -> Void = { _ in }) where S : StringProtocol, F : ParseableFormatStyle, F.FormatInput : BinaryFloatingPoint, F.FormatOutput == String { fatalError() }
}
*/
