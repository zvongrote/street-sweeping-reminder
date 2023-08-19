package com.zachvg.streetsweepingreminder

import com.zachvg.streetsweepingreminder.database.SweepingSchedule
import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.Month

class SweepingDateCalcTest {
    @Test
    fun singleScheduleWithOneDay_CurrentDateAfter_ResultInNextMonthFebruary2020_IsCorrect() {
        val currentDate = LocalDate.of(2020, Month.JANUARY, 23)

        // Monday
        var schedules = mutableListOf(SweepingSchedule(
                "Home",
                DayOfWeek.MONDAY,
                mutableListOf(1)))
        var correctDate = LocalDate.of(2020, Month.FEBRUARY, 3)
        var calculatedDate = nextSweepingDateFrom(currentDate, schedules)
        assertEquals(correctDate, calculatedDate)

        // Tuesday
        schedules = mutableListOf(SweepingSchedule(
                "Home",
                DayOfWeek.TUESDAY,
                mutableListOf(2)))
        correctDate = LocalDate.of(2020, Month.FEBRUARY, 11)
        calculatedDate = nextSweepingDateFrom(currentDate, schedules)
        assertEquals(correctDate, calculatedDate)

        // Wednesday
        schedules = mutableListOf(SweepingSchedule(
                "Home",
                DayOfWeek.WEDNESDAY,
                mutableListOf(4)))
        correctDate = LocalDate.of(2020, Month.FEBRUARY, 26)
        calculatedDate = nextSweepingDateFrom(currentDate, schedules)
        assertEquals(correctDate, calculatedDate)

        // Thursday
        schedules = mutableListOf(SweepingSchedule(
                "Home",
                DayOfWeek.THURSDAY,
                mutableListOf(3)))
        correctDate = LocalDate.of(2020, Month.FEBRUARY, 20)
        calculatedDate = nextSweepingDateFrom(currentDate, schedules)
        assertEquals(correctDate, calculatedDate)

        // Wednesday
        schedules = mutableListOf(SweepingSchedule(
                "Home",
                DayOfWeek.FRIDAY,
                mutableListOf(2)))
        correctDate = LocalDate.of(2020, Month.FEBRUARY, 14)
        calculatedDate = nextSweepingDateFrom(currentDate, schedules)
        assertEquals(correctDate, calculatedDate)

    }

    @Test
    fun singleScheduleWithOneDay_CurrentDateBefore_ResultInCurrentMonthJanuary2020_IsCorrect() {
        val currentDate = LocalDate.of(2020, Month.JANUARY, 1)

        // Monday
        var schedules = mutableListOf(SweepingSchedule(
                "Home",
                DayOfWeek.MONDAY,
                mutableListOf(2)))
        var correctDate = LocalDate.of(2020, Month.JANUARY, 13)
        var calculatedDate = nextSweepingDateFrom(currentDate, schedules)
        assertEquals(correctDate, calculatedDate)

        // Tuesday
        schedules = mutableListOf(SweepingSchedule(
                "Home",
                DayOfWeek.TUESDAY,
                mutableListOf(2)))
        correctDate = LocalDate.of(2020, Month.JANUARY, 14)
        calculatedDate = nextSweepingDateFrom(currentDate, schedules)
        assertEquals(correctDate, calculatedDate)

        // Wednesday
        schedules = mutableListOf(SweepingSchedule(
                "Home",
                DayOfWeek.WEDNESDAY,
                mutableListOf(2)))
        correctDate = LocalDate.of(2020, Month.JANUARY, 8)
        calculatedDate = nextSweepingDateFrom(currentDate, schedules)
        assertEquals(correctDate, calculatedDate)

        // Thursday
        schedules = mutableListOf(SweepingSchedule(
                "Home",
                DayOfWeek.THURSDAY,
                mutableListOf(2)))
        correctDate = LocalDate.of(2020, Month.JANUARY, 9)
        calculatedDate = nextSweepingDateFrom(currentDate, schedules)
        assertEquals(correctDate, calculatedDate)

        // Friday
        schedules = mutableListOf(SweepingSchedule(
                "Home",
                DayOfWeek.FRIDAY,
                mutableListOf(2)))
        correctDate = LocalDate.of(2020, Month.JANUARY, 10)
        calculatedDate = nextSweepingDateFrom(currentDate, schedules)
        assertEquals(correctDate, calculatedDate)
    }

