package com.example.allinone.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.example.allinone.data.database.*
import com.example.allinone.workspace.data.WorkspaceDao
import com.example.allinone.security.SecurityManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().create()

    @Provides
    @Singleton
    fun provideEncryptedSharedPreferences(
        @ApplicationContext context: Context
    ): SharedPreferences {
        return SecurityManager.getEncryptedPrefs(context)
    }

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        val passphrase = SecurityManager.getDatabasePassphrase(context).toByteArray()
        val factory = SupportOpenHelperFactory(passphrase)
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "all_in_one_db"
        )
        .openHelperFactory(factory)
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideWorkspaceDao(db: AppDatabase): WorkspaceDao = db.workspaceDao()

    @Provides
    fun provideTaskDao(db: AppDatabase): AppTaskDao = db.taskDao()

    @Provides
    fun provideHabitDao(db: AppDatabase): AppHabitDao = db.habitDao()

    @Provides
    fun provideWorkoutDao(db: AppDatabase): AppWorkoutDao = db.workoutDao()

    @Provides
    fun provideNoteDao(db: AppDatabase): AppNoteDao = db.noteDao()

    @Provides
    fun provideFinanceDao(db: AppDatabase): AppFinanceDao = db.financeDao()

    @Provides
    fun provideAiChatDao(db: AppDatabase): AiChatDao = db.aiChatDao()

    @Provides
    fun provideAssistantMemoryDao(db: AppDatabase): AssistantMemoryDao = db.assistantMemoryDao()
}
