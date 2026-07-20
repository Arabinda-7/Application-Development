package com.example.allinone.workspace.data

import androidx.room.*
import java.util.*

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val color: Int = -1,
    val iconRes: String = "",
    val status: String = "Active", // Active, Completed, Archived
    val progress: Int = 0,
    val weightedProgress: Int = 0,
    val health: String = "Healthy", // Healthy, At Risk, Delayed
    val deadline: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "goals",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("projectId")]
)
data class GoalEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val projectId: String,
    val parentGoalId: String? = null,
    val title: String,
    val description: String = "",
    val status: String = "Pending", // Pending, In Progress, Completed
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "workspace_notes",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("projectId")]
)
data class NoteEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val projectId: String,
    val title: String,
    val content: String, // Markdown
    val tags: String = "", // Comma separated
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("projectId")]
)
data class TaskEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val projectId: String,
    val milestoneId: String? = null, // Linked to GoalEntity or FeatureEntity
    val title: String,
    val description: String = "",
    val weight: Int = 1,
    val progress: Int = 0, // 0-100
    val status: String = "Todo", // Todo, In Progress, Review, Done
    val priority: Int = 1, // 0: Low, 1: Medium, 2: High
    val estimatedMinutes: Int = 0,
    val actualMinutes: Int = 0,
    val dueDate: Long? = null,
    val dependencies: String = "", // Comma separated IDs
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "features",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("projectId")]
)
data class FeatureEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val projectId: String,
    val title: String,
    val description: String = "",
    val complexity: String = "Medium", // Low, Medium, High
    val effortSize: String = "M", // XS, S, M, L, XL
    val requirements: String = "", // Must-haves / Checklist
    val targetVersion: String = "", // e.g., v1.0, MVP
    val successMetrics: String = "", // KPIs
    val status: String = "Backlog", // Backlog, Planning, Development, Testing, Shipped
    val lifecycleStage: String = "Proposal",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "bugs",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("projectId")]
)
data class BugEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val projectId: String,
    val title: String,
    val description: String = "",
    val severity: String = "Medium", // Low, Medium, High, Critical
    val priority: Int = 1, // 0: Low, 1: Medium, 2: High
    val status: String = "Open", // Open, Confirmed, Fixing, Fixed, Verified
    val environment: String = "Production",
    val version: String = "",
    val stepsToReproduce: String = "",
    val linkedTaskId: String? = null,
    val linkedFeatureId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "ideas",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("projectId")]
)
data class IdeaEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val projectId: String,
    val title: String,
    val description: String = "",
    val impact: Int = 1, // 1-5
    val difficulty: Int = 1, // 1-5
    val status: String = "New", // New, Evaluated, Converted, Rejected
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "resources",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("projectId")]
)
data class ResourceEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val projectId: String,
    val title: String,
    val type: String, // FILE, URL, CONTACT
    val pathOrUrl: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "activity_logs",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("projectId")]
)
data class ActivityLogEntity(
    @PrimaryKey(autoGenerate = true) val logId: Long = 0,
    val projectId: String,
    val entityType: String, // TASK, FEATURE, BUG, GOAL, etc.
    val entityId: String,
    val action: String, // CREATE, UPDATE, DELETE, CONVERT
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "note_cross_references",
    primaryKeys = ["noteId", "targetId"],
    foreignKeys = [
        ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("noteId"), Index("targetId")]
)
data class NoteCrossReferenceEntity(
    val noteId: String,
    val targetId: String, // ID of Task, Feature, Bug, or Goal
    val targetType: String // TASK, FEATURE, BUG, GOAL
)