    @Test
    fun singleScheduleWithTwoDays_CurrentDateBefore_ResultInCurrentMonthJanuary2020_IsCorrect() {
        // Monday
        val currentDate = LocalDate.of(2020, Month.JANUARY, 1)
        var schedules = mutableListOf(SweepingSchedule(
                "Home",
                DayOfWeek.MONDAY,
                mutableListOf(1, 3)))
        var correctDate = LocalDate.of(2020, Month.JANUARY, 6)
        var calculatedDate = nextSweepingDateFrom(currentDate, schedules)
        assertEquals(correctDate, calculatedDate)


        // Tuesday
        schedules =
                mutableListOf(SweepingSchedule(
                        "Home",
                        DayOfWeek.TUESDAY,
                        mutableListOf(1, 3)))
        correctDate = LocalDate.of(2020, Month.JANUARY, 7)
        calculatedDate = nextSweepingDateFrom(currentDate, schedules)
        assertEquals(correctDate, calculatedDate)

        // Wednesday
        schedules =
                mutableListOf(SweepingSchedule(
                        "Home",
                        DayOfWeek.WEDNESDAY,
                        mutableListOf(1, 3)))
        correctDate = LocalDate.of(2020, Month.JANUARY, 1)
        calculatedDate = nextSweepingDateFrom(currentDate, schedules)
        assertEquals(correctDate, calculatedDate)

        // Thursday
        schedules =
                mutableListOf(SweepingSchedule(
                        "Home",
                        DayOfWeek.THURSDAY,
                        mutableListOf(1, 3)))
        correctDate = LocalDate.of(2020, Month.JANUARY, 2)
        calculatedDate = nextSweepingDateFrom(currentDate, schedules)
        assertEquals(correctDate, calculatedDate)

        // Friday
        schedules =
                mutableListOf(SweepingSchedule(
                        "Home",
                        DayOfWeek.FRIDAY,
                        mutableListOf(1, 3)))
        correctDate = LocalDate.of(2020, Month.JANUARY, 3)
        calculatedDate = nextSweepingDateFrom(currentDate, schedules)
        assertEquals(correctDate, calculatedDate)
    }

    @Test
    fun singleScheduleWithTwoDays_CurrentDateInBetween_ResultInCurrentMonthJanuary2020_IsCorrect() {
        var currentDate = LocalDate.of(2020, Month.JANUARY, 15)

        // Monday
        var schedules = mutableListOf(SweepingSchedule(
                "Home",
                DayOfWeek.MONDAY,
                mutableListOf(1, 3)))
        var correctDate = LocalDate.of(2020, Month.JANUARY, 20)
        var calculatedDate = nextSweepingDateFrom(currentDate, schedules)
        assertEquals(correctDate, calculatedDate)

        // Tuesday
        schedules =
                mutableListOf(SweepingSchedule(
                        "Home",
                        DayOfWeek.TUESDAY,
                        mutableListOf(1, 3)))
        correctDate = LocalDate.of(2020, Month.JANUARY, 21)
        calculatedDate = nextSweepingDateFrom(currentDate, schedules)
        assertEquals(correctDate, calculatedDate)

        // Wednesday
        currentDate = LocalDate.of(2020, Month.JANUARY, 8)
        schedules =
                mutableListOf(SweepingSchedule(
                        "Home",
                        DayOfWeek.WEDNESDAY,
                        mutableListOf(1, 3)))
        correctDate = LocalDate.of(2020, Month.JANUARY, 15)
        calculatedDate = nextSweepingDateFrom(currentDate, schedules)
        assertEquals(correctDate, calculatedDate)

        // Thursday
        schedules =
                mutableListOf(SweepingSchedule(
                        "Home",
                        DayOfWeek.THURSDAY,
                        mutableListOf(1, 3)))
        correctDate = LocalDate.of(2020, Month.JANUARY, 16)
        calculatedDate = nextSweepingDateFrom(currentDate, schedules)
        assertEquals(correctDate, calculatedDate)

        // Friday
        schedules =
                mutableListOf(SweepingSchedule(
                        "Home",
                        DayOfWeek.FRIDAY,
                        mutableListOf(1, 3)))
        correctDate = LocalDate.of(2020, Month.JANUARY, 17)
        calculatedDate = nextSweepingDateFrom(currentDate, schedules)
        assertEquals(correctDate, calculatedDate)
    }

    @Test
    fun twoSchedulesWithOneDayEach_CurrentDateBefore_ResultInCurrentMonthJanuary2020() {
        val currentDate = LocalDate.of(2020, Month.JANUARY, 3)

        val schedules = mutableListOf(
                SweepingSchedule("Home - My Side", DayOfWeek.TUESDAY, mutableListOf(1)),
                SweepingSchedule("Home - Other Side", DayOfWeek.THURSDAY, mutableListOf(3))
        )

        val correctDate = LocalDate.of(2020, Month.JANUARY, 7)
        val calculatedDate = nextSweepingDateFrom(currentDate, schedules)
        assertEquals(correctDate, calculatedDate)
    }

