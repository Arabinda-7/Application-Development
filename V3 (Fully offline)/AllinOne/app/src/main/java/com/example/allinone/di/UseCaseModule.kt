package com.example.allinone.di

import com.example.allinone.domain.repository.*
import com.example.allinone.domain.usecase.task.*
import com.example.allinone.domain.usecase.habit.*
import com.example.allinone.domain.usecase.workout.*
import com.example.allinone.domain.usecase.note.*
import com.example.allinone.domain.usecase.finance.*
import com.example.allinone.domain.usecase.project.*
import com.example.allinone.domain.usecase.user.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    // --- Task Use Cases ---
    @Provides @Singleton fun provideGetTasksUseCase(repo: TaskRepository) = GetTasksUseCase(repo)
    @Provides @Singleton fun provideAddTaskUseCase(repo: TaskRepository) = AddTaskUseCase(repo)
    @Provides @Singleton fun provideUpdateTaskUseCase(repo: TaskRepository) = UpdateTaskUseCase(repo)
    @Provides @Singleton fun provideDeleteTaskUseCase(repo: TaskRepository) = DeleteTaskUseCase(repo)
    @Provides @Singleton fun provideToggleTaskCompletionUseCase(repo: TaskRepository) = ToggleTaskCompletionUseCase(repo)
    @Provides @Singleton fun provideClearCompletedTasksUseCase(repo: TaskRepository) = ClearCompletedTasksUseCase(repo)

    // --- Habit Use Cases ---
    @Provides @Singleton fun provideGetHabitsUseCase(repo: HabitRepository) = GetHabitsUseCase(repo)
    @Provides @Singleton fun provideGetHabitStatisticsUseCase() = GetHabitStatisticsUseCase()

    // --- Workout Use Cases ---
    @Provides @Singleton fun provideGetWorkoutsUseCase(repo: WorkoutRepository) = GetWorkoutsUseCase(repo)
    @Provides @Singleton fun provideGetWorkoutStatisticsUseCase() = GetWorkoutStatisticsUseCase()

    // --- User & Settings Use Cases ---
    @Provides @Singleton fun provideGetUserSettingsUseCase(repo: UserRepository) = GetUserSettingsUseCase(repo)
    @Provides @Singleton fun provideUpdateUserSettingsUseCase(repo: UserRepository) = UpdateUserSettingsUseCase(repo)
    @Provides @Singleton fun provideAddXPUseCase(repo: UserRepository) = AddXPUseCase(repo)
    @Provides @Singleton fun provideAddActivityUseCase(repo: UserRepository) = AddActivityUseCase(repo)

    // --- Project Use Cases ---
    @Provides @Singleton fun provideGetProjectsUseCase(repo: ProjectRepository) = GetProjectsUseCase(repo)
}
