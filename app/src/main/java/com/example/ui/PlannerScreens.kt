package com.example.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Folder
import com.example.data.Goal
import com.example.data.Streak
import com.example.data.Task
import com.example.ui.components.AdvancedSearchDialog
import com.example.ui.components.BilingualDatePickerDialog
import com.example.ui.theme.*
import com.example.util.AppStrings
import com.example.util.CalendarHelper
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerMainScreen(viewModel: PlannerViewModel) {
    val layoutDirection = if (viewModel.language == "fa") LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        val currentTab = remember { mutableStateOf(0) }
        val tabsList = listOf(
            AppStrings.get("today"),
            AppStrings.get("tasks"),
            AppStrings.get("goals"),
            AppStrings.get("calendar"),
            AppStrings.get("analytics"),
            AppStrings.get("settings")
        )
        val tabIcons = listOf(
            Icons.Default.Home,
            Icons.Default.List,
            Icons.Default.Star,
            Icons.Default.DateRange,
            Icons.Default.Info,
            Icons.Default.Settings
        )

        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    tabsList.forEachIndexed { index, label ->
                        NavigationBarItem(
                            selected = currentTab.value == index,
                            onClick = { currentTab.value = index },
                            icon = { Icon(tabIcons[index], contentDescription = label) },
                            label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues)
            ) {
                AnimatedContent(
                    targetState = currentTab.value,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "tab_transition"
                ) { tab ->
                    when (tab) {
                        0 -> TodayTabScreen(viewModel)
                        1 -> TasksTabScreen(viewModel)
                        2 -> GoalsTabScreen(viewModel)
                        3 -> CalendarTabScreen(viewModel)
                        4 -> AnalyticsTabScreen(viewModel)
                        5 -> SettingsTabScreen(viewModel)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TODAY TAB SCREEN
// -------------------------------------------------------------
@Composable
fun TodayTabScreen(viewModel: PlannerViewModel) {
    val tasks by viewModel.tasks.collectAsState()
    val streaks by viewModel.streaks.collectAsState()
    val folders by viewModel.folders.collectAsState()

    val context = LocalContext.current

    // Format today's date in 3 styles
    val todayGreg = CalendarHelper.getTodayGregorian()
    val todayJalali = CalendarHelper.gregorianToJalali(todayGreg[0], todayGreg[1], todayGreg[2])
    val todayHijri = CalendarHelper.gregorianToHijri(todayGreg[0], todayGreg[1], todayGreg[2])

    val weekdayIdx = CalendarHelper.getWeekdayIndex(todayGreg[0], todayGreg[1], todayGreg[2])
    val weekdayStr = if (viewModel.language == "fa") CalendarHelper.WEEKDAYS_FA[weekdayIdx] else CalendarHelper.WEEKDAYS_EN[weekdayIdx]

    val jalaliMonthName = if (viewModel.language == "fa") CalendarHelper.JALALI_MONTHS_FA[todayJalali[1] - 1] else CalendarHelper.JALALI_MONTHS_EN[todayJalali[1] - 1]
    val hijriMonthName = if (viewModel.language == "fa") CalendarHelper.HIJRI_MONTHS_FA[todayHijri[1] - 1] else CalendarHelper.HIJRI_MONTHS_EN[todayHijri[1] - 1]
    val gregMonthName = if (viewModel.language == "fa") CalendarHelper.GREGORIAN_MONTHS_FA[todayGreg[1] - 1] else CalendarHelper.GREGORIAN_MONTHS_EN[todayGreg[1] - 1]

    val formattedDateFull = if (viewModel.language == "fa") {
        "امروز: $weekdayStr، ${todayJalali[2]} $jalaliMonthName ${todayJalali[0]} | ${todayHijri[2]} $hijriMonthName ${todayHijri[0]} | ${todayGreg[2]} $gregMonthName ${todayGreg[0]}"
    } else {
        "Today: $weekdayStr, ${todayGreg[2]} $gregMonthName ${todayGreg[0]} | Shamsi: ${todayJalali[2]} $jalaliMonthName ${todayJalali[0]} | Hijri: ${todayHijri[2]} $hijriMonthName ${todayHijri[0]}"
    }

    val todayCal = remember { Calendar.getInstance() }
    val todayYear = remember(todayCal) { todayCal.get(Calendar.YEAR) }
    val todayDayOfYear = remember(todayCal) { todayCal.get(Calendar.DAY_OF_YEAR) }
    val todayStart = remember(todayCal) {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    // Filter tasks for today (tasks with today as due code, or no date if user wants to do today)
    val todayTasks = remember(tasks, todayYear, todayDayOfYear) {
        tasks.filter { task ->
            if (task.dueDate == null) true
            else {
                val elementCal = Calendar.getInstance().apply { timeInMillis = task.dueDate }
                elementCal.get(Calendar.YEAR) == todayYear &&
                        elementCal.get(Calendar.DAY_OF_YEAR) == todayDayOfYear
            }
        }
    }

    val totalTodayDone = remember(todayTasks) { todayTasks.count { it.isCompleted } }
    val progressRate = remember(todayTasks, totalTodayDone) { if (todayTasks.isEmpty()) 100 else (totalTodayDone * 100) / todayTasks.size }

    var showAddFastTaskDialog by remember { mutableStateOf(false) }
    var showAddHabitDialog by remember { mutableStateOf(false) }
    var showAdvancedSearchDialog by remember { mutableStateOf(false) }

    if (showAdvancedSearchDialog) {
        AdvancedSearchDialog(
            viewModel = viewModel,
            onDismiss = { showAdvancedSearchDialog = false }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Dynamic Rich Header
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = AppStrings.get("app_title"),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = formattedDateFull,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f),
                            fontSize = 11.sp
                        )
                    }

                    IconButton(
                        onClick = { showAdvancedSearchDialog = true },
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f),
                                RoundedCornerShape(12.dp)
                            )
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search Tasks",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        // Today highlight motivations message
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.ThumbUp,
                        contentDescription = "Idea",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp).padding(end = 8.dp)
                    )
                    Text(
                        text = if (progressRate >= 80) AppStrings.get("analysis_positive") else AppStrings.get("analysis_neutral"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Today productivity progress dial
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = AppStrings.get("completion_rate"),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = if (todayTasks.isEmpty()) {
                                AppStrings.get("no_tasks_today_completion")
                            } else {
                                "${todayTasks.size} ${AppStrings.get("tasks")} — $totalTodayDone ${AppStrings.get("percentage_done")}"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.7f)
                        )
                    }
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(72.dp)) {
                        CircularProgressIndicator(
                            progress = { if (todayTasks.isEmpty()) 0f else progressRate / 100f },
                            modifier = Modifier.size(70.dp),
                            strokeWidth = 8.dp,
                            color = if (todayTasks.isEmpty()) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Text(
                            text = if (todayTasks.isEmpty()) "—" else "$progressRate%",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (todayTasks.isEmpty()) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f) else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // STREAK & HABITS TRACKER (روزشمار عادت)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = AppStrings.get("streak_day_counter"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = AppStrings.get("streak_subtitle"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                    )
                }
                IconButton(onClick = { showAddHabitDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Habit", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        if (streaks.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = AppStrings.get("no_goals"),
                        color = MaterialTheme.colorScheme.onSurface.copy(0.5f),
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            items(streaks) { streak ->
                val isTickedToday = remember(streak.lastCheckDate, todayYear, todayDayOfYear) {
                    if (streak.lastCheckDate == 0L) false
                    else {
                        val checkCal = Calendar.getInstance().apply { timeInMillis = streak.lastCheckDate }
                        checkCal.get(Calendar.YEAR) == todayYear &&
                                checkCal.get(Calendar.DAY_OF_YEAR) == todayDayOfYear
                    }
                }

                var isExpanded by remember { mutableStateOf(false) }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (isTickedToday) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else Color.Transparent,
                            RoundedCornerShape(12.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isExpanded = !isExpanded }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = streak.name,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    // Accent Badge for Negative/Positive Custom Habits
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                if (streak.isNegative) MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (streak.isNegative) AppStrings.get("negative_habit") else AppStrings.get("positive_habit"),
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (streak.isNegative) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Toggle Details",
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Star, "Streak", tint = AccentYellow, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "${AppStrings.get("active_streaks")}: ${streak.currentStreak} ${AppStrings.get("days")}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(0.7f)
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, "Best Record", tint = AccentGreen, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "${AppStrings.get("best_streak")}: ${streak.bestStreak} ${AppStrings.get("days")}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(0.7f)
                                        )
                                    }
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Button(
                                    onClick = { viewModel.tickStreak(streak) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isTickedToday) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = if (isTickedToday) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                ) {
                                    Text(
                                        text = if (isTickedToday) AppStrings.get("un_tick") else {
                                            if (streak.isNegative) AppStrings.get("percentage_avoided") else AppStrings.get("tick")
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                IconButton(onClick = { viewModel.deleteStreak(streak.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(0.7f))
                                }
                            }
                        }

                        if (isExpanded) {
                            Divider(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                            HabitDetailsSection(streak, viewModel)
                        }
                    }
                }
            }
        }

        // TODAY TASKS LIST HEADER
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = AppStrings.get("all_tasks") + " (" + todayTasks.size + ")",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { showAddFastTaskDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Task", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        if (todayTasks.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(0.5f)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = AppStrings.get("no_tasks_today"),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                        )
                    }
                }
            }
        } else {
            items(todayTasks) { task ->
                val folder = folders.find { it.id == task.folderId }
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_item_card")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = task.isCompleted,
                            onCheckedChange = { viewModel.toggleTaskCompletion(task) },
                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = task.title,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (task.isCompleted) MaterialTheme.colorScheme.onSurface.copy(0.4f) else MaterialTheme.colorScheme.onSurface
                            )
                            if (task.description.isNotEmpty()) {
                                Text(
                                    text = task.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                                )
                            }
                            Row(
                                modifier = Modifier.padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Folder tag
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            if (folder != null) Color(android.graphics.Color.parseColor(folder.colorHex)).copy(
                                                alpha = 0.15f
                                            ) else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = folder?.name ?: AppStrings.get("no_folder"),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (folder != null) Color(android.graphics.Color.parseColor(folder.colorHex)) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Priority badge
                                val priorityPair = when (task.priority) {
                                    2 -> Pair(AppStrings.get("priority_high"), AccentRed)
                                    1 -> Pair(AppStrings.get("priority_med"), AccentYellow)
                                    else -> Pair(AppStrings.get("priority_low"), AccentGreen)
                                }
                                val priorityTxt = priorityPair.first
                                val pColor = priorityPair.second
                                Text(
                                    text = "• $priorityTxt",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = pColor
                                )

                                // Remind alert indicator
                                if (task.reminderTime != null) {
                                    Icon(
                                        Icons.Default.Notifications,
                                        contentDescription = "Alert On",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }

                        IconButton(onClick = { viewModel.deleteTask(task.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(0.6f))
                        }
                    }
                }
            }
        }
    }

    // Add fast task dialog
    if (showAddFastTaskDialog) {
        AddTaskDialog(
            viewModel = viewModel,
            onDismiss = { showAddFastTaskDialog = false }
        )
    }

    // Add Streak/Habit Dialog
    if (showAddHabitDialog) {
        var habitName by remember { mutableStateOf("") }
        var isNegativeHabit by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddHabitDialog = false },
            title = { Text(AppStrings.get("add_habit"), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = habitName,
                        onValueChange = { habitName = it },
                        label = { Text(AppStrings.get("habit_title")) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = AppStrings.get("habit_type") + ":",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { isNegativeHabit = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isNegativeHabit) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (!isNegativeHabit) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(AppStrings.get("positive_habit"), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { isNegativeHabit = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isNegativeHabit) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isNegativeHabit) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(AppStrings.get("negative_habit"), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (habitName.isNotEmpty()) {
                            viewModel.addStreak(habitName, isNegativeHabit)
                            showAddHabitDialog = false
                        }
                    }
                ) {
                    Text(AppStrings.get("save"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddHabitDialog = false }) {
                    Text(AppStrings.get("cancel"))
                }
            }
        )
    }
}

