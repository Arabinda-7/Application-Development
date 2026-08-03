package com.example.allinone.data.repository

import com.example.allinone.DayHistory
import com.example.allinone.data.datasource.UserLocalDataSource
import com.example.allinone.domain.repository.UserProfile
import com.example.allinone.domain.repository.UserRepository
import com.example.allinone.domain.repository.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val localDataSource: UserLocalDataSource
) : UserRepository {

    override fun getUserProfile(): Flow<UserProfile> = localDataSource.profile

    override suspend fun updateUserProfile(profile: UserProfile) {
        localDataSource.updateProfile(profile)
    }

    override suspend fun addXP(amount: Int): Boolean {
        val currentProfile = localDataSource.profile.first()
        var xp = currentProfile.xp + amount
        var level = currentProfile.level
        var leveledUp = false
        
        while (xp >= level * 100) {
            xp -= level * 100
            level++
            leveledUp = true
            addActivity("Leveled up to Level $level!")
        }
        
        localDataSource.updateProfile(currentProfile.copy(xp = xp, level = level))
        return leveledUp
    }

    override fun getUserSettings(): Flow<UserSettings> = localDataSource.settings

    override suspend fun updateUserSettings(settings: UserSettings) {
        localDataSource.updateSettings(settings)
    }

    override suspend fun addActivity(activity: String) {
        val currentProfile = localDataSource.profile.first()
        val activities = currentProfile.recentActivities.toMutableList()
        activities.add(0, activity)
        if (activities.size > 20) {
            activities.removeAt(activities.size - 1)
        }
        localDataSource.updateProfile(currentProfile.copy(recentActivities = activities))
    }

    override fun getDayHistory(): Flow<Map<String, DayHistory>> = localDataSource.history

    override suspend fun updateDayHistory(history: Map<String, DayHistory>) {
        localDataSource.updateHistory(history)
    }
}
