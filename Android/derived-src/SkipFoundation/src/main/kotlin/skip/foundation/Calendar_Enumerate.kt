// Copyright 2023–2026 Skip
// SPDX-License-Identifier: MPL-2.0
// This code is adapted from https://github.com/swiftlang/swift-foundation/blob/main/Sources/FoundationEssentials/Calendar/Calendar_Enumerate.swift which has the following license:

package skip.foundation

import skip.lib.*
import skip.lib.Set

//===----------------------------------------------------------------------===//
//
// This source file is part of the Swift.org open source project
//
// Copyright (c) 2014 - 2022 Apple Inc. and the Swift project authors
// Licensed under Apache License v2.0 with Runtime Library Exception
//
// See https://swift.org/LICENSE.txt for license information
// See https://swift.org/CONTRIBUTORS.txt for the list of Swift project authors
//
//===----------------------------------------------------------------------===//



// MARK: - Date Matching

// MARK: - Component Matchers

// MARK: - Date Verification

// MARK: - Date Adjustment

// MARK: - Helpers

internal val Calendar.Component.nextHigherUnit: Calendar.Component?
    get() {
        when (this) {
            Calendar.Component.timeZone, Calendar.Component.calendar -> return null // not really components
            Calendar.Component.era -> return null
            Calendar.Component.year, Calendar.Component.yearForWeekOfYear -> return Calendar.Component.era
            Calendar.Component.weekOfYear -> return Calendar.Component.yearForWeekOfYear
            Calendar.Component.quarter, Calendar.Component.month, Calendar.Component.dayOfYear -> return Calendar.Component.year
            Calendar.Component.day, Calendar.Component.weekOfMonth, Calendar.Component.weekdayOrdinal -> return Calendar.Component.month
            Calendar.Component.weekday -> return Calendar.Component.weekOfMonth
            Calendar.Component.hour -> return Calendar.Component.day
            Calendar.Component.minute -> return Calendar.Component.hour
            Calendar.Component.second -> return Calendar.Component.minute
            Calendar.Component.nanosecond -> return Calendar.Component.second
        }
    }

internal val Set<Calendar.Component>.highestSetUnit: Calendar.Component?
    get() {
        if (this.contains(Calendar.Component.era)) {
            return Calendar.Component.era
        }
        if (this.contains(Calendar.Component.year)) {
            return Calendar.Component.year
        }
        if (this.contains(Calendar.Component.dayOfYear)) {
            return Calendar.Component.dayOfYear
        }
        if (this.contains(Calendar.Component.quarter)) {
            return Calendar.Component.quarter
        }
        if (this.contains(Calendar.Component.month)) {
            return Calendar.Component.month
        }
        if (this.contains(Calendar.Component.day)) {
            return Calendar.Component.day
        }
        if (this.contains(Calendar.Component.hour)) {
            return Calendar.Component.hour
        }
        if (this.contains(Calendar.Component.minute)) {
            return Calendar.Component.minute
        }
        if (this.contains(Calendar.Component.second)) {
            return Calendar.Component.second
        }
        if (this.contains(Calendar.Component.weekday)) {
            return Calendar.Component.weekday
        }
        if (this.contains(Calendar.Component.weekdayOrdinal)) {
            return Calendar.Component.weekdayOrdinal
        }
        if (this.contains(Calendar.Component.weekOfMonth)) {
            return Calendar.Component.weekOfMonth
        }
        if (this.contains(Calendar.Component.weekOfYear)) {
            return Calendar.Component.weekOfYear
        }
        if (this.contains(Calendar.Component.yearForWeekOfYear)) {
            return Calendar.Component.yearForWeekOfYear
        }
        if (this.contains(Calendar.Component.nanosecond)) {
            return Calendar.Component.nanosecond
        }

        // The calendar and timeZone properties do not count as a 'highest unit set', since they are not ordered in time like the others are.
        return null
    }

internal val DateComponents.highestSetUnit: Calendar.Component?
    get() {
        // A note on performance: this approach is much faster than using key paths, which require a lot more allocations.
        if (this.era != null) {
            return Calendar.Component.era
        }
        if (this.year != null) {
            return Calendar.Component.year
        }
        if (this.dayOfYear != null) {
            return Calendar.Component.dayOfYear
        }
        if (this.quarter != null) {
            return Calendar.Component.quarter
        }
        if (this.month != null) {
            return Calendar.Component.month
        }
        if (this.day != null) {
            return Calendar.Component.day
        }
        if (this.hour != null) {
            return Calendar.Component.hour
        }
        if (this.minute != null) {
            return Calendar.Component.minute
        }
        if (this.second != null) {
            return Calendar.Component.second
        }

        // It may seem a bit odd to check in this order, but it's been a longstanding behavior.
        if (this.weekday != null) {
            return Calendar.Component.weekday
        }
        if (this.weekdayOrdinal != null) {
            return Calendar.Component.weekdayOrdinal
        }
        if (this.weekOfMonth != null) {
            return Calendar.Component.weekOfMonth
        }
        if (this.weekOfYear != null) {
            return Calendar.Component.weekOfYear
        }
        if (this.yearForWeekOfYear != null) {
            return Calendar.Component.yearForWeekOfYear
        }
        if (this.nanosecond != null) {
            return Calendar.Component.nanosecond
        }
        return null
    }