// -------------------------------------------------------------
// ADD TASK DIALOG
// -------------------------------------------------------------
@Composable
fun AddTaskDialog(viewModel: PlannerViewModel, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val folders by viewModel.folders.collectAsState()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedFolderId by remember { mutableStateOf<Long?>(null) }
    var priority by remember { mutableStateOf(1) } // 0 = Low, 1 = Med, 2 = High

    val initialDateCal = remember {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, viewModel.selectedCalendarYear)
            set(Calendar.MONTH, viewModel.selectedCalendarMonth - 1)
            set(Calendar.DAY_OF_MONTH, viewModel.selectedCalendarDay)
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
        }
    }
    val initialDateJal = remember {
        CalendarHelper.gregorianToJalali(viewModel.selectedCalendarYear, viewModel.selectedCalendarMonth, viewModel.selectedCalendarDay)
    }

    var dueDateText by remember { mutableStateOf(CalendarHelper.formatJalali(initialDateJal[0], initialDateJal[1], initialDateJal[2]) + " (${AppStrings.get("jalali")})") }
    var dueDateTimestamp by remember { mutableStateOf<Long?>(initialDateCal.timeInMillis) }
    var showDatePickerDialog by remember { mutableStateOf(false) }

    var reminderValText by remember { mutableStateOf("") }
    var reminderTimestamp by remember { mutableStateOf<Long?>(null) }

    if (showDatePickerDialog) {
        BilingualDatePickerDialog(
            onDismiss = { showDatePickerDialog = false },
            onDateSelected = { timestamp, formattedText ->
                dueDateTimestamp = timestamp
                dueDateText = formattedText
                showDatePickerDialog = false
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(AppStrings.get("add_task"), fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(AppStrings.get("task_title")) },
                    modifier = Modifier.fillMaxWidth().testTag("add_task_title_input")
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(AppStrings.get("task_subtitle")) },
                    modifier = Modifier.fillMaxWidth()
                )

                // Date Picker trigger with multi-calendar backing
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showDatePickerDialog = true }
                ) {
                    Text(
                        if (dueDateText.isEmpty()) AppStrings.get("due_date")
                        else "${AppStrings.get("due_date")}: $dueDateText"
                    )
                }

                // Reminder alarm trigger
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val cal = Calendar.getInstance()
                        TimePickerDialog(
                            context,
                            { _, hr, min ->
                                val selectedCal = Calendar.getInstance().apply {
                                    set(Calendar.HOUR_OF_DAY, hr)
                                    set(Calendar.MINUTE, min)
                                    set(Calendar.SECOND, 0)
                                }
                                reminderTimestamp = selectedCal.timeInMillis
                                reminderValText = "%02d:%02d".format(hr, min)
                            },
                            cal.get(Calendar.HOUR_OF_DAY),
                            cal.get(Calendar.MINUTE),
                            true
                        ).show()
                    }
                ) {
                    Text(
                        if (reminderValText.isEmpty()) AppStrings.get("remind_me_at")
                        else "${AppStrings.get("remind_me_at")}: $reminderValText"
                    )
                }

                // Folder category list picker
                Text(AppStrings.get("folder") + ":", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedFolderId == null,
                            onClick = { selectedFolderId = null },
                            label = { Text(AppStrings.get("no_folder")) }
                        )
                    }
                    items(folders.filter { it.type == "TASK" }) { folder ->
                        val parsedColor = remember(folder.colorHex) {
                            try {
                                Color(android.graphics.Color.parseColor(folder.colorHex))
                            } catch (e: Exception) {
                                Color.Gray
                            }
                        }
                        FilterChip(
                            selected = selectedFolderId == folder.id,
                            onClick = { selectedFolderId = folder.id },
                            label = { Text(folder.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = parsedColor.copy(0.2f),
                                selectedLabelColor = parsedColor
                            )
                        )
                    }
                }

                // Priority segmented selection
                Text(AppStrings.get("priority") + ":", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        0 to AppStrings.get("priority_low"),
                        1 to AppStrings.get("priority_med"),
                        2 to AppStrings.get("priority_high")
                    ).forEach { (pLvl, pLabel) ->
                        val isSelected = priority == pLvl
                        Button(
                            onClick = { priority = pLvl },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) {
                                    when (pLvl) {
                                        2 -> AccentRed
                                        1 -> AccentYellow
                                        else -> AccentGreen
                                    }
                                } else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(pLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotEmpty()) {
                        viewModel.addTask(
                            title = title,
                            description = description,
                            folderId = selectedFolderId,
                            dueDate = dueDateTimestamp,
                            reminderTime = reminderTimestamp,
                            priority = priority
                        )
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("dialog_submit_button")
            ) {
                Text(AppStrings.get("save"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(AppStrings.get("cancel"))
            }
        }
    )
}

