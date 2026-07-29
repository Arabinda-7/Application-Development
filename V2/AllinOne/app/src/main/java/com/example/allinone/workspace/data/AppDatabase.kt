package com.example.allinone.workspace.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import com.example.allinone.SecurityManager
import com.example.allinone.data.database.*

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
        NoteCrossReferenceEntity::class,
        // Global Entities
        com.example.allinone.data.database.TaskEntity::class,
        com.example.allinone.data.database.NoteEntity::class,
        HabitEntity::class,
        WorkoutEntity::class,
        TransactionEntity::class,
        PersonalLedgerEntity::class,
        LedgerEntryEntity::class
    ],
    version = 8, // Incremented version to resolve schema mismatch
    exportSchema = false
)
@TypeConverters(AppTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workspaceDao(): WorkspaceDao
    abstract fun taskDao(): AppTaskDao
    abstract fun habitDao(): AppHabitDao
    abstract fun workoutDao(): AppWorkoutDao
    abstract fun noteDao(): AppNoteDao
    abstract fun financeDao(): AppFinanceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val passphrase = SecurityManager.getDatabasePassphrase(context).toByteArray()
                val factory = SupportOpenHelperFactory(passphrase)
                
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database" // Renamed from workspace_database
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
            context.deleteDatabase("app_database")
        }
    }
}
