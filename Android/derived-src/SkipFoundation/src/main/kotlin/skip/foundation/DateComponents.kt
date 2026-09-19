package skip.foundation

import skip.lib.*
import skip.lib.Array
import skip.lib.Set

// Copyright 2023–2026 Skip
// SPDX-License-Identifier: MPL-2.0

typealias NSDateComponents = DateComponents

@androidx.annotation.Keep
class DateComponents: Codable, MutableStruct {
    // There is no direct analogue to DateComponents in Java (other then java.util.Calendar), so we store the individual properties here

    var calendar: Calendar? = null
        get() = field.sref({ this.calendar = it })
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
            willmutate()
            field = newValue
            didmutate()
        }
    var timeZone: TimeZone? = null
        get() = field.sref({ this.timeZone = it })
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
            willmutate()
            field = newValue
            didmutate()
        }
    var era: Int? = null
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    var year: Int? = null
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    var month: Int? = null
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    var day: Int? = null
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    var dayOfYear: Int? = null
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    var hour: Int? = null
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    var minute: Int? = null
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    var second: Int? = null
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    var nanosecond: Int? = null
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    var weekday: Int? = null
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    var weekdayOrdinal: Int? = null
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    var quarter: Int? = null
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    var weekOfMonth: Int? = null
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    var weekOfYear: Int? = null
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }
    var yearForWeekOfYear: Int? = null
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }

    constructor(calendar: Calendar? = null, timeZone: TimeZone? = null, era: Int? = null, year: Int? = null, month: Int? = null, day: Int? = null, dayOfYear: Int? = null, hour: Int? = null, minute: Int? = null, second: Int? = null, nanosecond: Int? = null, weekday: Int? = null, weekdayOrdinal: Int? = null, quarter: Int? = null, weekOfMonth: Int? = null, weekOfYear: Int? = null, yearForWeekOfYear: Int? = null) {
        this.calendar = calendar
        this.timeZone = timeZone
        this.era = era
        this.year = year
        this.month = month
        this.day = day
        this.dayOfYear = dayOfYear
        this.hour = hour
        this.minute = minute
        this.second = second
        this.nanosecond = nanosecond
        this.weekday = weekday
        this.weekdayOrdinal = weekdayOrdinal
        this.quarter = quarter
        this.weekOfMonth = weekOfMonth
        this.weekOfYear = weekOfYear
        this.yearForWeekOfYear = yearForWeekOfYear
    }

    internal constructor(fromCalendar: Calendar, in_: TimeZone? = null, from: Date? = null, to: Date? = null, with: Set<Calendar.Component>? = null) {
        val calendar = fromCalendar
        val zone = in_
        val date = from
        val endDate = to
        val components = with
        val platformCal = (calendar.platformValue.clone() as java.util.Calendar).sref()

        if (date != null) {
            platformCal.time = date.platformValue
        }

        val tz = (zone ?: calendar.timeZone).sref()
        platformCal.timeZone = tz.platformValue

        if (components?.contains(Calendar.Component.timeZone) != false) {
            this.timeZone = tz
        }

        if (endDate != null) {
            val endPlatformCal = (calendar.platformValue.clone() as java.util.Calendar).sref()
            endPlatformCal.time = endDate.platformValue
            endPlatformCal.timeZone = tz.platformValue

            // Calculate differences based on components.
            if (components?.contains(Calendar.Component.era) != false) {
                this.era = endPlatformCal.get(java.util.Calendar.ERA) - platformCal.get(java.util.Calendar.ERA)
            }
            if (components?.contains(Calendar.Component.year) != false) {
                this.year = endPlatformCal.get(java.util.Calendar.YEAR) - platformCal.get(java.util.Calendar.YEAR)
            }
            if (components?.contains(Calendar.Component.yearForWeekOfYear) != false) {
                this.yearForWeekOfYear = endPlatformCal.getWeekYear() - platformCal.getWeekYear()
            }
            if (components?.contains(Calendar.Component.quarter) != false) {
                val startQuarter = (platformCal.get(java.util.Calendar.MONTH) / 3) + 1
                val endQuarter = (endPlatformCal.get(java.util.Calendar.MONTH) / 3) + 1
                this.quarter = endQuarter - startQuarter
            }
            if (components?.contains(Calendar.Component.month) != false) {
                this.month = endPlatformCal.get(java.util.Calendar.MONTH) - platformCal.get(java.util.Calendar.MONTH)
            }
            if (components?.contains(Calendar.Component.weekday) != false) {
                this.weekday = endPlatformCal.get(java.util.Calendar.DAY_OF_WEEK) - platformCal.get(java.util.Calendar.DAY_OF_WEEK)
            }
            if (components?.contains(Calendar.Component.weekdayOrdinal) != false) {
                this.weekdayOrdinal = endPlatformCal.get(java.util.Calendar.DAY_OF_WEEK_IN_MONTH) - platformCal.get(java.util.Calendar.DAY_OF_WEEK_IN_MONTH)
            }
            if (components?.contains(Calendar.Component.weekOfMonth) != false) {
                this.weekOfMonth = endPlatformCal.get(java.util.Calendar.WEEK_OF_MONTH) - platformCal.get(java.util.Calendar.WEEK_OF_MONTH)
            }
            if (components?.contains(Calendar.Component.weekOfYear) != false) {
                this.weekOfYear = endPlatformCal.get(java.util.Calendar.WEEK_OF_YEAR) - platformCal.get(java.util.Calendar.WEEK_OF_YEAR)
            }
            if (components?.contains(Calendar.Component.day) != false) {
                this.day = endPlatformCal.get(java.util.Calendar.DAY_OF_MONTH) - platformCal.get(java.util.Calendar.DAY_OF_MONTH)
            }
            if (components?.contains(Calendar.Component.dayOfYear) != false) {
                this.dayOfYear = endPlatformCal.get(java.util.Calendar.DAY_OF_YEAR) - platformCal.get(java.util.Calendar.DAY_OF_YEAR)
            }
            if (components?.contains(Calendar.Component.hour) != false) {
                this.hour = endPlatformCal.get(java.util.Calendar.HOUR_OF_DAY) - platformCal.get(java.util.Calendar.HOUR_OF_DAY)
            }
            if (components?.contains(Calendar.Component.minute) != false) {
                this.minute = endPlatformCal.get(java.util.Calendar.MINUTE) - platformCal.get(java.util.Calendar.MINUTE)
            }
            if (components?.contains(Calendar.Component.second) != false) {
                this.second = endPlatformCal.get(java.util.Calendar.SECOND) - platformCal.get(java.util.Calendar.SECOND)
            }
            if (components?.contains(Calendar.Component.nanosecond) != false) {
                val startNanos = platformCal.get(java.util.Calendar.MILLISECOND) / 1_000_000
                val endNanos = endPlatformCal.get(java.util.Calendar.MILLISECOND) / 1_000_000
                this.nanosecond = Int(endNanos - startNanos)
            }
        } else {
            // If no endDate is provided, just extract the components from the current date.
            if (components?.contains(Calendar.Component.era) != false) {
                this.era = platformCal.get(java.util.Calendar.ERA)
            }
            if (components?.contains(Calendar.Component.year) != false) {
                this.year = platformCal.get(java.util.Calendar.YEAR)
            }
            if (components?.contains(Calendar.Component.yearForWeekOfYear) != false) {
                this.yearForWeekOfYear = platformCal.getWeekYear()
            }
            if (components?.contains(Calendar.Component.quarter) != false) {
                this.quarter = (platformCal.get(java.util.Calendar.MONTH) / 3) + 1
            }
            if (components?.contains(Calendar.Component.month) != false) {
                this.month = platformCal.get(java.util.Calendar.MONTH) + 1
            }
            if (components?.contains(Calendar.Component.weekday) != false) {
                this.weekday = platformCal.get(java.util.Calendar.DAY_OF_WEEK)
            }
            if (components?.contains(Calendar.Component.weekdayOrdinal) != false) {
                this.weekdayOrdinal = platformCal.get(java.util.Calendar.DAY_OF_WEEK_IN_MONTH)
            }
            if (components?.contains(Calendar.Component.weekOfMonth) != false) {
                this.weekOfMonth = platformCal.get(java.util.Calendar.WEEK_OF_MONTH)
            }
            if (components?.contains(Calendar.Component.weekOfYear) != false) {
                this.weekOfYear = platformCal.get(java.util.Calendar.WEEK_OF_YEAR)
            }
            if (components?.contains(Calendar.Component.day) != false) {
                this.day = platformCal.get(java.util.Calendar.DAY_OF_MONTH)
            }
            if (components?.contains(Calendar.Component.dayOfYear) != false) {
                this.dayOfYear = platformCal.get(java.util.Calendar.DAY_OF_YEAR)
            }
            if (components?.contains(Calendar.Component.hour) != false) {
                this.hour = platformCal.get(java.util.Calendar.HOUR_OF_DAY)
            }
            if (components?.contains(Calendar.Component.minute) != false) {
                this.minute = platformCal.get(java.util.Calendar.MINUTE)
            }
            if (components?.contains(Calendar.Component.second) != false) {
                this.second = platformCal.get(java.util.Calendar.SECOND)
            }
            if (components?.contains(Calendar.Component.nanosecond) != false) {
                this.nanosecond = platformCal.get(java.util.Calendar.MILLISECOND) * 1_000_000
            }
        }
    }

    /// Builds a java.util.Calendar from the fields.
    internal fun createCalendarComponents(timeZone: TimeZone? = null): java.util.Calendar {
        val c: java.util.Calendar = (this.calendar?.platformValue ?: Calendar.current.platformValue).sref()
        val cal: java.util.Calendar = ((c as java.util.Calendar).clone() as java.util.Calendar).sref()
        cal.clear() // clear the time and set the fields afresh
        cal.setTimeZone((timeZone ?: this.timeZone ?: TimeZone.current).platformValue)

        this.era?.let { era ->
            cal.set(java.util.Calendar.ERA, era)
        }
        this.year?.let { year ->
            cal.set(java.util.Calendar.YEAR, year)
        }
        this.quarter?.let { quarter ->
            val monthForQuarter = (quarter - 1) * 3
            cal.set(java.util.Calendar.MONTH, monthForQuarter)
            cal.set(java.util.Calendar.DAY_OF_MONTH, 1)
        }
        this.month?.let { month ->
            // Foundation starts at 1, but Java: “Field number for get and set indicating the month. This is a calendar-specific value. The first month of the year in the Gregorian and Julian calendars is JANUARY which is 0; the last depends on the number of months in a year.”
            cal.set(java.util.Calendar.MONTH, month - 1)
        }
        val matchtarget_0 = this.yearForWeekOfYear
        if (matchtarget_0 != null) {
            val yearForWeekOfYear = matchtarget_0
            val week = Int(this.weekOfYear ?: 1)
            val dayOfWeek = Int(this.weekday ?: cal.getFirstDayOfWeek())
            cal.setWeekDate(yearForWeekOfYear, week, dayOfWeek)
        } else {
            this.weekOfYear?.let { weekOfYear ->
                // Only set the week of year if no year for week of year is defined.
                cal.set(java.util.Calendar.WEEK_OF_YEAR, weekOfYear)
            }
        }
        this.weekOfMonth?.let { weekOfMonth ->
            cal.set(java.util.Calendar.WEEK_OF_MONTH, weekOfMonth)
        }
        this.weekday?.let { weekday ->
            cal.set(java.util.Calendar.DAY_OF_WEEK, weekday)
        }
        this.weekdayOrdinal?.let { weekdayOrdinal ->
            cal.set(java.util.Calendar.DAY_OF_WEEK_IN_MONTH, weekdayOrdinal)
        }
        this.day?.let { day ->
            cal.set(java.util.Calendar.DAY_OF_MONTH, day)
        }
        this.dayOfYear?.let { dayOfYear ->
            cal.set(java.util.Calendar.DAY_OF_YEAR, dayOfYear)
        }
        this.hour?.let { hour ->
            cal.set(java.util.Calendar.HOUR_OF_DAY, hour)
        }
        this.minute?.let { minute ->
            cal.set(java.util.Calendar.MINUTE, minute)
        }
        this.second?.let { second ->
            cal.set(java.util.Calendar.SECOND, second)
        }
        this.nanosecond?.let { nanosecond ->
            cal.set(java.util.Calendar.MILLISECOND, nanosecond / 1_000_000)
        }

        return cal.sref()
    }

    val date: Date?
        get() = Date(platformValue = createCalendarComponents().getTime())

    fun setValue(value: Int?, for_: Calendar.Component) {
        val component = for_
        willmutate()
        try {
            for (unusedi in 0..0) {
                when (component) {
                    Calendar.Component.era -> this.era = value
                    Calendar.Component.year -> this.year = value
                    Calendar.Component.yearForWeekOfYear -> this.yearForWeekOfYear = value
                    Calendar.Component.quarter -> this.quarter = value
                    Calendar.Component.month -> this.month = value
                    Calendar.Component.weekday -> this.weekday = value
                    Calendar.Component.weekdayOrdinal -> this.weekdayOrdinal = value
                    Calendar.Component.weekOfMonth -> this.weekOfMonth = value
                    Calendar.Component.weekOfYear -> this.weekOfYear = value
                    Calendar.Component.day -> this.day = value
                    Calendar.Component.dayOfYear -> this.dayOfYear = value
                    Calendar.Component.hour -> this.hour = value
                    Calendar.Component.minute -> this.minute = value
                    Calendar.Component.second -> this.second = value
                    Calendar.Component.nanosecond -> this.nanosecond = value
                    Calendar.Component.calendar, Calendar.Component.timeZone -> {
                        // Do nothing
                        break
                    }
                }
            }
        } finally {
            didmutate()
        }
    }

    fun add(components: DateComponents) {
        willmutate()
        try {
            val cal = createCalendarComponents()

            components.era?.let { value ->
                cal.add(java.util.Calendar.ERA, value)
            }
            components.year?.let { value ->
                cal.add(java.util.Calendar.YEAR, value)
            }
            components.yearForWeekOfYear?.let { value ->
                //cal.add(java.util.Calendar.YEARFORWEEKOFYEAR, value)
                fatalError("Skip DateComponents.yearForWeekOfYear unsupported in Skip")
            }
            components.quarter?.let { value ->
                cal.add(java.util.Calendar.MONTH, value * 3)
            }
            components.month?.let { value ->
                cal.add(java.util.Calendar.MONTH, value)
            }
            components.weekday?.let { value ->
                cal.add(java.util.Calendar.DAY_OF_WEEK, value)
            }
            components.weekdayOrdinal?.let { value ->
                cal.add(java.util.Calendar.DAY_OF_WEEK_IN_MONTH, value)
            }
            components.weekOfMonth?.let { value ->
                cal.add(java.util.Calendar.WEEK_OF_MONTH, value)
            }
            components.weekOfYear?.let { value ->
                cal.add(java.util.Calendar.WEEK_OF_YEAR, value)
            }
            components.day?.let { value ->
                cal.add(java.util.Calendar.DATE, value) // i.e., DAY_OF_MONTH
            }
            components.hour?.let { value ->
                cal.add(java.util.Calendar.HOUR_OF_DAY, value)
            }
            components.minute?.let { value ->
                cal.add(java.util.Calendar.MINUTE, value)
            }
            components.second?.let { value ->
                cal.add(java.util.Calendar.SECOND, value)
            }
            components.nanosecond?.let { value ->
                fatalError("Skip DateComponents.nanosecond unsupported in Skip")
            }
            assignfrom(DateComponents(fromCalendar = Calendar(platformValue = cal)))
        } finally {
            didmutate()
        }
    }

    fun roll(components: DateComponents) {
        willmutate()
        try {
            val cal = createCalendarComponents()

            components.era?.let { value ->
                cal.roll(java.util.Calendar.ERA, value)
            }
            components.year?.let { value ->
                cal.roll(java.util.Calendar.YEAR, value)
            }
            components.yearForWeekOfYear?.let { value ->
                //cal.roll(java.util.Calendar.YEARFORWEEKOFYEAR, value)
                fatalError("Skip DateComponents.yearForWeekOfYear unsupported in Skip")
            }
            components.quarter?.let { value ->
                val currentMonth = cal.get(java.util.Calendar.MONTH)
                val currentQuarter = currentMonth / 3
                val newQuarter = ((currentQuarter + value) % 4 + 4) % 4
                val monthInQuarter = currentMonth % 3
                val newMonth = newQuarter * 3 + monthInQuarter
                cal.set(java.util.Calendar.MONTH, newMonth)
            }
            components.month?.let { value ->
                cal.roll(java.util.Calendar.MONTH, value)
            }
            components.weekday?.let { value ->
                cal.roll(java.util.Calendar.DAY_OF_WEEK, value)
            }
            components.weekdayOrdinal?.let { value ->
                cal.roll(java.util.Calendar.DAY_OF_WEEK_IN_MONTH, value)
            }
            components.weekOfMonth?.let { value ->
                cal.roll(java.util.Calendar.WEEK_OF_MONTH, value)
            }
            components.weekOfYear?.let { value ->
                cal.roll(java.util.Calendar.WEEK_OF_YEAR, value)
            }
            components.day?.let { value ->
                cal.roll(java.util.Calendar.DATE, value) // i.e., DAY_OF_MONTH
            }
            components.hour?.let { value ->
                cal.roll(java.util.Calendar.HOUR_OF_DAY, value)
            }
            components.minute?.let { value ->
                cal.roll(java.util.Calendar.MINUTE, value)
            }
            components.second?.let { value ->
                cal.roll(java.util.Calendar.SECOND, value)
            }
            components.nanosecond?.let { value ->
                fatalError("Skip DateComponents.nanosecond unsupported in Skip")
            }
            assignfrom(DateComponents(fromCalendar = Calendar(platformValue = cal)))
        } finally {
            didmutate()
        }
    }

    fun addValue(value: Int, for_: Calendar.Component) {
        val component = for_
        willmutate()
        try {
            val cal = createCalendarComponents()

            for (unusedi in 0..0) {
                when (component) {
                    Calendar.Component.era -> cal.add(java.util.Calendar.ERA, value)
                    Calendar.Component.year -> cal.add(java.util.Calendar.YEAR, value)
                    Calendar.Component.yearForWeekOfYear -> {
                        //cal.add(java.util.Calendar.YEARFORWEEKOFYEAR, value)
                        fatalError("Skip DateComponents.yearForWeekOfYear unsupported in Skip")
                    }
                    Calendar.Component.quarter -> cal.add(java.util.Calendar.MONTH, value * 3)
                    Calendar.Component.month -> cal.add(java.util.Calendar.MONTH, value)
                    Calendar.Component.weekday -> cal.add(java.util.Calendar.DAY_OF_WEEK, value)
                    Calendar.Component.weekdayOrdinal -> cal.add(java.util.Calendar.DAY_OF_WEEK_IN_MONTH, value)
                    Calendar.Component.weekOfMonth -> cal.add(java.util.Calendar.WEEK_OF_MONTH, value)
                    Calendar.Component.weekOfYear -> cal.add(java.util.Calendar.WEEK_OF_YEAR, value)
                    Calendar.Component.day -> cal.add(java.util.Calendar.DATE, value) // i.e., DAY_OF_MONTH
                    Calendar.Component.hour -> cal.add(java.util.Calendar.HOUR_OF_DAY, value)
                    Calendar.Component.minute -> cal.add(java.util.Calendar.MINUTE, value)
                    Calendar.Component.second -> cal.add(java.util.Calendar.SECOND, value)
                    Calendar.Component.nanosecond -> break // unsupported
                    Calendar.Component.calendar, Calendar.Component.timeZone -> {
                        // Do nothing
                        break
                    }
                    else -> break
                }
            }
            assignfrom(DateComponents(fromCalendar = Calendar(platformValue = cal)))
        } finally {
            didmutate()
        }
    }

    fun rollValue(value: Int, for_: Calendar.Component) {
        val component = for_
        willmutate()
        try {
            val cal = createCalendarComponents()

            for (unusedi in 0..0) {
                when (component) {
                    Calendar.Component.era -> cal.roll(java.util.Calendar.ERA, value)
                    Calendar.Component.year -> cal.roll(java.util.Calendar.YEAR, value)
                    Calendar.Component.yearForWeekOfYear -> {
                        //cal.roll(java.util.Calendar.YEARFORWEEKOFYEAR, value)
                        fatalError("Skip DateComponents.yearForWeekOfYear unsupported in Skip")
                    }
                    Calendar.Component.quarter -> {
                        val currentMonth = cal.get(java.util.Calendar.MONTH)
                        val currentQuarter = currentMonth / 3
                        val newQuarter = ((currentQuarter + value) % 4 + 4) % 4
                        val monthInQuarter = currentMonth % 3
                        val newMonth = newQuarter * 3 + monthInQuarter
                        cal.set(java.util.Calendar.MONTH, newMonth)
                    }
                    Calendar.Component.month -> cal.roll(java.util.Calendar.MONTH, value)
                    Calendar.Component.weekday -> cal.roll(java.util.Calendar.DAY_OF_WEEK, value)
                    Calendar.Component.weekdayOrdinal -> cal.roll(java.util.Calendar.DAY_OF_WEEK_IN_MONTH, value)
                    Calendar.Component.weekOfMonth -> cal.roll(java.util.Calendar.WEEK_OF_MONTH, value)
                    Calendar.Component.weekOfYear -> cal.roll(java.util.Calendar.WEEK_OF_YEAR, value)
                    Calendar.Component.day -> cal.roll(java.util.Calendar.DATE, value) // i.e., DAY_OF_MONTH
                    Calendar.Component.hour -> cal.roll(java.util.Calendar.HOUR_OF_DAY, value)
                    Calendar.Component.minute -> cal.roll(java.util.Calendar.MINUTE, value)
                    Calendar.Component.second -> cal.roll(java.util.Calendar.SECOND, value)
                    Calendar.Component.nanosecond -> break // unsupported
                    Calendar.Component.calendar, Calendar.Component.timeZone -> {
                        // Do nothing
                        break
                    }
                    else -> break
                }
            }
            assignfrom(DateComponents(fromCalendar = Calendar(platformValue = cal)))
        } finally {
            didmutate()
        }
    }

    fun value(for_: Calendar.Component): Int? {
        val component = for_
        when (component) {
            Calendar.Component.era -> return this.era
            Calendar.Component.year -> return this.year
            Calendar.Component.yearForWeekOfYear -> return this.yearForWeekOfYear
            Calendar.Component.month -> return this.month
            Calendar.Component.weekday -> return this.weekday
            Calendar.Component.weekdayOrdinal -> return this.weekdayOrdinal
            Calendar.Component.quarter -> return this.quarter
            Calendar.Component.weekOfMonth -> return this.weekOfMonth
            Calendar.Component.weekOfYear -> return this.weekOfYear
            Calendar.Component.day -> return this.day
            Calendar.Component.dayOfYear -> return this.dayOfYear
            Calendar.Component.hour -> return this.hour
            Calendar.Component.minute -> return this.minute
            Calendar.Component.second -> return this.second
            Calendar.Component.nanosecond -> return this.nanosecond
            Calendar.Component.calendar, Calendar.Component.timeZone -> return null
        }
    }

    val description: String
        get() {
            var strs: Array<String> = arrayOf()
            this.calendar.sref()?.let { calendar ->
                strs.append("calendar=${calendar}")
            }
            this.timeZone.sref()?.let { timeZone ->
                strs.append("timeZone=${timeZone.identifier}")
            }
            this.era?.let { era ->
                strs.append("era=${era}")
            }
            this.year?.let { year ->
                strs.append("year=${year}")
            }
            this.quarter?.let { quarter ->
                strs.append("quarter=${quarter}")
            }
            this.month?.let { month ->
                strs.append("month=${month}")
            }
            this.weekday?.let { weekday ->
                strs.append("weekday=${weekday}")
            }
            this.weekdayOrdinal?.let { weekdayOrdinal ->
                strs.append("weekdayOrdinal=${weekdayOrdinal}")
            }
            this.weekOfMonth?.let { weekOfMonth ->
                strs.append("weekOfMonth=${weekOfMonth}")
            }
            this.weekOfYear?.let { weekOfYear ->
                strs.append("weekOfYear=${weekOfYear}")
            }
            this.yearForWeekOfYear?.let { yearForWeekOfYear ->
                strs.append("yearForWeekOfYear=${yearForWeekOfYear}")
            }
            this.day?.let { day ->
                strs.append("day=${day}")
            }
            this.dayOfYear?.let { dayOfYear ->
                strs.append("dayOfYear=${dayOfYear}")
            }
            this.hour?.let { hour ->
                strs.append("hour=${hour}")
            }
            this.minute?.let { minute ->
                strs.append("minute=${minute}")
            }
            this.second?.let { second ->
                strs.append("second=${second}")
            }
            this.nanosecond?.let { nanosecond ->
                strs.append("nanosecond=${nanosecond}")
            }
            return strs.joined(separator = " ")
        }

    val isValidDate: Boolean
        get() {
            val calendar_0 = this.calendar.sref()
            if (calendar_0 == null) {
                return false
            }
            return isValidDate(in_ = calendar_0)
        }

    fun isValidDate(in_: Calendar): Boolean {
        val calendar = in_
        // TODO: re-use implementation from: https://github.com/apple/swift-foundation/blob/68c2466c613a77d6c4453f3a06496a5da79a0cb9/Sources/FoundationInternationalization/DateComponents.swift#LL327C1-L328C1

        val cal = createCalendarComponents()
        return cal.getActualMinimum(java.util.Calendar.DAY_OF_MONTH) <= cal.get(java.util.Calendar.DAY_OF_MONTH) && cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH) >= cal.get(java.util.Calendar.DAY_OF_MONTH) && cal.getActualMinimum(java.util.Calendar.MONTH) <= cal.get(java.util.Calendar.MONTH) + (if (cal.get(java.util.Calendar.MONTH) == 2) (if ((cal as? java.util.GregorianCalendar)?.isLeapYear(this.year ?: -1) == true) 0 else 1) else 0) && cal.getActualMaximum(java.util.Calendar.MONTH) >= cal.get(java.util.Calendar.MONTH) && cal.getActualMinimum(java.util.Calendar.YEAR) <= cal.get(java.util.Calendar.YEAR) && cal.getActualMaximum(java.util.Calendar.YEAR) >= cal.get(java.util.Calendar.YEAR)
    }

    private constructor(copy: MutableStruct) {
        @Suppress("NAME_SHADOWING", "UNCHECKED_CAST") val copy = copy as DateComponents
        this.calendar = copy.calendar
        this.timeZone = copy.timeZone
        this.era = copy.era
        this.year = copy.year
        this.month = copy.month
        this.day = copy.day
        this.dayOfYear = copy.dayOfYear
        this.hour = copy.hour
        this.minute = copy.minute
        this.second = copy.second
        this.nanosecond = copy.nanosecond
        this.weekday = copy.weekday
        this.weekdayOrdinal = copy.weekdayOrdinal
        this.quarter = copy.quarter
        this.weekOfMonth = copy.weekOfMonth
        this.weekOfYear = copy.weekOfYear
        this.yearForWeekOfYear = copy.yearForWeekOfYear
    }

    override var supdate: ((Any) -> Unit)? = null
    override var smutatingcount = 0
    override fun scopy(): MutableStruct = DateComponents(this as MutableStruct)

    private fun assignfrom(target: DateComponents) {
        this.calendar = target.calendar
        this.timeZone = target.timeZone
        this.era = target.era
        this.year = target.year
        this.month = target.month
        this.day = target.day
        this.dayOfYear = target.dayOfYear
        this.hour = target.hour
        this.minute = target.minute
        this.second = target.second
        this.nanosecond = target.nanosecond
        this.weekday = target.weekday
        this.weekdayOrdinal = target.weekdayOrdinal
        this.quarter = target.quarter
        this.weekOfMonth = target.weekOfMonth
        this.weekOfYear = target.weekOfYear
        this.yearForWeekOfYear = target.yearForWeekOfYear
    }

    override fun toString(): String = description

    override fun equals(other: Any?): Boolean {
        if (other !is DateComponents) return false
        return calendar == other.calendar && timeZone == other.timeZone && era == other.era && year == other.year && month == other.month && day == other.day && dayOfYear == other.dayOfYear && hour == other.hour && minute == other.minute && second == other.second && nanosecond == other.nanosecond && weekday == other.weekday && weekdayOrdinal == other.weekdayOrdinal && quarter == other.quarter && weekOfMonth == other.weekOfMonth && weekOfYear == other.weekOfYear && yearForWeekOfYear == other.yearForWeekOfYear
    }

    override fun hashCode(): Int {
        var result = 1
        result = Hasher.combine(result, calendar)
        result = Hasher.combine(result, timeZone)
        result = Hasher.combine(result, era)
        result = Hasher.combine(result, year)
        result = Hasher.combine(result, month)
        result = Hasher.combine(result, day)
        result = Hasher.combine(result, dayOfYear)
        result = Hasher.combine(result, hour)
        result = Hasher.combine(result, minute)
        result = Hasher.combine(result, second)
        result = Hasher.combine(result, nanosecond)
        result = Hasher.combine(result, weekday)
        result = Hasher.combine(result, weekdayOrdinal)
        result = Hasher.combine(result, quarter)
        result = Hasher.combine(result, weekOfMonth)
        result = Hasher.combine(result, weekOfYear)
        result = Hasher.combine(result, yearForWeekOfYear)
        return result
    }

    private enum class CodingKeys(override val rawValue: String, @Suppress("UNUSED_PARAMETER") unusedp: Nothing? = null): CodingKey, RawRepresentable<String> {
        calendar("calendar"),
        timeZone("timeZone"),
        era("era"),
        year("year"),
        month("month"),
        day("day"),
        dayOfYear("dayOfYear"),
        hour("hour"),
        minute("minute"),
        second("second"),
        nanosecond("nanosecond"),
        weekday("weekday"),
        weekdayOrdinal("weekdayOrdinal"),
        quarter("quarter"),
        weekOfMonth("weekOfMonth"),
        weekOfYear("weekOfYear"),
        yearForWeekOfYear("yearForWeekOfYear");

        @androidx.annotation.Keep
        companion object {
            fun init(rawValue: String): CodingKeys? {
                return when (rawValue) {
                    "calendar" -> CodingKeys.calendar
                    "timeZone" -> CodingKeys.timeZone
                    "era" -> CodingKeys.era
                    "year" -> CodingKeys.year
                    "month" -> CodingKeys.month
                    "day" -> CodingKeys.day
                    "dayOfYear" -> CodingKeys.dayOfYear
                    "hour" -> CodingKeys.hour
                    "minute" -> CodingKeys.minute
                    "second" -> CodingKeys.second
                    "nanosecond" -> CodingKeys.nanosecond
                    "weekday" -> CodingKeys.weekday
                    "weekdayOrdinal" -> CodingKeys.weekdayOrdinal
                    "quarter" -> CodingKeys.quarter
                    "weekOfMonth" -> CodingKeys.weekOfMonth
                    "weekOfYear" -> CodingKeys.weekOfYear
                    "yearForWeekOfYear" -> CodingKeys.yearForWeekOfYear
                    else -> null
                }
            }
        }
    }

    override fun encode(to: Encoder) {
        val container = to.container(keyedBy = CodingKeys::class)
        container.encodeIfPresent(calendar, forKey = CodingKeys.calendar)
        container.encodeIfPresent(timeZone, forKey = CodingKeys.timeZone)
        container.encodeIfPresent(era, forKey = CodingKeys.era)
        container.encodeIfPresent(year, forKey = CodingKeys.year)
        container.encodeIfPresent(month, forKey = CodingKeys.month)
        container.encodeIfPresent(day, forKey = CodingKeys.day)
        container.encodeIfPresent(dayOfYear, forKey = CodingKeys.dayOfYear)
        container.encodeIfPresent(hour, forKey = CodingKeys.hour)
        container.encodeIfPresent(minute, forKey = CodingKeys.minute)
        container.encodeIfPresent(second, forKey = CodingKeys.second)
        container.encodeIfPresent(nanosecond, forKey = CodingKeys.nanosecond)
        container.encodeIfPresent(weekday, forKey = CodingKeys.weekday)
        container.encodeIfPresent(weekdayOrdinal, forKey = CodingKeys.weekdayOrdinal)
        container.encodeIfPresent(quarter, forKey = CodingKeys.quarter)
        container.encodeIfPresent(weekOfMonth, forKey = CodingKeys.weekOfMonth)
        container.encodeIfPresent(weekOfYear, forKey = CodingKeys.weekOfYear)
        container.encodeIfPresent(yearForWeekOfYear, forKey = CodingKeys.yearForWeekOfYear)
    }

    constructor(from: Decoder) {
        val container = from.container(keyedBy = CodingKeys::class)
        this.calendar = container.decodeIfPresent(Calendar::class, forKey = CodingKeys.calendar)
        this.timeZone = container.decodeIfPresent(TimeZone::class, forKey = CodingKeys.timeZone)
        this.era = container.decodeIfPresent(Int::class, forKey = CodingKeys.era)
        this.year = container.decodeIfPresent(Int::class, forKey = CodingKeys.year)
        this.month = container.decodeIfPresent(Int::class, forKey = CodingKeys.month)
        this.day = container.decodeIfPresent(Int::class, forKey = CodingKeys.day)
        this.dayOfYear = container.decodeIfPresent(Int::class, forKey = CodingKeys.dayOfYear)
        this.hour = container.decodeIfPresent(Int::class, forKey = CodingKeys.hour)
        this.minute = container.decodeIfPresent(Int::class, forKey = CodingKeys.minute)
        this.second = container.decodeIfPresent(Int::class, forKey = CodingKeys.second)
        this.nanosecond = container.decodeIfPresent(Int::class, forKey = CodingKeys.nanosecond)
        this.weekday = container.decodeIfPresent(Int::class, forKey = CodingKeys.weekday)
        this.weekdayOrdinal = container.decodeIfPresent(Int::class, forKey = CodingKeys.weekdayOrdinal)
        this.quarter = container.decodeIfPresent(Int::class, forKey = CodingKeys.quarter)
        this.weekOfMonth = container.decodeIfPresent(Int::class, forKey = CodingKeys.weekOfMonth)
        this.weekOfYear = container.decodeIfPresent(Int::class, forKey = CodingKeys.weekOfYear)
        this.yearForWeekOfYear = container.decodeIfPresent(Int::class, forKey = CodingKeys.yearForWeekOfYear)
    }

    @androidx.annotation.Keep
    companion object: DecodableCompanion<DateComponents> {
        override fun init(from: Decoder): DateComponents = DateComponents(from = from)

        private fun CodingKeys(rawValue: String): CodingKeys? = CodingKeys.init(rawValue = rawValue)
    }
}

