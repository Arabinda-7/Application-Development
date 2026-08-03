package com.example.allinone.di

import com.example.allinone.backup.BackupProvider
import com.example.allinone.backup.providers.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class BackupModule {

    @Binds
    @IntoSet
    abstract fun bindTaskBackupProvider(provider: TaskBackupProvider): BackupProvider

    @Binds
    @IntoSet
    abstract fun bindHabitBackupProvider(provider: HabitBackupProvider): BackupProvider

    @Binds
    @IntoSet
    abstract fun bindWorkoutBackupProvider(provider: WorkoutBackupProvider): BackupProvider

    @Binds
    @IntoSet
    abstract fun bindNoteBackupProvider(provider: NoteBackupProvider): BackupProvider

    @Binds
    @IntoSet
    abstract fun bindFinanceBackupProvider(provider: FinanceBackupProvider): BackupProvider

    @Binds
    @IntoSet
    abstract fun bindWorkspaceBackupProvider(provider: WorkspaceBackupProvider): BackupProvider

    @Binds
    @IntoSet
    abstract fun bindUserBackupProvider(provider: UserBackupProvider): BackupProvider

    @Binds
    @IntoSet
    abstract fun bindAiBackupProvider(provider: AiBackupProvider): BackupProvider
}
