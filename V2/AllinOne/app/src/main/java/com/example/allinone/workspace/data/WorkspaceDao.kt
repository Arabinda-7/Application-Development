package com.example.allinone.workspace.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkspaceDao {

    // Projects
    @Query("SELECT * FROM projects ORDER BY updatedAt DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :projectId")
    fun getProjectById(projectId: String): Flow<ProjectEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllProjects(projects: List<ProjectEntity>)

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Delete
    suspend fun deleteProject(project: ProjectEntity)

    @Query("SELECT * FROM projects")
    suspend fun getAllProjectsSync(): List<ProjectEntity>

    @Query("SELECT * FROM projects WHERE deadline >= :start AND deadline < :end AND status != 'Archived' AND status != 'Completed'")
    suspend fun getProjectsDueBetween(start: Long, end: Long): List<ProjectEntity>

    @Query("DELETE FROM projects")
    suspend fun deleteAllProjects()

    // Goals
    @Query("SELECT * FROM goals WHERE projectId = :projectId ORDER BY createdAt ASC")
    fun getGoalsForProject(projectId: String): Flow<List<GoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllGoals(goals: List<GoalEntity>)

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Delete
    suspend fun deleteGoal(goal: GoalEntity)

    @Query("SELECT * FROM goals")
    suspend fun getAllGoalsSync(): List<GoalEntity>

    @Query("SELECT * FROM goals WHERE deadline >= :start AND deadline < :end AND status != 'Completed'")
    suspend fun getGoalsDueBetween(start: Long, end: Long): List<GoalEntity>

    @Query("DELETE FROM goals")
    suspend fun deleteAllGoals()

    // Tasks
    @Query("SELECT * FROM tasks WHERE projectId = :projectId")
    fun getTasksForProject(projectId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE milestoneId = :milestoneId")
    suspend fun getTasksByMilestone(milestoneId: String): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTasks(tasks: List<TaskEntity>)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("SELECT * FROM tasks")
    suspend fun getAllTasksSync(): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE dueDate >= :start AND dueDate < :end AND status != 'Done'")
    suspend fun getTasksDueBetween(start: Long, end: Long): List<TaskEntity>

    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()

    // Features
    @Query("SELECT * FROM features WHERE projectId = :projectId")
    fun getFeaturesForProject(projectId: String): Flow<List<FeatureEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeature(feature: FeatureEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllFeatures(features: List<FeatureEntity>)

    @Update
    suspend fun updateFeature(feature: FeatureEntity)

    @Delete
    suspend fun deleteFeature(feature: FeatureEntity)

    @Query("SELECT * FROM features")
    suspend fun getAllFeaturesSync(): List<FeatureEntity>

    @Query("DELETE FROM features")
    suspend fun deleteAllFeatures()

    // Bugs
    @Query("SELECT * FROM bugs WHERE projectId = :projectId")
    fun getBugsForProject(projectId: String): Flow<List<BugEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBug(bug: BugEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllBugs(bugs: List<BugEntity>)

    @Update
    suspend fun updateBug(bug: BugEntity)

    @Delete
    suspend fun deleteBug(bug: BugEntity)

    @Query("SELECT * FROM bugs")
    suspend fun getAllBugsSync(): List<BugEntity>

    @Query("DELETE FROM bugs")
    suspend fun deleteAllBugs()

    // Ideas
    @Query("SELECT * FROM ideas WHERE projectId = :projectId")
    fun getIdeasForProject(projectId: String): Flow<List<IdeaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIdea(idea: IdeaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllIdeas(ideas: List<IdeaEntity>)

    @Update
    suspend fun updateIdea(idea: IdeaEntity)

    @Delete
    suspend fun deleteIdea(idea: IdeaEntity)

    @Query("SELECT * FROM ideas")
    suspend fun getAllIdeasSync(): List<IdeaEntity>

    @Query("DELETE FROM ideas")
    suspend fun deleteAllIdeas()

    // Notes
    @Query("SELECT * FROM workspace_notes WHERE projectId = :projectId")
    fun getNotesForProject(projectId: String): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllNotes(notes: List<NoteEntity>)

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("SELECT * FROM workspace_notes")
    suspend fun getAllNotesSync(): List<NoteEntity>

    @Query("DELETE FROM workspace_notes")
    suspend fun deleteAllNotes()

    // Resources
    @Query("SELECT * FROM resources WHERE projectId = :projectId")
    fun getResourcesForProject(projectId: String): Flow<List<ResourceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResource(resource: ResourceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllResources(resources: List<ResourceEntity>)

    @Update
    suspend fun updateResource(resource: ResourceEntity)

    @Delete
    suspend fun deleteResource(resource: ResourceEntity)

    @Query("SELECT * FROM resources")
    suspend fun getAllResourcesSync(): List<ResourceEntity>

    @Query("DELETE FROM resources")
    suspend fun deleteAllResources()

    // Activity Logs
    @Query("SELECT * FROM activity_logs WHERE projectId = :projectId ORDER BY timestamp DESC")
    fun getActivityLogs(projectId: String): Flow<List<ActivityLogEntity>>

    @Insert
    suspend fun insertActivityLog(log: ActivityLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllActivityLogs(logs: List<ActivityLogEntity>)

    @Query("SELECT * FROM activity_logs")
    suspend fun getAllActivityLogsSync(): List<ActivityLogEntity>

    @Query("DELETE FROM activity_logs")
    suspend fun deleteAllActivityLogs()

    // Cross References
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNoteCrossReference(ref: NoteCrossReferenceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllNoteCrossReferences(refs: List<NoteCrossReferenceEntity>)

    @Query("SELECT * FROM note_cross_references WHERE noteId = :noteId")
    fun getReferencesForNote(noteId: String): Flow<List<NoteCrossReferenceEntity>>

    @Query("SELECT * FROM note_cross_references")
    suspend fun getAllNoteCrossReferencesSync(): List<NoteCrossReferenceEntity>

    @Query("DELETE FROM note_cross_references")
    suspend fun deleteAllNoteCrossReferences()
}
