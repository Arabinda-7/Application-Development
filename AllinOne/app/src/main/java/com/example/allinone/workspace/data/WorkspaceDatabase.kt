package com.example.allinone.workspace.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import net.sqlcipher.database.SupportFactory
import com.example.allinone.SecurityManager

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
    version = 6,
    exportSchema = false
)
abstract class WorkspaceDatabase : RoomDatabase() {
    abstract fun workspaceDao(): WorkspaceDao

    companion object {
        @Volatile
        private var INSTANCE: WorkspaceDatabase? = null

        fun getDatabase(context: Context): WorkspaceDatabase {
            return INSTANCE ?: synchronized(this) {
                val passphrase = SecurityManager.getDatabasePassphrase(context).toByteArray()
                val factory = SupportFactory(passphrase)
                
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WorkspaceDatabase::class.java,
                    "workspace_database"
                )
                .openHelperFactory(factory)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        fun resetDatabase(context: Context) {
            INSTANCE?.close()
            INSTANCE = null
            context.deleteDatabase("workspace_database")
        }
    }
}
