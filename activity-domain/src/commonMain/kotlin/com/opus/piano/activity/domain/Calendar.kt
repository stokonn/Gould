package com.opus.piano.activity.domain

/** Pure proleptic-Gregorian civil-date helpers shared by Android and iOS.
 *  Epoch days follow java.time's convention: 1970-01-01 is day 0.
 *  Conversion uses Howard Hinnant's days_from_civil / civil_from_days so no
 *  platform calendar API is required in common code. */
data class CivilDate(val year: Int, val month: Int, val day: Int) {
    init {
        require(month in 1..12)
        require(day in 1..daysInMonth(year, month))
    }
}

fun daysInMonth(year: Int, month: Int): Int = when (month) {
    1, 3, 5, 7, 8, 10, 12 -> 31
    4, 6, 9, 11 -> 30
    else -> if (isLeapYear(year)) 29 else 28
}

fun isLeapYear(year: Int): Boolean = (year % 4 == 0 && year % 100 != 0) || year % 400 == 0

fun epochDayOf(year: Int, month: Int, day: Int): Long {
    val adjustedYear = if (month <= 2) year - 1 else year
    val era = if (adjustedYear >= 0) adjustedYear / 400 else (adjustedYear - 399) / 400
    val yearOfEra = adjustedYear - era * 400
    val monthShifted = if (month > 2) month - 3 else month + 9
    val dayOfYear = (153 * monthShifted + 2) / 5 + day - 1
    val dayOfEra = yearOfEra * 365 + yearOfEra / 4 - yearOfEra / 100 + dayOfYear
    return era * 146097L + dayOfEra - 719468L
}

fun civilFromEpochDay(epochDay: Long): CivilDate {
    var z = epochDay + 719468L
    val era = (if (z >= 0) z else z - 146096L) / 146097L
    val dayOfEra = z - era * 146097L
    val yearOfEra = (dayOfEra - dayOfEra / 1460L + dayOfEra / 36524L - dayOfEra / 146096L) / 365L
    val year = yearOfEra + era * 400L
    val dayOfYear = dayOfEra - (365L * yearOfEra + yearOfEra / 4 - yearOfEra / 100)
    val monthShifted = (5 * dayOfYear + 2) / 153L
    val day = (dayOfYear - (153 * monthShifted + 2) / 5 + 1).toInt()
    val month = (if (monthShifted < 10) monthShifted + 3 else monthShifted - 9).toInt()
    return CivilDate((if (month <= 2) year + 1 else year).toInt(), month, day)
}