// -------------------------------------------------------------
// TASKS TAB SCREEN (CARDS & CATEGORIES)
// -------------------------------------------------------------
@Composable
fun TasksTabScreen(viewModel: PlannerViewModel) {
    val tasks by viewModel.tasks.collectAsState()
    val folders by viewModel.folders.collectAsState()

    var showAddFolderDialog by remember { mutableStateOf(false) }
    var showAddTaskDialog by remember { mutableStateOf(false) }

    // Filter tasks for designated category
    val filteredTasks = remember(tasks, viewModel.selectedTaskFolderId) {
        if (viewModel.selectedTaskFolderId == null) {
            tasks
        } else {
            tasks.filter { it.folderId == viewModel.selectedTaskFolderId }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                AppStrings.get("folders"),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { showAddFolderDialog = true }) {
                Icon(Icons.Default.Add, "New Category", tint = MaterialTheme.colorScheme.primary)
            }
        }

        // Folder category chips horizontal slider selector
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = viewModel.selectedTaskFolderId == null,
                    onClick = { viewModel.selectedTaskFolderId = null },
                    label = { Text(AppStrings.get("all_categories")) }
                )
            }
            items(folders.filter { it.type == "TASK" }) { folder ->
                val fColor = remember(folder.colorHex) {
                    Color(android.graphics.Color.parseColor(folder.colorHex))
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = viewModel.selectedTaskFolderId == folder.id,
                        onClick = { viewModel.selectedTaskFolderId = folder.id },
                        label = { Text(folder.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = fColor.copy(0.2f),
                            selectedLabelColor = fColor
                        )
                    )
                    IconButton(
                        onClick = {
                            if (viewModel.selectedTaskFolderId == folder.id) viewModel.selectedTaskFolderId = null
                            viewModel.deleteFolder(folder.id)
                        },
                        modifier = Modifier.size(24.dp).padding(start = 2.dp)
                    ) {
                        Icon(Icons.Default.Delete, "Delete Folder", tint = Color.Red.copy(alpha = 0.5f), modifier = Modifier.size(14.dp))
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                AppStrings.get("tasks") + " (" + filteredTasks.size + ")",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = { showAddTaskDialog = true },
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(modifier = Modifier.width(4.dp))
                Text(AppStrings.get("add_task"))
            }
        }

        if (filteredTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = AppStrings.get("no_tasks_today"),
                    color = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredTasks) { task ->
                    val folder = folders.find { it.id == task.folderId }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = task.isCompleted,
                                onCheckedChange = { viewModel.toggleTaskCompletion(task) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = task.title,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurface.copy(0.4f) else MaterialTheme.colorScheme.onSurface
                                )
                                if (task.description.isNotEmpty()) {
                                    Text(
                                        text = task.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                                    )
                                }
                                Row(
                                    modifier = Modifier.padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (folder != null) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(android.graphics.Color.parseColor(folder.colorHex)).copy(0.15f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = folder.name,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(android.graphics.Color.parseColor(folder.colorHex))
                                            )
                                        }
                                    }

                                    // Priority badge
                                    val priorityPair = when (task.priority) {
                                        2 -> Pair(AppStrings.get("priority_high"), AccentRed)
                                        1 -> Pair(AppStrings.get("priority_med"), AccentYellow)
                                        else -> Pair(AppStrings.get("priority_low"), AccentGreen)
                                    }
                                    val priorityTxt = priorityPair.first
                                    val pColor = priorityPair.second
                                    Text(
                                        text = "• $priorityTxt",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = pColor
                                    )

                                    if (task.dueDate != null) {
                                        val dateStr = SimpleDateFormat(
                                            "yyyy/MM/dd",
                                            Locale.getDefault()
                                        ).format(Date(task.dueDate))
                                        Text(
                                            text = "• $dateStr",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                                        )
                                    }
                                }
                            }
                            IconButton(onClick = { viewModel.deleteTask(task.id) }) {
                                Icon(Icons.Default.Delete, "Delete", tint = Color.Red.copy(0.6f))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddFolderDialog) {
        CreateFolderDialog(viewModel, type = "TASK", onDismiss = { showAddFolderDialog = false })
    }

    if (showAddTaskDialog) {
        AddTaskDialog(viewModel, onDismiss = { showAddTaskDialog = false })
    }
}

