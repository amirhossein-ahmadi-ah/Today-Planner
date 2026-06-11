package com.example.util

import java.util.Calendar
import java.util.GregorianCalendar

object CalendarHelper {

    val JALALI_MONTHS_FA = listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )
    val JALALI_MONTHS_EN = listOf(
        "Farvardin", "Ordibehesht", "Khordad", "Tir", "Mordad", "Shahrivar",
        "Mehr", "Aban", "Azar", "Dey", "Bahman", "Esfand"
    )

    val HIJRI_MONTHS_FA = listOf(
        "محرم", "صفر", "ربیع‌الاول", "ربیع‌الثانی", "جمادی‌الاول", "جمادی‌الثانی",
        "رجب", "شعبان", "رمضان", "شوال", "ذی‌القعده", "ذی‌الحجه"
    )
    val HIJRI_MONTHS_EN = listOf(
        "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani", "Jumada al-Awwal", "Jumada al-Thani",
        "Rajab", "Sha'ban", "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
    )

    val GREGORIAN_MONTHS_FA = listOf(
        "ژانویه", "فوریه", "مارس", "آوریل", "مه", "ژوئن",
        "ژوئیه", "اوت", "سپتامبر", "اکتبر", "نوامبر", "دسامبر"
    )
    val GREGORIAN_MONTHS_EN = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    val WEEKDAYS_FA = listOf("شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه")
    val WEEKDAYS_EN = listOf("Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday")

    // Core Gregorian to Jalali conversion algorithm
    fun gregorianToJalali(gy: Int, gm: Int, gd: Int): IntArray {
        val gDaysInMonth = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        val jDaysInMonth = intArrayOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)

        var g_day_no = 365 * (gy - 1600) + (gy - 1600 + 3) / 4 - (gy - 1600 + 99) / 100 + (gy - 1600 + 399) / 400
        for (i in 0 until gm - 1) {
            g_day_no += gDaysInMonth[i]
        }
        if (gm > 2 && ((gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0))) {
            g_day_no += 1
        }
        g_day_no += gd - 1

        var j_day_no = g_day_no - 79

        val j_np = j_day_no / 12053
        j_day_no %= 12053

        var jy = 979 + 33 * j_np + 4 * (j_day_no / 1461)
        j_day_no %= 1461

        if (j_day_no >= 366) {
            jy += (j_day_no - 1) / 365
            j_day_no = (j_day_no - 1) % 365
        }

        var jm = 0
        while (jm < 11 && j_day_no >= jDaysInMonth[jm]) {
            j_day_no -= jDaysInMonth[jm]
            jm++
        }
        val jd = j_day_no + 1
        return intArrayOf(jy, jm + 1, jd)
    }

    // Jalali to Gregorian conversion algorithm
    fun jalaliToGregorian(jy: Int, jm: Int, jd: Int): IntArray {
        var jY = jy - 979
        val jM = jm - 1
        val jD = jd - 1

        var jDayNo = 365 * jY + (jY / 33) * 8 + (jY % 33 + 3) / 4
        for (i in 0 until jM) {
            jDayNo += if (i < 6) 31 else 30
        }
        jDayNo += jD

        var gDayNo = jDayNo + 79

        var gY = 1600 + 400 * (gDayNo / 146097)
        gDayNo %= 146097

        var leap = true
        if (gDayNo >= 36525) {
            gDayNo--
            gY += 100 * (gDayNo / 36524)
            gDayNo %= 36524
            if (gDayNo >= 365) {
                gDayNo++
            } else {
                leap = false
            }
        }

        gY += 4 * (gDayNo / 1461)
        gDayNo %= 1461

        if (gDayNo >= 366) {
            leap = false
            gDayNo--
            gY += gDayNo / 365
            gDayNo %= 365
        }

        var i = 0
        val gDaysInMonth = intArrayOf(
            31, if (leap) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31
        )
        while (i < 12 && gDayNo >= gDaysInMonth[i]) {
            gDayNo -= gDaysInMonth[i]
            i++
        }
        val gM = i + 1
        val gD = gDayNo + 1

        return intArrayOf(gY, gM, gD)
    }

    // Mathematical Gregorian to Tabular Islamic Calendar (approximate Hijri) conversion
    fun gregorianToHijri(gy: Int, gm: Int, gd: Int): IntArray {
        // Try to use modern java.time APIs
        try {
            val localDate = java.time.LocalDate.of(gy, gm, gd)
            val hijriDate = java.time.chrono.HijrahDate.from(localDate)
            val hY = hijriDate.get(java.time.temporal.ChronoField.YEAR)
            val hM = hijriDate.get(java.time.temporal.ChronoField.MONTH_OF_YEAR)
            val hD = hijriDate.get(java.time.temporal.ChronoField.DAY_OF_MONTH)
            if (hY > 0 && hM in 1..12 && hD in 1..30) {
                return intArrayOf(hY, hM, hD)
            }
        } catch (e: Throwable) {
            // Fallthrough to mathematical conversion
        }

        // Mathematical Gregorian to Tabular Islamic Calendar (approximate Hijri) conversion
        var y = gy
        var m = gm
        if (m < 3) {
            y -= 1
            m += 12
        }
        val a = Math.floor(y / 100.0).toInt()
        val b = 2 - a + Math.floor(a / 4.0).toInt()
        val jd = (Math.floor(365.25 * (y + 4716)) + Math.floor(30.6001 * (m + 1)) + gd + b - 1524).toInt()

        // 1 Muharram 1 AH epoch JD is 1948440
        val daysSinceEpoch = jd - 1948440
        if (daysSinceEpoch < 0) {
            return intArrayOf(1, 1, 1)
        }

        val cycleLength = 10631
        val cycleNumber = daysSinceEpoch / cycleLength
        val dayInCycle = daysSinceEpoch % cycleLength

        var yearInCycle = 0
        var daysLeft = dayInCycle
        val leapYears = intArrayOf(2, 5, 7, 10, 13, 16, 18, 21, 24, 26, 29)
        while (true) {
            val yearLength = if (leapYears.contains(yearInCycle + 1)) 355 else 354
            if (daysLeft >= yearLength) {
                daysLeft -= yearLength
                yearInCycle++
            } else {
                break
            }
        }
        val hYear = cycleNumber * 30 + yearInCycle + 1

        var month = 0
        var day = daysLeft
        while (month < 12) {
            val monthLength = if ((month + 1) % 2 != 0) 30 else {
                if (month + 1 == 12) {
                    if (leapYears.contains(yearInCycle + 1)) 30 else 29
                } else {
                    29
                }
            }
            if (day >= monthLength) {
                day -= monthLength
                month++
            } else {
                break
            }
        }
        val hMonth = (month + 1).coerceIn(1, 12)
        val hDay = (day + 1).coerceIn(1, 30)

        return intArrayOf(hYear, hMonth, hDay)
    }

    // Inverse Tabular Hijri to Gregorian conversion algorithm
    fun hijriToGregorian(hy: Int, hm: Int, hd: Int): IntArray {
        val jd = hd + Math.ceil(29.5 * (hm - 1)).toInt() + (hy - 1) * 354 + Math.floor((30 * hy + 3) / 30.0).toInt() + 1948440 - 385
        var l = jd + 68569
        val n = Math.floor((4 * l) / 146097.0).toInt()
        l -= Math.floor((146097 * n + 3) / 4.0).toInt()
        val i = Math.floor((4000 * (l + 1)) / 1464001.0).toInt()
        l -= Math.floor((1461 * i) / 4.0).toInt() - 31
        val j = Math.floor((80 * l) / 2447.0).toInt()
        val gd = l - Math.floor((2447 * j) / 80.0).toInt()
        l = Math.floor(j / 11.0).toInt()
        val gm = j + 2 - 12 * l
        val gy = 100 * (n - 49) + i + l
        return intArrayOf(gy, gm.coerceIn(1, 12), gd.coerceIn(1, 31))
    }

    private fun isHijriLeapYear(year: Int): Boolean {
        val leapYears = intArrayOf(2, 5, 7, 10, 13, 16, 18, 21, 24, 26, 29)
        val rem = year % 30
        return leapYears.contains(rem)
    }

    // Days in current Jalali Month
    fun getJalaliMonthLength(year: Int, month: Int): Int {
        if (month in 1..6) return 31
        if (month in 7..11) return 30
        if (month == 12) {
            // Leap year check in Hijri Shamsi
            val r = (year - 474) % 2820
            val isLeap = (((r + 474) + 38) * 31) % 128 < 31
            return if (isLeap) 30 else 29
        }
        return 30
    }

    // Days in Islamic month
    fun getHijriMonthLength(year: Int, month: Int): Int {
        if (month == 12) {
            return if (isHijriLeapYear(year)) 30 else 29
        }
        return if (month % 2 != 0) 30 else 29
    }

    // Weekday name of Gregorian date
    fun getWeekdayIndex(gy: Int, gm: Int, gd: Int): Int {
        val calendar = GregorianCalendar(gy, gm - 1, gd)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        // Calendar.SATURDAY = 7, SUNDAY = 1, MONDAY = 2, ...
        // We want: 0 = Saturday, 1 = Sunday, 2 = Monday, 3 = Tuesday, 4 = Wednesday, 5 = Thursday, 6 = Friday
        return when (dayOfWeek) {
            Calendar.SATURDAY -> 0
            Calendar.SUNDAY -> 1
            Calendar.MONDAY -> 2
            Calendar.TUESDAY -> 3
            Calendar.WEDNESDAY -> 4
            Calendar.THURSDAY -> 5
            Calendar.FRIDAY -> 6
            else -> 0
        }
    }

    // Format utility
    fun formatGregorian(gy: Int, gm: Int, gd: Int): String {
        return "%04d/%02d/%02d".format(gy, gm, gd)
    }

    fun formatJalali(jy: Int, jm: Int, jd: Int): String {
        return "%04d/%02d/%02d".format(jy, jm, jd)
    }

    fun formatHijri(hy: Int, hm: Int, hd: Int): String {
        return "%04d/%02d/%02d".format(hy, hm, hd)
    }

    // Get current local systems standard date elements
    fun getTodayGregorian(): IntArray {
        val cal = Calendar.getInstance()
        return intArrayOf(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }
}
