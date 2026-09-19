// Licensed under the GNU General Public License v2.0 or later
// SPDX-License-Identifier: GPL-2.0-or-later

package org.appfair.app.fairegames.gamemodel

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import skip.lib.*
import skip.lib.Array

import skip.foundation.*
import skip.model.*

private val defaults = UserDefaults.standard

internal fun <T> UserDefaults.value(forKey: String, default: T): T {
    val key = forKey
    val defaultValue = default
    return (UserDefaults.standard.object_(forKey = key) as? T ?: defaultValue).sref()
}

/// Top-level preferences for the Fair Games app shell.
///
/// Per-game preferences (vibrations, level filters, hard mode, etc.) live
/// inside each game module as their own observable types — see
/// `BlockBlastPreferences`, `TetrisPreferences`, and `JewelCrushPreferences`.
@Stable
open class GamePreferences: Observable {
    /// Whether to show beta (work-in-progress) games on the front screen.
    open var showBetaGames: Boolean
        get() = _showBetaGames.wrappedValue
        set(newValue) {
            _showBetaGames.wrappedValue = newValue
            defaults.set(showBetaGames, forKey = "showBetaGames")
        }
    var _showBetaGames: skip.model.Observed<Boolean> = skip.model.Observed(defaults.value(forKey = "showBetaGames", default = false))

    /// User-customised order of game tiles on the home screen, stored as the
    /// raw-value identifier of each game. An empty array means "use the
    /// app's default order".
    open var gameOrder: Array<String>
        get() = _gameOrder.wrappedValue.sref({ this.gameOrder = it })
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
            _gameOrder.wrappedValue = newValue
            defaults.set(gameOrder, forKey = "gameOrder")
        }
    var _gameOrder: skip.model.Observed<Array<String>> = skip.model.Observed(defaults.value(forKey = "gameOrder", default = Array<String>()))

    constructor() {
    }

    @androidx.annotation.Keep
    companion object: CompanionClass() {
    }
    open class CompanionClass {
    }
}
