package com.example.allinone.workspace.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ProjectEntity::class,
        GoalEntity::class,
        NoteEntity::class,
        TaskEntity::class,
        FeatureEntity::class,
        BugEntity::class,
        IdeaEntity::class,
        ResourceEntity::class,
        ActivityLogEntity::class,
        NoteCrossReferenceEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class WorkspaceDatabase : RoomDatabase() {
    abstract fun workspaceDao(): WorkspaceDao

    companion object {
        @Volatile
        private var INSTANCE: WorkspaceDatabase? = null

        fun getDatabase(context: Context): WorkspaceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WorkspaceDatabase::class.java,
                    "workspace_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