internal val DateComponents.lowestSetUnit: Calendar.Component?
    get() {
        // A note on performance: this approach is much faster than using key paths, which require a lot more allocations.
        if (this.nanosecond != null) {
            return Calendar.Component.nanosecond
        }

        // It may seem a bit odd to check in this order, but it's been a longstanding behavior.
        if (this.yearForWeekOfYear != null) {
            return Calendar.Component.yearForWeekOfYear
        }
        if (this.weekOfYear != null) {
            return Calendar.Component.weekOfYear
        }
        if (this.weekOfMonth != null) {
            return Calendar.Component.weekOfMonth
        }
        if (this.weekdayOrdinal != null) {
            return Calendar.Component.weekdayOrdinal
        }
        if (this.weekday != null) {
            return Calendar.Component.weekday
        }
        if (this.second != null) {
            return Calendar.Component.second
        }
        if (this.minute != null) {
            return Calendar.Component.minute
        }
        if (this.hour != null) {
            return Calendar.Component.hour
        }
        if (this.day != null) {
            return Calendar.Component.day
        }
        if (this.month != null) {
            return Calendar.Component.month
        }
        if (this.quarter != null) {
            return Calendar.Component.quarter
        }
        if (this.dayOfYear != null) {
            return Calendar.Component.dayOfYear
        }
        if (this.year != null) {
            return Calendar.Component.year
        }
        if (this.era != null) {
            return Calendar.Component.era
        }
        return null
    }

internal val DateComponents.setUnits: Set<Calendar.Component>
    get() {
        var units = Set<Calendar.Component>()
        if (this.era != null) {
            units.insert(Calendar.Component.era)
        }
        if (this.year != null) {
            units.insert(Calendar.Component.year)
        }
        if (this.quarter != null) {
            units.insert(Calendar.Component.quarter)
        }
        if (this.month != null) {
            units.insert(Calendar.Component.month)
        }
        if (this.day != null) {
            units.insert(Calendar.Component.day)
        }
        if (this.hour != null) {
            units.insert(Calendar.Component.hour)
        }
        if (this.minute != null) {
            units.insert(Calendar.Component.minute)
        }
        if (this.second != null) {
            units.insert(Calendar.Component.second)
        }
        if (this.weekday != null) {
            units.insert(Calendar.Component.weekday)
        }
        if (this.weekdayOrdinal != null) {
            units.insert(Calendar.Component.weekdayOrdinal)
        }
        if (this.weekOfMonth != null) {
            units.insert(Calendar.Component.weekOfMonth)
        }
        if (this.weekOfYear != null) {
            units.insert(Calendar.Component.weekOfYear)
        }
        if (this.yearForWeekOfYear != null) {
            units.insert(Calendar.Component.yearForWeekOfYear)
        }
        if (this.dayOfYear != null) {
            units.insert(Calendar.Component.dayOfYear)
        }
        if (this.nanosecond != null) {
            units.insert(Calendar.Component.nanosecond)
        }
        return units
    }

internal val DateComponents.setUnitCount: Int
    get() = this.setUnits.count

internal fun DateComponents.mismatchedUnits(comparedTo: DateComponents): Set<Calendar.Component> {
    val other = comparedTo
    var mismatched = Set<Calendar.Component>()
    if (this.era != other.era) {
        mismatched.insert(Calendar.Component.era)
    }
    if (this.year != other.year) {
        mismatched.insert(Calendar.Component.year)
    }
    if (this.quarter != other.quarter) {
        mismatched.insert(Calendar.Component.quarter)
    }
    if (this.month != other.month) {
        mismatched.insert(Calendar.Component.month)
    }
    if (this.day != other.day) {
        mismatched.insert(Calendar.Component.day)
    }
    if (this.hour != other.hour) {
        mismatched.insert(Calendar.Component.hour)
    }
    if (this.minute != other.minute) {
        mismatched.insert(Calendar.Component.minute)
    }
    if (this.second != other.second) {
        mismatched.insert(Calendar.Component.second)
    }
    if (this.weekday != other.weekday) {
        mismatched.insert(Calendar.Component.weekday)
    }
    if (this.weekdayOrdinal != other.weekdayOrdinal) {
        mismatched.insert(Calendar.Component.weekdayOrdinal)
    }
    if (this.weekOfMonth != other.weekOfMonth) {
        mismatched.insert(Calendar.Component.weekOfMonth)
    }
    if (this.weekOfYear != other.weekOfYear) {
        mismatched.insert(Calendar.Component.weekOfYear)
    }
    if (this.yearForWeekOfYear != other.yearForWeekOfYear) {
        mismatched.insert(Calendar.Component.yearForWeekOfYear)
    }
    if (this.nanosecond != other.nanosecond) {
        mismatched.insert(Calendar.Component.nanosecond)
    }
    return mismatched.sref()
}