// -------------------------------------------------------------
// GOALS TAB SCREEN (لیست اهداف کارهای شما)
// -------------------------------------------------------------
@Composable
fun GoalsTabScreen(viewModel: PlannerViewModel) {
    val goals by viewModel.goals.collectAsState()
    val folders by viewModel.folders.collectAsState()

    var showAddGoalDialog by remember { mutableStateOf(false) }
    var showAddFolderDialog by remember { mutableStateOf(false) }

    val filteredGoals = remember(goals, viewModel.selectedGoalFolderId) {
        if (viewModel.selectedGoalFolderId == null) {
            goals
        } else {
            goals.filter { it.folderId == viewModel.selectedGoalFolderId }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                AppStrings.get("folders"),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { showAddFolderDialog = true }) {
                Icon(Icons.Default.Add, "New Category", tint = MaterialTheme.colorScheme.primary)
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = viewModel.selectedGoalFolderId == null,
                    onClick = { viewModel.selectedGoalFolderId = null },
                    label = { Text(AppStrings.get("all_categories")) }
                )
            }
            items(folders.filter { it.type == "GOAL" }) { folder ->
                val fColor = remember(folder.colorHex) {
                    Color(android.graphics.Color.parseColor(folder.colorHex))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilterChip(
                        selected = viewModel.selectedGoalFolderId == folder.id,
                        onClick = { viewModel.selectedGoalFolderId = folder.id },
                        label = { Text(folder.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = fColor.copy(0.2f),
                            selectedLabelColor = fColor
                        )
                    )
                    IconButton(
                        onClick = {
                            if (viewModel.selectedGoalFolderId == folder.id) viewModel.selectedGoalFolderId = null
                            viewModel.deleteFolder(folder.id)
                        },
                        modifier = Modifier.size(24.dp).padding(start = 2.dp)
                    ) {
                        Icon(Icons.Default.Delete, "Delete Folder", tint = Color.Red.copy(alpha = 0.5f), modifier = Modifier.size(14.dp))
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                AppStrings.get("goals") + " (" + filteredGoals.size + ")",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = { showAddGoalDialog = true },
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(modifier = Modifier.width(4.dp))
                Text(AppStrings.get("add_goal"))
            }
        }

        if (filteredGoals.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = AppStrings.get("no_goals"),
                    color = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredGoals) { goal ->
                    val folder = folders.find { it.id == goal.folderId }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Checkbox(
                                        checked = goal.isCompleted,
                                        onCheckedChange = { viewModel.toggleGoalCompletion(goal) }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = goal.title,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (goal.isCompleted) MaterialTheme.colorScheme.onSurface.copy(0.4f) else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (goal.description.isNotEmpty()) {
                                            Text(
                                                text = goal.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                                            )
                                        }
                                    }
                                }
                                IconButton(onClick = { viewModel.deleteGoal(goal.id) }) {
                                    Icon(Icons.Default.Delete, "Delete", tint = Color.Red.copy(0.6f))
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Interactive Progress slider
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${AppStrings.get("progress")}: ${goal.progress}%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(90.dp)
                                )
                                Slider(
                                    value = goal.progress.toFloat(),
                                    onValueChange = { viewModel.updateGoalProgress(goal, it.toInt()) },
                                    valueRange = 0f..100f,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                if (folder != null) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(android.graphics.Color.parseColor(folder.colorHex)).copy(0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = folder.name,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(android.graphics.Color.parseColor(folder.colorHex))
                                        )
                                    }
                                }

                                if (goal.targetDate != null) {
                                    val dateStr = SimpleDateFormat(
                                        "yyyy/MM/dd",
                                        Locale.getDefault()
                                    ).format(Date(goal.targetDate))
                                    Text(
                                        text = "${AppStrings.get("goal_target")}: $dateStr",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddFolderDialog) {
        CreateFolderDialog(viewModel, type = "GOAL", onDismiss = { showAddFolderDialog = false })
    }

    if (showAddGoalDialog) {
        var title by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var selectedFolderId by remember { mutableStateOf<Long?>(null) }
        var targetText by remember { mutableStateOf("") }
        var targetTimestamp by remember { mutableStateOf<Long?>(null) }
        var showDatePickerInGoalDialog by remember { mutableStateOf(false) }

        // Family / Group synchronization options
        var isSharedGoal by remember { mutableStateOf(false) }
        var sharedGroupCode by remember { mutableStateOf("") }

        if (showDatePickerInGoalDialog) {
            BilingualDatePickerDialog(
                onDismiss = { showDatePickerInGoalDialog = false },
                onDateSelected = { timestamp, label ->
                    targetTimestamp = timestamp
                    targetText = label
                    showDatePickerInGoalDialog = false
                }
            )
        }

        AlertDialog(
            onDismissRequest = { showAddGoalDialog = false },
            title = { Text(AppStrings.get("add_goal"), fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(AppStrings.get("task_title")) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text(AppStrings.get("task_subtitle")) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Bilingual, modern Date Picker trigger
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { showDatePickerInGoalDialog = true }
                    ) {
                        Text(
                            if (targetText.isEmpty()) AppStrings.get("goal_target")
                            else "${AppStrings.get("goal_target")}: $targetText"
                        )
                    }

                    Divider(modifier = Modifier.padding(vertical = 4.dp))

                    // Group / Family Sync Toggle option
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = AppStrings.get("group"),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = if (isSharedGoal) AppStrings.get("group_sync_status") else "Local only",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                        Switch(
                            checked = isSharedGoal,
                            onCheckedChange = { isSharedGoal = it }
                        )
                    }

                    if (isSharedGoal) {
                        OutlinedTextField(
                            value = sharedGroupCode,
                            onValueChange = { sharedGroupCode = it },
                            label = { Text(AppStrings.get("group_code")) },
                            placeholder = { Text(AppStrings.get("group_code_placeholder")) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    Text(AppStrings.get("folder") + ":", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = selectedFolderId == null,
                                onClick = { selectedFolderId = null },
                                label = { Text(AppStrings.get("no_folder")) }
                            )
                        }
                        items(folders.filter { it.type == "GOAL" }) { folder ->
                            val fColor = remember(folder.colorHex) {
                                Color(android.graphics.Color.parseColor(folder.colorHex))
                            }
                            FilterChip(
                                selected = selectedFolderId == folder.id,
                                onClick = { selectedFolderId = folder.id },
                                label = { Text(folder.name) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = fColor.copy(0.2f),
                                    selectedLabelColor = fColor
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotEmpty()) {
                            viewModel.addGoal(
                                title,
                                description,
                                selectedFolderId,
                                targetTimestamp,
                                isSharedGoal,
                                if (isSharedGoal) sharedGroupCode else null
                            )
                            showAddGoalDialog = false
                        }
                    }
                ) {
                    Text(AppStrings.get("save"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddGoalDialog = false }) {
                    Text(AppStrings.get("cancel"))
                }
            }
        )
    }
}

// -------------------------------------------------------------
// FOLDER CREATING DIALOG
// -------------------------------------------------------------
@Composable
fun CreateFolderDialog(viewModel: PlannerViewModel, type: String, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#3B82F6") }

    val colorsList = listOf("#3B82F6", "#10B981", "#EF4444", "#F59E0B", "#8B5CF6", "#EC4899", "#14B8A6")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(AppStrings.get("add_folder"), fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(AppStrings.get("folder_name")) },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(AppStrings.get("color") + ":", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    colorsList.forEach { hex ->
                        val parsed = Color(android.graphics.Color.parseColor(hex))
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(parsed)
                                .border(
                                    2.dp,
                                    if (selectedColor == hex) MaterialTheme.colorScheme.onBackground else Color.Transparent,
                                    CircleShape
                                )
                                .clickable { selectedColor = hex }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotEmpty()) {
                        viewModel.createFolder(name, selectedColor, "Folder", type)
                        onDismiss()
                    }
                }
            ) {
                Text(AppStrings.get("save"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(AppStrings.get("cancel"))
            }
        }
    )
}

// -------------------------------------------------------------
// 3-WAY FUNCTIONAL CALENDAR SCREEN (شمسی، قمری، میلادی)
// -------------------------------------------------------------
@Composable
fun CalendarTabScreen(viewModel: PlannerViewModel) {
    var calendarYear by remember { mutableStateOf(viewModel.selectedCalendarYear) }
    var calendarMonth by remember { mutableStateOf(viewModel.selectedCalendarMonth) }

    val context = LocalContext.current
    val tasks by viewModel.tasks.collectAsState()

    // Determine calendar headings
    val activeCalLabel = when (viewModel.activeCalendarViewType) {
        0 -> AppStrings.get("jalali")
        1 -> AppStrings.get("hijri")
        else -> AppStrings.get("gregorian")
    }

    // Convert active month and year to localized names
    val monthTitle = when (viewModel.activeCalendarViewType) {
        0 -> { // Shamsi
            val currentJalali = CalendarHelper.gregorianToJalali(calendarYear, calendarMonth, 15)
            val monthIdx = currentJalali[1] - 1
            val mName = if (viewModel.language == "fa") CalendarHelper.JALALI_MONTHS_FA[monthIdx] else CalendarHelper.JALALI_MONTHS_EN[monthIdx]
            "$mName ${currentJalali[0]}"
        }
        1 -> { // Hijri
            val currentHijri = CalendarHelper.gregorianToHijri(calendarYear, calendarMonth, 15)
            val monthIdx = currentHijri[1] - 1
            val mName = if (viewModel.language == "fa") CalendarHelper.HIJRI_MONTHS_FA[monthIdx] else CalendarHelper.HIJRI_MONTHS_EN[monthIdx]
            "$mName ${currentHijri[0]}"
        }
        else -> { // Gregorian
            val monthIdx = calendarMonth - 1
            val mName = if (viewModel.language == "fa") CalendarHelper.GREGORIAN_MONTHS_FA[monthIdx] else CalendarHelper.GREGORIAN_MONTHS_EN[monthIdx]
            "$mName $calendarYear"
        }
    }

    // List of day cells for active month grid
    val daysInActiveMonth = when (viewModel.activeCalendarViewType) {
        0 -> { // Jalali Shamsi Days calculation
            val jalali1st = CalendarHelper.gregorianToJalali(calendarYear, calendarMonth, 1)
            val monthLength = CalendarHelper.getJalaliMonthLength(jalali1st[0], jalali1st[1])
            (1..monthLength).toList()
        }
        1 -> { // Hijri Qamari Days calculation
            val hijri1st = CalendarHelper.gregorianToHijri(calendarYear, calendarMonth, 1)
            val monthLength = CalendarHelper.getHijriMonthLength(hijri1st[0], hijri1st[1])
            (1..monthLength).toList()
        }
        else -> { // Miladi days
            val cal = GregorianCalendar(calendarYear, calendarMonth - 1, 1)
            val monthLength = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            (1..monthLength).toList()
        }
    }

    // Weekday column headings
    val localizedWeekdays = if (viewModel.language == "fa") CalendarHelper.WEEKDAYS_FA else CalendarHelper.WEEKDAYS_EN

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Multi-calendar presets buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(0 to AppStrings.get("jalali"), 1 to AppStrings.get("hijri"), 2 to AppStrings.get("gregorian")).forEach { (idx, label) ->
                val isSelected = viewModel.activeCalendarViewType == idx
                Button(
                    onClick = { viewModel.activeCalendarViewType = idx },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Calendar month header controller
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        // Previous Month
                        calendarMonth--
                        if (calendarMonth == 0) {
                            calendarMonth = 12
                            calendarYear--
                        }
                    }
                ) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Prev Month")
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        activeCalLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        monthTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = {
                        // Next Month
                        calendarMonth++
                        if (calendarMonth == 13) {
                            calendarMonth = 1
                            calendarYear++
                        }
                    }
                ) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Next Month")
                }
            }
        }

        // Weekdays columns headings
        Row(modifier = Modifier.fillMaxWidth()) {
            localizedWeekdays.forEach { label ->
                Text(
                    text = label.take(2),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                )
            }
        }

        // Grid cells drawing (Custom Row & Columns for responsiveness)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                val chunks = daysInActiveMonth.chunked(7)
                chunks.forEach { weekDays ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        // Align empty cells to starts if needed
                        if (weekDays.size < 7 && chunks.indexOf(weekDays) == 0) {
                            val pads = 7 - weekDays.size
                            repeat(pads) { Spacer(modifier = Modifier.weight(1f)) }
                        }

                        weekDays.forEach { cellDay ->
                            val isSelectedDay = cellDay == viewModel.selectedCalendarDay &&
                                    calendarMonth == viewModel.selectedCalendarMonth &&
                                    calendarYear == viewModel.selectedCalendarYear

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(2.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelectedDay) MaterialTheme.colorScheme.primaryContainer
                                        else Color.Transparent
                                    )
                                    .clickable {
                                        viewModel.selectedCalendarDay = cellDay
                                        viewModel.selectedCalendarMonth = calendarMonth
                                        viewModel.selectedCalendarYear = calendarYear
                                    }
                            ) {
                                Text(
                                    text = cellDay.toString(),
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelectedDay) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelectedDay) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        if (weekDays.size < 7 && chunks.indexOf(weekDays) > 0) {
                            val pads = 7 - weekDays.size
                            repeat(pads) { Spacer(modifier = Modifier.weight(1f)) }
                        }
                    }
                }
            }
        }

        // Actions listing scheduled on selected date
        val selectedDateFormatted = remember(viewModel.selectedCalendarYear, viewModel.selectedCalendarMonth, viewModel.selectedCalendarDay) {
            val jal = CalendarHelper.gregorianToJalali(viewModel.selectedCalendarYear, viewModel.selectedCalendarMonth, viewModel.selectedCalendarDay)
            "${jal[0]}/${"%02d".format(jal[1])}/${"%02d".format(jal[2])} (${AppStrings.get("jalali")})"
        }

        var showAddTaskForDayDialog by remember { mutableStateOf(false) }

        if (showAddTaskForDayDialog) {
            AddTaskDialog(
                viewModel = viewModel,
                onDismiss = { showAddTaskForDayDialog = false }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${AppStrings.get("tasks")} ($selectedDateFormatted):",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = { showAddTaskForDayDialog = true },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Schedule", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (viewModel.language == "fa") "ثبت کار برای امروز" else "Schedule Task",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Filter scheduled tasks
        val dateTasks = remember(tasks, viewModel.selectedCalendarYear, viewModel.selectedCalendarMonth, viewModel.selectedCalendarDay) {
            tasks.filter { task ->
                if (task.dueDate == null) false
                else {
                    val taskCal = Calendar.getInstance().apply { timeInMillis = task.dueDate }
                    taskCal.get(Calendar.YEAR) == viewModel.selectedCalendarYear &&
                            taskCal.get(Calendar.MONTH) == (viewModel.selectedCalendarMonth - 1) &&
                            taskCal.get(Calendar.DAY_OF_MONTH) == viewModel.selectedCalendarDay
                }
            }
        }

        if (dateTasks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(AppStrings.get("no_tasks_today"), color = MaterialTheme.colorScheme.onSurface.copy(0.5f), fontSize = 12.sp)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                items(dateTasks) { task ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(task.isCompleted, onCheckedChange = { viewModel.toggleTaskCompletion(task) })
                                Column {
                                    Text(task.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    if (task.description.isNotEmpty()) {
                                        Text(task.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                                    }
                                }
                            }
                            IconButton(onClick = { viewModel.deleteTask(task.id) }) {
                                Icon(Icons.Default.Delete, "Delete", tint = Color.Red.copy(0.6f))
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// ANALYTICS & HIGH CONTRAST CUSTOM CHART SCREEN (تحلیل بازدهی)
// -------------------------------------------------------------
@Composable
fun AnalyticsTabScreen(viewModel: PlannerViewModel) {
    val tasks by viewModel.tasks.collectAsState()
    val folders by viewModel.folders.collectAsState()
    val isFa = viewModel.language == "fa"

    val labelDays = remember(isFa) {
        if (isFa) {
            listOf("شنبه", "۱ش", "۲ش", "۳ش", "۴ش", "۵ش", "جمعه")
        } else {
            listOf("Sat", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri")
        }
    }

    // Compute actual 7-day completion rates reactively from database state
    val real7DaysPerformance = remember(tasks) {
        val list = mutableListOf<Float>()
        // Map last 7 calendar days
        for (i in 6 downTo 0) {
            val targetCal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -i)
            }
            val dayTasks = tasks.filter { task ->
                if (task.dueDate == null) false
                else {
                    val taskCal = Calendar.getInstance().apply { timeInMillis = task.dueDate }
                    taskCal.get(Calendar.YEAR) == targetCal.get(Calendar.YEAR) &&
                            taskCal.get(Calendar.DAY_OF_YEAR) == targetCal.get(Calendar.DAY_OF_YEAR)
                }
            }
            if (dayTasks.isEmpty()) {
                list.add(0f)
            } else {
                val done = dayTasks.count { it.isCompleted }
                list.add((done * 100f) / dayTasks.size)
            }
        }
        list
    }

    // Weekly average calculation
    val weeklyAverage = remember(real7DaysPerformance) {
        if (real7DaysPerformance.isEmpty()) 0 else (real7DaysPerformance.sum() / 7f).toInt()
    }

    // Peak performance calculation
    val peakPerformance = remember(real7DaysPerformance) {
        real7DaysPerformance.maxOrNull()?.toInt() ?: 0
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val accentColor = MaterialTheme.colorScheme.secondary

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                AppStrings.get("analytics"),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        // Insight Card Analysis
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = AppStrings.get("stats_summary"),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 14.sp
                    )
                    Text(
                        text = if (weeklyAverage >= 60) AppStrings.get("analysis_positive") else AppStrings.get("analysis_neutral"),
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(0.9f),
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Custom High-Fidelity Draw Canvas Chart
        item {
            Text(
                AppStrings.get("last_7_days"),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Background gridlines
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        val gridLinesCount = 4
                        for (i in 0..gridLinesCount) {
                            val y = (h / gridLinesCount) * i
                            drawLine(
                                color = Color.Gray.copy(alpha = 0.12f),
                                start = Offset(0f, y),
                                end = Offset(w, y),
                                strokeWidth = 1.dp.toPx()
                            )
                        }

                        // Compute points coordinates
                        val stepX = w / 6f
                        val points = real7DaysPerformance.mapIndexed { index, percent ->
                            val x = index * stepX
                            val y = h - (h * (percent / 100f))
                            Offset(x, y)
                        }

                        // Draw smooth curve paths
                        val path = Path().apply {
                            if (points.isNotEmpty()) {
                                moveTo(points[0].x, points[0].y)
                                for (i in 1 until points.size) {
                                    val prev = points[i - 1]
                                    val curr = points[i]
                                    val controlX = (prev.x + curr.x) / 2
                                    val controlY = (prev.y + curr.y) / 2
                                    quadraticTo(prev.x, prev.y, controlX, controlY)
                                }
                                lineTo(points.last().x, points.last().y)
                            }
                        }

                        // Stroke Path (Draw Line Chart)
                        drawPath(
                            path = path,
                            color = primaryColor,
                            style = Stroke(width = 3.dp.toPx())
                        )

                        // Fill gradient below curve
                        val filledPath = Path().apply {
                            addPath(path)
                            if (points.isNotEmpty()) {
                                lineTo(points.last().x, h)
                                lineTo(points.first().x, h)
                                close()
                            }
                        }
                        drawPath(
                            path = filledPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(primaryColor.copy(alpha = 0.25f), Color.Transparent)
                            )
                        )

                        // Draw circular anchors data markers
                        points.forEachIndexed { idx, pt ->
                            drawCircle(
                                color = accentColor,
                                radius = 5.dp.toPx(),
                                center = pt
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 2.5.dp.toPx(),
                                center = pt
                            )
                        }
                    }

                    // Days axis labels row overlay bottom
                    Row(
                        modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        labelDays.forEach { dLabel ->
                            Text(
                                text = dLabel,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }

        // Performance efficiency metrics logs summary
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "$weeklyAverage%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AccentGreen)
                        Text(text = if (viewModel.language == "fa") "میانگین این هفته" else "Avg This Week", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                    }
                    Divider(modifier = Modifier.height(36.dp).width(1.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "$peakPerformance%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = primaryColor)
                        Text(text = if (viewModel.language == "fa") "بهترین عملکرد" else "Peak Performance", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                    }
                }
            }
        }

        // Pie Arc segmentation for Categories / Folders
        item {
            CategoryPieChart(tasks = tasks, folders = folders)
        }

        // Dynamic Priority allocation distribution progress bar
        item {
            PriorityBreakdownChart(tasks = tasks)
        }
    }
}

@Composable
fun CategoryPieChart(tasks: List<Task>, folders: List<com.example.data.Folder>) {
    val isFa = AppStrings.activeLanguage == "fa"
    val folderCounts = remember(tasks, folders) {
        val map = mutableMapOf<String, Int>()
        tasks.forEach { task ->
            val folder = folders.find { it.id == task.folderId }
            val name = folder?.name ?: AppStrings.get("all_categories")
            map[name] = (map[name] ?: 0) + 1
        }
        map
    }

    val totalTasks = folderCounts.values.sum()

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = if (isFa) "سهم هر پوشه از کل برنامه‌ها" else "Folder Task Distribution",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )

            if (totalTasks == 0) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = AppStrings.get("no_results"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.4f)
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val colorsList = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary,
                        MaterialTheme.colorScheme.tertiary,
                        Color(0xFFE15A3E),
                        Color(0xFF0F9D58),
                        Color(0xFF6366F1),
                        Color(0xFFEC4899)
                    )

                    // Draw Pie
                    Canvas(modifier = Modifier.size(110.dp)) {
                        var startAngle = -90f
                        folderCounts.entries.forEachIndexed { idx, entry ->
                            val sweepAngle = (entry.value.toFloat() / totalTasks) * 360f
                            val color = colorsList[idx % colorsList.size]
                            drawArc(
                                color = color,
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = true
                            )
                            startAngle += sweepAngle
                        }
                    }

                    // Legends side list
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        folderCounts.entries.forEachIndexed { idx, entry ->
                            val color = colorsList[idx % colorsList.size]
                            val percent = ((entry.value.toFloat() / totalTasks) * 100).toInt()
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(modifier = Modifier.size(8.dp).background(color, RoundedCornerShape(2.dp)))
                                Text(
                                    text = "${entry.key} ($percent%)",
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PriorityBreakdownChart(tasks: List<Task>) {
    val isFa = AppStrings.activeLanguage == "fa"
    val counts = remember(tasks) {
        val array = intArrayOf(0, 0, 0) // Low (0), Med (1), High (2)
        tasks.forEach { task ->
            if (task.priority in 0..2) {
                array[task.priority]++
            }
        }
        array
    }

    val total = counts.sum()

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = if (isFa) "تفکیک برنامه‌ها بر اساس اهمیت" else "Task Priority Allocation",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )

            if (total == 0) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = AppStrings.get("no_results"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.4f)
                    )
                }
            } else {
                val priorities = listOf(
                    Triple(2, AppStrings.get("priority_high"), MaterialTheme.colorScheme.error),
                    Triple(1, AppStrings.get("priority_med"), MaterialTheme.colorScheme.secondary),
                    Triple(0, AppStrings.get("priority_low"), MaterialTheme.colorScheme.outline)
                )

                priorities.forEach { (pCode, label, color) ->
                    val count = counts[pCode]
                    val fraction = if (total == 0) 0f else count.toFloat() / total

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("$count (${(fraction * 100).toInt()}%)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
                        }
                        LinearProgressIndicator(
                            progress = { fraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = color,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SETTINGS TAB SCREEN & CLOUD SYNC CONFIGS
// -------------------------------------------------------------
@Composable
fun SettingsTabScreen(viewModel: PlannerViewModel) {
    var showSyncSuccessSnackbar by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            AppStrings.get("settings"),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        // Language Select segment
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = AppStrings.get("language"),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { viewModel.setAppLanguage("fa") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (viewModel.language == "fa") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (viewModel.language == "fa") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("فارسی (Persian)")
                    }
                    Button(
                        onClick = { viewModel.setAppLanguage("en") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (viewModel.language == "en") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (viewModel.language == "en") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("English")
                    }
                }
            }
        }

        // Theme Preset selection
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = AppStrings.get("select_theme"),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("light" to AppStrings.get("theme_light"), "dark" to AppStrings.get("theme_dark"), "amoled" to AppStrings.get("theme_amoled")).forEach { (preset, label) ->
                        val isSel = viewModel.themePreset == preset
                        Button(
                            onClick = { viewModel.setAppTheme(preset) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }

        // Simulated Cloud Synchronization backup
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = AppStrings.get("cloud_sync"),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = if (viewModel.language == "fa") "برنامه‌ها، اهداف و روزشمار‌ها را بر روی حساب کاربری ابری ذخیره و در سایر دستگاه‌ها همگام و بازیابی کنید."
                    else "Backup and restore your local tasks, goals, & streaks on secure account cloud storage to sync on other device screens.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                if (viewModel.lastSyncTime > 0L) {
                    val dateFormatted = SimpleDateFormat(
                        "yyyy/MM/dd HH:mm:ss",
                        Locale.getDefault()
                    ).format(Date(viewModel.lastSyncTime))
                    Text(
                        text = "${AppStrings.get("last_sync")}: $dateFormatted",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentGreen
                    )
                }

                Button(
                    onClick = {
                        viewModel.performCloudSync()
                        showSyncSuccessSnackbar = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !viewModel.isSyncing
                ) {
                    if (viewModel.isSyncing) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                    } else {
                        Icon(Icons.Default.Refresh, null)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(AppStrings.get("sync_now"))
                }
            }
        }

        // Feedback / Alerts history
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(AppStrings.get("important_alerts") + ":", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                Text(
                    text = if (viewModel.language == "fa") "سیستم هوشمند پایش فردا فعال است." else "Smart notification systems are operational.",
                    fontSize = 11.sp
                )
            }
        }

        // Dynamic Snackbar notifier
        if (showSyncSuccessSnackbar && !viewModel.isSyncing) {
            Snackbar(
                action = {
                    TextButton(onClick = { showSyncSuccessSnackbar = false }) {
                        Text(if (viewModel.language == "fa") "باشه" else "Dismiss", color = MaterialTheme.colorScheme.primary)
                    }
                },
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text(AppStrings.get("synced_just_now"), fontSize = 12.sp)
            }
        }
    }
}

data class DayProgress(
    val timeMs: Long,
    val label: String,
    val isToday: Boolean,
    val isTicked: Boolean
)

@Composable
fun HabitDetailsSection(streak: Streak, viewModel: PlannerViewModel) {
    val isFa = viewModel.language == "fa"
    var showFullHistoryDialog by remember { mutableStateOf(false) }
    
    // Parse individual midnight timestamp strings from streak.historyJson safely
    val parsedHistory = remember(streak.historyJson) {
        if (streak.historyJson.isBlank()) {
            emptyList<Long>()
        } else {
            streak.historyJson.split(",")
                .mapNotNull { it.trim().toLongOrNull() }
                .distinct()
                .sorted()
        }
    }

    val todayStart = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    
    val dayInMs = 24 * 60 * 60 * 1000L
    
    // Last 7 days midnight timestamps (from oldest to newest: todayStart - 6 days ago, up to todayStart)
    val last7Days = remember(todayStart) {
        (6 downTo 0).map { todayStart - it * dayInMs }
    }

    // Number of days completed out of the last 7 days (No Calendar allocation)
    val completedInLast7Days = remember(parsedHistory, last7Days) {
        last7Days.count { dayTime ->
            parsedHistory.any { Math.abs(it - dayTime) < 12 * 60 * 60 * 1000L }
        }
    }

    val weeklyCompletionRate = (completedInLast7Days * 100f / 7f).toInt()

    // Pre-calculate loop objects to make drawing butter-smooth with ZERO allocations
    val daysProgressList = remember(parsedHistory, last7Days, isFa, todayStart) {
        last7Days.map { dayTime ->
            val isTickedOnDay = parsedHistory.any { Math.abs(it - dayTime) < 12 * 60 * 60 * 1000L }
            val cal = Calendar.getInstance().apply { timeInMillis = dayTime }
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            val dayLabel = if (isFa) {
                when (dayOfWeek) {
                    Calendar.SATURDAY -> "ش"
                    Calendar.SUNDAY -> "ی"
                    Calendar.MONDAY -> "د"
                    Calendar.TUESDAY -> "س"
                    Calendar.WEDNESDAY -> "چ"
                    Calendar.THURSDAY -> "پ"
                    Calendar.FRIDAY -> "ج"
                    else -> ""
                }
            } else {
                when (dayOfWeek) {
                    Calendar.SATURDAY -> "Sa"
                    Calendar.SUNDAY -> "Su"
                    Calendar.MONDAY -> "Mo"
                    Calendar.TUESDAY -> "Tu"
                    Calendar.WEDNESDAY -> "We"
                    Calendar.THURSDAY -> "Th"
                    Calendar.FRIDAY -> "Fr"
                    else -> ""
                }
            }
            val isToday = Math.abs(dayTime - todayStart) < 12 * 60 * 60 * 1000L
            DayProgress(
                timeMs = dayTime,
                label = dayLabel,
                isToday = isToday,
                isTicked = isTickedOnDay
            )
        }
    }

    // Pre-calculate history logs to bypass Calendar overhead inside loops
    val recentHistoryLogs = remember(parsedHistory, isFa) {
        parsedHistory.takeLast(5).reversed().map { dateMs ->
            val cal = Calendar.getInstance().apply { timeInMillis = dateMs }
            val gy = cal.get(Calendar.YEAR)
            val gm = cal.get(Calendar.MONTH) + 1
            val gd = cal.get(Calendar.DAY_OF_MONTH)
            val label = if (isFa) {
                val jal = CalendarHelper.gregorianToJalali(gy, gm, gd)
                val monthName = CalendarHelper.JALALI_MONTHS_FA[jal[1] - 1]
                "${jal[2]} $monthName ${jal[0]}"
            } else {
                val monthName = CalendarHelper.GREGORIAN_MONTHS_EN[gm - 1]
                "$monthName $gd, $gy"
            }
            Pair(dateMs, label)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Daily Grid Chart / Row
        Text(
            text = AppStrings.get("performance"),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            daysProgressList.forEach { progressItem ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = progressItem.label,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (progressItem.isToday) FontWeight.Bold else FontWeight.Normal,
                        color = if (progressItem.isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                if (progressItem.isTicked) {
                                    if (streak.isNegative) MaterialTheme.colorScheme.error else AccentGreen
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                }
                            )
                            .border(
                                width = 1.dp,
                                color = if (progressItem.isToday) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (progressItem.isTicked) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Ticked",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                            )
                        }
                    }
                }
            }
        }

        // Mini progress summary
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = AppStrings.get("habit_progress") + ":",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = "$weeklyCompletionRate% ($completedInLast7Days/7 " + (if (isFa) "روز" else "days") + ")",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (weeklyCompletionRate >= 60) AccentGreen else MaterialTheme.colorScheme.primary
            )
        }

        LinearProgressIndicator(
            progress = { weeklyCompletionRate / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = if (streak.isNegative) MaterialTheme.colorScheme.error else AccentGreen,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        // Historical entries (detailed listing of past 7 ticked dates)
        Text(
            text = AppStrings.get("history") + ":",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        if (recentHistoryLogs.isEmpty()) {
            Text(
                text = AppStrings.get("no_history"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 4.dp)
            )
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                recentHistoryLogs.forEach { logItem ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Logged Date",
                            tint = if (streak.isNegative) MaterialTheme.colorScheme.error.copy(alpha = 0.7f) else AccentGreen.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = logItem.second,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = if (streak.isNegative) AppStrings.get("percentage_avoided") else AppStrings.get("status_completed"),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (streak.isNegative) MaterialTheme.colorScheme.error else AccentGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (parsedHistory.isNotEmpty()) {
            OutlinedButton(
                onClick = { showFullHistoryDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = AppStrings.get("show_full_history"),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    if (showFullHistoryDialog) {
        FullHabitHistoryDialog(
            streak = streak,
            parsedHistory = parsedHistory,
            isFa = isFa,
            onDismiss = { showFullHistoryDialog = false }
        )
    }
}

@Composable
fun FullHabitHistoryDialog(
    streak: Streak,
    parsedHistory: List<Long>,
    isFa: Boolean,
    onDismiss: () -> Unit
) {
    val allHistoryLogs = remember(parsedHistory, isFa) {
        parsedHistory.reversed().map { dateMs ->
            val cal = Calendar.getInstance().apply { timeInMillis = dateMs }
            val gy = cal.get(Calendar.YEAR)
            val gm = cal.get(Calendar.MONTH) + 1
            val gd = cal.get(Calendar.DAY_OF_MONTH)
            val label = if (isFa) {
                val jal = CalendarHelper.gregorianToJalali(gy, gm, gd)
                val monthName = CalendarHelper.JALALI_MONTHS_FA[jal[1] - 1]
                "${jal[2]} $monthName ${jal[0]}"
            } else {
                val monthName = CalendarHelper.GREGORIAN_MONTHS_EN[gm - 1]
                "$monthName $gd, $gy"
            }
            label
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = streak.name,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = AppStrings.get("all_records"),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 280.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Info statistical card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = AccentYellow,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = AppStrings.get("days_total"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "${parsedHistory.size} " + (if (isFa) "روز" else "days"),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                // Scrollable log list
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(allHistoryLogs) { dateLabel ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                tint = if (streak.isNegative) MaterialTheme.colorScheme.error.copy(alpha = 0.7f) else AccentGreen.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = dateLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = if (streak.isNegative) AppStrings.get("percentage_avoided") else AppStrings.get("status_completed"),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (streak.isNegative) MaterialTheme.colorScheme.error else AccentGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = AppStrings.get("close"),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