    @Test
    fun twoSchedulesWithOneDayEach_CurrentDateBetween_ResultsInCurrentMonthJanuary2020() {
        val currentDate = LocalDate.of(2020, Month.JANUARY, 15)

        val schedules = mutableListOf(
                SweepingSchedule("Home - My Side", DayOfWeek.TUESDAY, mutableListOf(1)),
                SweepingSchedule("Home - Other Side", DayOfWeek.THURSDAY, mutableListOf(3))
        )

        val correctDate = LocalDate.of(2020, Month.JANUARY, 16)
        val calculatedDate = nextSweepingDateFrom(currentDate, schedules)
        assertEquals(correctDate, calculatedDate)
    }

    @Test
    fun twoSchedulesWithOneDayEach_CurrentDateAfter_ResultsInNextMonthFebruary2020() {
        val currentDate = LocalDate.of(2020, Month.JANUARY, 24)

        val schedules = mutableListOf(
                SweepingSchedule("Home - My Side", DayOfWeek.TUESDAY, mutableListOf(1)),
                SweepingSchedule("Home - Other Side", DayOfWeek.THURSDAY, mutableListOf(3))
        )

        val correctDate = LocalDate.of(2020, Month.FEBRUARY, 4)
        val calculatedDate = nextSweepingDateFrom(currentDate, schedules)
        assertEquals(correctDate, calculatedDate)
    }

    @Test
    fun twoSchedulesWithTwoDaysEach_CurrentBeforeAll_ResultInCurrentMonthJanuary2020() {
        val currentDate = LocalDate.of(2020, Month.JANUARY, 2)

        val schedules = mutableListOf(
                SweepingSchedule("Home - My Side", DayOfWeek.TUESDAY, mutableListOf(1, 3)),
                SweepingSchedule("Home - Other Side", DayOfWeek.THURSDAY, mutableListOf(2, 4))
        )

        val correctDate = LocalDate.of(2020, Month.JANUARY, 7)
        val calculatedDate = nextSweepingDateFrom(currentDate, schedules)
        assertEquals(correctDate, calculatedDate)
    }

    @Test
    fun twoSchedulesWithTwoDaysEach_CurrentBeforeSecond_ResultInCurrentMonthJanuary2020() {
        val currentDate = LocalDate.of(2020, Month.JANUARY, 22)

        val schedules = mutableListOf(
                SweepingSchedule("Home - My Side", DayOfWeek.TUESDAY, mutableListOf(1, 3)),
                SweepingSchedule("Home - Other Side", DayOfWeek.THURSDAY, mutableListOf(2, 4))
        )

        val correctDate = LocalDate.of(2020, Month.JANUARY, 23)
        val calculatedDate = nextSweepingDateFrom(currentDate, schedules)
        assertEquals(correctDate, calculatedDate)
    }

    @Test
    fun twoSchedulesWithTwoDaysEach_CurrentBeforeSecondDateOfFirst_ResultInCurrentMonthJanuary2020() {
        val currentDate = LocalDate.of(2020, Month.JANUARY, 20)

        val schedules = mutableListOf(
                SweepingSchedule("Home - My Side", DayOfWeek.TUESDAY, mutableListOf(1, 3)),
                SweepingSchedule("Home - Other Side", DayOfWeek.THURSDAY, mutableListOf(2, 4))
        )

        val correctDate = LocalDate.of(2020, Month.JANUARY, 21)
        val calculatedDate = nextSweepingDateFrom(currentDate, schedules)
        assertEquals(correctDate, calculatedDate)
    }

    @Test
    fun twoSchedulesWithTwoDaysEach_CurrentBeforeSecondDateOfSecond_ResultInCurrentMonthJanuary2020() {
        val currentDate = LocalDate.of(2020, Month.JANUARY, 20)

        val schedules = mutableListOf(
                SweepingSchedule("Home - My Side", DayOfWeek.TUESDAY, mutableListOf(1, 3)),
                SweepingSchedule("Home - Other Side", DayOfWeek.THURSDAY, mutableListOf(2, 4))
        )

        val correctDate = LocalDate.of(2020, Month.JANUARY, 21)
        val calculatedDate = nextSweepingDateFrom(currentDate, schedules)
        assertEquals(correctDate, calculatedDate)
    }

    @Test
    fun twoSchedulesWithTwoDaysEach_CurrentAfter_ResultInNextMonthFebruary2020() {
        val currentDate = LocalDate.of(2020, Month.JANUARY, 31)

        val schedules = mutableListOf(
                SweepingSchedule("Home - My Side", DayOfWeek.TUESDAY, mutableListOf(1, 3)),
                SweepingSchedule("Home - Other Side", DayOfWeek.THURSDAY, mutableListOf(2, 4))
        )

        val correctDate = LocalDate.of(2020, Month.FEBRUARY, 4)
        val calculatedDate = nextSweepingDateFrom(currentDate, schedules)
        assertEquals(correctDate, calculatedDate)
    }

