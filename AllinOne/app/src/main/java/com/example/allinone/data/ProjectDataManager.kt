package com.example.allinone.data

import com.example.allinone.Note
import com.example.allinone.R

object ProjectDataManager {
    var projects = mutableListOf<Note>()
    
    var projectAutoArchive: Boolean = false
    var projectSynergySync: Boolean = false
    var projectDeadlineAlerts: Boolean = true
    var projectAnalyticsEnabled: Boolean = false
    var projectCustomTags = mutableListOf("TASKS", "NOTES", "FEATURES", "BUGS", "RESOURCES")
    var projectSortCompletedToBottom: Boolean = true
    var projectActiveExpanded: Boolean = true
    var projectCompletedExpanded: Boolean = false
    var ideaActiveExpanded: Boolean = true
    var ideaCompletedExpanded: Boolean = false
    var projectAutoSaveIdeas: Boolean = true
    var projectDualExistEnabled: Boolean = false
    var projectIdeasEnabled: Boolean = true
    var projectRoadmapsEnabled: Boolean = true
    
    var projectTemplates: MutableMap<String, List<String>> = mutableMapOf(
        "App Feature" to listOf("UI Design", "Business Logic", "Integration", "Testing", "Deployment"),
        "Personal Goal" to listOf("Planning", "Execution", "Review"),
        "Bug Fix" to listOf("Reproduction", "Debugging", "Fix", "Verification")
    )
    
    var globalProjectColor: Int = -1
    var projectAddThemeColor: Int = -1
    var globalProjectIcon: Int = R.drawable.ic_project
}
