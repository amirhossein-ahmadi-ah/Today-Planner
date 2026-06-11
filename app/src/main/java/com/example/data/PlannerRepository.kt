package com.example.data

import kotlinx.coroutines.flow.Flow

class PlannerRepository(private val plannerDao: PlannerDao) {
    val allFolders: Flow<List<Folder>> = plannerDao.getAllFolders()
    val allTasks: Flow<List<Task>> = plannerDao.getAllTasks()
    val allGoals: Flow<List<Goal>> = plannerDao.getAllGoals()
    val allStreaks: Flow<List<Streak>> = plannerDao.getAllStreaks()

    fun getTasksInFolder(folderId: Long): Flow<List<Task>> = plannerDao.getTasksByFolder(folderId)
    fun getGoalsInFolder(folderId: Long): Flow<List<Goal>> = plannerDao.getGoalsByFolder(folderId)

    suspend fun insertFolder(folder: Folder): Long = plannerDao.insertFolder(folder)
    suspend fun deleteFolder(id: Long) = plannerDao.deleteFolder(id)

    suspend fun insertTask(task: Task): Long = plannerDao.insertTask(task)
    suspend fun updateTask(task: Task) = plannerDao.updateTask(task)
    suspend fun deleteTask(id: Long) = plannerDao.deleteTask(id)

    suspend fun insertGoal(goal: Goal): Long = plannerDao.insertGoal(goal)
    suspend fun updateGoal(goal: Goal) = plannerDao.updateGoal(goal)
    suspend fun deleteGoal(id: Long) = plannerDao.deleteGoal(id)

    suspend fun insertStreak(streak: Streak): Long = plannerDao.insertStreak(streak)
    suspend fun updateStreak(streak: Streak) = plannerDao.updateStreak(streak)
    suspend fun deleteStreak(id: Long) = plannerDao.deleteStreak(id)
}