    @Test
    fun singleScheduleEveryWeek_CurrentVaries_ResultsInCurrentMonthJanuary2020() {
        // 5 weeks in the month

        var currentDate = LocalDate.of(2020, Month.JANUARY, 1)

        val schedules = mutableListOf(
                SweepingSchedule("Home", DayOfWeek.THURSDAY, mutableListOf(1, 2, 3, 4, 5))
        )

        // Before 1st
        var correctDate = LocalDate.of(2020, Month.JANUARY, 2)
        var calculatedDate = nextSweepingDateFrom(currentDate, schedules)
        assertEquals(correctDate, calculatedDate)

        // Between 1st and 2nd
        currentDate = LocalDate.of(2020, Month.JANUARY, 6)
        correctDate = LocalDate.of(2020, Month.JANUARY, 9)
        calculatedDate = nextSweepingDateFrom(currentDate, schedules)
        assertEquals(correctDate, calculatedDate)

        // Between 2nd and 3rd
        currentDate = LocalDate.of(2020, Month.JANUARY, 12)
        correctDate = LocalDate.of(2020, Month.JANUARY, 16)
        calculatedDate = nextSweepingDateFrom(currentDate, schedules)
        assertEquals(correctDate, calculatedDate)

        // Between 3rd and 4th
        currentDate = LocalDate.of(2020, Month.JANUARY, 18)
        correctDate = LocalDate.of(2020, Month.JANUARY, 23)
        calculatedDate = nextSweepingDateFrom(currentDate, schedules)
        assertEquals(correctDate, calculatedDate)

        // Between 4th and 5th
        currentDate = LocalDate.of(2020, Month.JANUARY, 28)
        correctDate = LocalDate.of(2020, Month.JANUARY, 30)
        calculatedDate = nextSweepingDateFrom(currentDate, schedules)
        assertEquals(correctDate, calculatedDate)
    }

    @Test
    fun singleScheduleEveryWeek_CurrentAfter_ResultsInNextMonthFebruary2020() {
        // Current month has 5 weeks but the result is in the next month

        val currentDate = LocalDate.of(2020, Month.JANUARY, 31)

        val schedules = mutableListOf(
                SweepingSchedule("Home", DayOfWeek.MONDAY, mutableListOf(1, 2, 3, 4, 5))
        )

        val correctDate = LocalDate.of(2020, Month.FEBRUARY, 3)
        val calculatedDate = nextSweepingDateFrom(currentDate, schedules)
        assertEquals(correctDate, calculatedDate)
    }

    @Test
    fun singleScheduleEveryWeek_CurrentVaries_ResultsInCurrentMonthMay2020() {
        // 4 weeks in the month

        var currentDate = LocalDate.of(2020, Month.MAY, 4)

        val schedules = mutableListOf(
                SweepingSchedule("Home", DayOfWeek.TUESDAY, mutableListOf(1, 2, 3, 4, 5))
        )

        // Before 1st
        var correctDate = LocalDate.of(2020, Month.MAY, 5)
        var calculatedDate = nextSweepingDateFrom(currentDate, schedules)
        assertEquals(correctDate, calculatedDate)

        // Between 1st and 2nd
        currentDate = LocalDate.of(2020, Month.MAY, 7)
        correctDate = LocalDate.of(2020, Month.MAY, 12)
        calculatedDate = nextSweepingDateFrom(currentDate, schedules)
        assertEquals(correctDate, calculatedDate)

        // Between 2nd and 3rd
        currentDate = LocalDate.of(2020, Month.MAY, 17)
        correctDate = LocalDate.of(2020, Month.MAY, 19)
        calculatedDate = nextSweepingDateFrom(currentDate, schedules)
        assertEquals(correctDate, calculatedDate)

        // Between 3rd and 4th
        currentDate = LocalDate.of(2020, Month.MAY, 23)
        correctDate = LocalDate.of(2020, Month.MAY, 26)
        calculatedDate = nextSweepingDateFrom(currentDate, schedules)
        assertEquals(correctDate, calculatedDate)
    }

    @Test
    fun singleScheduleEveryWeek_CurrentAfter_ResultsInNextMonthJune2020() {
        // Current month has 4 weeks but the result is in the next month

        val currentDate = LocalDate.of(2020, Month.MAY, 27)

        val schedules = mutableListOf(
                SweepingSchedule("Home", DayOfWeek.MONDAY, mutableListOf(1, 2, 3, 4, 5))
        )

        val correctDate = LocalDate.of(2020, Month.JUNE, 1)
        val calculatedDate = nextSweepingDateFrom(currentDate, schedules)
        assertEquals(correctDate, calculatedDate)
    }
}
