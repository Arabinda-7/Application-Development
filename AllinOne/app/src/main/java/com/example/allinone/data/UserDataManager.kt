package com.example.allinone.data

import com.example.allinone.R

object UserDataManager {
    var userXP: Int = 0
    var userLevel: Int = 1
    var userName: String = "User"
    var userBio: String = ""
    var userAvatarRes: Int = R.drawable.boy_avatar_profile
    var userProfileImageUri: String? = null
    var recentActivities = mutableListOf<String>()
    var dailyMoods = mutableMapOf<String, String>()
    var lastMoodTimestamp: Long = 0
    
    var displaySize: String = "S"
    var homeDisplaySize: String = "S"
    var homeFocusSize: String = "M"
    var fontSize: String = "S"
    var isSystemAppearanceEnabled: Boolean = true
    
    var appThemeMode: String = "DARK"
    var appAccentColor: Int = -1
    var appFontFamily: String = "DEFAULT"
    var appBorderRadius: Int = 16
    var appCardStyle: String = "GLASS"
    var appShowShadows: Boolean = true
    var startupLoadingTime: Int = 2000
    
    var showHabitSection: Boolean = true
    var showWorkoutSection: Boolean = true
    var showTaskSection: Boolean = true
    var showNoteSection: Boolean = true
    var showProjectSection: Boolean = true
    var showFinanceSection: Boolean = true
    
    var isOnboardingCompleted: Boolean = false
    var isAppLockEnabled: Boolean = false
    var isAppUnlocked: Boolean = false
    var appLockPin: String? = null
    var appLockQuestion: String? = null
    var appLockAnswer: String? = null
    
    var userCustomColors = mutableListOf<Int>()

    fun addActivity(activity: String) {
        recentActivities.add(0, activity)
        if (recentActivities.size > 20) {
            recentActivities.removeAt(recentActivities.size - 1)
        }
    }

    fun addXP(amount: Int): Boolean {
        var leveledUp = false
        userXP += amount
        while (userXP >= userLevel * 100) {
            userXP -= userLevel * 100
            userLevel++
            leveledUp = true
            addActivity("Leveled up to Level $userLevel!")
        }
        return leveledUp
    }
}
