package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.util.AppStrings
import com.example.util.CalendarHelper
import java.util.Calendar
import java.util.GregorianCalendar

@Composable
fun BilingualDatePickerDialog(
    onDismiss: () -> Unit,
    onDateSelected: (Long, String) -> Unit
) {
    val isFa = AppStrings.activeLanguage == "fa"

    // Active tab in DatePicker: 0 = Shamsi, 1 = Qamari (Hijri), 2 = Miladi (Gregorian)
    var activeCalendarTab by remember { mutableStateOf(if (isFa) 0 else 2) }

    // Selected internal elements
    val todayGreg = CalendarHelper.getTodayGregorian()
    val todayJalali = CalendarHelper.gregorianToJalali(todayGreg[0], todayGreg[1], todayGreg[2])
    val todayHijri = CalendarHelper.gregorianToHijri(todayGreg[0], todayGreg[1], todayGreg[2])

    // Current operating states for the picker
    var selectedY by remember { mutableStateOf(todayGreg[0]) }
    var selectedM by remember { mutableStateOf(todayGreg[1]) }
    var selectedD by remember { mutableStateOf(todayGreg[2]) }

    var shamsiY by remember { mutableStateOf(todayJalali[0]) }
    var shamsiM by remember { mutableStateOf(todayJalali[1]) }
    var shamsiD by remember { mutableStateOf(todayJalali[2]) }

    var hijriY by remember { mutableStateOf(todayHijri[0]) }
    var hijriM by remember { mutableStateOf(todayHijri[1]) }
    var hijriD by remember { mutableStateOf(todayHijri[2]) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            elevation = CardDefaults.cardElevation(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Display
                Text(
                    text = if (isFa) "انتخاب تاریخ برنامه‌ریزی" else "Select Schedule Date",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Calendar Tabs Selection
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(0.5f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val tabs = listOf(
                        0 to AppStrings.get("jalali"),
                        1 to AppStrings.get("hijri"),
                        2 to AppStrings.get("gregorian")
                    )
                    tabs.forEach { (idx, label) ->
                        val isSel = activeCalendarTab == idx
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSel) MaterialTheme.colorScheme.primary
                                    else Color.Transparent
                                )
                                .clickable {
                                    activeCalendarTab = idx
                                }
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Main controllers based on selected tab
                when (activeCalendarTab) {
                    0 -> { // Shamsi Jalali Picker
                        DatePickerCore(
                            year = shamsiY,
                            month = shamsiM,
                            day = shamsiD,
                            onYearChange = { shamsiY = it },
                            onMonthChange = { shamsiM = it },
                            onDayChange = { shamsiD = it },
                            getMonthLength = { y, m -> CalendarHelper.getJalaliMonthLength(y, m) },
                            getMonthName = { m ->
                                if (isFa) CalendarHelper.JALALI_MONTHS_FA[m - 1]
                                else CalendarHelper.JALALI_MONTHS_EN[m - 1]
                            },
                            yearRange = 1400..1425
                        )
                    }
                    1 -> { // Lunar Hijri Picker
                        DatePickerCore(
                            year = hijriY,
                            month = hijriM,
                            day = hijriD,
                            onYearChange = { hijriY = it },
                            onMonthChange = { hijriM = it },
                            onDayChange = { hijriD = it },
                            getMonthLength = { y, m -> CalendarHelper.getHijriMonthLength(y, m) },
                            getMonthName = { m ->
                                if (isFa) CalendarHelper.HIJRI_MONTHS_FA[m - 1]
                                else CalendarHelper.HIJRI_MONTHS_EN[m - 1]
                            },
                            yearRange = 1445..1470
                        )
                    }
                    2 -> { // Gregorian Miladi Picker
                        DatePickerCore(
                            year = selectedY,
                            month = selectedM,
                            day = selectedD,
                            onYearChange = { selectedY = it },
                            onMonthChange = { selectedM = it },
                            onDayChange = { selectedD = it },
                            getMonthLength = { y, m ->
                                GregorianCalendar(y, m - 1, 1).getActualMaximum(Calendar.DAY_OF_MONTH)
                            },
                            getMonthName = { m ->
                                if (isFa) CalendarHelper.GREGORIAN_MONTHS_FA[m - 1]
                                else CalendarHelper.GREGORIAN_MONTHS_EN[m - 1]
                            },
                            yearRange = 2025..2035
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Actions Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(AppStrings.get("cancel"))
                    }

                    Button(
                        onClick = {
                            // Compile final date timestamp based on chosen tab
                            val timestamp: Long
                            val text: String

                            when (activeCalendarTab) {
                                0 -> { // Shamsi
                                    val greg = CalendarHelper.jalaliToGregorian(shamsiY, shamsiM, shamsiD)
                                    val cal = Calendar.getInstance().apply {
                                        set(greg[0], greg[1] - 1, greg[2], 12, 0, 0)
                                    }
                                    timestamp = cal.timeInMillis
                                    text = CalendarHelper.formatJalali(shamsiY, shamsiM, shamsiD) + " (${AppStrings.get("jalali")})"
                                }
                                1 -> { // Hijri
                                    val greg = CalendarHelper.hijriToGregorian(hijriY, hijriM, hijriD)
                                    val cal = Calendar.getInstance().apply {
                                        set(greg[0], greg[1] - 1, greg[2], 12, 0, 0)
                                    }
                                    timestamp = cal.timeInMillis
                                    text = CalendarHelper.formatHijri(hijriY, hijriM, hijriD) + " (${AppStrings.get("hijri")})"
                                }
                                else -> { // Gregorian miladi
                                    val cal = Calendar.getInstance().apply {
                                        set(selectedY, selectedM - 1, selectedD, 12, 0, 0)
                                    }
                                    timestamp = cal.timeInMillis
                                    text = CalendarHelper.formatGregorian(selectedY, selectedM, selectedD) + " (${AppStrings.get("gregorian")})"
                                }
                            }
                            onDateSelected(timestamp, text)
                        },
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(AppStrings.get("save"))
                    }
                }
            }
        }
    }
}

@Composable
fun DatePickerCore(
    year: Int,
    month: Int,
    day: Int,
    onYearChange: (Int) -> Unit,
    onMonthChange: (Int) -> Unit,
    onDayChange: (Int) -> Unit,
    getMonthLength: (Int, Int) -> Int,
    getMonthName: (Int) -> String,
    yearRange: IntRange
) {
    val monthLength = remember(year, month) { getMonthLength(year, month) }
    val dayCoerced = day.coerceIn(1, monthLength)
    if (dayCoerced != day) {
        onDayChange(dayCoerced)
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Year & Month selective scrollbar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    val prevM = month - 1
                    if (prevM >= 1) {
                        onMonthChange(prevM)
                    } else {
                        onMonthChange(12)
                        val prevY = year - 1
                        if (prevY in yearRange) onYearChange(prevY)
                    }
                }
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Prev Month")
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = getMonthName(month),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { if (year - 1 in yearRange) onYearChange(year - 1) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Prev Year", modifier = Modifier.size(16.dp))
                    }
                    Text(
                        text = year.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    IconButton(
                        onClick = { if (year + 1 in yearRange) onYearChange(year + 1) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Next Year", modifier = Modifier.size(16.dp))
                    }
                }
            }

            IconButton(
                onClick = {
                    val nextM = month + 1
                    if (nextM <= 12) {
                        onMonthChange(nextM)
                    } else {
                        onMonthChange(1)
                        val nextY = year + 1
                        if (nextY in yearRange) onYearChange(nextY)
                    }
                }
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Next Month")
            }
        }

        // Days Grid Selection
        Text(
            text = if (AppStrings.activeLanguage == "fa") "انتخاب روز" else "Select Day",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            fontWeight = FontWeight.Bold
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier
                .height(200.dp)
                .fillMaxWidth()
        ) {
            items(monthLength) { dIdx ->
                val currD = dIdx + 1
                val isSelected = currD == day
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(2.dp)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else Color.Transparent
                        )
                        .clickable { onDayChange(currD) }
                ) {
                    Text(
                        text = currD.toString(),
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
