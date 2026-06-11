package com.example.ui

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.util.AlarmScheduler
import com.example.util.AppStrings
import com.example.util.CalendarHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class PlannerViewModel(
    private val application: Application,
    private val repository: PlannerRepository
) : AndroidViewModel(application) {

    // Persistent storage for preferences
    private val prefs = application.getSharedPreferences("smart_planner_prefs", Context.MODE_PRIVATE)

    // Language state
    var language by mutableStateOf(prefs.getString("lang", "fa") ?: "fa")
        private set

    // Theme state: "light", "dark", "amoled"
    var themePreset by mutableStateOf(prefs.getString("theme", "dark") ?: "dark")
        private set

    // Selected folders filter (null means "all")
    var selectedTaskFolderId by mutableStateOf<Long?>(null)
    var selectedGoalFolderId by mutableStateOf<Long?>(null)

    // Calendar standard state (Gregorian year, month, day)
    var selectedCalendarYear by mutableStateOf(CalendarHelper.getTodayGregorian()[0])
    var selectedCalendarMonth by mutableStateOf(CalendarHelper.getTodayGregorian()[1])
    var selectedCalendarDay by mutableStateOf(CalendarHelper.getTodayGregorian()[2])

    // Current active calendar tab in calendar screen: 0 = Shamsi, 1 = Qamari, 2 = Miladi
    var activeCalendarViewType by mutableStateOf(0)

    // Sync state
    var isSyncing by mutableStateOf(false)
    var lastSyncTime by mutableStateOf(prefs.getLong("last_sync_time", 0L))

    init {
        AppStrings.activeLanguage = language
    }

    // Setters for localized state
    fun setAppLanguage(lang: String) {
        language = lang
        AppStrings.activeLanguage = lang
        prefs.edit().putString("lang", lang).apply()
    }

    fun setAppTheme(theme: String) {
        themePreset = theme
        prefs.edit().putString("theme", theme).apply()
    }

    // DB flows
    val folders: StateFlow<List<Folder>> = repository.allFolders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasks: StateFlow<List<Task>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val goals: StateFlow<List<Goal>> = repository.allGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val streaks: StateFlow<List<Streak>> = repository.allStreaks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Simulated cloud backup sync
    fun performCloudSync() {
        viewModelScope.launch {
            isSyncing = true
            kotlinx.coroutines.delay(1800) // Realistic server-side network round-trip simulation
            lastSyncTime = System.currentTimeMillis()
            prefs.edit().putLong("last_sync_time", lastSyncTime).apply()
            isSyncing = false
        }
    }

    // Folders actions
    fun createFolder(name: String, colorHex: String, iconName: String, type: String) {
        viewModelScope.launch {
            repository.insertFolder(Folder(name = name, colorHex = colorHex, iconName = iconName, type = type))
        }
    }

    fun deleteFolder(id: Long) {
        viewModelScope.launch {
            repository.deleteFolder(id)
        }
    }

    // Tasks Actions
    fun addTask(title: String, description: String, folderId: Long?, dueDate: Long?, reminderTime: Long?, priority: Int) {
        viewModelScope.launch {
            val task = Task(
                title = title,
                description = description,
                folderId = folderId,
                dueDate = dueDate,
                reminderTime = reminderTime,
                priority = priority
            )
            val generatedId = repository.insertTask(task)
            
            // Set Alarm if remind time designated
            if (reminderTime != null && reminderTime > System.currentTimeMillis()) {
                val updatedTask = task.copy(id = generatedId)
                AlarmScheduler.scheduleTaskReminder(application, updatedTask)
            }
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            val isCheckedNew = !task.isCompleted
            val completedTime = if (isCheckedNew) System.currentTimeMillis() else null
            val updated = task.copy(isCompleted = isCheckedNew, completionDate = completedTime)
            repository.updateTask(updated)
            
            if (isCheckedNew) {
                // If checked complete, cancel alarm
                AlarmScheduler.cancelTaskReminder(application, task.id)
            } else {
                // re-schedule alarm if it has time and is upcoming
                if (task.reminderTime != null && task.reminderTime > System.currentTimeMillis()) {
                    AlarmScheduler.scheduleTaskReminder(application, updated)
                }
            }
        }
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch {
            AlarmScheduler.cancelTaskReminder(application, id)
            repository.deleteTask(id)
        }
    }

    // Goals Actions
    fun addGoal(title: String, description: String, folderId: Long?, targetDate: Long?, isShared: Boolean = false, sharedGroupCode: String? = null) {
        viewModelScope.launch {
            repository.insertGoal(Goal(
                title = title,
                description = description,
                folderId = folderId,
                targetDate = targetDate,
                isShared = isShared,
                sharedGroupCode = sharedGroupCode
            ))
        }
    }

    fun updateGoalProgress(goal: Goal, progress: Int) {
        viewModelScope.launch {
            val updated = goal.copy(
                progress = progress,
                isCompleted = progress >= 100
            )
            repository.updateGoal(updated)
        }
    }

    fun toggleGoalCompletion(goal: Goal) {
        viewModelScope.launch {
            val isCheckedNew = !goal.isCompleted
            val updated = goal.copy(
                isCompleted = isCheckedNew,
                progress = if (isCheckedNew) 100 else 0
            )
            repository.updateGoal(updated)
        }
    }

    fun deleteGoal(id: Long) {
        viewModelScope.launch {
            repository.deleteGoal(id)
        }
    }

    // Habits Streaks Actions (روزشمار)
    fun addStreak(name: String, isNegative: Boolean = false) {
        viewModelScope.launch {
            repository.insertStreak(Streak(name = name, isNegative = isNegative))
        }
    }

    fun tickStreak(streak: Streak) {
        viewModelScope.launch {
            val todayStart = getTodayStartTimestamp()
            val dayInMs = 24 * 60 * 60 * 1000L

            val lastCheck = streak.lastCheckDate
            val isTickedToday = isSameDay(lastCheck, todayStart)

            if (isTickedToday) {
                // Untick today (toggle off)
                val currentHist = streak.historyJson.split(",").toMutableList()
                val todayStr = todayStart.toString()
                currentHist.remove(todayStr)
                
                var prevStreak = streak.currentStreak - 1
                if (prevStreak < 0) prevStreak = 0

                val updated = streak.copy(
                    lastCheckDate = if (currentHist.isNotEmpty()) currentHist.last().toLong() else 0L,
                    currentStreak = prevStreak,
                    historyJson = currentHist.joinToString(",")
                )
                repository.updateStreak(updated)
            } else {
                // Tick for today
                val currentHist = if (streak.historyJson.isEmpty()) mutableListOf() else streak.historyJson.split(",").toMutableList()
                currentHist.add(todayStart.toString())

                var nextStreak = 1
                val isYesterdayTicked = isSameDay(lastCheck, todayStart - dayInMs)
                if (isYesterdayTicked) {
                    nextStreak = streak.currentStreak + 1
                }

                val originalBest = streak.bestStreak
                val nextBest = if (nextStreak > originalBest) nextStreak else originalBest

                val updated = streak.copy(
                    lastCheckDate = todayStart,
                    currentStreak = nextStreak,
                    bestStreak = nextBest,
                    historyJson = currentHist.joinToString(",")
                )
                repository.updateStreak(updated)
            }
        }
    }

    fun deleteStreak(id: Long) {
        viewModelScope.launch {
            repository.deleteStreak(id)
        }
    }

    private fun isSameDay(time1: Long, time2: Long): Boolean {
        if (time1 == 0L || time2 == 0L) return false
        val cal1 = Calendar.getInstance().apply { timeInMillis = time1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = time2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun getTodayStartTimestamp(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    companion object {
        fun provideFactory(application: Application, repository: PlannerRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(PlannerViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return PlannerViewModel(application, repository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }
}
