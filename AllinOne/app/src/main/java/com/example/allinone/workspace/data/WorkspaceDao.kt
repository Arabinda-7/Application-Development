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

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Delete
    suspend fun deleteProject(project: ProjectEntity)

    // Goals
    @Query("SELECT * FROM goals WHERE projectId = :projectId ORDER BY createdAt ASC")
    fun getGoalsForProject(projectId: String): Flow<List<GoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity)

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Delete
    suspend fun deleteGoal(goal: GoalEntity)

    // Tasks
    @Query("SELECT * FROM tasks WHERE projectId = :projectId")
    fun getTasksForProject(projectId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE milestoneId = :milestoneId")
    suspend fun getTasksByMilestone(milestoneId: String): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    // Features
    @Query("SELECT * FROM features WHERE projectId = :projectId")
    fun getFeaturesForProject(projectId: String): Flow<List<FeatureEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeature(feature: FeatureEntity)

    @Update
    suspend fun updateFeature(feature: FeatureEntity)

    @Delete
    suspend fun deleteFeature(feature: FeatureEntity)

    // Bugs
    @Query("SELECT * FROM bugs WHERE projectId = :projectId")
    fun getBugsForProject(projectId: String): Flow<List<BugEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBug(bug: BugEntity)

    @Update
    suspend fun updateBug(bug: BugEntity)

    // Ideas
    @Query("SELECT * FROM ideas WHERE projectId = :projectId")
    fun getIdeasForProject(projectId: String): Flow<List<IdeaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIdea(idea: IdeaEntity)

    @Update
    suspend fun updateIdea(idea: IdeaEntity)

    @Delete
    suspend fun deleteIdea(idea: IdeaEntity)

    // Notes
    @Query("SELECT * FROM workspace_notes WHERE projectId = :projectId")
    fun getNotesForProject(projectId: String): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Update
    suspend fun updateNote(note: NoteEntity)

    // Resources
    @Query("SELECT * FROM resources WHERE projectId = :projectId")
    fun getResourcesForProject(projectId: String): Flow<List<ResourceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResource(resource: ResourceEntity)

    // Activity Logs
    @Query("SELECT * FROM activity_logs WHERE projectId = :projectId ORDER BY timestamp DESC")
    fun getActivityLogs(projectId: String): Flow<List<ActivityLogEntity>>

    @Insert
    suspend fun insertActivityLog(log: ActivityLogEntity)

    // Cross References
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNoteCrossReference(ref: NoteCrossReferenceEntity)

    @Query("SELECT * FROM note_cross_references WHERE noteId = :noteId")
    fun getReferencesForNote(noteId: String): Flow<List<NoteCrossReferenceEntity>>
}
