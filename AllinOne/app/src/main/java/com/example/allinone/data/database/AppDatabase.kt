package com.example.allinone.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import com.example.allinone.security.SecurityManager
import com.example.allinone.workspace.data.*

@Database(
    entities = [
        ProjectEntity::class,
        GoalEntity::class,
        WorkspaceNoteEntity::class,
        WorkspaceTaskEntity::class,
        FeatureEntity::class,
        BugEntity::class,
        IdeaEntity::class,
        ResourceEntity::class,
        ActivityLogEntity::class,
        NoteCrossReferenceEntity::class,
        GlobalTaskEntity::class,
        HabitEntity::class,
        WorkoutEntity::class,
        TransactionEntity::class,
        PersonalLedgerEntity::class,
        LedgerEntryEntity::class,
        GlobalNoteEntity::class,
        AiChatEntity::class,
        AiChatSessionEntity::class,
        AssistantMemoryEntity::class
    ],
    version = 14, // Incremented version
    exportSchema = false
)
@TypeConverters(WorkspaceTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workspaceDao(): WorkspaceDao
    abstract fun taskDao(): AppTaskDao
    abstract fun habitDao(): AppHabitDao
    abstract fun workoutDao(): AppWorkoutDao
    abstract fun noteDao(): AppNoteDao
    abstract fun financeDao(): AppFinanceDao
    abstract fun aiChatDao(): AiChatDao
    abstract fun assistantMemoryDao(): AssistantMemoryDao

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
                    "all_in_one_db"
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
            context.deleteDatabase("all_in_one_db")
        }
    }
}
