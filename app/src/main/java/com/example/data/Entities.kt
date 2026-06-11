package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "folders")
data class Folder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorHex: String,
    val iconName: String,
    val type: String // "TASK" or "GOAL"
)

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val folderId: Long? = null,
    val isCompleted: Boolean = false,
    val dueDate: Long? = null, // timestamp in ms
    val reminderTime: Long? = null, // timestamp or time in minute-of-day
    val priority: Int = 1, // 0 = Low, 1 = Med, 2 = High
    val completionDate: Long? = null // timestamp in ms
)

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val folderId: Long? = null,
    val isCompleted: Boolean = false,
    val targetDate: Long? = null, // timestamp in ms
    val progress: Int = 0, // 0 to 100
    val isShared: Boolean = false,
    val sharedGroupCode: String? = null
)

@Entity(tableName = "streaks")
data class Streak(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val startDate: Long = System.currentTimeMillis(),
    val lastCheckDate: Long = 0L, // timestamp when ticked
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val historyJson: String = "", // array or comma-separated timestamps
    val isNegative: Boolean = false
)
