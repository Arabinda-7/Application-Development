package com.example.allinone.data

import android.content.Context
import com.example.allinone.DayHistory
import com.example.allinone.data.model.Note
import com.example.allinone.data.model.ProjectFeature
import com.example.allinone.data.model.Task
import com.example.allinone.data.model.Workout
import com.example.allinone.data.preferences.UserPreferencesRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataManagerFacade: Combines UserDataManager, WorkspaceDataManager, BackupDataManager, 
 * and UserPreferencesRepository into a single unified API while maintaining 100% backward compatibility.
 */
@Singleton
class DataManagerFacade @Inject constructor(
    val userData: UserDataManager,
    val workspaceData: WorkspaceDataManager,
    val backupData: BackupDataManager,
    val preferences: UserPreferencesRepository
) {
    val tasks: MutableList<Task> get() = userData.tasks
    val workouts: MutableList<Workout> get() = userData.workouts
    val projects: MutableList<Note> get() = userData.projects
    val currentEditingIdeaSubFeatures: MutableList<ProjectFeature> get() = workspaceData.currentEditingIdeaSubFeatures

    fun calculateDayHistory(dateKey: String): DayHistory = userData.calculateDayHistory(dateKey)
    fun getUniqueFeatureName(baseName: String, existingList: List<ProjectFeature>): String =
        workspaceData.getUniqueFeatureName(baseName, existingList)

    fun saveData(context: Context) = backupData.saveData(context)
    fun loadData(context: Context) = backupData.loadData(context)
}
