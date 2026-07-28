package com.example.allinone.data

import com.example.allinone.AgendaItem

object WorkspaceDataManager {
    var workspaceTodayAgenda: MutableMap<String, List<AgendaItem>> = java.util.Collections.synchronizedMap(mutableMapOf<String, List<AgendaItem>>())
    var lastViewedNotificationDate: String = ""
    var lastSummaryNotificationDate: String = ""
    var hasNewTodayNotifications: Boolean = false
}
