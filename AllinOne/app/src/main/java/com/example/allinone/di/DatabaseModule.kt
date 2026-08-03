package com.example.allinone.di

import android.content.Context
import android.content.SharedPreferences
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
import kotlinx.serialization.json.Json
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
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }

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
        return AppDatabase.getDatabase(context)
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
    @Singleton
    fun provideAiChatRepository(dao: AiChatDao): com.example.allinone.data.repository.AiChatRepository = 
        com.example.allinone.data.repository.AiChatRepository(dao)

    @Provides
    fun provideAssistantMemoryDao(db: AppDatabase): AssistantMemoryDao = db.assistantMemoryDao()
}
