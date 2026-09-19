package org.appfair.app.fairegames.games.twentyfortyeight

import skip.lib.*


internal val skip.foundation.Bundle.Companion.module: skip.foundation.Bundle
    get() = _moduleBundle
private val _moduleBundle: skip.foundation.Bundle by lazy {
    skip.foundation.Bundle(_ModuleBundleLocator::class)
}
@androidx.annotation.Keep
internal class _ModuleBundleLocator {}
