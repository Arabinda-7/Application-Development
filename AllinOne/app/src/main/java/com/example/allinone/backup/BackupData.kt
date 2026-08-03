package com.example.allinone.backup

import com.example.allinone.data.database.*
import com.example.allinone.workspace.data.*
import com.example.allinone.domain.repository.*
import kotlinx.serialization.Serializable

@Serializable
data class BackupData(
    val version: Int,
    val timestamp: Long,
    // Global Data (Entities)
    val tasks: List<GlobalTaskEntity> = emptyList(),
    val habits: List<HabitEntity> = emptyList(),
    val workouts: List<WorkoutEntity> = emptyList(),
    val notes: List<GlobalNoteEntity> = emptyList(),
    val transactions: List<TransactionEntity> = emptyList(),
    val personalLedgers: List<PersonalLedgerEntity> = emptyList(),
    val ledgerEntries: List<LedgerEntryEntity> = emptyList(),
    // Workspace Data (Entities)
    val workspaceProjects: List<ProjectEntity> = emptyList(),
    val workspaceGoals: List<GoalEntity> = emptyList(),
    val workspaceTasks: List<WorkspaceTaskEntity> = emptyList(),
    val workspaceFeatures: List<FeatureEntity> = emptyList(),
    val workspaceBugs: List<BugEntity> = emptyList(),
    val workspaceIdeas: List<IdeaEntity> = emptyList(),
    val workspaceNotes: List<WorkspaceNoteEntity> = emptyList(),
    val workspaceResources: List<ResourceEntity> = emptyList(),
    val workspaceLogs: List<ActivityLogEntity> = emptyList(),
    val workspaceRefs: List<NoteCrossReferenceEntity> = emptyList(),
    // AI Data (Entities)
    val aiChatMessages: List<AiChatEntity> = emptyList(),
    val aiChatSessions: List<AiChatSessionEntity> = emptyList(),
    val assistantMemories: List<AssistantMemoryEntity> = emptyList(),
    // Profile & Settings (Domain Models)
    val userProfile: UserProfile? = null,
    val userSettings: UserSettings? = null,
    val financeSettings: FinanceSettings? = null,
    val habitSettings: HabitSettings? = null,
    val workoutSettings: WorkoutSettings? = null,
    val noteSettings: NoteSettings? = null,
    val projectSettings: ProjectSettings? = null
)
