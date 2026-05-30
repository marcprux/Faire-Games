package skip.foundation

import skip.lib.*
import skip.lib.Array
import skip.lib.Set

// Copyright 2023–2026 Skip
// SPDX-License-Identifier: MPL-2.0

// Needed to expose `clone`:
fun java.util.Calendar.clone(): java.util.Calendar { return this.clone() as java.util.Calendar }

@androidx.annotation.Keep
@Suppress("MUST_BE_INITIALIZED")
class Calendar: Codable, KotlinConverting<java.util.Calendar>, MutableStruct {
    internal var platformValue: java.util.Calendar
        get() = field.sref({ this.platformValue = it })
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
            willmutate()
            field = newValue
            didmutate()
        }

    constructor(platformValue: java.util.Calendar) {
        this.platformValue = platformValue
        this.locale = Locale.current
    }

    constructor(identifier: Calendar.Identifier) {
        this.platformValue = Companion.platformValue(for_ = identifier)
        this.locale = Locale.current
    }

    constructor(from: Decoder) {
        val decoder = from
        val container = decoder.singleValueContainer()
        val identifier = container.decode(Calendar.Identifier::class)
        this.platformValue = Companion.platformValue(for_ = identifier)
        this.locale = Locale.current
    }

    override fun encode(to: Encoder) {
        val encoder = to
        var container = encoder.singleValueContainer()
        container.encode(identifier)
    }

    var locale: Locale
        set(newValue) {
            willmutate()
            field = newValue
            didmutate()
        }

    var timeZone: TimeZone
        get() = TimeZone(platformValue.getTimeZone()).sref({ this.timeZone = it })
        set(newValue) {
            @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
            platformValue.setTimeZone(newValue.platformValue)
        }

    val description: String
        get() = platformValue.description

    val identifier: Calendar.Identifier
        get() {
            // TODO: non-gregorian calendar
            if (gregorianCalendar != null) {
                return Calendar.Identifier.gregorian
            } else {
                return Calendar.Identifier.iso8601
            }
        }

    internal fun toDate(): Date = Date(platformValue = platformValue.getTime())

    private val dateFormatSymbols: java.text.DateFormatSymbols
        get() = java.text.DateFormatSymbols.getInstance(locale.platformValue)

    private val gregorianCalendar: java.util.GregorianCalendar?
        get() = platformValue as? java.util.GregorianCalendar

    var firstWeekday: Int
        get() = platformValue.getFirstDayOfWeek()
        set(newValue) {
            platformValue.setFirstDayOfWeek(newValue)
        }

    var minimumDaysInFirstWeek: Int
        get() = Int(platformValue.getMinimalDaysInFirstWeek())
        set(newValue) {
            platformValue.setMinimalDaysInFirstWeek(newValue)
        }

    val eraSymbols: Array<String>
        get() = Array(dateFormatSymbols.getEras().toList())

    @Deprecated("This API is not yet available in Skip. Consider placing it within a #if !SKIP block. You can file an issue against the owning library at https://github.com/skiptools, or see the library README for information on adding support", level = DeprecationLevel.ERROR)
    val longEraSymbols: Array<String>
        get() {
            fatalError()
        }

    val monthSymbols: Array<String>
        get() {
            // The java.text.DateFormatSymbols.getInstance().getMonths() method in Java returns an array of 13 symbols because it includes both the 12 months of the year and an additional symbol
            // some documentation says the blank symbol is at index 0, but other tests show it at the end, so just pare it out
            return Array(dateFormatSymbols.getMonths().toList()).filter({ it ->
                it?.isEmpty == false
            })
        }

    val shortMonthSymbols: Array<String>
        get() {
            return Array(dateFormatSymbols.getShortMonths().toList()).filter({ it ->
                it?.isEmpty == false
            })
        }

    @Deprecated("This API is not yet available in Skip. Consider placing it within a #if !SKIP block. You can file an issue against the owning library at https://github.com/skiptools, or see the library README for information on adding support", level = DeprecationLevel.ERROR)
    val veryShortMonthSymbols: Array<String>
        get() {
            fatalError()
        }

    @Deprecated("This API is not yet available in Skip. Consider placing it within a #if !SKIP block. You can file an issue against the owning library at https://github.com/skiptools, or see the library README for information on adding support", level = DeprecationLevel.ERROR)
    val standaloneMonthSymbols: Array<String>
        get() {
            fatalError()
        }

    @Deprecated("This API is not yet available in Skip. Consider placing it within a #if !SKIP block. You can file an issue against the owning library at https://github.com/skiptools, or see the library README for information on adding support", level = DeprecationLevel.ERROR)
    val shortStandaloneMonthSymbols: Array<String>
        get() {
            fatalError()
        }

    @Deprecated("This API is not yet available in Skip. Consider placing it within a #if !SKIP block. You can file an issue against the owning library at https://github.com/skiptools, or see the library README for information on adding support", level = DeprecationLevel.ERROR)
    val veryShortStandaloneMonthSymbols: Array<String>
        get() {
            fatalError()
        }

    val weekdaySymbols: Array<String>
        get() {
            return Array(dateFormatSymbols.getWeekdays().toList()).filter({ it ->
                it?.isEmpty == false
            })
        }

    val shortWeekdaySymbols: Array<String>
        get() {
            return Array(dateFormatSymbols.getShortWeekdays().toList()).filter({ it ->
                it?.isEmpty == false
            })
        }

    @Deprecated("This API is not yet available in Skip. Consider placing it within a #if !SKIP block. You can file an issue against the owning library at https://github.com/skiptools, or see the library README for information on adding support", level = DeprecationLevel.ERROR)
    val veryShortWeekdaySymbols: Array<String>
        get() {
            fatalError()
        }

    @Deprecated("This API is not yet available in Skip. Consider placing it within a #if !SKIP block. You can file an issue against the owning library at https://github.com/skiptools, or see the library README for information on adding support", level = DeprecationLevel.ERROR)
    val standaloneWeekdaySymbols: Array<String>
        get() {
            fatalError()
        }

    @Deprecated("This API is not yet available in Skip. Consider placing it within a #if !SKIP block. You can file an issue against the owning library at https://github.com/skiptools, or see the library README for information on adding support", level = DeprecationLevel.ERROR)
    val shortStandaloneWeekdaySymbols: Array<String>
        get() {
            fatalError()
        }

    @Deprecated("This API is not yet available in Skip. Consider placing it within a #if !SKIP block. You can file an issue against the owning library at https://github.com/skiptools, or see the library README for information on adding support", level = DeprecationLevel.ERROR)
    val veryShortStandaloneWeekdaySymbols: Array<String>
        get() {
            fatalError()
        }

    @Deprecated("This API is not yet available in Skip. Consider placing it within a #if !SKIP block. You can file an issue against the owning library at https://github.com/skiptools, or see the library README for information on adding support", level = DeprecationLevel.ERROR)
    val quarterSymbols: Array<String>
        get() {
            fatalError()
        }

    @Deprecated("This API is not yet available in Skip. Consider placing it within a #if !SKIP block. You can file an issue against the owning library at https://github.com/skiptools, or see the library README for information on adding support", level = DeprecationLevel.ERROR)
    val shortQuarterSymbols: Array<String>
        get() {
            fatalError()
        }

    @Deprecated("This API is not yet available in Skip. Consider placing it within a #if !SKIP block. You can file an issue against the owning library at https://github.com/skiptools, or see the library README for information on adding support", level = DeprecationLevel.ERROR)
    val standaloneQuarterSymbols: Array<String>
        get() {
            fatalError()
        }

    @Deprecated("This API is not yet available in Skip. Consider placing it within a #if !SKIP block. You can file an issue against the owning library at https://github.com/skiptools, or see the library README for information on adding support", level = DeprecationLevel.ERROR)
    val shortStandaloneQuarterSymbols: Array<String>
        get() {
            fatalError()
        }

    val amSymbol: String
        get() = dateFormatSymbols.getAmPmStrings()[0]

    val pmSymbol: String
        get() = dateFormatSymbols.getAmPmStrings()[1]

    fun component(component: Calendar.Component, from: Date): Int {
        val date = from
        return dateComponents(setOf(component), from = date).value(for_ = component) ?: 0
    }

    fun minimumRange(of: Calendar.Component): IntRange? {
        val component = of
        val platformCal = (platformValue.clone() as java.util.Calendar).sref()

        when (component) {
            Calendar.Component.era -> {
                // Eras are internally represented as 0 and 1 (BC/AD).
                return platformCal.getMinimum(java.util.Calendar.ERA)..<(platformCal.getLeastMaximum(java.util.Calendar.ERA) + 1)
            }
            Calendar.Component.year -> {
                // Year typically starts at 1 and has no defined maximum.
                return 1..<platformCal.getMaximum(java.util.Calendar.YEAR)
            }
            Calendar.Component.quarter -> {
                // There are always 4 quarters in a year.
                return 1..<5
            }
            Calendar.Component.month -> {
                // Java's month is 0-based (0-11), but Swift expects 1-based (1-12).
                return 1..<(platformCal.getMaximum(java.util.Calendar.MONTH) + 2)
            }
            Calendar.Component.weekday -> {
                // Weekday ranges from 1 (Sunday) to 7 (Saturday).
                return platformCal.getMinimum(java.util.Calendar.DAY_OF_WEEK)..<(platformCal.getMaximum(java.util.Calendar.DAY_OF_WEEK) + 1)
            }
            Calendar.Component.weekdayOrdinal -> {
                // Weekday ordinal ranges from 1 to 4 (smallest possible maximum occurrences in a month).
                return platformCal.getMinimum(java.util.Calendar.DAY_OF_WEEK_IN_MONTH)..<(platformCal.getLeastMaximum(java.util.Calendar.DAY_OF_WEEK_IN_MONTH) + 2)
            }
            Calendar.Component.weekOfMonth -> {
                // Week of month ranges from 1 to 4 (smallest possible maximum).
                return (platformCal.getMinimum(java.util.Calendar.WEEK_OF_MONTH) + 1..<(platformCal.getLeastMaximum(java.util.Calendar.WEEK_OF_MONTH) + 2)).sref()
            }
            Calendar.Component.weekOfYear -> {
                // Week of year ranges from 1 to 52 (smallest possible maximum).
                return 1..<53
            }
            Calendar.Component.day -> {
                // getMaximum() gives the largest value that field could theoretically have.
                // getActualMaximum() gives the largest value that field actually has for the specific calendar state.

                // calendar.getActualMaximum(java.util.Calendar.DATE)
                // will return 28 because February 2023 has 28 days (it’s not a leap year).
                platformCal.set(java.util.Calendar.DAY_OF_MONTH, 1)
                clearTime(in_ = platformCal)
                platformCal.set(java.util.Calendar.MONTH, java.util.Calendar.FEBRUARY)
                platformCal.set(java.util.Calendar.YEAR, 2023)
                // Minimum days in a month is 1, maximum can vary (28 for February).
                return platformCal.getMinimum(java.util.Calendar.DATE)..<platformCal.getActualMaximum(java.util.Calendar.DATE) + 1
            }
            Calendar.Component.dayOfYear -> {
                // Day of year ranges from 1 to 365 (smallest possible maximum).
                return 1..<366
            }
            Calendar.Component.hour -> {
                // Hours are in the range 0-23.
                return platformCal.getMinimum(java.util.Calendar.HOUR_OF_DAY)..<(platformCal.getMaximum(java.util.Calendar.HOUR_OF_DAY) + 1)
            }
            Calendar.Component.minute -> {
                // Minutes are in the range 0-59.
                return platformCal.getMinimum(java.util.Calendar.MINUTE)..<(platformCal.getMaximum(java.util.Calendar.MINUTE) + 1)
            }
            Calendar.Component.second -> {
                // Seconds are in the range 0-59.
                return platformCal.getMinimum(java.util.Calendar.SECOND)..<(platformCal.getMaximum(java.util.Calendar.SECOND) + 1)
            }
            else -> return null
        }
    }

    fun maximumRange(of: Calendar.Component): IntRange? {
        val component = of
        val platformCal = (platformValue.clone() as java.util.Calendar).sref()
        when (component) {
            Calendar.Component.day -> {
                // Maximum number of days in a month can vary (e.g., 28, 29, 30, or 31 days).
                return platformCal.getMinimum(java.util.Calendar.DATE)..<(platformCal.getMaximum(java.util.Calendar.DATE) + 1)
            }
            Calendar.Component.weekOfYear, Calendar.Component.dayOfYear, Calendar.Component.weekdayOrdinal -> {
                val minRange = minimumRange(of = component)!!
                return minRange.lowerBound..<(minRange.upperBound + 1)
            }
            Calendar.Component.weekOfMonth -> {
                val minRange = minimumRange(of = component)!!
                return minRange.lowerBound..<(minRange.upperBound + 2)
            }
            else -> {
                // Maximum range is usually the same logic as minimum but could differ in some cases.
                return minimumRange(of = component)
            }
        }
    }

    fun range(of: Calendar.Component, in_: Calendar.Component, for_: Date): IntRange? {
        val smaller = of
        val larger = in_
        val date = for_
        val platformCal = (platformValue.clone() as java.util.Calendar).sref()
        platformCal.time = date.platformValue

        when (larger) {
            Calendar.Component.month -> {
                if (smaller == Calendar.Component.day) {
                    // Range of days in the current month
                    val numDays = platformCal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
                    return 1..<(numDays + 1)
                } else if (smaller == Calendar.Component.weekOfMonth) {
                    // Range of weeks in the current month
                    val numWeeks = platformCal.getActualMaximum(java.util.Calendar.WEEK_OF_MONTH)
                    return 1..<(numWeeks + 1)
                }
            }
            Calendar.Component.year -> {
                if (smaller == Calendar.Component.weekOfYear) {
                    // Range of weeks in the current year
                    // Seems like Swift always returns Maximum not for an actual date
                    val numWeeks = platformCal.getMaximum(java.util.Calendar.WEEK_OF_YEAR)
                    return 1..<(numWeeks + 1)
                } else if (smaller == Calendar.Component.day) {
                    // Range of days in the current year
                    val numDays = platformCal.getActualMaximum(java.util.Calendar.DAY_OF_YEAR)
                    return 1..<(numDays + 1)
                } else if (smaller == Calendar.Component.month) {
                    // Range of months in the current year (1 to 12)
                    return 1..<13
                }
            }
            else -> return null
        }

        return null
    }

    private fun clearTime(in_: java.util.Calendar) {
        val calendar = in_
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0) // “The HOUR_OF_DAY, HOUR and AM_PM fields are handled independently and the the resolution rule for the time of day is applied. Clearing one of the fields doesn't reset the hour of day value of this Calendar. Use set(Calendar.HOUR_OF_DAY, 0) to reset the hour value.”
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
    }

    fun dateInterval(of: Calendar.Component, for_: Date): DateInterval? {
        val component = of
        val date = for_
        var start = Date()
        var interval: Double = 0.0
        if (dateInterval(of = component, start = InOut({ start }, { start = it }), interval = InOut({ interval }, { interval = it }), for_ = date)) {
            return DateInterval(start = start, duration = interval)
        }
        return null
    }

    fun dateInterval(of: Calendar.Component, start: InOut<Date>, interval: InOut<Double>, for_: Date): Boolean {
        val component = of
        val date = for_
        val platformCal = (platformValue.clone() as java.util.Calendar).sref()
        platformCal.time = date.platformValue

        when (component) {
            Calendar.Component.second -> {
                platformCal.set(java.util.Calendar.MILLISECOND, 0)
                start.value = Date(platformValue = platformCal.time)
                interval.value = TimeInterval(1)
                return true
            }
            Calendar.Component.minute -> {
                platformCal.set(java.util.Calendar.SECOND, 0)
                platformCal.set(java.util.Calendar.MILLISECOND, 0)
                start.value = Date(platformValue = platformCal.time)
                interval.value = TimeInterval(60)
                return true
            }
            Calendar.Component.hour -> {
                platformCal.set(java.util.Calendar.MINUTE, 0)
                platformCal.set(java.util.Calendar.SECOND, 0)
                platformCal.set(java.util.Calendar.MILLISECOND, 0)
                start.value = Date(platformValue = platformCal.time)
                interval.value = TimeInterval(60 * 60)
                return true
            }
            Calendar.Component.day, Calendar.Component.dayOfYear -> {
                clearTime(in_ = platformCal)
                start.value = Date(platformValue = platformCal.time)
                interval.value = TimeInterval(24 * 60 * 60)
                return true
            }
            Calendar.Component.weekday, Calendar.Component.weekdayOrdinal -> {
                clearTime(in_ = platformCal)
                start.value = Date(platformValue = platformCal.time)
                interval.value = TimeInterval(24 * 60 * 60)
                return true
            }
            Calendar.Component.month -> {
                platformCal.set(java.util.Calendar.DAY_OF_MONTH, 1)
                clearTime(in_ = platformCal)
                start.value = Date(platformValue = platformCal.time)
                val numberOfDays = platformCal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
                interval.value = TimeInterval(numberOfDays) * TimeInterval(24 * 60 * 60)
                return true
            }
            Calendar.Component.weekOfMonth, Calendar.Component.weekOfYear -> {
                platformCal.set(java.util.Calendar.DAY_OF_WEEK, platformCal.firstDayOfWeek)
                clearTime(in_ = platformCal)
                start.value = Date(platformValue = platformCal.time)
                interval.value = TimeInterval(7 * 24 * 60 * 60)
                return true
            }
            Calendar.Component.quarter -> {
                val currentMonth = platformCal.get(java.util.Calendar.MONTH)
                val quarterStartMonth = (currentMonth / 3) * 3 // Find the first month of the current quarter
                platformCal.set(java.util.Calendar.MONTH, quarterStartMonth)
                platformCal.set(java.util.Calendar.DAY_OF_MONTH, 1)
                clearTime(in_ = platformCal)
                start.value = Date(platformValue = platformCal.time)
                val nextQuarterCal = (platformCal.clone() as java.util.Calendar).sref()
                nextQuarterCal.add(java.util.Calendar.MONTH, 3)
                val durationMillis = (nextQuarterCal.timeInMillis - platformCal.timeInMillis).sref()
                interval.value = TimeInterval(durationMillis) / 1000.0
                return true
            }
            Calendar.Component.year -> {
                platformCal.set(java.util.Calendar.MONTH, java.util.Calendar.JANUARY)
                platformCal.set(java.util.Calendar.DAY_OF_MONTH, 1)
                clearTime(in_ = platformCal)
                start.value = Date(platformValue = platformCal.time)
                val numberOfDays = platformCal.getActualMaximum(java.util.Calendar.DAY_OF_YEAR)
                interval.value = TimeInterval(numberOfDays) * TimeInterval(24 * 60 * 60)
                return true
            }
            Calendar.Component.era -> {
                platformCal.set(java.util.Calendar.YEAR, 1)
                platformCal.set(java.util.Calendar.MONTH, java.util.Calendar.JANUARY)
                platformCal.set(java.util.Calendar.DAY_OF_MONTH, 1)
                clearTime(in_ = platformCal)
                start.value = Date(platformValue = platformCal.time)
                interval.value = Double.infinity
                return true
            }
            else -> return false
        }
    }

    fun ordinality(of: Calendar.Component, in_: Calendar.Component, for_: Date): Int? {
        val smaller = of
        val larger = in_
        val date = for_
        val platformCal = (platformValue.clone() as java.util.Calendar).sref()
        platformCal.time = date.platformValue

        when (larger) {
            Calendar.Component.year -> {
                if (smaller == Calendar.Component.day) {
                    return platformCal.get(java.util.Calendar.DAY_OF_YEAR)
                } else if (smaller == Calendar.Component.weekOfYear) {
                    return platformCal.get(java.util.Calendar.WEEK_OF_YEAR)
                }
            }
            Calendar.Component.month -> {
                if (smaller == Calendar.Component.day) {
                    return platformCal.get(java.util.Calendar.DAY_OF_MONTH)
                } else if (smaller == Calendar.Component.weekOfMonth) {
                    return platformCal.get(java.util.Calendar.WEEK_OF_MONTH)
                }
            }
            else -> return null
        }
        return null
    }

    fun date(from: DateComponents): Date? {
        val components = from
        var localComponents = components.sref()
        localComponents.calendar = this
        return Date(platformValue = localComponents.createCalendarComponents(timeZone = this.timeZone).getTime())
    }

    fun date(byAdding: DateComponents, to: Date, wrappingComponents: Boolean = false): Date? {
        val components = byAdding
        val date = to
        var comps = DateComponents(fromCalendar = this, in_ = this.timeZone, from = date)
        if (!wrappingComponents) {
            comps.add(components)
        } else {
            comps.roll(components)
        }
        return date(from = comps)
    }

    fun date(byAdding: Calendar.Component, value: Int, to: Date, wrappingComponents: Boolean = false): Date? {
        val component = byAdding
        val date = to
        var comps = DateComponents(fromCalendar = this, in_ = this.timeZone, from = date)
        if (!wrappingComponents) {
            comps.addValue(value, for_ = component)
        } else {
            comps.rollValue(value, for_ = component)
        }
        return date(from = comps)
    }

    fun date(bySetting: Calendar.Component, value: Int, of: Date): Date? {
        val component = bySetting
        val date = of
        val currentValue_0 = this.dateComponents(setOf(component), from = date).value(for_ = component)
        if (currentValue_0 == null) {
            return null
        }
        if (currentValue_0 == value) {
            return date.sref()
        }

        var result: Date? = null
        var targetComponents = DateComponents()
        targetComponents.setValue(value, for_ = component)
        this.enumerateDates(startingAfter = date, matching = targetComponents, matchingPolicy = Calendar.MatchingPolicy.nextTime, repeatedTimePolicy = Calendar.RepeatedTimePolicy.first, direction = Calendar.SearchDirection.forward) { date, exactMatch, stop ->
            result = date.sref()
            stop.value = true
        }
        return result.sref()
    }

    fun date(bySettingHour: Int, minute: Int, second: Int, of: Date, matchingPolicy: Calendar.MatchingPolicy = Calendar.MatchingPolicy.nextTime, repeatedTimePolicy: Calendar.RepeatedTimePolicy = Calendar.RepeatedTimePolicy.first, direction: Calendar.SearchDirection = Calendar.SearchDirection.forward): Date? {
        val hour = bySettingHour
        val date = of
        val interval_0 = this.dateInterval(of = Calendar.Component.day, for_ = date)
        if (interval_0 == null) {
            return null
        }

        val comps = DateComponents(hour = hour, minute = minute, second = second)
        val restrictedMatchingPolicy: Calendar.MatchingPolicy
        if (matchingPolicy == Calendar.MatchingPolicy.nextTime || matchingPolicy == Calendar.MatchingPolicy.strict) {
            restrictedMatchingPolicy = matchingPolicy
        } else {
            restrictedMatchingPolicy = Calendar.MatchingPolicy.nextTime
        }
        val result_0 = this.nextDate(after = interval_0.start.addingTimeInterval(-0.5), matching = comps, matchingPolicy = restrictedMatchingPolicy, repeatedTimePolicy = repeatedTimePolicy, direction = direction)
        if (result_0 == null) {
            return null
        }

        if (result_0 < interval_0.start) {
            return this.nextDate(after = interval_0.start, matching = comps, matchingPolicy = matchingPolicy, repeatedTimePolicy = repeatedTimePolicy, direction = direction)
        } else {
            return result_0.sref()
        }
    }

    fun date(date: Date, matchesComponents: DateComponents): Boolean {
        val components = matchesComponents
        val comparedUnits: Set<Calendar.Component> = setOf(Calendar.Component.era, Calendar.Component.year, Calendar.Component.month, Calendar.Component.day, Calendar.Component.hour, Calendar.Component.minute, Calendar.Component.second, Calendar.Component.weekday, Calendar.Component.weekdayOrdinal, Calendar.Component.quarter, Calendar.Component.weekOfMonth, Calendar.Component.weekOfYear, Calendar.Component.yearForWeekOfYear, Calendar.Component.dayOfYear, Calendar.Component.nanosecond)

        val actualUnits = comparedUnits.filter l@{ unit -> return@l components.value(for_ = unit) != null }

        return components == this.dateComponents(actualUnits, from = date)
    }

    fun dateComponents(in_: TimeZone? = null, from: Date): DateComponents {
        val zone = in_
        val date = from
        return DateComponents(fromCalendar = this, in_ = zone ?: this.timeZone, from = date)
    }

    fun dateComponents(components: Set<Calendar.Component>, from: Date, to: Date): DateComponents {
        val start = from
        val end = to
        return DateComponents(fromCalendar = this, in_ = this.timeZone, from = start, to = end)
    }

    fun dateComponents(components: Set<Calendar.Component>, from: Date): DateComponents {
        val date = from
        return DateComponents(fromCalendar = this, in_ = this.timeZone, from = date, with = components)
    }

    fun startOfDay(for_: Date): Date {
        val date = for_
        // Clone the calendar to avoid mutating the original
        val platformCal = (platformValue.clone() as java.util.Calendar).sref()
        platformCal.time = date.platformValue

        // Set the time components to the start of the day
        clearTime(in_ = platformCal)

        // Return the new Date representing the start of the day
        return Date(platformValue = platformCal.time)
    }

    fun compare(date1: Date, to: Date, toGranularity: Calendar.Component): ComparisonResult {
        val date2 = to
        val component = toGranularity
        val platformCal1 = (platformValue.clone() as java.util.Calendar).sref()
        val platformCal2 = (platformValue.clone() as java.util.Calendar).sref()

        platformCal1.time = date1.platformValue
        platformCal2.time = date2.platformValue

        when (component) {
            Calendar.Component.year -> {
                val year1 = platformCal1.get(java.util.Calendar.YEAR)
                val year2 = platformCal2.get(java.util.Calendar.YEAR)
                return if (year1 < year2) ComparisonResult.orderedAscending else if (year1 > year2) ComparisonResult.orderedDescending else ComparisonResult.orderedSame
            }
            Calendar.Component.month -> {
                val year1 = platformCal1.get(java.util.Calendar.YEAR)
                val year2 = platformCal2.get(java.util.Calendar.YEAR)
                val month1 = platformCal1.get(java.util.Calendar.MONTH)
                val month2 = platformCal2.get(java.util.Calendar.MONTH)
                if (year1 != year2) {
                    return if (year1 < year2) ComparisonResult.orderedAscending else ComparisonResult.orderedDescending
                }
                return if (month1 < month2) ComparisonResult.orderedAscending else if (month1 > month2) ComparisonResult.orderedDescending else ComparisonResult.orderedSame
            }
            Calendar.Component.day -> {
                val year1 = platformCal1.get(java.util.Calendar.YEAR)
                val year2 = platformCal2.get(java.util.Calendar.YEAR)
                val day1 = platformCal1.get(java.util.Calendar.DAY_OF_YEAR)
                val day2 = platformCal2.get(java.util.Calendar.DAY_OF_YEAR)
                if (year1 != year2) {
                    return if (year1 < year2) ComparisonResult.orderedAscending else ComparisonResult.orderedDescending
                }
                return if (day1 < day2) ComparisonResult.orderedAscending else if (day1 > day2) ComparisonResult.orderedDescending else ComparisonResult.orderedSame
            }
            else -> return ComparisonResult.orderedSame
        }
    }

    fun isDate(date1: Date, equalTo: Date, toGranularity: Calendar.Component): Boolean {
        val date2 = equalTo
        val component = toGranularity
        return compare(date1, to = date2, toGranularity = component) == ComparisonResult.orderedSame
    }

    fun isDate(date1: Date, inSameDayAs: Date): Boolean {
        val date2 = inSameDayAs
        return isDate(date1, equalTo = date2, toGranularity = Calendar.Component.day)
    }

    fun isDateInToday(date: Date): Boolean {
        val platformCal = (platformValue.clone() as java.util.Calendar).sref()
        platformCal.time = Date().platformValue

        val targetCal = (platformValue.clone() as java.util.Calendar).sref()
        targetCal.time = date.platformValue

        return platformCal.get(java.util.Calendar.YEAR) == targetCal.get(java.util.Calendar.YEAR) && platformCal.get(java.util.Calendar.DAY_OF_YEAR) == targetCal.get(java.util.Calendar.DAY_OF_YEAR)
    }

    @Deprecated("This API is not yet available in Skip. Consider placing it within a #if !SKIP block. You can file an issue against the owning library at https://github.com/skiptools, or see the library README for information on adding support", level = DeprecationLevel.ERROR)
    fun isDateInYesterday(date: Date): Boolean {
        fatalError()
    }

    @Deprecated("This API is not yet available in Skip. Consider placing it within a #if !SKIP block. You can file an issue against the owning library at https://github.com/skiptools, or see the library README for information on adding support", level = DeprecationLevel.ERROR)
    fun isDateInTomorrow(date: Date): Boolean {
        fatalError()
    }

    fun isDateInWeekend(date: Date): Boolean {
        val components = dateComponents(from = date)
        return components.weekday == java.util.Calendar.SATURDAY || components.weekday == java.util.Calendar.SUNDAY
    }

    @Deprecated("This API is not yet available in Skip. Consider placing it within a #if !SKIP block. You can file an issue against the owning library at https://github.com/skiptools, or see the library README for information on adding support", level = DeprecationLevel.ERROR)
    fun dateIntervalOfWeekend(containing: Date, start: InOut<Date>, interval: InOut<Double>): Boolean {
        val date = containing
        fatalError()
    }

    @Deprecated("This API is not yet available in Skip. Consider placing it within a #if !SKIP block. You can file an issue against the owning library at https://github.com/skiptools, or see the library README for information on adding support", level = DeprecationLevel.ERROR)
    fun dateIntervalOfWeekend(containing: Date): DateInterval? {
        val date = containing
        fatalError()
    }

    @Deprecated("This API is not yet available in Skip. Consider placing it within a #if !SKIP block. You can file an issue against the owning library at https://github.com/skiptools, or see the library README for information on adding support", level = DeprecationLevel.ERROR)
    fun nextWeekend(startingAfter: Date, start: InOut<Date>, interval: InOut<Double>, direction: Calendar.SearchDirection = Calendar.SearchDirection.forward): Boolean {
        val date = startingAfter
        fatalError()
    }

    @Deprecated("This API is not yet available in Skip. Consider placing it within a #if !SKIP block. You can file an issue against the owning library at https://github.com/skiptools, or see the library README for information on adding support", level = DeprecationLevel.ERROR)
    fun nextWeekend(startingAfter: Date, direction: Calendar.SearchDirection = Calendar.SearchDirection.forward): DateInterval? {
        val date = startingAfter
        fatalError()
    }

    fun enumerateDates(startingAfter: Date, matching: DateComponents, matchingPolicy: Calendar.MatchingPolicy, repeatedTimePolicy: Calendar.RepeatedTimePolicy = Calendar.RepeatedTimePolicy.first, direction: Calendar.SearchDirection = Calendar.SearchDirection.forward, using: (Date?, Boolean, InOut<Boolean>) -> Unit) {
        val start = startingAfter
        val components = matching
        val block = using

        val STOP_EXHAUSTIVE_SEARCH_AFTER_MAX_ITERATIONS = 100 // To prevent infinite loops
        var searchingDate = start.sref()
        var previouslyReturnedMatchDate: Date? = null
        var iterations = -1

        do {
            iterations += 1
            try {
                val result = this._enumerateDatesStep(startingAfter = start, matching = components, matchingPolicy = matchingPolicy, repeatedTimePolicy = repeatedTimePolicy, direction = direction, inSearchingDate = searchingDate, previouslyReturnedMatchDate = previouslyReturnedMatchDate)

                val matchtarget_0 = result.result
                if (matchtarget_0 != null) {
                    val found = matchtarget_0
                    val (matchDate, exactMatch) = found.sref()
                    var stop = false
                    previouslyReturnedMatchDate = matchDate.sref()
                    block(matchDate, exactMatch, InOut({ stop }, { stop = it }))
                    if (stop) {
                        return
                    }
                    searchingDate = matchDate.sref()
                } else if (iterations < STOP_EXHAUSTIVE_SEARCH_AFTER_MAX_ITERATIONS) {
                    // Try again on nil result
                    searchingDate = result.newSearchDate.sref()
                    continue
                } else {
                    // Give up
                    return
                }
            } catch (error: Throwable) {
                @Suppress("NAME_SHADOWING") val error = error.aserror()
                return
            }
        } while (true)
    }

    fun nextDate(after: Date, matching: DateComponents, matchingPolicy: Calendar.MatchingPolicy, repeatedTimePolicy: Calendar.RepeatedTimePolicy = Calendar.RepeatedTimePolicy.first, direction: Calendar.SearchDirection = Calendar.SearchDirection.forward): Date? {
        val date = after
        val components = matching
        var result: Date? = null
        this.enumerateDates(startingAfter = date, matching = components, matchingPolicy = matchingPolicy, repeatedTimePolicy = repeatedTimePolicy, direction = direction) { date, exactMatch, stop ->
            result = date.sref()
            stop.value = true
        }
        return result.sref()
    }

    enum class Component {
        era,
        year,
        month,
        day,
        dayOfYear,
        hour,
        minute,
        second,
        weekday,
        weekdayOrdinal,
        quarter,
        weekOfMonth,
        weekOfYear,
        yearForWeekOfYear,
        nanosecond,
        calendar,
        timeZone;

        @androidx.annotation.Keep
        companion object {
        }
    }

    /// Calendar supports many different kinds of calendars. Each is identified by an identifier here.
    @androidx.annotation.Keep
    enum class Identifier(override val rawValue: Int, @Suppress("UNUSED_PARAMETER") unusedp: Nothing? = null): Codable, RawRepresentable<Int> {
        /// The common calendar in Europe, the Western Hemisphere, and elsewhere.
        gregorian(0),
        buddhist(1),
        chinese(2),
        coptic(3),
        ethiopicAmeteMihret(4),
        ethiopicAmeteAlem(5),
        hebrew(6),
        iso8601(7),
        indian(8),
        islamic(9),
        islamicCivil(10),
        japanese(11),
        persian(12),
        republicOfChina(13),
        islamicTabular(14),
        islamicUmmAlQura(15);

        override fun encode(to: Encoder) {
            val container = to.singleValueContainer()
            container.encode(rawValue)
        }

        @androidx.annotation.Keep
        companion object: DecodableCompanion<Calendar.Identifier> {
            override fun init(from: Decoder): Calendar.Identifier = Identifier(from = from)

            fun init(rawValue: Int): Calendar.Identifier? {
                return when (rawValue) {
                    0 -> Identifier.gregorian
                    1 -> Identifier.buddhist
                    2 -> Identifier.chinese
                    3 -> Identifier.coptic
                    4 -> Identifier.ethiopicAmeteMihret
                    5 -> Identifier.ethiopicAmeteAlem
                    6 -> Identifier.hebrew
                    7 -> Identifier.iso8601
                    8 -> Identifier.indian
                    9 -> Identifier.islamic
                    10 -> Identifier.islamicCivil
                    11 -> Identifier.japanese
                    12 -> Identifier.persian
                    13 -> Identifier.republicOfChina
                    14 -> Identifier.islamicTabular
                    15 -> Identifier.islamicUmmAlQura
                    else -> null
                }
            }
        }
    }

    enum class SearchDirection {
        forward,
        backward;

        @androidx.annotation.Keep
        companion object {
        }
    }

    enum class RepeatedTimePolicy {
        first,
        last;

        @androidx.annotation.Keep
        companion object {
        }
    }

    enum class MatchingPolicy {
        nextTime,
        nextTimePreservingSmallerComponents,
        previousTimePreservingSmallerComponents,
        strict;

        @androidx.annotation.Keep
        companion object {
        }
    }

    override fun kotlin(nocopy: Boolean): java.util.Calendar = (if (nocopy) platformValue else platformValue.clone() as java.util.Calendar).sref()

    @Suppress("MUST_BE_INITIALIZED")
    internal class SearchStepResult: MutableStruct {
        internal var result: Tuple2<Date, Boolean>? = null
            get() = field.sref({ this.result = it })
            set(newValue) {
                @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
                willmutate()
                field = newValue
                didmutate()
            }
        internal var newSearchDate: Date
            get() = field.sref({ this.newSearchDate = it })
            set(newValue) {
                @Suppress("NAME_SHADOWING") val newValue = newValue.sref()
                willmutate()
                field = newValue
                didmutate()
            }

        constructor(result: Tuple2<Date, Boolean>? = null, newSearchDate: Date) {
            this.result = result
            this.newSearchDate = newSearchDate
        }

        override var supdate: ((Any) -> Unit)? = null
        override var smutatingcount = 0
        override fun scopy(): MutableStruct = Calendar.SearchStepResult(result, newSearchDate)
    }

    internal sealed class CalendarEnumerationError: Exception(), Error {
        class DateOutOfRangeCase(val associated0: Calendar.Component, val associated1: Date): CalendarEnumerationError() {
        }
        class NotAdvancingCase(val associated0: Date, val associated1: Date): CalendarEnumerationError() {
        }
        class UnexpectedResultCase(val associated0: Calendar.Component, val associated1: Date): CalendarEnumerationError() {
        }

        @androidx.annotation.Keep
        companion object {
            fun dateOutOfRange(associated0: Calendar.Component, associated1: Date): CalendarEnumerationError = DateOutOfRangeCase(associated0, associated1)
            fun notAdvancing(associated0: Date, associated1: Date): CalendarEnumerationError = NotAdvancingCase(associated0, associated1)
            fun unexpectedResult(associated0: Calendar.Component, associated1: Date): CalendarEnumerationError = UnexpectedResultCase(associated0, associated1)
        }
    }

    private fun _enumerateDatesStep(startingAfter: Date, matching: DateComponents, matchingPolicy: Calendar.MatchingPolicy, repeatedTimePolicy: Calendar.RepeatedTimePolicy, direction: Calendar.SearchDirection, inSearchingDate: Date, previouslyReturnedMatchDate: Date?): Calendar.SearchStepResult {
        val start = startingAfter
        val matchingComponents = matching
        val searchingDate = inSearchingDate

        // Step A: Call helper method that does the searching.
        val compsToMatch = this._adjustedComponents(matchingComponents, date = searchingDate, direction = direction)
        val unadjustedMatchDate_0 = this._matchingDate(after = searchingDate, matching = compsToMatch, direction = direction, matchingPolicy = matchingPolicy, repeatedTimePolicy = repeatedTimePolicy)
        if (unadjustedMatchDate_0 == null) {
            return SearchStepResult(result = null, newSearchDate = searchingDate)
        }

        val adjustedMatchDate = this._adjustedDate(unadjustedMatchDate_0, startingAfter = start, matching = matchingComponents, adjustedMatchingComponents = compsToMatch, matchingPolicy = matchingPolicy, repeatedTimePolicy = repeatedTimePolicy, direction = direction, inSearchingDate = searchingDate, previouslyReturnedMatchDate = previouslyReturnedMatchDate)

        return adjustedMatchDate.sref()
    }

    private fun _matchingDate(after: Date, matching: DateComponents, direction: Calendar.SearchDirection, matchingPolicy: Calendar.MatchingPolicy, repeatedTimePolicy: Calendar.RepeatedTimePolicy): Date? {
        val startDate = after
        val comps = matching

        var matchedEra = true
        var searchStartDate = startDate.sref()
        val isStrictMatching = matchingPolicy == Calendar.MatchingPolicy.strict

        this.dateAfterMatchingEra(startingAt = searchStartDate, components = comps, direction = direction, matchedEra = InOut({ matchedEra }, { matchedEra = it }))?.let { result ->
            searchStartDate = result.sref()
        }

        // If era doesn't match we can just bail here instead of continuing on. A date from another era can't match. It's up to the caller to decide how to handle this mismatch.
        if (!matchedEra) {
            return null
        }

        this.dateAfterMatchingYear(startingAt = searchStartDate, components = comps, direction = direction)?.let { result ->
            searchStartDate = result.sref()
        }

        this.dateAfterMatchingQuarter(startingAt = searchStartDate, components = comps, direction = direction)?.let { result ->
            searchStartDate = result.sref()
        }

        this.dateAfterMatchingWeekOfYear(startingAt = searchStartDate, components = comps, direction = direction)?.let { result ->
            searchStartDate = result.sref()
        }

        this.dateAfterMatchingDayOfYear(startingAt = searchStartDate, components = comps, direction = direction)?.let { result ->
            searchStartDate = result.sref()
        }

        this.dateAfterMatchingMonth(startingAt = searchStartDate, components = comps, direction = direction, strictMatching = isStrictMatching)?.let { result ->
            searchStartDate = result.sref()
        }

        this.dateAfterMatchingWeekOfMonth(startingAt = searchStartDate, components = comps, direction = direction)?.let { result ->
            searchStartDate = result.sref()
        }

        this.dateAfterMatchingWeekdayOrdinal(startingAt = searchStartDate, components = comps, direction = direction)?.let { result ->
            searchStartDate = result.sref()
        }

        this.dateAfterMatchingWeekday(startingAt = searchStartDate, components = comps, direction = direction)?.let { result ->
            searchStartDate = result.sref()
        }

        this.dateAfterMatchingDay(startingAt = searchStartDate, originalStartDate = startDate, components = comps, direction = direction, isStrictMatching = isStrictMatching)?.let { result ->
            searchStartDate = result.sref()
        }

        this.dateAfterMatchingHour(startingAt = searchStartDate, originalStartDate = startDate, components = comps, direction = direction, findLastMatch = repeatedTimePolicy == Calendar.RepeatedTimePolicy.last, isStrictMatching = isStrictMatching, matchingPolicy = matchingPolicy)?.let { result ->
            searchStartDate = result.sref()
        }

        this.dateAfterMatchingMinute(startingAt = searchStartDate, components = comps, direction = direction)?.let { result ->
            searchStartDate = result.sref()
        }

        this.dateAfterMatchingSecond(startingAt = searchStartDate, originalStartDate = startDate, components = comps, direction = direction)?.let { result ->
            searchStartDate = result.sref()
        }

        return searchStartDate.sref()
    }

    private fun _matchingDate(after: Date, matching: DateComponents, inNextHighestUnit: Calendar.Component, direction: Calendar.SearchDirection, matchingPolicy: Calendar.MatchingPolicy, repeatedTimePolicy: Calendar.RepeatedTimePolicy): Date? {
        val startDate = after
        val comps = matching
        val foundRange_0 = this.dateInterval(of = inNextHighestUnit, for_ = startDate)
        if (foundRange_0 == null) {
            throw CalendarEnumerationError.dateOutOfRange(inNextHighestUnit, startDate)
        }

        var nextSearchDate: Date? = null
        var innerDirection = direction

        if (innerDirection == Calendar.SearchDirection.backward) {
            if (inNextHighestUnit == Calendar.Component.day) {
                /*
                If nextHighestUnit is day, it's a safe assumption that the highest actual set unit is the hour.
                There are cases where we're looking for a minute and/or second within the first hour of the day. If we start just at the top of the day and go backwards, we could end up missing the minute/second we're looking for.
                E.g.
                We're looking for { hour: 0, minute: 30, second: 0 } in the day before the start date 2017-05-26 07:19:50 UTC. At this point, foundRange.start would be 2017-05-26 07:00:00 UTC.
                In this case, the algorithm would do the following:
                start at 2017-05-26 07:00:00 UTC, see that the hour is already set to what we want, jump to minute.
                when checking for minute, it will cycle forward to 2017-05-26 07:30:00 +0000 but then compare to the start and see that that date is incorrect because it's in the future. Then it will cycle the date back to 2017-05-26 06:30:00 +0000.
                the matchingDate call below will exit with 2017-05-26 06:30:00 UTC and the algorithm will see that date is incorrect and reset the new search date go back a day to 2017-05-25 07:19:50 UTC. Then we get back here to this method and move the start to 2017-05-25 07:00:00 UTC and the call to matchingDate below will return 2017-05-25 06:30:00 UTC, which skips what we want (2017-05-25 07:30:00 UTC) and the algorithm eventually keeps moving further and further into the past until it exhausts itself and returns nil.
                To adjust for this scenario, we add this line below that sets nextSearchDate to the last minute of the previous day (using the above example, 2017-05-26 06:59:59 UTC), which causes the algorithm to not skip the minutes/seconds within the first hour of the previous day. (<rdar://problem/32609242>)
                */
                nextSearchDate = foundRange_0.start.addingTimeInterval(-1)

                // One caveat: if we are looking for a date within the first hour of the day (i.e. between 12 and 1 am), we want to ensure we go forwards in time to hit the exact minute and/or second we're looking for since nextSearchDate is now in the previous day. (<rdar://problem/33944890>).
                if (comps.hour == 0) {
                    innerDirection = Calendar.SearchDirection.forward
                }
            } else {
                nextSearchDate = foundRange_0.start.sref()
            }
        } else {
            nextSearchDate = foundRange_0.start.addingTimeInterval(foundRange_0.duration)
        }

        return this._matchingDate(after = nextSearchDate!!, matching = comps, direction = innerDirection, matchingPolicy = matchingPolicy, repeatedTimePolicy = repeatedTimePolicy)
    }

    private fun dateAfterMatchingEra(startingAt: Date, components: DateComponents, direction: Calendar.SearchDirection, matchedEra: InOut<Boolean>): Date? {
        val era_0 = components.era
        if (era_0 == null) {
            // Nothing to do
            return null
        }

        val dateEra = this.component(Calendar.Component.era, from = startingAt)
        if (era_0 == dateEra) {
            // Already matches
            return null
        }

        if ((direction == Calendar.SearchDirection.backward && era_0 <= dateEra) || (direction == Calendar.SearchDirection.forward && era_0 >= dateEra)) {
            var dateComp = DateComponents()
            dateComp.era = era_0
            dateComp.year = 1
            dateComp.month = 1
            dateComp.day = 1
            dateComp.hour = 0
            dateComp.minute = 0
            dateComp.second = 0
            dateComp.nanosecond = 0

            val matchtarget_1 = this.date(from = dateComp)
            if (matchtarget_1 != null) {
                val result = matchtarget_1
                val dateCompEra = this.component(Calendar.Component.era, from = result)
                if (dateCompEra != era_0) {
                    matchedEra.value = false
                }
                return result.sref()
            } else {
                matchedEra.value = false
                return null
            }
        } else {
            matchedEra.value = false
            return null
        }
    }

    private fun dateAfterMatchingYear(startingAt: Date, components: DateComponents, direction: Calendar.SearchDirection): Date? {
        val year_0 = components.year
        if (year_0 == null) {
            // Nothing to do
            return null
        }

        val dateYear = this.component(Calendar.Component.year, from = startingAt)
        val dateEra = this.component(Calendar.Component.era, from = startingAt)
        if (year_0 == dateYear) {
            // Already matches
            return null
        }
        val yearBegin_0 = this.dateIfEraHasYear(era = dateEra, year = year_0)
        if (yearBegin_0 == null) {
            // Consider if this is an error or not
            return null
        }

        if (if (direction == Calendar.SearchDirection.backward) year_0 > dateYear else year_0 < dateYear) {
            // Out of range
            throw CalendarEnumerationError.dateOutOfRange(Calendar.Component.hour, startingAt)
        }

        // We set searchStartDate to the end of the year ONLY if we know we will be trying to match anything else beyond just the year and it'll be a backwards search; otherwise, we set searchStartDate to the start of the year.
        val totalSetUnits = components.setUnitCount
        if (direction == Calendar.SearchDirection.backward && totalSetUnits > 1) {
            val foundRange_1 = this.dateInterval(of = Calendar.Component.year, for_ = yearBegin_0)
            if (foundRange_1 == null) {
                // Out of range
                throw CalendarEnumerationError.dateOutOfRange(Calendar.Component.year, yearBegin_0)
            }

            return yearBegin_0.addingTimeInterval(foundRange_1.duration - 1)
        } else {
            return yearBegin_0.sref()
        }
    }

    private fun dateAfterMatchingQuarter(startingAt: Date, components: DateComponents, direction: Calendar.SearchDirection): Date? {
        val quarter_0 = components.quarter
        if (quarter_0 == null) {
            // Nothing to do
            return null
        }
        val foundRange_2 = this.dateInterval(of = Calendar.Component.year, for_ = startingAt)
        if (foundRange_2 == null) {
            throw CalendarEnumerationError.dateOutOfRange(Calendar.Component.year, startingAt)
        }

        if (quarter_0 < 1 || quarter_0 > 4) {
            // Out of range
            throw CalendarEnumerationError.dateOutOfRange(Calendar.Component.quarter, startingAt)
        }

        if (direction == Calendar.SearchDirection.backward) {
            var count = 4
            var quarterBegin = foundRange_2.start.addingTimeInterval(foundRange_2.duration - 1)
            while (count != quarter_0 && count > 0) {
                val quarterRange_0 = this.dateInterval(of = Calendar.Component.quarter, for_ = quarterBegin)
                if (quarterRange_0 == null) {
                    // Out of range
                    throw CalendarEnumerationError.dateOutOfRange(Calendar.Component.quarter, quarterBegin)
                }

                quarterBegin = quarterRange_0.start.addingTimeInterval(-quarterRange_0.duration)
                count -= 1
            }

            return quarterBegin.sref()
        } else {
            var count = 1
            var quarterBegin = foundRange_2.start.sref()
            while (count != quarter_0 && count < 5) {
                val quarterRange_1 = this.dateInterval(of = Calendar.Component.quarter, for_ = quarterBegin)
                if (quarterRange_1 == null) {
                    // Out of range
                    throw CalendarEnumerationError.dateOutOfRange(Calendar.Component.quarter, quarterBegin)
                }

                // Move past this quarter. The is the first instant of the next quarter.
                quarterBegin = quarterRange_1.start.addingTimeInterval(quarterRange_1.duration)
                count += 1
            }

            return quarterBegin.sref()
        }
    }

    private fun dateAfterMatchingMonth(startingAt: Date, components: DateComponents, direction: Calendar.SearchDirection, strictMatching: Boolean): Date? {
        val month_0 = components.month
        if (month_0 == null) {
            // Nothing to do
            return null
        }

        if (month_0 < 1 || month_0 > 12) {
            // Out of range
            throw CalendarEnumerationError.dateOutOfRange(Calendar.Component.month, startingAt)
        }

        // After this point, result is at least startDate.
        var result = startingAt.sref()
        var dateMonth = this.component(Calendar.Component.month, from = result)
        if (month_0 != dateMonth) {
            do {
                val lastResult = result.sref()
                val foundRange_3 = this.dateInterval(of = Calendar.Component.month, for_ = result)
                if (foundRange_3 == null) {
                    throw CalendarEnumerationError.dateOutOfRange(Calendar.Component.month, result)
                }

                var duration = foundRange_3.duration
                if (direction == Calendar.SearchDirection.backward) {
                    val numMonth = this.component(Calendar.Component.month, from = foundRange_3.start)
                    if (numMonth == 3 && (this.identifier == Calendar.Identifier.gregorian || this.identifier == Calendar.Identifier.buddhist || this.identifier == Calendar.Identifier.japanese || this.identifier == Calendar.Identifier.iso8601 || this.identifier == Calendar.Identifier.republicOfChina)) {
                        // Take it back 3 days so we land in february. That is, March has 31 days, and Feb can have 28 or 29, so to ensure we get to either Feb 1 or 2, we need to take it back 3 days.
                        duration -= 86400 * 3
                    } else {
                        // Take it back a day.
                        duration -= 86400
                    }

                    // So we can go backwards in time.
                    duration *= -1
                }

                val searchDate = foundRange_3.start.addingTimeInterval(duration)
                dateMonth = component(Calendar.Component.month, from = searchDate)
                result = searchDate.sref()

                verifyAdvancingResult(result, previous = lastResult, direction = direction)
            } while (month_0 != dateMonth)
        }

        return result.sref()
    }

    private fun dateAfterMatchingWeekOfYear(startingAt: Date, components: DateComponents, direction: Calendar.SearchDirection): Date? {
        val weekOfYear_0 = components.weekOfYear
        if (weekOfYear_0 == null) {
            // Nothing to do
            return null
        }

        var dateWeekOfYear = this.component(Calendar.Component.weekOfYear, from = startingAt)
        if (weekOfYear_0 == dateWeekOfYear) {
            // Already matches
            return null
        }

        if (weekOfYear_0 < 1 || weekOfYear_0 > 53) {
            // Out of range
            throw CalendarEnumerationError.dateOutOfRange(Calendar.Component.month, startingAt)
        }

        // After this point, the result is at least the start date.
        var result = startingAt.sref()
        do {
            // Used to check if we are not advancing the week of year.
            val lastResult = result.sref()
            val foundRange_4 = this.dateInterval(of = Calendar.Component.weekOfYear, for_ = result)
            if (foundRange_4 == null) {
                // Out of range
                throw CalendarEnumerationError.dateOutOfRange(Calendar.Component.weekOfYear, result)
            }

            if (direction == Calendar.SearchDirection.backward) {
                val searchDate = foundRange_4.start.addingTimeInterval(-foundRange_4.duration)
                dateWeekOfYear = this.component(Calendar.Component.weekOfYear, from = searchDate)
                result = searchDate.sref()
            } else {
                val searchDate = foundRange_4.start.addingTimeInterval(foundRange_4.duration)
                dateWeekOfYear = this.component(Calendar.Component.weekOfYear, from = searchDate)
                result = searchDate.sref()
            }

            this.verifyAdvancingResult(result, previous = lastResult, direction = direction)
        } while (weekOfYear_0 != dateWeekOfYear)

        return result.sref()
    }

    private fun dateAfterMatchingWeekOfMonth(startingAt: Date, components: DateComponents, direction: Calendar.SearchDirection): Date? {
        val weekOfMonth_0 = components.weekOfMonth
        if (weekOfMonth_0 == null) {
            // Nothing to do
            return null
        }

        var dateWeekOfMonth = this.component(Calendar.Component.weekOfMonth, from = startingAt)
        if (weekOfMonth_0 == dateWeekOfMonth) {
            // Already matches
            return null
        }

        // After this point, result is at least startDate.
        var result = startingAt.sref()
        do {
            val foundRange_5 = this.dateInterval(of = Calendar.Component.weekOfMonth, for_ = result)
            if (foundRange_5 == null) {
                // Out of range
                throw CalendarEnumerationError.dateOutOfRange(Calendar.Component.weekOfMonth, result)
            }

            // We need to advance or rewind to the next week.
            // This is simple when we can jump by a whole week interval, but there are complications around WoM == 1 because it can start on any day of the week. Jumping forward/backward by a whole week can miss it.
            //
            // A week 1 which starts on any day but Sunday contains days from week 5 of the previous month, e.g.
            //
            //        June 2018
            //   Su Mo Tu We Th Fr Sa
            //                   1  2
            //    3  4  5  6  7  8  9
            //   10 11 12 13 14 15 16
            //   17 18 19 20 21 22 23
            //   24 25 26 27 28 29 30
            //
            // Week 1 of June 2018 starts on Friday; any day before that is week 5 of May.
            // We can jump by a week interval if we're not looking for WoM == 2 or we're not close.
            var advanceDaily = weekOfMonth_0 == 1 // we're looking for WoM == 1
            if (direction == Calendar.SearchDirection.backward) {
                // Last week/earlier this week is week 1.
                advanceDaily = advanceDaily && dateWeekOfMonth <= 2
            } else {
                // We need to be careful if it's the last week of the month. We can't assume what number week that would be, so figure it out.
                val range = this.range(of = Calendar.Component.weekOfMonth, in_ = Calendar.Component.month, for_ = result) ?: 0..<Int.max
                advanceDaily = advanceDaily && dateWeekOfMonth == (range.upperBound - range.lowerBound)
            }

            var tempSearchDate: Date? = null
            if (!advanceDaily) {
                // We can jump directly to next/last week. There's just one further wrinkle here when doing so backwards: due to DST, it's possible that this week is longer/shorter than last week.
                // That means that if we rewind by womInv (the length of this week), we could completely skip last week, or end up not at its first instant.
                //
                // We can avoid this by not rewinding by womInv, but by going directly to the start.
                if (direction == Calendar.SearchDirection.backward) {
                    // Any instant before foundRange.start is last week
                    val lateLastWeek = foundRange_5.start.addingTimeInterval(-1)
                    val matchtarget_2 = this.dateInterval(of = Calendar.Component.weekOfMonth, for_ = lateLastWeek)
                    if (matchtarget_2 != null) {
                        val interval = matchtarget_2
                        tempSearchDate = interval.start.sref()
                    } else {
                        // Fall back to below case
                        advanceDaily = true
                    }
                } else {
                    // Skipping forward doesn't have these DST concerns, since foundRange already represents the length of this week.
                    tempSearchDate = foundRange_5.start.addingTimeInterval(foundRange_5.duration)
                }
            }

            // This is a separate condition because it represents a "possible" fallthrough from above.
            if (advanceDaily) {
                var today = foundRange_5.start.sref()
                while (this.component(Calendar.Component.day, from = today) != 1) {
                    val matchtarget_3 = this.date(byAdding = Calendar.Component.day, value = if (direction == Calendar.SearchDirection.backward) -1 else 1, to = today)
                    if (matchtarget_3 != null) {
                        val next = matchtarget_3
                        today = next.sref()
                    } else {
                        break
                    }
                }

                tempSearchDate = today.sref()
            }

            dateWeekOfMonth = this.component(Calendar.Component.weekOfMonth, from = tempSearchDate!!)
            this.verifyAdvancingResult(tempSearchDate, previous = result, direction = direction)
            result = tempSearchDate.sref()
        } while (weekOfMonth_0 != dateWeekOfMonth)

        return result.sref()
    }

    private fun dateAfterMatchingDayOfYear(startingAt: Date, components: DateComponents, direction: Calendar.SearchDirection): Date? {
        val dayOfYear_0 = components.dayOfYear
        if (dayOfYear_0 == null) {
            // Nothing to do
            return null
        }

        var dateDayOfYear = this.component(Calendar.Component.dayOfYear, from = startingAt)
        if (dayOfYear_0 == dateDayOfYear) {
            // Already matches
            return null
        }

        val year = components.year ?: this.component(Calendar.Component.year, from = startingAt)
        val isLeapYear = (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
        if (dayOfYear_0 < 1 || dayOfYear_0 > (if (isLeapYear) 366 else 365)) {
            // Out of range
            throw CalendarEnumerationError.dateOutOfRange(Calendar.Component.dayOfYear, startingAt)
        }

        var result = startingAt.sref()
        do {
            val lastResult = result.sref()
            val foundRange_6 = this.dateInterval(of = Calendar.Component.dayOfYear, for_ = result)
            if (foundRange_6 == null) {
                throw CalendarEnumerationError.dateOutOfRange(Calendar.Component.dayOfYear, result)
            }

            if (direction == Calendar.SearchDirection.backward) {
                val searchDate = foundRange_6.start.addingTimeInterval(-foundRange_6.duration)
                dateDayOfYear = this.component(Calendar.Component.dayOfYear, from = searchDate)
                result = searchDate.sref()
            } else {
                val searchDate = foundRange_6.start.addingTimeInterval(foundRange_6.duration)
                dateDayOfYear = this.component(Calendar.Component.dayOfYear, from = searchDate)
                result = searchDate.sref()
            }

            this.verifyAdvancingResult(result, previous = lastResult, direction = direction)
        } while (dayOfYear_0 != dateDayOfYear)

        return result.sref()
    }

    private fun dateAfterMatchingDay(startingAt: Date, originalStartDate: Date, components: DateComponents, direction: Calendar.SearchDirection, isStrictMatching: Boolean): Date? {
        val day_0 = components.day
        if (day_0 == null) {
            // Nothing to do
            return null
        }

        var result = startingAt.sref()
        val month = components.month
        var dateDay = this.component(Calendar.Component.day, from = startingAt)
        if (month != null && direction == Calendar.SearchDirection.backward) {
            // Are we in the right month already?  If we are and backwards is set, we should move to the beginning of the last day of the month and work backwards.
            this.dateInterval(of = Calendar.Component.month, for_ = result)?.let { foundRange ->
                val tempSearchDate = foundRange.end.addingTimeInterval(-1)
                // Check the order to make sure we didn't jump ahead of the start date.
                if (tempSearchDate > originalStartDate) {
                    // We went too far ahead. Just go back to using the start date as our upper bound.
                    result = originalStartDate.sref()
                } else {
                    this.dateInterval(of = Calendar.Component.day, for_ = tempSearchDate)?.let { anotherFoundRange ->
                        result = anotherFoundRange.start.sref()
                        dateDay = this.component(Calendar.Component.day, from = result)
                    }
                }
            }
        }

        if (day_0 != dateDay) {
            // The condition below keeps us from blowing past a month day by day to find a day which does not exist.
            // e.g. trying to find the 30th of February starting in January would go to March 30th if we don't stop here
            val originalMonth = this.component(Calendar.Component.month, from = result)
            var advancedPastWholeMonth = false
            var lastFoundDuration: Double = 0.0

            do {
                val foundRange_7 = this.dateInterval(of = Calendar.Component.day, for_ = result)
                if (foundRange_7 == null) {
                    throw CalendarEnumerationError.dateOutOfRange(Calendar.Component.day, result)
                }

                // Used to track if we went past end of month below.
                lastFoundDuration = foundRange_7.duration

                // We need to either advance or rewind by a day.
                // * Advancing to tomorrow is relatively simple: get the start of today and get the length of that day — then, advance by that length
                // * Rewinding to the start of yesterday is more complicated: the length of today is not necessarily the length of yesterday if DST transitions are involved:
                //   * Today can have 25 hours: if we rewind 25 hours from the start of today, we'll skip yesterday altogether
                //   * Today can have 24 hours: if we rewind 24 hours from the start of today, we might skip yesterday if it had 23 hours, or end up at the wrong time if it had 25
                //   * Today can have 23 hours: if we rewind 23 hours from the start of today, we'll end up at the wrong time yesterday
                //
                // We need to account for DST by ensuring we rewind to exactly the time we want.

                val tempSearchDate: Date
                if (direction == Calendar.SearchDirection.backward) {
                    // Any time prior to dayBegin is yesterday. Since we want to rewind to the start of yesterday, do that directly.
                    val lateYesterday = foundRange_7.start.addingTimeInterval(-1)

                    // Now we can get the exact moment that yesterday began on.
                    // It shouldn't be possible to fail to find this interval, but if that somehow happens, we can try to fall back to the simple but wrong method.
                    val matchtarget_4 = this.dateInterval(of = Calendar.Component.day, for_ = lateYesterday)
                    if (matchtarget_4 != null) {
                        val yesterdayRange = matchtarget_4
                        tempSearchDate = yesterdayRange.start.sref()
                    } else {
                        // This fallback is only really correct when today and yesterday have the same length.
                        // Again, it shouldn't be possible to hit this case.
                        tempSearchDate = foundRange_7.start.addingTimeInterval(-foundRange_7.duration)
                    }
                } else {
                    // This is always correct to do since we are using today's length on today - there can't be a mismatch.
                    tempSearchDate = foundRange_7.end.sref()
                }

                dateDay = this.component(Calendar.Component.day, from = tempSearchDate)
                val dateMonth = this.component(Calendar.Component.month, from = tempSearchDate)
                this.verifyAdvancingResult(tempSearchDate, previous = result, direction = direction)
                result = tempSearchDate.sref()

                if (abs(dateMonth - originalMonth) >= 2) {
                    advancedPastWholeMonth = true
                    break
                }
            } while (day_0 != dateDay)

            // If we blew past a month in its entirety, roll back by a day to the very end of the month.
            if ((advancedPastWholeMonth)) {
                result = result.addingTimeInterval(-lastFoundDuration)
            }
        } else {
            // When the search date matches the day we're looking for, we still need to clear the lower components in case they are not part of the components we're looking for.
            this.dateInterval(of = Calendar.Component.day, for_ = result)?.let { foundRange ->
                result = foundRange.start.sref()
            }
        }

        return result.sref()
    }

    private fun dateAfterMatchingWeekdayOrdinal(startingAt: Date, components: DateComponents, direction: Calendar.SearchDirection): Date? {
        val weekdayOrdinal_0 = components.weekdayOrdinal
        if (weekdayOrdinal_0 == null) {
            // Nothing to do
            return null
        }

        var dateWeekdayOrdinal = this.component(Calendar.Component.weekdayOrdinal, from = startingAt)
        if (weekdayOrdinal_0 == dateWeekdayOrdinal) {
            // Nothing to do
            return null
        }

        // After this point, result is at least startDate.
        var result = startingAt.sref()
        do {
            val lastResult = result.sref()
            val foundRange_8 = this.dateInterval(of = Calendar.Component.weekdayOrdinal, for_ = result)
            if (foundRange_8 == null) {
                // Out of range
                throw CalendarEnumerationError.dateOutOfRange(Calendar.Component.weekdayOrdinal, result)
            }

            if (direction == Calendar.SearchDirection.backward) {
                val searchDate = foundRange_8.start.addingTimeInterval(-foundRange_8.duration)
                dateWeekdayOrdinal = this.component(Calendar.Component.weekdayOrdinal, from = searchDate)
                result = searchDate.sref()
            } else {
                val searchDate = foundRange_8.start.addingTimeInterval(foundRange_8.duration)
                dateWeekdayOrdinal = this.component(Calendar.Component.weekdayOrdinal, from = searchDate)
                result = searchDate.sref()
            }

            this.verifyAdvancingResult(result, previous = lastResult, direction = direction)
        } while (weekdayOrdinal_0 != dateWeekdayOrdinal)
        val weekday_0 = components.weekday
        if (weekday_0 == null) {
            // Skip weekday
            return result.sref()
        }

        // Once we're here, it means we found a day with the correct ordinality, but it may not be the specific weekday we're also looking for (e.g. we found the 2nd Thursday of the month when we're looking for the 2nd Friday).
        var dateWeekday = this.component(Calendar.Component.weekday, from = result)
        if (weekday_0 == dateWeekday) {
            // Already matches
            return result.sref()
        }

        // Start result over (it is reset in all paths below).
        if (dateWeekday > weekday_0) {
            // We're past the weekday we want. Go to the beginning of the week.
            // We use startDate again here, not result.
            val matchtarget_5 = this.dateInterval(of = Calendar.Component.weekdayOrdinal, for_ = startingAt)
            if (matchtarget_5 != null) {
                val foundRange = matchtarget_5
                result = foundRange.start.sref()
                val units: Set<Calendar.Component> = setOf(Calendar.Component.weekday, Calendar.Component.weekdayOrdinal)
                val startingDayWeekdayComps = this.dateComponents(units, from = result)
                val weekday_1 = startingDayWeekdayComps.weekday
                if (weekday_1 == null) {
                    // This should not be possible
                    throw CalendarEnumerationError.unexpectedResult(Calendar.Component.weekdayOrdinal, result)
                }
                val weekdayOrdinal_1 = startingDayWeekdayComps.weekdayOrdinal
                if (weekdayOrdinal_1 == null) {
                    // This should not be possible
                    throw CalendarEnumerationError.unexpectedResult(Calendar.Component.weekdayOrdinal, result)
                }
                dateWeekday = weekday_1
                dateWeekdayOrdinal = weekdayOrdinal_1
            } else {
                // We need to have a value here - use the start date.
                result = startingAt.sref()
            }
        } else {
            result = startingAt.sref()
        }

        while ((weekday_0 != dateWeekday) || (weekdayOrdinal_0 != dateWeekdayOrdinal)) {
            // Now iterate through each day of the week until we find the specific weekday we're looking for.
            val lastResult = result.sref()
            val foundRange_9 = this.dateInterval(of = Calendar.Component.day, for_ = result)
            if (foundRange_9 == null) {
                throw CalendarEnumerationError.unexpectedResult(Calendar.Component.day, result)
            }

            val nextDay = foundRange_9.start.addingTimeInterval(foundRange_9.duration)
            val units: Set<Calendar.Component> = setOf(Calendar.Component.weekday, Calendar.Component.weekdayOrdinal)
            val nextDayComponents = this.dateComponents(units, from = nextDay)
            val weekday_2 = nextDayComponents.weekday
            if (weekday_2 == null) {
                // This should not be possible.
                throw CalendarEnumerationError.unexpectedResult(Calendar.Component.weekday, nextDay)
            }
            val weekdayOrdinal_2 = nextDayComponents.weekdayOrdinal
            if (weekdayOrdinal_2 == null) {
                // This should not be possible.
                throw CalendarEnumerationError.unexpectedResult(Calendar.Component.weekday, nextDay)
            }

            dateWeekday = weekday_2
            dateWeekdayOrdinal = weekdayOrdinal_2
            result = nextDay.sref()

            this.verifyAdvancingResult(result, previous = lastResult, direction = direction)
        }

        return result.sref()
    }

    private fun dateAfterMatchingWeekday(startingAt: Date, components: DateComponents, direction: Calendar.SearchDirection): Date? {
        val weekday_3 = components.weekday
        if (weekday_3 == null) {
            // Nothing to do
            return null
        }

        // NOTE: This differs from the weekday check in weekdayOrdinal because weekday is meant to be ambiguous and can be set without setting the ordinality.
        // e.g. inquiries like "find the next Tuesday after 2017-06-01" or "find every Wednesday before 2012-12-25"
        var dateWeekday = this.component(Calendar.Component.weekday, from = startingAt)
        if (weekday_3 == dateWeekday) {
            // Already matches
            return null
        }

        if (weekday_3 < 1 || weekday_3 > 7) {
            // Out of range
            throw CalendarEnumerationError.dateOutOfRange(Calendar.Component.weekday, startingAt)
        }

        // After this point, result is at least startDate.
        var result = startingAt.sref()
        do {
            val foundRange_10 = this.dateInterval(of = Calendar.Component.weekday, for_ = result)
            if (foundRange_10 == null) {
                throw CalendarEnumerationError.dateOutOfRange(Calendar.Component.weekday, result)
            }

            // We need to either advance or rewind by a day.
            // * Advancing to tomorrow is relatively simple: get the start of today and get the length of that day — then, advance by that length
            // * Rewinding to the start of yesterday is more complicated: the length of today is not necessarily the length of yesterday if DST transitions are involved:
            //   * Today can have 25 hours: if we rewind 25 hours from the start of today, we'll skip yesterday altogether
            //   * Today can have 24 hours: if we rewind 24 hours from the start of today, we might skip yesterday if it had 23 hours, or end up at the wrong time if it had 25
            //   * Today can have 23 hours: if we rewind 23 hours from the start of today, we'll end up at the wrong time yesterday
            //
            // We need to account for DST by ensuring we rewind to exactly the time we want.
            val tempSearchDate: Date
            if (direction == Calendar.SearchDirection.backward) {
                val lateYesterday = foundRange_10.start.addingTimeInterval(-1)
                val matchtarget_6 = this.dateInterval(of = Calendar.Component.day, for_ = lateYesterday)
                if (matchtarget_6 != null) {
                    val anotherFoundRange = matchtarget_6
                    tempSearchDate = anotherFoundRange.start.sref()
                } else {
                    // This fallback is only really correct when today and yesterday have the same length.
                    // Again, it shouldn't be possible to hit this case.
                    tempSearchDate = foundRange_10.start.addingTimeInterval(-foundRange_10.duration)
                }
            } else {
                // This is always correct to do since we are using today's length on today — there can't be a mismatch.
                tempSearchDate = foundRange_10.start.addingTimeInterval(foundRange_10.duration)
            }

            dateWeekday = this.component(Calendar.Component.weekday, from = tempSearchDate)
            this.verifyAdvancingResult(tempSearchDate, previous = result, direction = direction)
            result = tempSearchDate.sref()
        } while (weekday_3 != dateWeekday)

        return result.sref()
    }

    private fun dateAfterMatchingHour(startingAt: Date, originalStartDate: Date, components: DateComponents, direction: Calendar.SearchDirection, findLastMatch: Boolean, isStrictMatching: Boolean, matchingPolicy: Calendar.MatchingPolicy): Date? {
        val startDate = startingAt
        val hour_0 = components.hour
        if (hour_0 == null) {
            // Nothing to do
            return null
        }

        if (hour_0 < 0 || hour_0 > 23) {
            // Out of range
            throw CalendarEnumerationError.dateOutOfRange(Calendar.Component.hour, startingAt)
        }

        var result = startDate.sref()
        var adjustedSearchStartDate = false

        var dateHour = this.component(Calendar.Component.hour, from = result)

        // The loop below here takes care of advancing forward in the case of an hour mismatch, taking DST into account.
        // However, it does not take into account a unique circumstance: searching for hour 0 of a day on a day that has no hour 0 due to DST.
        //
        // America/Sao_Paulo, for instance, is a time zone which has DST at midnight -- an instant after 11:59:59 PM can become 1:00 AM, which is the start of the new day:
        //
        //            2018-11-03                      2018-11-04
        //    ┌─────11:00 PM (GMT-3)─────┐ │ ┌ ─ ─ 12:00 AM (GMT-3)─ ─ ─┐ ┌─────1:00 AM (GMT-2) ─────┐
        //    │                          │ │ |                          │ │                          │
        //    └──────────────────────────┘ │ └ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─┘ └▲─────────────────────────┘
        //                                            Nonexistent           └── Start of Day
        //
        // The issue with this specifically is that parts of the rewinding algorithm that handle overshooting rewind to the start of the day to search again (or alternatively, adjusting higher components tends to send us to the start of the day).
        // This doesn't work when the day starts past the time we're looking for if we're looking for hour 0.
        //
        // If we're not matching strictly, we need to check whether we're already a non-strict match and not an overshoot.
        if (hour_0 == 0 && !isStrictMatching) {
            this.dateInterval(of = Calendar.Component.day, for_ = result)?.let { foundRange ->
                val dayBegin = foundRange.start.sref()
                val firstHourOfTheDay = this.component(Calendar.Component.hour, from = dayBegin)
                if (firstHourOfTheDay != 0 && dateHour == firstHourOfTheDay) {
                    // We're at the start of the day; it's just not hour 0.
                    // We have a candidate match. We can modify that match based on the actual options we need to set.

                    if (matchingPolicy == Calendar.MatchingPolicy.nextTime) {
                        // We don't need to preserve the smallest components. We can wipe them out.
                        // Note that we rewind to the start of the hour by rewinding to the start of the day -- normally we'd want to rewind to the start of _this_ hour in case there were a difference in a first/last scenario (repeated hour DST transition), but we can't both be missing hour 0 _and_ be the second hour in a repeated transition.
                        result = dayBegin.sref()
                    } else if (matchingPolicy == Calendar.MatchingPolicy.nextTimePreservingSmallerComponents || matchingPolicy == Calendar.MatchingPolicy.previousTimePreservingSmallerComponents) {
                        // We want to preserve any currently set smaller units (hour and minute), so don't do anything.
                        // If we need to match the previous time (i.e. go back an hour), that adjustment will be made elsewhere, in the generalized isForwardDST adjustment in the main loop.
                    }

                    // Avoid making any further adjustments again.
                    adjustedSearchStartDate = true
                }
            }
        }

        // This is a real mismatch and not due to hour 0 being missing.
        // NOTE: The behavior of generalized isForwardDST checking depends on the behavior of this loop!
        //        Right now, in the general case, this loop stops iteration _before_ a forward DST transition. If that changes, please take a look at the isForwardDST code for when `beforeTransition = false` and adjust as necessary.
        if (hour_0 != dateHour && !adjustedSearchStartDate) {
            do {
                val lastResult = result.sref()
                val foundRange_11 = this.dateInterval(of = Calendar.Component.hour, for_ = result)
                if (foundRange_11 == null) {
                    throw CalendarEnumerationError.dateOutOfRange(Calendar.Component.hour, result)
                }

                val prevDateHour = dateHour
                val tempSearchDate = foundRange_11.start.addingTimeInterval(foundRange_11.duration)

                dateHour = this.component(Calendar.Component.hour, from = tempSearchDate)

                // Sometimes we can get into a position where the next hour is also equal to hour (as in we hit a backwards DST change). In this case, we could be at the first time this hour occurs. If we want the next time the hour is technically the same (as in we need to go to the second time this hour occurs), we check to see if we hit a backwards DST change.
                val possibleBackwardDSTDate = foundRange_11.start.addingTimeInterval(foundRange_11.duration * 2.0)
                val secondDateHour = this.component(Calendar.Component.hour, from = possibleBackwardDSTDate)

                if (((dateHour - prevDateHour) == 2) || (prevDateHour == 23 && dateHour == 1)) {
                    // We've hit a forward DST transition.
                    dateHour = dateHour - 1
                    result = foundRange_11.start.sref()
                } else if ((secondDateHour == dateHour) && findLastMatch) {
                    // If we're not trying to find the last match, just pass on the match we already found.
                    // We've hit a backwards DST transition.
                    result = possibleBackwardDSTDate.sref()
                } else {
                    result = tempSearchDate.sref()
                }

                adjustedSearchStartDate = true

                // Verify the hour value (it changes even if the result does not).
                if ((result == lastResult && prevDateHour == dateHour)) {
                    // We are not advancing. Bail out of the loop.
                    throw CalendarEnumerationError.notAdvancing(result, lastResult)
                }
            } while (hour_0 != dateHour)

            if (direction == Calendar.SearchDirection.backward && originalStartDate < result) {
                // We've gone into the future when we were supposed to go into the past. We're ahead by a day.
                this.date(byAdding = Calendar.Component.day, value = -1, to = result)?.let { rolledBack ->
                    result = rolledBack.sref()
                }

                // Check hours again to see if they match (they may not because of DST change already being handled implicitly by dateByAddingUnit:).
                dateHour = this.component(Calendar.Component.hour, from = result)
                if ((dateHour - hour_0) == 1) {
                    // Detecting a DST transition.
                    // We have moved an hour ahead of where we want to be so we go back 1 hour to readjust.
                    this.date(byAdding = Calendar.Component.hour, value = -1, to = result)?.let { adjusted ->
                        result = adjusted.sref()
                    }
                } else if ((hour_0 - dateHour) == 1) {
                    // <rdar://problem/31051045>
                    // This is a weird special edge case that only gets hit when you're searching backwards and move past a forward (skip an hour) DST transition.
                    // We're not at a DST transition but the hour of our date got moved because the previous day had a DST transition.
                    // So we're an hour before where we want to be. We move an hour ahead to correct and get back to where we need to be.
                    this.date(byAdding = Calendar.Component.hour, value = 1, to = result)?.let { adjusted ->
                        result = adjusted.sref()
                    }
                }
            }
        }

        if (findLastMatch) {
            this.dateInterval(of = Calendar.Component.hour, for_ = result)?.let { foundRange ->
                // Rewind forward/back hour-by-hour until we get to a different hour. A loop here is necessary because not all DST transitions are only an hour long.
                var next = foundRange.start.sref()
                var nextHour = hour_0
                while (nextHour == hour_0) {
                    result = next.sref()
                    val offset = (if (direction == Calendar.SearchDirection.backward) -1 else 1)
                    val matchtarget_7 = this.date(byAdding = Calendar.Component.hour, value = offset, to = next)
                    if (matchtarget_7 != null) {
                        val nextDate = matchtarget_7
                        next = nextDate.sref()
                    } else {
                        break
                    }
                    nextHour = this.component(Calendar.Component.hour, from = next)
                }
            }
        }

        if (!adjustedSearchStartDate) {
            // This applies if we didn't hit the above cases to adjust the search start date, i.e. the hour already matches the start hour and either:
            // 1) We're not looking to match the "last" (repeated) hour in a DST transition (regardless of whether we're in a DST transition), or
            // 2) We are looking to match that hour, but we're not in that DST transition.
            //
            // In either case, we need to clear the lower components in case they are not part of the components we're looking for.
            this.dateInterval(of = Calendar.Component.hour, for_ = result)?.let { foundRange ->
                result = foundRange.start.sref()
                adjustedSearchStartDate = true
            }
        }

        return result.sref()
    }

    private fun dateAfterMatchingMinute(startingAt: Date, components: DateComponents, direction: Calendar.SearchDirection): Date? {
        val minute_0 = components.minute
        if (minute_0 == null) {
            // Nothing to do
            return null
        }

        if (minute_0 < 0 || minute_0 > 60) {
            // Out of range
            throw CalendarEnumerationError.dateOutOfRange(Calendar.Component.minute, startingAt)
        }

        var result = startingAt.sref()
        var dateMinute = this.component(Calendar.Component.minute, from = result)
        if (minute_0 != dateMinute) {
            do {
                val foundRange_12 = this.dateInterval(of = Calendar.Component.minute, for_ = result)
                if (foundRange_12 == null) {
                    throw CalendarEnumerationError.dateOutOfRange(Calendar.Component.minute, result)
                }

                val tempSearchDate = foundRange_12.start.addingTimeInterval(foundRange_12.duration)
                dateMinute = this.component(Calendar.Component.minute, from = tempSearchDate)

                this.verifyUnequalResult(tempSearchDate, previous = result, startingAt = startingAt, components = components, direction = direction, strict = null)

                result = tempSearchDate.sref()
            } while (minute_0 != dateMinute)
        } else {
            // When the search date matches the minute we're looking for, we need to clear the lower components in case they are not part of the components we're looking for.
            this.dateInterval(of = Calendar.Component.minute, for_ = result)?.let { foundRange ->
                result = foundRange.start.sref()
            }
        }

        return result.sref()
    }

    private fun dateAfterMatchingSecond(startingAt: Date, originalStartDate: Date, components: DateComponents, direction: Calendar.SearchDirection): Date? {
        val startDate = startingAt
        val second_0 = components.second
        if (second_0 == null) {
            // Nothing to do
            return null
        }

        if (second_0 < 0 || second_0 > 60) {
            // Out of range
            throw CalendarEnumerationError.dateOutOfRange(Calendar.Component.minute, startingAt)
        }

        // After this point, result is at least startDate.
        var result = startDate.sref()
        var dateSecond = this.component(Calendar.Component.second, from = result)
        if (second_0 != dateSecond) {
            do {
                val foundRange_13 = this.dateInterval(of = Calendar.Component.second, for_ = result)
                if (foundRange_13 == null) {
                    throw CalendarEnumerationError.dateOutOfRange(Calendar.Component.second, result)
                }

                val tempSearchDate = foundRange_13.start.addingTimeInterval(foundRange_13.duration)
                dateSecond = this.component(Calendar.Component.second, from = tempSearchDate)
                this.verifyUnequalResult(tempSearchDate, previous = result, startingAt = startDate, components = components, direction = direction, strict = null)
                result = tempSearchDate.sref()
            } while (second_0 != dateSecond)

            if (originalStartDate < result) {
                if (direction == Calendar.SearchDirection.backward) {
                    // We've gone into the future when we were supposed to go into the past.
                    // There are multiple times a day where the seconds repeat. Need to take that into account.
                    val originalStartSecond = this.component(Calendar.Component.second, from = originalStartDate)
                    if (dateSecond > originalStartSecond) {
                        val new_0 = this.date(byAdding = Calendar.Component.minute, value = -1, to = result)
                        if (new_0 == null) {
                            return null
                        }
                        result = new_0.sref()
                    }
                } else {
                    // This handles the case where dateSecond started ahead of second, so doing the above landed us in the next minute. If minute is not set, we are fine. But if minute is set, then we are now in the wrong minute and we have to readjust. <rdar://problem/31098131>
                    var searchStartMin = this.component(Calendar.Component.minute, from = result)
                    components.minute?.let { minute ->
                        if (searchStartMin > minute) {
                            // We've gone ahead of where we needed to be.
                            do {
                                val foundRange_14 = this.dateInterval(of = Calendar.Component.minute, for_ = result)
                                if (foundRange_14 == null) {
                                    throw CalendarEnumerationError.dateOutOfRange(Calendar.Component.minute, result)
                                }

                                val tempSearchDate = foundRange_14.start.addingTimeInterval(-foundRange_14.duration)
                                searchStartMin = this.component(Calendar.Component.minute, from = tempSearchDate)
                                this.verifyAdvancingResult(tempSearchDate, previous = result, direction = direction)
                                result = tempSearchDate.sref()
                            } while (searchStartMin > minute)
                        }
                    }
                }
            }
        } else {
            val anotherFoundRange_0 = this.dateInterval(of = Calendar.Component.second, for_ = result)
            if (anotherFoundRange_0 == null) {
                throw CalendarEnumerationError.dateOutOfRange(Calendar.Component.second, result)
            }
            result = anotherFoundRange_0.start.sref()
            // Now searchStartDate <= startDate.
        }

        return result.sref()
    }

    private fun verifyAdvancingResult(next: Date, previous: Date, direction: Calendar.SearchDirection) {
        if ((direction == Calendar.SearchDirection.forward && next <= previous) || (direction == Calendar.SearchDirection.backward && next >= previous)) {
            // We are not advancing. Bail out of the loop.
            throw CalendarEnumerationError.notAdvancing(next, previous)
        }
    }

    private fun verifyUnequalResult(next: Date, previous: Date, startingAt: Date, components: DateComponents, direction: Calendar.SearchDirection, strict: Boolean?) {
        if ((next == previous)) {
            // We are not advancing. Bail out of the loop.
            throw CalendarEnumerationError.notAdvancing(next, previous)
        }
    }

    private fun _adjustedDate(unadjustedMatchDate: Date, startingAfter: Date, allowStartDate: Boolean = false, matching: DateComponents, adjustedMatchingComponents: DateComponents, matchingPolicy: Calendar.MatchingPolicy, repeatedTimePolicy: Calendar.RepeatedTimePolicy, direction: Calendar.SearchDirection, inSearchingDate: Date, previouslyReturnedMatchDate: Date?): Calendar.SearchStepResult {
        val start = startingAfter
        val matchingComponents = matching
        val compsToMatch = adjustedMatchingComponents

        var exactMatch = true
        var isLeapDay = false
        var searchingDate = inSearchingDate.sref()

        // NOTE: Several comments reference "isForwardDST" as a way to relate areas in forward DST handling.
        var isForwardDST = false
        val matchDate_0 = this._adjustedDateForMismatches(start = start, searchingDate = searchingDate, matchDate = unadjustedMatchDate, matchingComponents = matchingComponents, compsToMatch = compsToMatch, direction = direction, matchingPolicy = matchingPolicy, repeatedTimePolicy = repeatedTimePolicy, isForwardDST = InOut({ isForwardDST }, { isForwardDST = it }), isExactMatch = InOut({ exactMatch }, { exactMatch = it }), isLeapDay = InOut({ isLeapDay }, { isLeapDay = it }))
        if (matchDate_0 == null) {

            // Try again with a bumped up date.
            this.bumpedDateUpToNextHigherUnitInComponents(searchingDate, matchingComponents, direction, null)?.let { newSearchingDate ->
                searchingDate = newSearchingDate.sref()
            }

            return SearchStepResult(result = null, newSearchDate = searchingDate)
        }

        // Check the components to see if they match what was desired.
        val matchResult = this.date(matchDate_0, containsMatchingComponents = matchingComponents)
        val mismatchedUnits = matchResult.element0
        val dateMatchesComps = matchResult.element1
        if (dateMatchesComps && !exactMatch) {
            exactMatch = true
        }

        // Bump up the next highest unit.
        this.bumpedDateUpToNextHigherUnitInComponents(searchingDate, matchingComponents, direction, matchDate_0)?.let { newSearchingDate ->
            searchingDate = newSearchingDate.sref()
        }

        // Nanosecond and quarter mismatches are not considered inexact.
        val notAnExactMatch = (dateMatchesComps == false) && (mismatchedUnits.contains(Calendar.Component.nanosecond) == false) && (mismatchedUnits.contains(Calendar.Component.quarter) == false)
        if (notAnExactMatch) {
            exactMatch = false
        }

        val order: ComparisonResult
        if (previouslyReturnedMatchDate != null) {
            order = previouslyReturnedMatchDate.compare(matchDate_0)
        } else {
            order = start.compare(matchDate_0)
        }

        if (((direction == Calendar.SearchDirection.backward && order == ComparisonResult.orderedAscending) || (direction == Calendar.SearchDirection.forward && order == ComparisonResult.orderedDescending)) && mismatchedUnits.contains(Calendar.Component.nanosecond) == false) {
            // We've gone ahead when we should have gone backwards or we went in the past when we were supposed to move forwards.
            // Normally, it's sufficient to set matchDate to nil and move on with the existing searching date. However, the searching date has been bumped forward by the next highest date component, which isn't always correct.
            // Specifically, if we're in a type of transition when the highest date component can repeat between now and the next highest date component, then we need to move forward by less.
            //
            // This can happen during a "fall back" DST transition in which an hour is repeated:
            //
            //   ┌─────1:00 PDT─────┐ ┌─────1:00 PST─────┐
            //   │                  │ │                  │
            //   └───────────▲───▲──┘ └───────────▲──────┘
            //               │   │                │
            //               |   |                valid
            //               │   last match/start
            //               │
            //               matchDate
            //
            // Instead of jumping ahead by a whole day, we can jump ahead by an hour to the next appropriate match. `valid` here would be the result found by searching with matchLast.
            // In this case, before giving up on the current match date, we need to adjust the next search date with this information.
            //
            // Currently, the case we care most about is adjusting for DST, but we might need to expand this to handle repeated months in some calendars.
            if (compsToMatch.highestSetUnit == Calendar.Component.hour) {
                val matchHour = this.component(Calendar.Component.hour, from = matchDate_0)
                val hourAdjustment = if (direction == Calendar.SearchDirection.backward) -3600.0 else 3600.0
                val potentialNextMatchDate = matchDate_0.addingTimeInterval(hourAdjustment)
                val potentialMatchHour = this.component(Calendar.Component.hour, from = potentialNextMatchDate)

                if (matchHour == potentialMatchHour) {
                    // We're in a DST transition where the hour repeats. Use this date as the next search date.
                    searchingDate = potentialNextMatchDate.sref()
                }
            }

            // In any case, return nil.
            return SearchStepResult(result = null, newSearchDate = searchingDate)
        }

        // At this point, the date we matched is allowable unless:
        // 1) It's not an exact match AND
        // 2) We require an exact match (strict) OR
        // 3) It's not an exact match but not because we found a DST hour or day that doesn't exist in the month (i.e. it's truly the wrong result)
        val allowInexactMatchingDueToTimeSkips = isForwardDST || isLeapDay
        if (!exactMatch && (matchingPolicy == Calendar.MatchingPolicy.strict || !allowInexactMatchingDueToTimeSkips)) {
            return SearchStepResult(result = null, newSearchDate = searchingDate)
        }

        // If we get a result that is exactly the same as the start date, skip.
        if (!allowStartDate && (order == ComparisonResult.orderedSame)) {
            return SearchStepResult(result = null, newSearchDate = searchingDate)
        }

        return SearchStepResult(result = Tuple2(matchDate_0.sref(), exactMatch), newSearchDate = searchingDate)
    }

    private fun _adjustedDateForMismatches(start: Date, searchingDate: Date, matchDate: Date, matchingComponents: DateComponents, compsToMatch: DateComponents, direction: Calendar.SearchDirection, matchingPolicy: Calendar.MatchingPolicy, repeatedTimePolicy: Calendar.RepeatedTimePolicy, isForwardDST: InOut<Boolean>, isExactMatch: InOut<Boolean>, isLeapDay: InOut<Boolean>): Date? {

        // Set up some default answers for the out args.
        isForwardDST.value = false
        isExactMatch.value = true
        isLeapDay.value = false

        // Use this to find the units that don't match and then those units become the bailedUnit.
        val result = this.date(matchDate, containsMatchingComponents = compsToMatch)
        val mismatchedUnits = result.element0
        val dateMatchesComps = result.element1

        // Skip trying to correct nanoseconds or quarters. We don't want differences in these two (partially unsupported) fields to cause mismatched dates. <rdar://problem/30229247> / <rdar://problem/30229506>
        val nanoSecondsMismatch = mismatchedUnits.contains(Calendar.Component.nanosecond)
        val quarterMismatch = mismatchedUnits.contains(Calendar.Component.quarter)
        if (nanoSecondsMismatch || quarterMismatch) {
            // Everything else is fine. Just return this date.
            return matchDate.sref()
        }

        // Check if *only* the hour is mismatched.
        if (mismatchedUnits.count == 1 && mismatchedUnits.contains(Calendar.Component.hour)) {
            this._adjustedDateForMismatchedHour(matchDate = matchDate, compsToMatch = compsToMatch, matchingPolicy = matchingPolicy, repeatedTimePolicy = repeatedTimePolicy, isExactMatch = InOut({ isExactMatch.value }, { isExactMatch.value = it }))?.let { resultAdjustedForDST ->
                isForwardDST.value = true
                // Skip the next set of adjustments too.
                return resultAdjustedForDST.sref()
            }
        }

        if (dateMatchesComps) {
            // Everything is already fine. Just return the value.
            return matchDate.sref()
        }
        val bailedUnit_0 = mismatchedUnits.highestSetUnit
        if (bailedUnit_0 == null) {
            // There was no real mismatch, apparently. Return the matchDate.
            return matchDate.sref()
        }

        var nextHighestUnit = bailedUnit_0.nextHigherUnit
        if (nextHighestUnit == null) {
            // Just return the original date in this case.
            return matchDate.sref()
        }

        // Corrective measures.
        if (bailedUnit_0 == Calendar.Component.era) {
            nextHighestUnit = Calendar.Component.year
        } else if (bailedUnit_0 == Calendar.Component.year || bailedUnit_0 == Calendar.Component.yearForWeekOfYear) {
            nextHighestUnit = bailedUnit_0
        }

        // We need to check for leap* situations.
        val isGregorianCalendar = this.identifier == Calendar.Identifier.gregorian
        if (nextHighestUnit == Calendar.Component.year) {
            val desiredMonth = compsToMatch.month
            val desiredDay = compsToMatch.day

            if (!((desiredMonth != null) && (desiredDay != null))) {
                // Just return the original date in this case.
                return matchDate.sref()
            }

            // Here is where we handle the other leap* situations (e.g. leap years in Gregorian calendar, leap months in Hebrew calendar).
            val monthMismatched = mismatchedUnits.contains(Calendar.Component.month)
            val dayMismatched = mismatchedUnits.contains(Calendar.Component.day)
            if (monthMismatched || dayMismatched) {
                // Force unwrap nextHighestUnit because it must be set here (or we should have gone down the path).
                return this._adjustedDateForMismatchedLeapMonthOrDay(start = start, searchingDate = searchingDate, matchDate = matchDate, matchingComponents = matchingComponents, compsToMatch = compsToMatch, nextHighestUnit = nextHighestUnit!!, direction = direction, matchingPolicy = matchingPolicy, repeatedTimePolicy = repeatedTimePolicy, isExactMatch = InOut({ isExactMatch.value }, { isExactMatch.value = it }), isLeapDay = InOut({ isLeapDay.value }, { isLeapDay.value = it }))
            }

            // Last opportunity here is just to return the original match date.
            return matchDate.sref()
        } else if (nextHighestUnit == Calendar.Component.month && isGregorianCalendar && this.component(Calendar.Component.month, from = matchDate) == 2) {
            // We've landed here because we couldn't find the date we wanted in February, because it doesn't exist (e.g. Feb 31st or 30th, or 29th on a non-leap-year).
            // matchDate is the end of February, so we need to advance to the beginning of March.
            this.dateInterval(of = Calendar.Component.month, for_ = matchDate)?.let { february ->
                var adjustedDate = february.start.addingTimeInterval(february.duration)
                if (matchingPolicy == Calendar.MatchingPolicy.nextTimePreservingSmallerComponents) {
                    // Advancing has caused us to lose all smaller units, so if we're looking to preserve them we need to add them back.
                    val smallerUnits = this.dateComponents(setOf(Calendar.Component.hour, Calendar.Component.minute, Calendar.Component.second, Calendar.Component.nanosecond), from = start)
                    val matchtarget_8 = this.date(byAdding = smallerUnits, to = adjustedDate)
                    if (matchtarget_8 != null) {
                        val tempSearchDate = matchtarget_8
                        adjustedDate = tempSearchDate.sref()
                    } else {
                        return null
                    }
                }

                // This isn't strictly a leap day, just a day that doesn't exist.
                isLeapDay.value = true
                isExactMatch.value = false
                return adjustedDate.sref()
            }

            return matchDate.sref()
        } else {
            // Go to the top of the next period for the next highest unit of the one that bailed.
            // Force unwrap nextHighestUnit because it must be set here (or we should have gone down the leapMonthMismatch path).
            return this._matchingDate(after = searchingDate, matching = matchingComponents, inNextHighestUnit = nextHighestUnit!!, direction = direction, matchingPolicy = matchingPolicy, repeatedTimePolicy = repeatedTimePolicy)
        }
    }

    private fun _adjustedDateForMismatchedHour(matchDate: Date, compsToMatch: DateComponents, matchingPolicy: Calendar.MatchingPolicy, repeatedTimePolicy: Calendar.RepeatedTimePolicy, isExactMatch: InOut<Boolean>): Date? {
        val found_0 = this.dateInterval(of = Calendar.Component.hour, for_ = matchDate)
        if (found_0 == null) {
            // Not DST
            return null
        }

        // MatchDate may not match because of a forward DST transition (e.g. spring forward, hour is lost).
        // MatchDate may be before or after this lost hour, so look in both directions.
        val currentHour = this.component(Calendar.Component.hour, from = found_0.start)

        var isForwardDST = false
        var beforeTransition = true

        val next = found_0.start.addingTimeInterval(found_0.duration)
        val nextHour = this.component(Calendar.Component.hour, from = next)
        if ((nextHour - currentHour) > 1 || (currentHour == 23 && nextHour > 0)) {
            // We're just before a forward DST transition, e.g., for America/Sao_Paulo:
            //
            //            2018-11-03                      2018-11-04
            //    ┌─────11:00 PM (GMT-3)─────┐ │ ┌ ─ ─ 12:00 AM (GMT-3)─ ─ ─┐ ┌─────1:00 AM (GMT-2) ─────┐
            //    │                          │ │ |                          │ │                          │
            //    └──────▲───────────────────┘ │ └ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─┘ └──────────────────────────┘
            //           └── Here                        Nonexistent
            //
            isForwardDST = true
        } else {
            // We might be just after such a transition.
            val previous = found_0.start.addingTimeInterval(-1)
            val previousHour = this.component(Calendar.Component.hour, from = previous)

            if (((currentHour - previousHour) > 1 || (previousHour == 23 && currentHour > 0))) {
                // We're just after a forward DST transition, e.g., for America/Sao_Paulo:
                //
                //            2018-11-03                      2018-11-04
                //    ┌─────11:00 PM (GMT-3)─────┐ │ ┌ ─ ─ 12:00 AM (GMT-3)─ ─ ─┐ ┌─────1:00 AM (GMT-2) ─────┐
                //    │                          │ │ |                          │ │                          │
                //    └──────────────────────────┘ │ └ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─┘ └──▲───────────────────────┘
                //                                            Nonexistent            └── Here
                //
                isForwardDST = true
                beforeTransition = false
            }
        }

        // We can only adjust when matches need not be strict.
        if (!(isForwardDST && matchingPolicy != Calendar.MatchingPolicy.strict)) {
            return null
        }

        // We can adjust the time as necessary to make this match close enough.
        // Since we aren't trying to strictly match and are now going to make a best guess approximation, we set exactMatch to false.
        isExactMatch.value = false

        if (beforeTransition) {
            if (matchingPolicy == Calendar.MatchingPolicy.nextTimePreservingSmallerComponents) {
                return this.date(byAdding = Calendar.Component.hour, value = 1, to = matchDate)
            } else if (matchingPolicy == Calendar.MatchingPolicy.nextTime) {
                return next.sref()
            } else {
                // No need to check `previousTimePreservingSmallerUnits` or `strict`:
                // * If we're matching the previous time, `matchDate` is already correct because we're pre-transition
                // * If we're matching strictly, we shouldn't be here (should be guarded by the if-statement condition): we can't adjust a strict match
                return matchDate.sref()
            }
        } else if (matchingPolicy == Calendar.MatchingPolicy.nextTime) {
            // `startOfHour` is the start of the hour containing `matchDate` (i.e. take `matchDate` but wipe the minute and second)
            return found_0.start.sref()
        } else if (matchingPolicy == Calendar.MatchingPolicy.previousTimePreservingSmallerComponents) {
            // We've arrived here after a mismatch due to a forward DST transition, and specifically, one which produced a candidate matchDate which was _after_ the transition.
            // At the time of writing this (2018-07-11), the only way to hit this case is under the following circumstances:
            //
            //   * DST transition in a time zone which transitions at `hour = 0` (i.e. 11:59:59 -> 01:00:00)
            //   * Components request `hour = 0`
            //   * Components contain a date component higher than hour which advanced us to the start of the day from a prior day
            //
            // If the DST transition is not at midnight, the components request any other hour, or there is no higher date component, we will have fallen into the usual hour-rolling loop.
            // That loop right now takes care to stop looping _before_ the transition.
            //
            // This means that right now, if we attempt to match the previous time while preserving smaller components (i.e. rewinding by an hour), we will no longer match the higher date component which had been requested.
            // For instance, if searching for `weekday = 1` (Sunday) got us here, rewinding by an hour brings us back to Saturday. Similarly, if asking for `month = x` got us here, rewinding by an hour would bring us to `month = x - 1`.
            // These mismatches are not proper candidates and should not be accepted.
            //
            // However, if the conditions of the hour-rolling loop ever change, I am including the code which would be correct to use here: attempt to roll back by an hour, and check whether we've introduced a new mismatch.

            // We don't actually have a match. Claim it's not DST too, to avoid accepting matchDate as-is anyway further on (which is what isForwardDST = true allows for).
            return null
        } else {
            // No need to check `nextTimePreservingSmallerUnits` or `strict`:
            // * If we're matching the next time, `matchDate` is already correct because we're post-transition
            // * If we're matching strictly, we shouldn't be here (should be guarded by the if-statement condition): we can't adjust a strict match
            return matchDate.sref()
        }
    }

    private fun _adjustedDateForMismatchedLeapMonthOrDay(start: Date, searchingDate: Date, matchDate: Date, matchingComponents: DateComponents, compsToMatch: DateComponents, nextHighestUnit: Calendar.Component, direction: Calendar.SearchDirection, matchingPolicy: Calendar.MatchingPolicy, repeatedTimePolicy: Calendar.RepeatedTimePolicy, isExactMatch: InOut<Boolean>, isLeapDay: InOut<Boolean>): Date? {
        val searchDateComps = this.dateComponents(setOf(Calendar.Component.year, Calendar.Component.month, Calendar.Component.day), from = searchingDate)

        val searchDateDay = searchDateComps.day
        val searchDateMonth = searchDateComps.month
        val searchDateYear = searchDateComps.year
        val desiredMonth = compsToMatch.month
        val desiredDay = compsToMatch.day

        val detectedLeapYearSituation = ((desiredDay != null) && (searchDateDay != desiredDay)) || ((desiredMonth != null) && (searchDateMonth != desiredMonth))
        if (detectedLeapYearSituation == false) {
            return null
        }
        val sYear_0 = searchDateYear
        if (sYear_0 == null) {
            return null
        }
        val sMonth_0 = searchDateMonth
        if (sMonth_0 == null) {
            return null
        }
        val dDay_0 = desiredDay
        if (dDay_0 == null) {
            return null
        }
        val dMonth_0 = desiredMonth
        if (dMonth_0 == null) {
            return null
        }

        var foundGregLeapMatchesComps = false
        var result: Date? = matchDate.sref()

        if (this.identifier == Calendar.Identifier.gregorian) {
            if (dMonth_0 == 2 && matchingComponents.month == 2) {
                var amountToAdd: Int
                if (direction == Calendar.SearchDirection.backward) {
                    amountToAdd = (sYear_0 % 4) * -1
                    if (amountToAdd == 0 && sMonth_0 >= dMonth_0) {
                        amountToAdd = amountToAdd - 4
                    }
                } else {
                    amountToAdd = 4 - (sYear_0 % 4)
                }

                this.date(byAdding = Calendar.Component.year, value = amountToAdd, to = searchingDate)?.let { searchDateInLeapYear ->
                    this.dateInterval(of = Calendar.Component.year, for_ = searchDateInLeapYear)?.let { leapYearInterval ->
                        val inner_0 = this._matchingDate(after = leapYearInterval.start, matching = compsToMatch, direction = Calendar.SearchDirection.forward, matchingPolicy = matchingPolicy, repeatedTimePolicy = repeatedTimePolicy)
                        if (inner_0 == null) {
                            return null
                        }

                        val leapCheck = this.date(inner_0, containsMatchingComponents = compsToMatch)
                        foundGregLeapMatchesComps = leapCheck.element1
                        result = inner_0.sref()
                    }
                }
            }
        }

        if (foundGregLeapMatchesComps == false) {
            if (matchingPolicy == Calendar.MatchingPolicy.strict) {
                if (this.identifier == Calendar.Identifier.gregorian) {
                    isExactMatch.value = false
                } else {
                    result = this._matchingDate(after = searchingDate, matching = matchingComponents, inNextHighestUnit = nextHighestUnit, direction = direction, matchingPolicy = matchingPolicy, repeatedTimePolicy = repeatedTimePolicy)
                }
            } else {
                var compsCopy = compsToMatch.sref()
                var tempComps = DateComponents()
                tempComps.year = sYear_0
                tempComps.month = dMonth_0
                tempComps.day = 1

                if (matchingPolicy == Calendar.MatchingPolicy.nextTime) {
                    val matchtarget_9 = compsToMatch.year
                    if (matchtarget_9 != null) {
                        val cYear = matchtarget_9
                        compsCopy.year = if (cYear > sYear_0) cYear else sYear_0
                    } else {
                        compsCopy.year = sYear_0
                    }
                    val tempDate_0 = this.date(from = tempComps)
                    if (tempDate_0 == null) {
                        return null
                    }
                    val followingMonthDate_0 = this.date(byAdding = Calendar.Component.month, value = 1, to = tempDate_0)
                    if (followingMonthDate_0 == null) {
                        return null
                    }

                    compsCopy.month = this.component(Calendar.Component.month, from = followingMonthDate_0)
                    compsCopy.day = 1
                    val inner_1 = this._matchingDate(after = start, matching = compsCopy, direction = direction, matchingPolicy = matchingPolicy, repeatedTimePolicy = repeatedTimePolicy)
                    if (inner_1 == null) {
                        return null
                    }

                    val innerCheck = this.date(inner_1, containsMatchingComponents = compsCopy)
                    if (innerCheck.element1) {
                        val matchtarget_10 = this.dateInterval(of = Calendar.Component.day, for_ = inner_1)
                        if (matchtarget_10 != null) {
                            val foundRange = matchtarget_10
                            result = foundRange.start.sref()
                        } else {
                            result = inner_1.sref()
                        }
                    } else {
                        result = null
                    }
                } else {
                    this.preserveSmallerUnits(start, compsToMatch = compsToMatch, compsToModify = InOut({ compsCopy }, { compsCopy = it }))
                    if (matchingPolicy == Calendar.MatchingPolicy.nextTimePreservingSmallerComponents) {
                        val matchtarget_11 = compsToMatch.year
                        if (matchtarget_11 != null) {
                            val cYear = matchtarget_11
                            compsCopy.year = if (cYear > sYear_0) cYear else sYear_0
                        } else {
                            compsCopy.year = sYear_0
                        }

                        tempComps.year = compsCopy.year
                        val tempDate_1 = this.date(from = tempComps)
                        if (tempDate_1 == null) {
                            return null
                        }
                        val followingMonthDate_1 = this.date(byAdding = Calendar.Component.month, value = 1, to = tempDate_1)
                        if (followingMonthDate_1 == null) {
                            return null
                        }

                        compsCopy.month = this.component(Calendar.Component.month, from = followingMonthDate_1)
                        compsCopy.day = 1
                    } else {
                        val tempDate_2 = this.date(from = tempComps)
                        if (tempDate_2 == null) {
                            return null
                        }
                        val range_0 = this.range(of = Calendar.Component.day, in_ = Calendar.Component.month, for_ = tempDate_2)
                        if (range_0 == null) {
                            return null
                        }

                        val lastDayOfMonth = range_0.upperBound - range_0.lowerBound
                        if (dDay_0 >= lastDayOfMonth) {
                            compsCopy.day = lastDayOfMonth
                        } else {
                            compsCopy.day = dDay_0 - 1
                        }
                    }
                    val inner_2 = this._matchingDate(after = searchingDate, matching = compsCopy, direction = direction, matchingPolicy = matchingPolicy, repeatedTimePolicy = repeatedTimePolicy)
                    if (inner_2 == null) {
                        return null
                    }

                    val finalCheck = this.date(inner_2, containsMatchingComponents = compsCopy)
                    if (finalCheck.element1 == false) {
                        result = null
                    } else {
                        result = inner_2.sref()
                    }
                }

                isExactMatch.value = false
                isLeapDay.value = true
            }
        }

        return result.sref()
    }

    private fun _adjustedComponents(comps: DateComponents, date: Date, direction: Calendar.SearchDirection): DateComponents {
        // This method ensures that the algorithm enumerates through each year or month if they are not explicitly set in the DateComponents passed into enumerateDates. This only applies to cases where the highest set unit is month or day (at least for now). For full in context explanation, see where it gets called in enumerateDates.
        val highestSetUnit = comps.highestSetUnit
        when (highestSetUnit) {
            Calendar.Component.month -> {
                var adjusted = comps.sref()
                adjusted.year = this.component(Calendar.Component.year, from = date)
                this.date(from = adjusted)?.let { adjustedDate ->
                    if (direction == Calendar.SearchDirection.forward && date > adjustedDate) {
                        adjusted.year = (adjusted.year ?: 0) + 1
                    } else if (direction == Calendar.SearchDirection.backward && date < adjustedDate) {
                        adjusted.year = (adjusted.year ?: 0) - 1
                    }
                }
                return adjusted.sref()
            }
            Calendar.Component.day -> {
                var adjusted = comps.sref()
                if (direction == Calendar.SearchDirection.backward) {
                    val dateDay = this.component(Calendar.Component.day, from = date)
                    // We need to make sure we don't surpass the day we want.
                    if (comps.day ?: Int.max >= dateDay) {
                        val tempDate = this.date(byAdding = Calendar.Component.month, value = -1, to = date)!!
                        adjusted.month = this.component(Calendar.Component.month, from = tempDate)
                    } else {
                        // Adjusted is the date components we're trying to match against; dateDay is the current day of the current search date.
                        // See the comment in enumerateDates for the justification for adding the month to the components here.
                        //
                        // However, we can't unconditionally add the current month to these components. If the current search date is on month M and day D, and the components we're trying to match have day D' set, the resultant date components to match against are {day=D', month=M}.
                        // This is only correct sometimes:
                        //
                        //  * If D' > D (e.g. we're on Nov 05, and trying to find the next 15th of the month), then it's okay to try to match Nov 15.
                        //  * However, if D' <= D (e.g. we're on Nov 05, and are trying to find the next 2nd of the month), then it's not okay to try to match Nov 02.
                        //
                        // We can only adjust the month if it won't cause us to search "backwards" in time (causing us to elsewhere end up skipping the first [correct] match we find).
                        // These same changes apply to the backwards case above.
                        val dateMonth = this.component(Calendar.Component.month, from = date)
                        adjusted.month = dateMonth
                    }
                } else {
                    val dateDay = this.component(Calendar.Component.day, from = date)
                    if (comps.day ?: Int.max > dateDay) {
                        adjusted.month = this.component(Calendar.Component.month, from = date)
                    }
                }
                return adjusted.sref()
            }
            else -> {
                // Nothing to adjust
                return comps.sref()
            }
        }
    }

    private fun dateIfEraHasYear(era: Int, year: Int): Date? {
        var dateComp = DateComponents()
        dateComp.era = era
        dateComp.year = year
        dateComp.month = 1
        dateComp.day = 1
        var date_0 = this.date(from = dateComp)
        if (date_0 == null) {
            return null
        }

        var currentEra = this.component(Calendar.Component.era, from = date_0)
        var currentYear = this.component(Calendar.Component.year, from = date_0)

        if (year == 1) {
            val addingComp = DateComponents(day = 1)

            // This is needed for Japanese calendar (and maybe other calendars with more than a few eras too).
            while (currentEra < era) {
                val newDate_0 = this.date(byAdding = addingComp, to = date_0!!)
                if (newDate_0 == null) {
                    return null
                }
                date_0 = newDate_0.sref()
                currentEra = this.component(Calendar.Component.era, from = date_0)
            }

            currentYear = this.component(Calendar.Component.year, from = date_0)
        }

        if (currentEra == era && currentYear == year) {
            // For Gregorian calendar at least, era and year should always match up so date should always be assigned to result.
            return date_0.sref()
        }

        return null
    }

    private fun date(date: Date, containsMatchingComponents: DateComponents, @Suppress("UNUSED_PARAMETER") unusedp_0: Nothing? = null): Tuple2<Set<Calendar.Component>, Boolean> {
        val compsToMatch = containsMatchingComponents
        var dateMatchesComps = true
        val units = compsToMatch.setUnits.sref()
        var compsFromDate = this.dateComponents(units, from = date)

        if (compsToMatch.calendar != null) {
            compsFromDate.calendar = compsToMatch.calendar
        }
        if (compsToMatch.timeZone != null) {
            compsFromDate.timeZone = compsToMatch.timeZone
        }

        if (compsFromDate != compsToMatch) {
            dateMatchesComps = false
            var mismatchedUnitsOut = compsFromDate.mismatchedUnits(comparedTo = compsToMatch)
            if (mismatchedUnitsOut.isEmpty) {
                return Tuple2(setOf(), true)
            }

            return Tuple2(mismatchedUnitsOut.sref(), false)
        } else {
            return Tuple2(setOf(), true)
        }
    }

    private fun bumpedDateUpToNextHigherUnitInComponents(searchingDate: Date, components: DateComponents, direction: Calendar.SearchDirection, matchDate: Date?): Date? {
        val highestSetUnit_0 = components.highestSetUnit
        if (highestSetUnit_0 == null) {
            // Empty components?
            return null
        }

        val nextUnitAboveHighestSet: Calendar.Component

        if (highestSetUnit_0 == Calendar.Component.era) {
            nextUnitAboveHighestSet = Calendar.Component.year
        } else if (highestSetUnit_0 == Calendar.Component.year || highestSetUnit_0 == Calendar.Component.yearForWeekOfYear) {
            nextUnitAboveHighestSet = highestSetUnit_0
        } else {
            val next_0 = highestSetUnit_0.nextHigherUnit
            if (next_0 == null) {
                return null
            }
            nextUnitAboveHighestSet = next_0
        }
        val foundRange_15 = this.dateInterval(of = nextUnitAboveHighestSet, for_ = searchingDate)
        if (foundRange_15 == null) {
            return null
        }

        var result = foundRange_15.start.addingTimeInterval(if (direction == Calendar.SearchDirection.backward) -1.0 else foundRange_15.duration)
        if (matchDate != null) {
            val ordering = matchDate.compare(result)
            if ((ordering != ComparisonResult.orderedAscending && direction == Calendar.SearchDirection.forward) || (ordering != ComparisonResult.orderedDescending && direction == Calendar.SearchDirection.backward)) {
                // We need to advance searchingDate so that it starts just after matchDate.
                // We already guarded against an empty components above, so force unwrap here.
                components.lowestSetUnit?.let { lowestSetUnit ->
                    val date_1 = this.date(byAdding = lowestSetUnit, value = if (direction == Calendar.SearchDirection.backward) -1 else 1, to = matchDate)
                    if (date_1 == null) {
                        return null
                    }
                    result = date_1.sref()
                }
            }
        }

        return result.sref()
    }

    private fun preserveSmallerUnits(date: Date, compsToMatch: DateComponents, compsToModify: InOut<DateComponents>) {
        val smallerUnits = this.dateComponents(setOf(Calendar.Component.hour, Calendar.Component.minute, Calendar.Component.second), from = date)

        // Either preserve the units we're trying to match if they are explicitly defined or preserve the hour/min/sec in the date.
        compsToModify.value.hour = compsToMatch.hour ?: smallerUnits.hour
        compsToModify.value.minute = compsToMatch.minute ?: smallerUnits.minute
        compsToModify.value.second = compsToMatch.second ?: smallerUnits.second
    }

    private constructor(copy: MutableStruct) {
        @Suppress("NAME_SHADOWING", "UNCHECKED_CAST") val copy = copy as Calendar
        this.platformValue = copy.platformValue
        this.locale = copy.locale
    }

    override var supdate: ((Any) -> Unit)? = null
    override var smutatingcount = 0
    override fun scopy(): MutableStruct = Calendar(this as MutableStruct)

    override fun toString(): String = description

    override fun equals(other: Any?): Boolean {
        if (other !is Calendar) return false
        return platformValue == other.platformValue && locale == other.locale
    }

    override fun hashCode(): Int {
        var result = 1
        result = Hasher.combine(result, platformValue)
        result = Hasher.combine(result, locale)
        return result
    }

    @androidx.annotation.Keep
    companion object: DecodableCompanion<Calendar> {

        val current: Calendar
            get() = Calendar(platformValue = java.util.Calendar.getInstance())

        @Deprecated("This API is not yet available in Skip. Consider placing it within a #if !SKIP block. You can file an issue against the owning library at https://github.com/skiptools, or see the library README for information on adding support", level = DeprecationLevel.ERROR)
        val autoupdatingCurrent: Calendar
            get() {
                fatalError()
            }

        private fun platformValue(for_: Calendar.Identifier): java.util.Calendar {
            val identifier = for_
            when (identifier) {
                Calendar.Identifier.gregorian -> return java.util.GregorianCalendar()
                Calendar.Identifier.iso8601 -> return java.util.Calendar.getInstance()
                else -> {
                    // TODO: how to support the other calendars?
                    return java.util.Calendar.getInstance()
                }
            }
        }

        override fun init(from: Decoder): Calendar = Calendar(from = from)

        fun Identifier(from: Decoder): Calendar.Identifier {
            val container = from.singleValueContainer()
            val rawValue = container.decode(Int::class)
            return Identifier(rawValue = rawValue) ?: throw ErrorException(cause = NullPointerException())
        }

        fun Identifier(rawValue: Int): Calendar.Identifier? = Identifier.init(rawValue = rawValue)
    }
}

