package org.appfair.app.fairegames.ui

import skip.lib.*


internal val <E0, E1> Tuple2<E0, E1>.key: E0
    get() = element0

internal val <E0, E1> Tuple2<E0, E1>.value: E1
    get() = element1

internal val skip.foundation.Bundle.Companion.module: skip.foundation.Bundle
    get() = _moduleBundle
private val _moduleBundle: skip.foundation.Bundle by lazy {
    skip.foundation.Bundle(_ModuleBundleLocator::class)
}
@androidx.annotation.Keep
internal class _ModuleBundleLocator {}
