package com.example.allinone.di

import com.example.allinone.data.repository.*
import com.example.allinone.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTaskRepository(
        impl: TaskRepositoryImpl
    ): TaskRepository

    @Binds
    @Singleton
    abstract fun bindHabitRepository(
        impl: HabitRepositoryImpl
    ): HabitRepository

    @Binds
    @Singleton
    abstract fun bindWorkoutRepository(
        impl: WorkoutRepositoryImpl
    ): WorkoutRepository

    @Binds
    @Singleton
    abstract fun bindNoteRepository(
        impl: NoteRepositoryImpl
    ): NoteRepository

    @Binds
    @Singleton
    abstract fun bindFinanceRepository(
        impl: FinanceRepositoryImpl
    ): FinanceRepository

    @Binds
    @Singleton
    abstract fun bindProjectRepository(
        impl: ProjectRepositoryImpl
    ): ProjectRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        impl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindBackupRepository(
        impl: BackupRepositoryImpl
    ): BackupRepository

    @Binds
    @Singleton
    abstract fun bindAssistantMemoryRepository(
        impl: com.example.allinone.assistant.memory.AssistantMemoryRepositoryImpl
    ): com.example.allinone.assistant.memory.AssistantMemoryRepository
}
