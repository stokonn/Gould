package com.opus.piano.activity.domain

/** One cell of the year board: a real calendar date rendered as a single grid slot.
 *  [inYear] marks cells belonging to other years (kept for continuous weekday
 *  alignment, rendered without content), [future] marks dates after [today]. */
data class ActivityBoardCell(
    val epochDay: Long,
    val inYear: Boolean,
    val future: Boolean,
    val activity: DayActivity?,
)

/** A Monday-anchored week column. [monthLabel] is the month (1-12) whose first
 *  in-year day falls inside this week, so labels always sit above their own weeks. */
data class ActivityBoardWeek(
    val monday: Long,
    val cells: List<ActivityBoardCell>,
    val monthLabel: Int?,
)

/** Continuous GitHub-Contributions-style year grid: Monday columns, seven rows,
 *  no month gaps. Cells cover every day from the Monday on/before Jan 1 through
 *  the Sunday on/after Dec 31 of [year]. */
fun activityBoard(year: Int, today: Long, activities: List<DayActivity>): List<ActivityBoardWeek> {
    require(year >= 1)
    val first = epochDayOf(year, 1, 1)
    val last = epochDayOf(year, 12, 31)
    val startMonday = mondayOf(first)
    val endMonday = mondayOf(last)
    val byDay = activities.associateBy { it.epochDay }
    val weeks = mutableListOf<ActivityBoardWeek>()
    var monday = startMonday
    while (monday <= endMonday) {
        val cells = (0L..6L).map { offset ->
            val date = monday + offset
            val civil = civilFromEpochDay(date)
            ActivityBoardCell(
                epochDay = date,
                inYear = civil.year == year,
                future = date > today,
                activity = byDay[date],
            )
        }
        val monthLabel = cells.firstOrNull { cell ->
            cell.inYear && civilFromEpochDay(cell.epochDay).day == 1
        }?.let { civilFromEpochDay(it.epochDay).month }
        weeks += ActivityBoardWeek(monday, cells, monthLabel)
        monday += DAYS_PER_WEEK
    }
    return weeks
}
